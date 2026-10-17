package dev.linqfy.bigCasares.modules.legendary;

import dev.linqfy.legendary.LegendaryWeaponsPlugin;
import dev.linqfy.bigCasares.module.runtime.BukkitRuntimeRegistrations;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** One scheduler owns every finite effect and projectile. All Bukkit work is on the main thread. */
final class LegendaryRuntime implements Listener, Runnable {
    private final LegendaryWeaponsPlugin plugin;
    private final LegendaryStateStore store;
    private final LegendarySpearEscrow escrow;
    private final LegendaryCombatPolicy policy;
    private final BukkitRuntimeRegistrations registrations;
    private final Map<SessionKey, Session> sessions = new HashMap<>();
    private final Map<UUID, Long> fallProtection = new HashMap<>();
    private final NamespacedKey temporary = new NamespacedKey("bigcasares", "legendary_temporary");
    private final java.util.function.Predicate<UUID> eventTransaction;
    private int tick;
    private boolean stopped;
    private LivingEntity damageTarget;
    private boolean damageAccepted;

    LegendaryRuntime(LegendaryWeaponsPlugin plugin, LegendaryStateStore store, BukkitRuntimeRegistrations registrations, java.util.function.Predicate<UUID> eventTransaction) {
        this.plugin = plugin;
        this.eventTransaction = eventTransaction;
        this.store = store;
        this.escrow = new LegendarySpearEscrow(store);
        this.policy = new LegendaryCombatPolicy(plugin);
        this.registrations = registrations;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent event) {
        ItemStack held = event.getItem();
        if (escrow.locked(event.getPlayer(), held) || crafting(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        var weapon = plugin.getCustomItemRegistry().resolveItemId(held).flatMap(LegendaryWeapon::find);
        if (weapon.isEmpty()) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (weapon.orElseThrow() == LegendaryWeapon.THUNDER_SPEAR) {
                dust(event.getPlayer().getEyeLocation(), Color.AQUA, 5);
            }
            return; // Native attacks, Lunge cost and attack cooldown are untouched.
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        boolean denied = LegendaryControls.denied(event);
        // Deny vanilla Charge even during cooldown; only this catalog item is intercepted.
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);
        if (LegendaryControls.decide(weapon.orElseThrow(), true, event.getHand() == EquipmentSlot.HAND, denied)
            != LegendaryControls.Decision.ACTIVATE_SPECIAL || stopped) {
            return;
        }
        activate(event.getPlayer(), weapon.orElseThrow());
    }

    private void activate(Player player, LegendaryWeapon weapon) {
        long now = System.currentTimeMillis();
        long remaining = store.remaining(player.getUniqueId(), weapon, now);
        if (remaining > 0) {
            cooldownBar(player, weapon, remaining);
            return;
        }
        if (sessions.containsKey(new SessionKey(player.getUniqueId(), weapon))
            || (weapon == LegendaryWeapon.THUNDER_SPEAR && escrow.pending(player.getUniqueId()))) {
            player.sendActionBar(Component.text("Espera a que termine la habilidad activa."));
            return;
        }
        if (sessions.size() >= integer("max-active-effects", 32, 1, 128)) {
            player.sendActionBar(Component.text("Hay demasiadas habilidades activas; inténtalo de nuevo."));
            return;
        }
        if (!policy.allowed(player, player.getLocation(), null, LegendaryProtectionEvent.Action.MOVE)) {
            player.sendActionBar(Component.text("No puedes usar esta habilidad en esta región."));
            return;
        }
        Session session = new Session(player.getUniqueId(), weapon, player.getLocation());
        if (weapon == LegendaryWeapon.SHADOW_BLADE) {
            if (!shadow(player, session, true)) {
                player.sendActionBar(Component.text("No hay un destino seguro."));
                return;
            }
        }
        try {
            if (!store.beginCooldown(player.getUniqueId(), weapon, now)) {
                return;
            }
            if (weapon == LegendaryWeapon.SHADOW_BLADE && !shadow(player, session, false)) return;
            sessions.put(session.key(), session);
            if (weapon == LegendaryWeapon.THUNDER_SPEAR) {
                ItemStack original = escrow.launch(player, player.getInventory().getHeldItemSlot());
                session.center = player.getEyeLocation();
                session.velocity = session.center.getDirection().multiply(number("spear.flight-speed", 1.8, 0.2, 4));
                session.display = display(session.center, original);
            } else if (weapon == LegendaryWeapon.HYPERION) {
                session.center = player.getEyeLocation();
                session.velocity = session.center.getDirection().multiply(1.2);
            } else if (weapon == LegendaryWeapon.BLOODLUST) {
                potion(session, player, PotionEffectType.STRENGTH, 200, 2);
            } else if (weapon == LegendaryWeapon.WINDWEAVER) {
                player.setFallDistance(0);
                player.setVelocity(LegendaryWindLaunch.velocity(player.getVelocity(),
                    number("windweaver.upward-velocity", 1.3, 1, 2.5)));
                fallProtection.put(player.getUniqueId(), now + 10_000);
                player.getWorld().spawnParticle(Particle.GUST, player.getLocation().add(0, .2, 0), 2, .4, .1, .4, 0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.2f, .8f);
            }
            player.sendActionBar(Component.text(weapon.abilityName + " — activada (40 s)"));
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0.7f);
        } catch (IOException | RuntimeException failure) {
            sessions.remove(session.key());
            if (session.display != null) session.display.remove();
            plugin.getLogger().log(Level.SEVERE, "No se pudo activar " + weapon.id + "; se conserva el estado de recuperación.", failure);
            player.sendMessage("§cLa habilidad no pudo activarse. Tu arma queda protegida para recuperación.");
        }
    }

