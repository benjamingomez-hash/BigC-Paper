package dev.linqfy.bigCasares.modules.legendary;

import dev.linqfy.legendary.LegendaryWeaponsPlugin;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Predicate;

/** Owns only the first custom-model-data float; never changes item identity or combat. */
final class LegendaryModelAnimations implements Listener, Runnable {
    private final LegendaryWeaponsPlugin plugin;
    private final Predicate<Player> pending;
    private final LegendaryAnimationCatalog catalog;
    private final LegendaryAnimationTimeline timeline;
    private long tick;
    private boolean stopped;

    LegendaryModelAnimations(LegendaryWeaponsPlugin plugin, Predicate<Player> pending)
            throws IOException {
        this.plugin = plugin;
        this.pending = pending;
        try (var input = plugin.getResource("legendary-animations.properties")) {
            catalog = LegendaryAnimationCatalog.load(input);
        }
        timeline = new LegendaryAnimationTimeline(catalog);
    }

    private LegendaryWeapon weapon(ItemStack item) {
        return plugin.getCustomItemRegistry()
                .resolveItemId(item)
                .flatMap(LegendaryWeapon::find)
                .orElse(null);
    }

    private boolean apply(ItemStack item, int frame) {
        if (weapon(item) == null) return false;
        var meta = item.getItemMeta();
        var data = meta.getCustomModelDataComponent();
        var floats = new ArrayList<>(data.getFloats());
        if (floats.isEmpty() && frame == 0) return false;
        if (!floats.isEmpty() && Float.compare(floats.getFirst(), frame) == 0) return false;
        if (floats.isEmpty()) floats.add((float) frame);
        else floats.set(0, (float) frame);
        data.setFloats(floats);
        meta.setCustomModelDataComponent(data);
        item.setItemMeta(meta);
        return true;
    }

    @Override
    public void run() {
        if (stopped) return;
        tick++;
        for (var player : plugin.getServer().getOnlinePlayers()) {
            if (pending.test(player)) {
                timeline.forget(player.getUniqueId());
                continue;
            }
            var inventory = player.getInventory();
            int slot = inventory.getHeldItemSlot();
            for (int index = 0; index < inventory.getSize(); index++) {
                if (index == slot) continue;
                ItemStack item = inventory.getItem(index);
                if (apply(item, 0)) inventory.setItem(index, item);
            }
            ItemStack held = inventory.getItemInMainHand();
            LegendaryWeapon weapon = weapon(held);
            if (weapon == null || player.isDead()) {
                timeline.forget(player.getUniqueId());
                continue;
            }
            int frame = timeline.frame(player.getUniqueId(), weapon, slot, tick);
            if (apply(held, frame)) inventory.setItem(slot, held);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void swing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING
                || pending.test(event.getPlayer())) return;
        Player player = event.getPlayer();
        var weapon = weapon(player.getInventory().getItemInMainHand());
        if (weapon == null) return;
        String clip =
                switch (weapon) {
                    case BLOODLUST -> player.isSneaking() ? "estocada" : "ataque_tajo";
                    case SHADOW_BLADE -> "golpe_oscuro";
                    case WINDWEAVER -> "golpe_lateral";
                    default -> "";
                };
        timeline.play(
                player.getUniqueId(),
                weapon,
                player.getInventory().getHeldItemSlot(),
                tick,
                1,
                clip);
    }

    void ability(Player player, LegendaryWeapon weapon) {
        String[] clips =
                switch (weapon) {
                    case BLOODLUST -> new String[] {"estocada"};
                    case SHADOW_BLADE -> new String[] {"aura_oscura", "tira_particulas"};
                    case WINDWEAVER -> new String[] {"golpe_martillo"};
                    case THUNDER_SPEAR -> new String[] {"rayo"};
                    default -> new String[0];
                };
        timeline.play(
                player.getUniqueId(),
                weapon,
                player.getInventory().getHeldItemSlot(),
                tick,
                2,
                clips);
    }

    void display(ItemDisplay display, LegendaryWeapon weapon, long age) {
        if (display == null || !display.isValid()) return;
        var clip = catalog.clip(weapon, catalog.idle(weapon));
        ItemStack item = display.getItemStack();
        if (clip != null && apply(item, clip.frame(age))) display.setItemStack(item);
    }

    private void reset(Player player) {
        timeline.forget(player.getUniqueId());
        if (pending.test(player)) return;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (apply(item, 0)) player.getInventory().setItem(slot, item);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void drop(PlayerDropItemEvent event) {
        if (pending.test(event.getPlayer())) return;
        var item = event.getItemDrop().getItemStack();
        if (apply(item, 0)) event.getItemDrop().setItemStack(item);
        timeline.forget(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void death(PlayerDeathEvent event) {
        if (!pending.test(event.getEntity())) event.getDrops().forEach(item -> apply(item, 0));
        timeline.forget(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        reset(event.getPlayer());
    }

    void shutdown() {
        if (stopped) return;
        stopped = true;
        plugin.getServer().getOnlinePlayers().forEach(this::reset);
        timeline.clear();
    }
}
