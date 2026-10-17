package dev.linqfy.bigCasares.modules.legendary;

import dev.linqfy.legendary.LegendaryWeaponsPlugin;
import dev.linqfy.bigCasares.module.PluginModule;
import dev.linqfy.bigCasares.module.runtime.BukkitRuntimeRegistrations;
import dev.linqfy.bigCasares.module.runtime.RuntimeRegistrationScope;

public final class LegendaryWeaponsModule implements PluginModule {
    public static final String MODULE_ID = "legendary-weapons";
    private final LegendaryWeaponsPlugin plugin;
    private RuntimeRegistrationScope compatibilityScope;
    private LegendaryRuntime runtime;
    private LegendaryCraftingEvents events;

    public LegendaryWeaponsModule(LegendaryWeaponsPlugin plugin) { this.plugin = plugin; }
    @Override public String getId() { return MODULE_ID; }
    @Override public void onEnable() {
        compatibilityScope = new RuntimeRegistrationScope();
        onEnable(compatibilityScope);
    }
    @Override public void onEnable(RuntimeRegistrationScope scope) {
        try {
            LegendaryStateStore store = new LegendaryStateStore(plugin.getDataFolder().toPath().resolve("data/legendary/state.properties"));
            BukkitRuntimeRegistrations registrations = new BukkitRuntimeRegistrations(plugin, scope);
            if (store.snapshot().keySet().stream().anyMatch(key -> key.startsWith("altar.") || key.startsWith("craft."))) {
                throw new IllegalStateException("Hay datos de altares anteriores. Conserva una copia y usa una instalación nueva para Crafting PvP.");
            }
            events = new LegendaryCraftingEvents(plugin, store, registrations);
            runtime = new LegendaryRuntime(plugin, store, registrations, events::pending, events::protectedCell);
            scope.register("legendary-effects", runtime::shutdown);
            scope.register("legendary-events", events::shutdown);
            registrations.registerListener("legendary-event-listener", events);
            registrations.registerListener("legendary-listener", runtime);
            registrations.scheduleRepeating("legendary-effects-tick", runtime, 1, 1);
            registrations.scheduleRepeating("legendary-event-tick", events, 1, 1);
            var command = plugin.getCommand("weapons");
            if (command == null) throw new IllegalStateException("Falta el comando weapons en plugin.yml.");
            var executor = new LegendaryWeaponsCommand(plugin, store, events);
            registrations.bindCommand("legendary-command", command, executor, executor);
            for (var world : plugin.getServer().getWorlds()) {
                for (var chunk : world.getLoadedChunks()) {
                    runtime.cleanTemporary(chunk.getEntities());
                    events.clean(chunk.getEntities());
                }
            }
            for (var player : plugin.getServer().getOnlinePlayers()) {
                events.recover(player);
                events.discover(player);
                runtime.recover(player);
            }
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("No se pudo abrir el estado persistente de armas legendarias.", failure);
        }
    }
    @Override public void onDisable() {
        if (compatibilityScope != null) {
            compatibilityScope.close();
            compatibilityScope = null;
        }
        if (runtime != null) runtime.shutdown();
        if (events != null) events.shutdown();
        runtime = null;
        events = null;
    }
}
