package dev.linqfy.bigCasares.modules.legendary;

import dev.linqfy.legendary.LegendaryWeaponsPlugin;
import dev.linqfy.bigCasares.module.runtime.BukkitRuntimeRegistrations;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class LegendaryAltars implements Listener, Runnable {
    record Altar(UUID id, LegendaryWeapon weapon, UUID world, int x, int y, int z) {
        Location location(LegendaryWeaponsPlugin plugin) {
            World instance = plugin.getServer().getWorld(world);
            return instance == null ? null : new Location(instance, x + 0.5, y, z + 0.5);
        }
        String encode() { return weapon.id + "," + world + "," + x + "," + y + "," + z; }
        static Altar decode(String key, String value) {
            String[] fields = value.split(",");
            if (fields.length != 5) throw new IllegalArgumentException("Altar persistido inválido");
            return new Altar(UUID.fromString(key.substring(6)), LegendaryWeapon.find(fields[0]).orElseThrow(),
                UUID.fromString(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4]));
        }
    }
    private record ChunkCandidate(UUID world, int x, int z) {}
    private final LegendaryWeaponsPlugin plugin;
    private final LegendaryStateStore store;
    private final LegendaryCombatPolicy policy;
    private final BukkitRuntimeRegistrations registrations;
    private final Map<UUID, Altar> altars = new LinkedHashMap<>();
    private final Map<UUID, ItemDisplay> displays = new HashMap<>();
    private final Map<UUID, Integer> animations = new HashMap<>();
    private final ArrayDeque<ChunkCandidate> generation = new ArrayDeque<>();
    private final NamespacedKey marker = new NamespacedKey("bigcasares", "legendary_altar_display");
    private int ticks;

    LegendaryAltars(LegendaryWeaponsPlugin plugin, LegendaryStateStore store, BukkitRuntimeRegistrations registrations) {
        this.plugin = plugin;
        this.store = store;
        this.registrations = registrations;
        this.policy = new LegendaryCombatPolicy(plugin);
        store.snapshot().forEach((key, value) -> {
            if (key.startsWith("altar.")) {
                Altar altar = Altar.decode(key, value);
                altars.put(altar.id(), altar);
            }
        });
    }

    Altar create(Player actor, LegendaryWeapon weapon, Location location) throws IOException {
        Location base = location.getBlock().getLocation();
        if (altars.values().stream().anyMatch(altar -> altar.world().equals(base.getWorld().getUID())
            && Math.abs(altar.x() - base.getBlockX()) < 6 && Math.abs(altar.z() - base.getBlockZ()) < 6)) {
            throw new IllegalArgumentException("Ya existe un altar en este lugar.");
        }
        if (!validSite(base, actor)) throw new IllegalArgumentException("El altar necesita un área libre y segura de 5×5.");
        if (actor != null) {
            Location standing = base.clone().add(0.5, 2, 0.5);
            if (!policy.allowed(actor, standing, null, LegendaryProtectionEvent.Action.MOVE)
                || !actor.teleport(standing, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                throw new IllegalArgumentException("No se pudo ubicar al jugador de forma segura sobre el altar.");
            }
        }
        Altar altar = new Altar(UUID.randomUUID(), weapon, base.getWorld().getUID(), base.getBlockX(), base.getBlockY(), base.getBlockZ());
        store.put("altar." + altar.id(), altar.encode());
        altars.put(altar.id(), altar);
        build(altar);
        show(altar);
        return altar;
    }

    private boolean validSite(Location base, Player actor) {
        if (base.getY() < base.getWorld().getMinHeight() + 2 || base.getY() > base.getWorld().getMaxHeight() - 5) return false;
        if (base.getWorld().getNearbyEntities(base.clone().add(0.5, 1, 0.5), 2.5, 2, 2.5).stream()
            .anyMatch(entity -> entity instanceof org.bukkit.entity.LivingEntity && !entity.equals(actor))) return false;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location cell = base.clone().add(x, 0, z);
                if (!base.getWorld().isChunkLoaded(cell.getBlockX() >> 4, cell.getBlockZ() >> 4)
                    || !base.getWorld().getWorldBorder().isInside(cell)
                    || !cell.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) return false;
                for (int y = 0; y <= 3; y++) {
                    Location at = cell.clone().add(0, y, 0);
                    if (!at.getBlock().getType().isAir()
                        || !policy.allowed(actor, at, null, LegendaryProtectionEvent.Action.ALTAR)) return false;
                }
            }
        }
        return true;
    }

    private void build(Altar altar) {
        Location center = altar.location(plugin);
        if (center == null) return;
        Material base = switch (altar.weapon()) {
            case BLOODLUST -> Material.CRIMSON_PLANKS;
            case FROST_SCYTHE -> Material.BLUE_ICE;
            case SHADOW_BLADE -> Material.POLISHED_DEEPSLATE;
            case HYPERION -> Material.AMETHYST_BLOCK;
            case WINDWEAVER -> Material.QUARTZ_BLOCK;
            case EARTHBREAKER -> Material.DEEPSLATE_BRICKS;
            case VOID_KATANA -> Material.PURPUR_BLOCK;
            case THUNDER_SPEAR -> Material.COPPER_BLOCK;
        };
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            center.clone().add(x, 0, z).getBlock().setType(base, false);
        }
        center.clone().add(0, 1, 0).getBlock().setType(Material.CHISELED_QUARTZ_BLOCK, false);
        if (altar.weapon() == LegendaryWeapon.THUNDER_SPEAR) {
            for (int x : new int[]{-2, 2}) for (int z : new int[]{-2, 2}) {
                center.clone().add(x, 1, z).getBlock().setType(Material.PRISMARINE_BRICKS, false);
                center.clone().add(x, 2, z).getBlock().setType(Material.LIGHTNING_ROD, false);
            }
        }
    }

    void remove(Player actor, Altar altar) throws IOException {
        Location location = altar.location(plugin);
        if (location == null || !policy.allowed(actor, location, null, LegendaryProtectionEvent.Action.ALTAR)) {
            throw new IllegalArgumentException("No puedes retirar este altar.");
        }
        store.put("altar." + altar.id(), null);
        altars.remove(altar.id());
        ItemDisplay display = displays.remove(altar.id());
        if (display != null) display.remove();
        // Keep blocks: administrators may have incorporated the structure into a build.
    }

    java.util.Optional<Altar> nearest(Player player, LegendaryWeapon weapon) {
        return altars.values().stream().filter(altar -> altar.world().equals(player.getWorld().getUID()))
            .filter(altar -> weapon == null || altar.weapon() == weapon)
            .min(java.util.Comparator.comparingDouble(altar -> altar.location(plugin).distanceSquared(player.getLocation())));
    }

    java.util.Optional<Altar> locate(Player player, LegendaryWeapon weapon) {
        return nearest(player, weapon).or(() -> altars.values().stream()
            .filter(altar -> altar.weapon() == weapon && altar.location(plugin) != null).findFirst());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND
            || event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Location block = event.getClickedBlock().getLocation();
        var altar = altars.values().stream().filter(entry -> entry.world().equals(block.getWorld().getUID())
            && entry.x() == block.getBlockX() && entry.y() + 1 == block.getBlockY() && entry.z() == block.getBlockZ()).findFirst();
        if (altar.isEmpty()) return;
        event.setCancelled(true);
        if (!policy.allowed(event.getPlayer(), block, null, LegendaryProtectionEvent.Action.ALTAR)) return;
        Menu menu = new Menu(altar.orElseThrow().id());
        menu.inventory = plugin.getServer().createInventory(menu, 27, altar.orElseThrow().weapon().altarName);
        int slot = 10;
        for (var ingredient : altar.orElseThrow().weapon().ingredients.entrySet()) {
            menu.inventory.setItem(slot++, new ItemStack(Material.valueOf(ingredient.getKey()), ingredient.getValue()));
        }
        ItemStack button = plugin.getCustomItemRegistry().createItemStack(altar.orElseThrow().weapon().id, 1);
        var meta = button.getItemMeta();
        meta.setLore(java.util.List.of("§eHaz clic para fabricar.", "§7Los ingredientes se toman de tu inventario."));
        button.setItemMeta(meta);
        menu.inventory.setItem(22, button);
        event.getPlayer().openInventory(menu.inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        event.setCancelled(true);
        if (event.getRawSlot() != 22 || !event.isLeftClick() || !(event.getWhoClicked() instanceof Player player)) return;
        Altar altar = altars.get(menu.altar);
        if (altar == null || !altar.world().equals(player.getWorld().getUID())
            || altar.location(plugin).distanceSquared(player.getLocation()) > 36
            || !policy.allowed(player, altar.location(plugin), null, LegendaryProtectionEvent.Action.ALTAR)) return;
        if (store.get("spear." + player.getUniqueId()) != null) {
            player.sendMessage("§eEspera a recuperar tu lanza antes de fabricar.");
            return;
        }
        if (animations.containsKey(player.getUniqueId()) || store.get("craft." + player.getUniqueId()) != null) {
            player.sendMessage("§eTu fabricación anterior todavía está en curso.");
            return;
        }
        try {
            ItemStack[] before = player.getInventory().getStorageContents();
            ItemStack[] after = java.util.Arrays.stream(before).map(item -> item == null ? null : item.clone()).toArray(ItemStack[]::new);
            Map<String, Integer> counts = new HashMap<>();
            for (ItemStack item : after) {
                if (vanillaIngredient(item)) counts.merge(item.getType().name(), item.getAmount(), Integer::sum);
            }
            if (!altar.weapon().canCraft(counts)) {
                player.sendMessage("§cNo tienes todos los ingredientes del altar.");
                return;
            }
            for (var ingredient : altar.weapon().ingredients.entrySet()) {
                int remaining = ingredient.getValue();
                for (int i = 0; i < after.length && remaining > 0; i++) {
                    ItemStack item = after[i];
                    if (!vanillaIngredient(item) || !item.getType().name().equals(ingredient.getKey())) continue;
                    int taken = Math.min(remaining, item.getAmount());
                    remaining -= taken;
                    item.setAmount(item.getAmount() - taken);
                    if (item.getAmount() == 0) after[i] = null;
                }
            }
            int empty = -1;
            for (int i = 0; i < after.length; i++) if (after[i] == null || after[i].getType().isAir()) { empty = i; break; }
            if (empty < 0) {
                player.sendMessage("§cLibera un espacio para fabricar.");
                return;
            }
            after[empty] = plugin.getCustomItemRegistry().createItemStack(altar.weapon().id, 1);
            var itemMeta = after[empty].getItemMeta();
            String transaction = UUID.randomUUID().toString();
            itemMeta.getPersistentDataContainer().set(new NamespacedKey("bigcasares", "legendary_instance"),
                PersistentDataType.STRING, transaction);
            after[empty].setItemMeta(itemMeta);
            // Recovery is the exact post-transaction inventory. Synchronous
            // save/ack leaves no interval where a player can spend inputs twice.
            LegendaryPlayerSaveVerifier.checkpoint(player);
            store.put("craft." + player.getUniqueId(), Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(after)));
            player.getInventory().setStorageContents(after);
            LegendaryPlayerSaveVerifier.save(player, new NamespacedKey("bigcasares", "legendary_instance"), transaction);
            store.put("craft." + player.getUniqueId(), null);
            animations.put(player.getUniqueId(), 40);
            player.closeInventory();
            player.sendMessage("§aHas fabricado " + altar.weapon().displayName + " en " + altar.weapon().altarName + ".");
            player.getWorld().playSound(altar.location(plugin), Sound.BLOCK_END_PORTAL_SPAWN, 0.6f, 1.5f);
        } catch (IOException | RuntimeException failure) {
            player.closeInventory();
            player.sendMessage("§cLa fabricación quedó protegida para recuperación.");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "No se pudo confirmar la fabricación.", failure);
        }
    }

    private boolean vanillaIngredient(ItemStack item) {
        return item != null && !item.getType().isAir()
            && plugin.getCustomItemRegistry().resolveItemId(item).isEmpty()
            && (!item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey("bigcasares", "legendary_receipt"), PersistentDataType.STRING));
    }

    @EventHandler public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }
    @EventHandler public void join(PlayerJoinEvent event) { recoverCraft(event.getPlayer()); }

    void recoverCraft(Player player) {
        String encoded = store.get("craft." + player.getUniqueId());
        if (encoded == null) return;
        try {
            player.getInventory().setStorageContents(ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(encoded)));
            LegendaryPlayerSaveVerifier.checkpoint(player);
            store.put("craft." + player.getUniqueId(), null);
        } catch (IOException | RuntimeException failure) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "La fabricación sigue pendiente.", failure);
        }
    }

    @EventHandler public void chunk(ChunkLoadEvent event) {
        for (var entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(marker, PersistentDataType.STRING)) entity.remove();
        }
        if (event.isNewChunk() && plugin.getConfig().getBoolean("legendary-weapons.altars.generate", true)
            && generation.size() < 128) {
            generation.add(new ChunkCandidate(event.getWorld().getUID(), event.getChunk().getX(), event.getChunk().getZ()));
        }
    }

    @Override public void run() {
        ticks++;
        for (Altar altar : altars.values()) {
            Location location = altar.location(plugin);
            if (location == null || !location.getWorld().isChunkLoaded(altar.x() >> 4, altar.z() >> 4)) continue;
            show(altar);
            location.getWorld().spawnParticle(Particle.ENCHANT, location.clone().add(0, 2.2, 0), 5, 0.5, 0.3, 0.5, 0.2);
            ItemDisplay display = displays.get(altar.id());
            if (display != null) {
                Location floating = location.clone().add(0, 2.7 + Math.sin(ticks * 0.3) * 0.15, 0);
                floating.setYaw(ticks * 7);
                display.teleport(floating);
            }
        }
        animations.replaceAll((id, remaining) -> {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 12, 0.5, 0.5, 0.5, 0.03);
            return remaining - 5;
        });
        animations.values().removeIf(value -> value <= 0);
        if (ticks % 4 == 0 && !generation.isEmpty()) generate(generation.removeFirst());
    }

    private void generate(ChunkCandidate candidate) {
        World world = plugin.getServer().getWorld(candidate.world());
        if (world == null || !world.isChunkLoaded(candidate.x(), candidate.z())) return;
        int spacing = Math.max(16, plugin.getConfig().getInt("legendary-weapons.altars.chance-denominator", 128));
        long hash = world.getSeed() ^ candidate.x() * 341873128712L ^ candidate.z() * 132897987541L;
        if (Math.floorMod(hash, spacing) != 0) return;
        int x = candidate.x() * 16 + 8;
        int z = candidate.z() * 16 + 8;
        for (LegendaryWeapon weapon : LegendaryWeapon.values()) {
            if (!world.getEnvironment().name().equals(weapon.environment)) continue;
            int cap = Math.max(1, plugin.getConfig().getInt("legendary-weapons.altars.max-per-weapon-per-world", 1));
            if (altars.values().stream().filter(altar -> altar.weapon() == weapon && altar.world().equals(world.getUID())).count() >= cap) continue;
            if (world.getEnvironment() == World.Environment.THE_END && (long) x * x + (long) z * z < 1_000_000) continue;
            if (weapon == LegendaryWeapon.EARTHBREAKER && world.getHighestBlockYAt(x, z) < 120) continue;
            boolean cave = weapon == LegendaryWeapon.SHADOW_BLADE || weapon == LegendaryWeapon.EARTHBREAKER;
            int top = cave ? Math.min(96, world.getMaxHeight() - 5)
                : world.getEnvironment() == World.Environment.NETHER ? 110 : world.getHighestBlockYAt(x, z) + 1;
            int bottom = cave ? world.getMinHeight() + 3 : world.getEnvironment() == World.Environment.NETHER ? 35 : top;
            for (int y = top; y >= bottom; y--) {
                Location site = new Location(world, x, y, z);
                if (!world.getBiome(x, y, z).name().equals(weapon.biome) || !validSite(site, null)) continue;
                if (weapon == LegendaryWeapon.WINDWEAVER && y < 120) continue;
                if (cave && !hasRoof(site)) continue;
                try { create(null, weapon, site); } catch (IOException | RuntimeException failure) {
                    plugin.getLogger().log(java.util.logging.Level.WARNING, "No se pudo generar un altar.", failure);
                }
                return;
            }
        }
    }

    private boolean hasRoof(Location site) {
        for (int y = 4; y < 24; y++) if (site.clone().add(0, y, 0).getBlock().getType().isSolid()) return true;
        return false;
    }

    private void show(Altar altar) {
        ItemDisplay previous = displays.get(altar.id());
        if (previous != null && previous.isValid()) return;
        Location location = altar.location(plugin);
        if (location == null) return;
        displays.put(altar.id(), location.getWorld().spawn(location.clone().add(0, 2.7, 0), ItemDisplay.class, display -> {
            display.setItemStack(plugin.getCustomItemRegistry().createItemStack(altar.weapon().id, 1));
            display.setPersistent(false);
            display.setTeleportDuration(5);
            display.getPersistentDataContainer().set(marker, PersistentDataType.STRING, altar.id().toString());
        }));
    }

    void shutdown() {
        displays.values().forEach(ItemDisplay::remove);
        displays.clear();
        animations.clear();
        generation.clear();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu) player.closeInventory();
        }
    }

    private static final class Menu implements InventoryHolder {
        final UUID altar;
        Inventory inventory;
        Menu(UUID altar) { this.altar = altar; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
