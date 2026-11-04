package dev.linqfy.bigCasares.duperestore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class DupeRestorePlugin extends JavaPlugin implements Listener {
    private FlagLease flags;
    private PaperDupeFlags access;
    private StringDupeListener strings;

    @Override public void onEnable() {
        saveDefaultConfig();
        try {
            applySettings();
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("DupeRestore activo: " + status());
        } catch (ReflectiveOperationException | RuntimeException error) {
            getLogger().log(Level.SEVERE, "No se pudo activar DupeRestore. Requiere las opciones nativas de Paper 26.2.", error);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void applySettings() throws ReflectiveOperationException {
        FileConfiguration config = getConfig();
        for (String key : List.of("piston-duplication", "tripwire-hook-duplication", "string-duplication")) {
            if (!config.isBoolean(key)) throw new IllegalArgumentException("La opción " + key + " debe ser true o false.");
        }
        EnumSet<FlagLease.Key> requested = EnumSet.noneOf(FlagLease.Key.class);
        if (config.getBoolean("piston-duplication")) requested.add(FlagLease.Key.PISTONS);
        if (config.getBoolean("tripwire-hook-duplication")) requested.add(FlagLease.Key.TRIPWIRE_HOOKS);
        PaperDupeFlags candidate = new PaperDupeFlags(Bukkit.getServer().getClass().getClassLoader());
        stopRuntime();
        flags = FlagLease.apply(candidate, requested);
        access = candidate;
        if (config.getBoolean("string-duplication")) {
            strings = new StringDupeListener(this);
            getServer().getPluginManager().registerEvents(strings, this);
        }
    }

    @EventHandler public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.RELOAD) return;
        try { applySettings(); }
        catch (ReflectiveOperationException | RuntimeException error) {
            getLogger().log(Level.SEVERE, "No se pudieron reaplicar los dupes después de recargar Paper.", error);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override public void onDisable() { stopRuntime(); }

    private void stopRuntime() {
        if (strings != null) strings.close();
        strings = null;
        if (flags != null) flags.close();
        flags = null;
        access = null;
    }

    private String status() {
        if (access == null) return "inactivo";
        return "TNT/alfombras/raíles=" + access.get(FlagLease.Key.PISTONS)
                + ", ganchos=" + access.get(FlagLease.Key.TRIPWIRE_HOOKS) + ", hilo=" + (strings != null);
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duperestore.admin")) {
            sender.sendMessage("No tenés permiso para administrar DupeRestore.");
            return true;
        }
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("status"))) {
            sender.sendMessage("DupeRestore: " + status());
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                reloadConfig();
                applySettings();
                sender.sendMessage("DupeRestore recargado: " + status());
            } catch (ReflectiveOperationException | RuntimeException error) {
                getLogger().log(Level.WARNING, "No se pudo recargar la configuración de DupeRestore.", error);
                sender.sendMessage("No se pudo recargar: " + error.getMessage());
            }
            return true;
        }
        return false;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("duperestore.admin") || args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return List.of("status", "reload").stream().filter(option -> option.startsWith(prefix)).toList();
    }
}
