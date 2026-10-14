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
    private LegendaryAltars altars;

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
            runtime = new LegendaryRuntime(plugin, store, registrations);
            altars = new LegendaryAltars(plugin, store, registrations);
            scope.register("legendary-effects", runtime::shutdown);
            scope.register("legendary-altars", altars::shutdown);
            registrations.registerListener("legendary-listener", runtime);
            registrations.registerListener("legendary-altar-listener", altars);
            registrations.scheduleRepeating("legendary-effects-tick", runtime, 1, 1);
            registrations.scheduleRepeating("legendary-altar-tick", altars, 5, 5);
            var command = plugin.getCommand("weapons");
            if (command == null) throw new IllegalStateException("Falta el comando weapons en plugin.yml.");
            var executor = new LegendaryWeaponsCommand(plugin, store, altars);
            registrations.bindCommand("legendary-command", command, executor, executor);
            for (var world : plugin.getServer().getWorlds()) {
                for (var chunk : world.getLoadedChunks()) runtime.cleanTemporary(chunk.getEntities());
            }
            for (var player : plugin.getServer().getOnlinePlayers()) {
                altars.recoverCraft(player);
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
        if (altars != null) altars.shutdown();
        runtime = null;
        altars = null;
    }
}