    @Override
    public void run() {
        if (stopped) return;
        tick++;
        for (Session session : new ArrayList<>(sessions.values())) {
            try {
                Player owner = plugin.getServer().getPlayer(session.owner);
                if (owner == null || !owner.isOnline() || owner.isDead() || !owner.getWorld().equals(session.center.getWorld())) {
                    finish(session, false);
                    continue;
                }
                session.age++;
                session.ephemeral.removeIf(entity -> !entity.isValid());
                session.effects.prune(session.age);
                switch (session.weapon) {
                    case BLOODLUST -> {
                        session.center = owner.getLocation();
                        if (session.age % 4 == 0) ring(session.center, 1.1, Color.RED, session.age);
                        if (session.age >= 200) {
                            finish(session, true);
                        }
                    }
                    case FROST_SCYTHE -> frost(owner, session);
                    case WINDWEAVER -> wind(owner, session);
                    case SHADOW_BLADE -> { if (session.age >= 60) finish(session, true); }
                    case EARTHBREAKER -> earthquake(owner, session);
                    case VOID_KATANA -> katana(owner, session);
                    case HYPERION, THUNDER_SPEAR -> projectile(owner, session);
                }
            } catch (RuntimeException | IOException failure) {
                plugin.getLogger().log(Level.SEVERE, "La habilidad se canceló de forma segura.", failure);
                try { finish(session, false); } catch (IOException ignored) { /* journal remains */ }
            }
        }
        if (tick % 20 == 0) {
            fallProtection.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!sessions.containsKey(new SessionKey(player.getUniqueId(), LegendaryWeapon.THUNDER_SPEAR)) && !player.isDead()
                    && store.get("spear.defer." + player.getUniqueId()) == null) {
                    try { escrow.restore(player); } catch (IOException failure) { reportRecovery(failure); }
                }
                if (escrow.receipt(player.getInventory().getItemInMainHand())) {
                    cooldownBar(player, LegendaryWeapon.THUNDER_SPEAR,
                        store.remaining(player.getUniqueId(), LegendaryWeapon.THUNDER_SPEAR, System.currentTimeMillis()));
                }
                plugin.getCustomItemRegistry().resolveItemId(player.getInventory().getItemInMainHand())
                    .flatMap(LegendaryWeapon::find).ifPresent(weapon -> {
                        long remaining = store.remaining(player.getUniqueId(), weapon, System.currentTimeMillis());
                        if (remaining > 0) cooldownBar(player, weapon, remaining);
                    });
            }
        }
    }

    private void frost(Player owner, Session session) throws IOException {
        session.center = owner.getLocation();
        Color color = Color.fromRGB(170, 235, 255);
        double radius = 5;
        if (session.age % 3 == 0) {
            for (int layer = 0; layer < 3; layer++) ring(session.center.clone().add(0, layer * 0.8, 0), radius - layer * 0.6, color, session.age);
        }
        if ((session.age - 1) % 20 == 0) {
            for (LivingEntity target : enemies(owner, session.center, radius)) {
                if (!spellDamage(owner, target, number("frost-scythe.damage-per-pulse", 4, 0, 20))) continue;
                potion(session, target, PotionEffectType.SLOWNESS, 35, 2);
                FreezeLease prior = session.freezes.get(target.getUniqueId());
                int frozen = Math.max(target.getFreezeTicks(), target.getMaxFreezeTicks() + 20);
                session.freezes.put(target.getUniqueId(), new FreezeLease(target,
                    prior == null ? target.getFreezeTicks() : prior.previous(),
                    prior == null ? session.age : prior.previousAt(), frozen));
                target.setFreezeTicks(frozen);
                owner.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 8, 0.4, 0.4, 0.4, 0.03);
            }
        }
        if (session.age >= 160) finish(session, true);
    }

    private void wind(Player owner, Session session) throws IOException {
        session.center = owner.getLocation();
        if (session.age % 3 == 0) ring(session.center, .8, Color.WHITE, session.age);
        if (session.age >= 200 || (session.age > 5 && owner.isOnGround())) finish(session, true);
    }

    private void earthquake(Player owner, Session session) throws IOException {
        if (session.age > LegendaryEarthquakePulse.DURATION) {
            if (session.age >= LegendaryEarthquakePulse.DURATION + LegendaryEarthquakePulse.SLOWNESS_TICKS) {
                finish(session, true);
            }
            return;
        }
        double radius = LegendaryEarthquakePulse.radius(session.age);
        ring(session.center, radius, Color.fromRGB(120, 95, 75), session.age);
        if (session.age % 2 == 0) {
            int count = integer("particles-per-ring", 24, 4, 48);
            for (int i = 0; i < count; i++) {
                double angle = i * Math.PI * 2 / count;
                owner.getWorld().spawnParticle(Particle.BLOCK,
                    session.center.clone().add(Math.cos(angle) * radius, .15, Math.sin(angle) * radius),
                    3, .15, .2, .15, Material.DEEPSLATE.createBlockData());
            }
        }
        var candidates = session.center.getWorld().getNearbyEntities(session.center, radius, 2.5, radius).stream()
            .filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity).toList();
        var targets = LegendaryTargets.select(candidates, LivingEntity::getUniqueId,
            target -> target.getLocation().distanceSquared(session.center), target -> {
                Vector delta = target.getLocation().toVector().subtract(session.center.toVector());
                return LegendaryEarthquakePulse.inRange(delta.getX(), delta.getY(), delta.getZ(), radius)
                    && policy.target(owner, target);
            }, integer("max-targets-per-pulse", 12, 1, 64));
        for (LivingEntity target : session.earthquake.strike(session.age, targets, LivingEntity::getUniqueId,
            enemy -> spellDamage(owner, enemy, number("earthbreaker.damage-per-wave", 8, 0, 30)))) {
            knockback(session.center, target, .4, .6);
            potion(session, target, PotionEffectType.SLOWNESS, LegendaryEarthquakePulse.SLOWNESS_TICKS, 1);
            owner.getWorld().spawnParticle(Particle.BLOCK, target.getLocation(), 16, .5, .4, .5,
                Material.DEEPSLATE.createBlockData());
        }
        if (LegendaryEarthquakePulse.startsWave(session.age)) {
            owner.getWorld().playSound(session.center, Sound.ENTITY_GENERIC_EXPLODE, 1.4f, .5f);
        }
    }

    private void katana(Player owner, Session session) throws IOException {
        if (session.age == 1 || session.age == 7 || session.age == 13) {
            Location before = owner.getLocation();
            if (!move(owner, before.clone().add(before.getDirection().setY(0).normalize().multiply(3)))) {
                finish(session, false);
                return;
            }
            owner.swingMainHand();
            ring(owner.getLocation().add(0, 1, 0), 2.5, Color.PURPLE, session.age);
            for (LivingEntity target : enemies(owner, owner.getLocation(), 3)) {
                if (damage(owner, target, number("void-katana.damage-per-cut", 5, 0, 30))) {
                    potion(session, target, PotionEffectType.DARKNESS, 30, 0);
                }
            }
        }
        if (session.age >= 14) finish(session, true);
    }

    private boolean shadow(Player owner, Session session, boolean preview) {
        double range = number("shadow-blade.range", 16, 2, 32);
        var hit = owner.getWorld().rayTrace(owner.getEyeLocation(), owner.getEyeLocation().getDirection(),
            range, FluidCollisionMode.NEVER, true, 0.4,
            entity -> entity instanceof LivingEntity living && policy.target(owner, living));
        LivingEntity target = hit != null && hit.getHitEntity() instanceof LivingEntity living ? living : null;
        Location destination = target == null
            ? owner.getLocation().add(owner.getLocation().getDirection().setY(0).normalize().multiply(4))
            : target.getLocation().subtract(target.getLocation().getDirection().setY(0).normalize().multiply(1.5));
        if (preview) return canMove(owner, destination);
        if (!move(owner, destination)) return false;
        dust(owner.getLocation(), Color.fromRGB(35, 0, 50), 20);
        potion(session, owner, PotionEffectType.SPEED, 60, 1);
        if (target != null && spellDamage(owner, target, number("shadow-blade.strike-damage", 10, 0, 30))) {
            potion(session, target, PotionEffectType.DARKNESS, 40, 0);
            owner.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, .7f);
        }
        return true;
    }

    private void projectile(Player owner, Session session) throws IOException {
        if (!session.impacted) {
            if (session.age > integer("spear.max-flight-ticks", 80, 10, 200)
                || !session.center.getWorld().isChunkLoaded(session.center.getBlockX() >> 4, session.center.getBlockZ() >> 4)
                || session.center.getY() < session.center.getWorld().getMinHeight() + 1) {
                finish(session, false);
                return;
            }
            var step = LegendaryProjectileSweep.advance(session.center, session.velocity, (direction, distance) ->
                session.center.getWorld().rayTrace(session.center, direction, distance, FluidCollisionMode.NEVER, true, 0.3,
                    entity -> entity instanceof LivingEntity living && policy.target(owner, living)));
            if (step.impact()) {
                session.center = step.destination();
                session.impacted = true;
                session.impactTick = session.age;
                if (session.display != null) {
                    Location stuck = session.center.clone();
                    stuck.setPitch(90);
                    session.display.teleport(stuck);
                }
                if (session.weapon == LegendaryWeapon.HYPERION) {
                    arcaneBurst(owner, session.center);
                    owner.getWorld().playSound(session.center, Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 1.3f);
                }
            } else {
                session.center = step.destination();
                if (session.weapon == LegendaryWeapon.THUNDER_SPEAR) session.velocity.setY(session.velocity.getY() - 0.025);
                if (session.weapon == LegendaryWeapon.HYPERION) {
                    int points = integer("particles-per-ring", 24, 4, 48);
                    for (int i = 0; i < points; i++) {
                        double y = 1 - 2 * (i + 0.5) / points;
                        double horizontal = Math.sqrt(1 - y * y);
                        double angle = i * 2.399963229728653;
                        dust(session.center.clone().add(Math.cos(angle) * horizontal * 0.65, y * 0.65,
                            Math.sin(angle) * horizontal * 0.65), Color.PURPLE, 1);
                    }
                } else {
                    dust(session.center, Color.AQUA, 8);
                }
                if (session.display != null) {
                    Location oriented = session.center.clone().setDirection(session.velocity);
                    session.display.teleport(oriented);
                    var transform = session.display.getTransformation();
                    transform.getLeftRotation().rotationX((float) (Math.PI / 2)).rotateY(session.age * 0.3f);
                    session.display.setTransformation(transform);
                }
                return;
            }
        }
        int elapsed = session.age - session.impactTick;
        if (session.weapon == LegendaryWeapon.THUNDER_SPEAR) {
            if (elapsed >= LegendaryWeapon.STORM_TICKS) {
                session.returnTicks++;
                if (session.display == null || session.returnTicks >= 10) {
                    finish(session, true);
                    return;
                }
                Vector toOwner = owner.getEyeLocation().toVector().subtract(session.center.toVector());
                session.center.add(toOwner.multiply(1.0 / (11 - session.returnTicks)));
                session.display.teleport(session.center);
                dust(session.center, Color.AQUA, 5);
                return;
            }
            double radius = number("spear.storm-radius", 7, 1, 16);
            if (elapsed % 4 == 0) ring(session.center, radius, Color.AQUA, elapsed);
            if (LegendaryStormTiming.pulse(elapsed)) {
                owner.getWorld().playSound(session.center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 1);
                lightning(session, session.center);
                for (LivingEntity target : enemies(owner, session.center, radius)) {
                    if (!damage(owner, target, number("spear.lightning-damage", 7, 0, 40))) continue;
                    lightning(session, target.getLocation());
                    dust(target.getLocation(), Color.WHITE, 18);
                }
            }
        } else {
            if (elapsed % 4 == 0) ring(session.center, 5, Color.PURPLE, elapsed);
            if (elapsed > 0 && elapsed % 20 == 0) {
                for (LivingEntity target : enemies(owner, session.center, 5)) {
                    spellDamage(owner, target, number("hyperion.field-damage", 4, 0, 20));
                }
            }
            if (elapsed >= 100) finish(session, true);
        }
    }

    private List<LivingEntity> enemies(Player owner, Location center, double radius) {
        var candidates = center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
            .filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity).toList();
        return LegendaryTargets.select(candidates, LivingEntity::getUniqueId,
            entity -> entity.getLocation().distanceSquared(center),
            entity -> entity.getLocation().distanceSquared(center) <= radius * radius && policy.target(owner, entity),
            integer("max-targets-per-pulse", 12, 1, 64));
    }

    private boolean damage(Player owner, LivingEntity target, double amount) {
        return damage(owner, target, amount, false);
    }

    private boolean spellDamage(Player owner, LivingEntity target, double amount) {
        return damage(owner, target, amount, true);
    }

    private boolean damage(Player owner, LivingEntity target, double amount, boolean magic) {
        if (!policy.target(owner, target)) return false;
        damageTarget = target;
        damageAccepted = false;
        try {
            if (magic) {
                target.damage(amount, DamageSource.builder(DamageType.MAGIC)
                    .withCausingEntity(owner).withDirectEntity(owner).build());
            } else {
                target.damage(amount, owner);
            }
            return damageAccepted;
        } finally {
            damageTarget = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void observedDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().equals(damageTarget)) {
            damageAccepted = !event.isCancelled() && event.getFinalDamage() > 0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void melee(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || damageTarget != null
            || !(event.getEntity() instanceof LivingEntity)) return;
        if (escrow.locked(player, player.getInventory().getItemInMainHand()) || crafting(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void falling(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && crafting(player)) {
            event.setCancelled(true);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
            && fallProtection.getOrDefault(event.getEntity().getUniqueId(), 0L) > System.currentTimeMillis()) {
            event.setCancelled(true);
            fallProtection.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler public void drop(PlayerDropItemEvent event) {
        if (escrow.locked(event.getPlayer(), event.getItemDrop().getItemStack()) || crafting(event.getPlayer())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void milk(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            sessions.values().forEach(session -> session.effects.forget(event.getPlayer().getUniqueId()));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void pendingConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        if (crafting(event.getPlayer())) event.setCancelled(true);
    }
    @EventHandler public void swap(PlayerSwapHandItemsEvent event) {
        if (escrow.locked(event.getPlayer(), event.getMainHandItem()) || escrow.locked(event.getPlayer(), event.getOffHandItem())
            || crafting(event.getPlayer())) event.setCancelled(true);
    }
    @EventHandler public void inventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (crafting(player) || escrow.locked(player, event.getCurrentItem()) || escrow.locked(player, event.getCursor())
            || (event.getHotbarButton() >= 0 && escrow.locked(player, player.getInventory().getItem(event.getHotbarButton())))
            || (event.getClick() == org.bukkit.event.inventory.ClickType.SWAP_OFFHAND
                && escrow.locked(player, player.getInventory().getItemInOffHand()))) {
            event.setCancelled(true);
        }
    }
    @EventHandler public void drag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (crafting(player) || escrow.locked(player, event.getOldCursor())
            || event.getNewItems().values().stream().anyMatch(item -> escrow.locked(player, item))) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.LOWEST) public void death(PlayerDeathEvent event) {
        event.getDrops().removeIf(item -> escrow.locked(event.getEntity(), item));
        if (escrow.pending(event.getEntity().getUniqueId())
            && "on-join".equals(plugin.getConfig().getString("legendary-weapons.spear.death-return"))) {
            try { store.put("spear.defer." + event.getEntity().getUniqueId(), "true"); }
            catch (IOException failure) { reportRecovery(failure); }
        }
        clearPlayerSessions(event.getEntity().getUniqueId());
        // Escrow remains until a safe respawn. The configurable policy selects
        // immediate respawn recovery or a later reconnect recovery, never a drop.
    }
    @EventHandler public void respawn(PlayerRespawnEvent event) {
        if (!"on-join".equals(plugin.getConfig().getString("legendary-weapons.spear.death-return", "on-respawn"))) {
            registrations.scheduleImmediate("spear-respawn", () -> recover(event.getPlayer()));
        }
    }
    @EventHandler public void quit(PlayerQuitEvent event) {
        clearPlayerSessions(event.getPlayer().getUniqueId());
    }

    private void clearPlayerSessions(UUID player) {
        fallProtection.remove(player);
        for (Session session : new ArrayList<>(sessions.values())) {
            if (!session.owner.equals(player)) continue;
            sessions.remove(session.key());
            if (session.display != null) session.display.remove();
            session.ephemeral.forEach(Entity::remove);
            session.cleanupEffects();
        }
    }

    @EventHandler public void join(PlayerJoinEvent event) {
        try { store.put("spear.defer." + event.getPlayer().getUniqueId(), null); }
        catch (IOException failure) { reportRecovery(failure); return; }
        registrations.scheduleImmediate("legendary-spear-join-recovery", () -> recover(event.getPlayer()));
    }
    @EventHandler public void chunk(ChunkLoadEvent event) { cleanTemporary(event.getChunk().getEntities()); }

    void cleanTemporary(Entity[] entities) {
        for (Entity entity : entities) {
            if (entity.getPersistentDataContainer().has(temporary, PersistentDataType.BYTE)
                && sessions.values().stream().noneMatch(session -> entity.equals(session.display)
                    || session.ephemeral.contains(entity))) entity.remove();
        }
    }

    void recover(Player player) {
        if (store.get("spear.defer." + player.getUniqueId()) != null) return;
        try { escrow.restore(player); } catch (IOException failure) { reportRecovery(failure); }
    }

    void shutdown() {
        stopped = true;
        for (Session session : new ArrayList<>(sessions.values())) {
            try { finish(session, false); } catch (IOException failure) { reportRecovery(failure); }
        }
        fallProtection.clear();
    }

    private void finish(Session session, boolean natural) throws IOException {
        sessions.remove(session.key());
        if (session.weapon == LegendaryWeapon.WINDWEAVER) fallProtection.remove(session.owner);
        if (session.display != null) session.display.remove();
        session.ephemeral.forEach(Entity::remove);
        session.cleanupEffects();
        if (session.weapon == LegendaryWeapon.THUNDER_SPEAR) {
            Player owner = plugin.getServer().getPlayer(session.owner);
            if (owner != null && owner.isOnline() && !owner.isDead()) escrow.restore(owner);
        }
    }

    private ItemDisplay display(Location location, ItemStack item) {
        return location.getWorld().spawn(location, ItemDisplay.class, entity -> {
            entity.setItemStack(item.clone());
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            entity.setPersistent(false);
            entity.setTeleportDuration(1);
            entity.getPersistentDataContainer().set(temporary, PersistentDataType.BYTE, (byte) 1);
        });
    }

    private void lightning(Session session, Location location) {
        var strike = location.getWorld().strikeLightningEffect(location);
        strike.setPersistent(false);
        strike.getPersistentDataContainer().set(temporary, PersistentDataType.BYTE, (byte) 1);
        session.ephemeral.add(strike);
    }

    private boolean move(Player player, Location destination) {
        return canMove(player, destination)
            && player.teleport(destination, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    private boolean canMove(Player player, Location destination) {
        Location start = player.getLocation();
        if (!start.getWorld().equals(destination.getWorld())) return false;
        Vector delta = destination.toVector().subtract(start.toVector());
        int steps = Math.max(1, (int) Math.ceil(delta.length() / 0.25));
        for (int i = 1; i <= steps; i++) {
            Location sample = start.clone().add(delta.clone().multiply(i / (double) steps));
            if (!safe(sample) || !policy.allowed(player, sample, null, LegendaryProtectionEvent.Action.MOVE)) return false;
        }
        return true;
    }

    private boolean safe(Location location) {
        var world = location.getWorld();
        if (location.getY() <= world.getMinHeight() + 1 || location.getY() >= world.getMaxHeight() - 2
            || !world.getWorldBorder().isInside(location)
            || !world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return false;
        var box = new org.bukkit.util.BoundingBox(location.getX() - 0.3, location.getY(), location.getZ() - 0.3,
            location.getX() + 0.3, location.getY() + 1.8, location.getZ() + 0.3);
        for (int x = (int) Math.floor(box.getMinX()); x <= Math.floor(box.getMaxX()); x++)
            for (int y = (int) Math.floor(box.getMinY()); y <= Math.floor(box.getMaxY()); y++)
                for (int z = (int) Math.floor(box.getMinZ()); z <= Math.floor(box.getMaxZ()); z++) {
                    var block = world.getBlockAt(x, y, z);
                    if (block.isLiquid() || block.getType() == Material.FIRE || block.getType() == Material.SOUL_FIRE
                        || block.getType() == Material.POWDER_SNOW) return false;
                    if (block.getCollisionShape().getBoundingBoxes().stream()
                        .anyMatch(local -> local.clone().shift(block.getLocation()).overlaps(box))) return false;
                }
        for (int below = 1; below <= 3; below++) {
            var floor = world.getBlockAt(location.getBlockX(), location.getBlockY() - below, location.getBlockZ());
            if (floor.getType() == Material.MAGMA_BLOCK || floor.isLiquid()) return false;
            if (floor.getType().isSolid()) return true;
        }
        return false;
    }

    private void arcaneBurst(Player owner, Location center) {
        ring(center, 6, Color.PURPLE, tick);
        ring(center.clone().add(0, 1, 0), 4.2, Color.PURPLE, tick);
        for (LivingEntity target : enemies(owner, center, 6)) {
            if (spellDamage(owner, target, number("hyperion.impact-damage", 12, 0, 40))) {
                knockback(center, target, .6, .3);
            }
        }
    }

    private void knockback(Location center, LivingEntity target, double horizontal, double up) {
        Vector direction = target.getLocation().toVector().subtract(center.toVector()).setY(0);
        if (direction.lengthSquared() < 0.001) direction = new Vector(1, 0, 0);
        target.setVelocity(direction.normalize().multiply(horizontal).setY(up));
    }

    private void ring(Location center, double radius, Color color, int phase) {
        int count = integer("particles-per-ring", 24, 4, 48);
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2 / count + phase * 0.12;
            dust(center.clone().add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius), color, 1);
        }
    }

    private void dust(Location location, Color color, int count) {
        location.getWorld().spawnParticle(Particle.DUST, location, count, 0.12, 0.12, 0.12, 0,
            new Particle.DustOptions(color, 1.3f));
    }

    private static void potion(Session session, LivingEntity target, PotionEffectType type, int ticks, int amplifier) {
        session.affected.put(target.getUniqueId(), target);
        session.effects.apply(target.getUniqueId(), type.getKey().toString(),
            new LegendaryEffectOwnership.Effect(ticks, amplifier, false, true, true), session.age);
    }


    private static void cooldownBar(Player player, LegendaryWeapon weapon, long remaining) {
        player.sendActionBar(Component.text(weapon.abilityName + " — " + ((remaining + 999) / 1000) + " s"));
    }

    private double number(String path, double fallback, double minimum, double maximum) {
        double value = plugin.getConfig().getDouble("legendary-weapons." + path, fallback);
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }
    private int integer(String path, int fallback, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, plugin.getConfig().getInt("legendary-weapons." + path, fallback)));
    }
    private boolean crafting(Player player) {
        return eventTransaction.test(player.getUniqueId()) || store.get("craft." + player.getUniqueId()) != null;
    }

    private void reportRecovery(Exception failure) {
        plugin.getLogger().log(Level.SEVERE, "La devolución sigue pendiente en el diario persistente.", failure);
    }

    private record SessionKey(UUID owner, LegendaryWeapon weapon) {}
    private record FreezeLease(LivingEntity target, int previous, int previousAt, int owned) {}

    private static final class Session {
        final UUID owner;
        final LegendaryWeapon weapon;
        final LegendaryEarthquakePulse earthquake = new LegendaryEarthquakePulse();
        final List<Entity> ephemeral = new ArrayList<>();
        final Map<UUID, LivingEntity> affected = new HashMap<>();
        final Map<UUID, FreezeLease> freezes = new HashMap<>();
        final LegendaryEffectOwnership effects = new LegendaryEffectOwnership(new LegendaryEffectOwnership.Gateway() {
            private PotionEffectType type(String key) {
                return PotionEffectType.getByKey(NamespacedKey.fromString(key));
            }
            @Override public java.util.Optional<LegendaryEffectOwnership.Effect> current(UUID target, String key) {
                PotionEffect effect = affected.get(target).getPotionEffect(type(key));
                return effect == null ? java.util.Optional.empty() : java.util.Optional.of(
                    new LegendaryEffectOwnership.Effect(effect.getDuration(), effect.getAmplifier(),
                        effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
            }
            @Override public boolean apply(UUID target, String key, LegendaryEffectOwnership.Effect effect) {
                return affected.get(target).addPotionEffect(new PotionEffect(type(key), effect.duration(), effect.amplifier(),
                    effect.ambient(), effect.particles(), effect.icon()));
            }
            @Override public void remove(UUID target, String key) { affected.get(target).removePotionEffect(type(key)); }
        });
        Location center;
        Vector velocity;
        ItemDisplay display;
        int age;
        int impactTick;
        int returnTicks;
        boolean impacted;

        SessionKey key() { return new SessionKey(owner, weapon); }

        void cleanupEffects() {
            effects.cleanup(age);
            freezes.values().forEach(lease -> {
                if (lease.target().getFreezeTicks() <= lease.owned()) {
                    lease.target().setFreezeTicks(Math.max(0, lease.previous() - 2 * (age - lease.previousAt())));
                }
            });
            freezes.clear();
        }

        Session(UUID owner, LegendaryWeapon weapon, Location center) {
            this.owner = owner;
            this.weapon = weapon;
            this.center = center.clone();
        }
    }
}
