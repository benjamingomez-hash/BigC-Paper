package dev.linqfy.bigCasares.modules.schizophrenia;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

final class SchizophreniaListener implements Listener {

    private final SchizophreniaRuntime runtime;
    private final LobotomizerItem lobotomizer;
    private final NamespacedKey recipeKey;

    SchizophreniaListener(SchizophreniaRuntime runtime, LobotomizerItem lobotomizer, NamespacedKey recipeKey) {
        this.runtime = runtime;
        this.lobotomizer = lobotomizer;
        this.recipeKey = recipeKey;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPhantomDamaged(EntityDamageEvent event) {
        if (!runtime.ownsPhantom(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
        if (event instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof Player player) {
            runtime.dispelPhantom(player, event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhantomTouched(PlayerInteractEntityEvent event) {
        if (runtime.ownsPhantom(event.getRightClicked())) {
            event.setCancelled(true);
            runtime.dispelPhantom(event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLobotomy(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player surgeon && event.getEntity() instanceof Player patient
            && lobotomizer.isLobotomizer(surgeon.getInventory().getItemInMainHand())) {
            runtime.lobotomize(surgeon, patient, lobotomizer);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMilk(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET && runtime.isAffected(event.getPlayer())) {
            event.getPlayer().sendMessage(Component.text(
                "La leche no cura la cabeza.", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        runtime.resume(event.getPlayer());
        if (recipeKey != null) {
            event.getPlayer().discoverRecipe(recipeKey);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        runtime.suspend(event.getPlayer().getUniqueId());
    }
}
