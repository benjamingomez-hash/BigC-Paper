package dev.linqfy.bigCasares.duperestore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class DupeRestorePlugin extends JavaPlugin {
    private StringDupeListener strings;

    @Override public void onEnable() {
        try {
            saveDefaultConfig();
            applyStringSettings();
        } catch (RuntimeException error) {
            getLogger().log(Level.SEVERE, "No se pudo activar DupeRestore.", error);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("DupeRestore activo: hilo=" + (strings != null));
        getLogger().info(nativeStatus());
        if (getConfig().contains("piston-duplication", true) || getConfig().contains("tripwire-hook-duplication", true)) {
            getLogger().warning("Las opciones antiguas de pistones y ganchos ya no se aplican desde este plugin. "
                    + "Configurá las dos opciones de unsupported-settings en config/paper-global.yml y reiniciá Paper.");
        }
    }

    private void applyStringSettings() {
        FileConfiguration config = getConfig();
        if (!config.isBoolean("string-duplication")) {
            throw new IllegalArgumentException("La opción string-duplication debe ser true o false.");
        }
        StringDupeListener candidate = config.getBoolean("string-duplication") ? new StringDupeListener(this) : null;
        if (candidate != null) {
            try { getServer().getPluginManager().registerEvents(candidate, this); }
            catch (RuntimeException error) { candidate.close(); throw error; }
        }
        StringDupeListener previous = strings;
        strings = candidate;
        if (previous != null) previous.close();
    }

    private String nativeStatus() {
        try {
            // Paper's public API returns a configuration snapshot. Reading it
            // does not change either the live configuration or its files.
            FileConfiguration snapshot = getServer().spigot().getPaperConfig();
            PaperOptions options = PaperOptions.read(snapshot::get);
            return "Paper configurado en archivo: TNT/alfombras/raíles=" + options.pistons()
                    + ", ganchos=" + options.hooks() + ". Reiniciá Paper después de cambiar esas opciones.";
        } catch (RuntimeException error) {
            return "No se pudo consultar la configuración de Paper. Revisá las dos opciones de unsupported-settings del LEEME.";
        }
    }

    @Override public void onDisable() {
        if (strings != null) strings.close();
        strings = null;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duperestore.admin")) {
            sender.sendMessage("No tenés permiso para administrar DupeRestore.");
            return true;
        }
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("status"))) {
            sender.sendMessage("DupeRestore: hilo=" + (strings != null));
            sender.sendMessage(nativeStatus());
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                reloadConfig();
                applyStringSettings();
                sender.sendMessage("Configuración del hilo recargada: hilo=" + (strings != null));
            } catch (RuntimeException error) {
                getLogger().log(Level.WARNING, "No se pudo recargar la configuración del hilo.", error);
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
