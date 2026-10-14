package dev.linqfy.bigCasares.modules.legendary;

import dev.linqfy.legendary.LegendaryWeaponsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

final class LegendaryWeaponsCommand implements CommandExecutor, TabCompleter {
    private final LegendaryWeaponsPlugin plugin;
    private final LegendaryStateStore store;
    private final LegendaryAltars altars;
    LegendaryWeaponsCommand(LegendaryWeaponsPlugin plugin, LegendaryStateStore store, LegendaryAltars altars) {
        this.plugin = plugin;
        this.store = store;
        this.altars = altars;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            String action = args.length == 0 ? "help" : args[0].toLowerCase(java.util.Locale.ROOT);
            if (action.equals("help")) {
                sender.sendMessage("§6Armas legendarias: §e/weapons list §7| §einfo <arma> §7| §egive <jugador> <arma>");
                sender.sendMessage("§e/weapons altar <create|remove|locate|teleport> [arma]");
                sender.sendMessage("§e/weapons cooldown reset <jugador> §7| §e/weapons reload");
            } else if (action.equals("list")) {
                for (var weapon : LegendaryWeapon.values()) sender.sendMessage("§b" + weapon.id + " §7— " + weapon.abilityName + " (40 s)");
            } else if (action.equals("info")) {
                var weapon = weapon(args, 1);
                sender.sendMessage("§6" + weapon.displayName + " §7— " + weapon.abilityName + " · cooldown: 40 s");
                sender.sendMessage("§e" + weapon.altarName + " §7— " + weapon.environment + " / " + weapon.biome);
                weapon.ingredients.forEach((material, amount) -> sender.sendMessage("§7" + amount + " × " + material));
            } else {
                if (!sender.hasPermission("bigcasares.weapons.admin")) throw new IllegalArgumentException("No tienes permiso para administrar armas legendarias.");
                switch (action) {
                    case "give" -> {
                        if (args.length != 3) throw new IllegalArgumentException("Uso: /weapons give <jugador> <arma>");
                        Player target = plugin.getServer().getPlayerExact(args[1]);
                        if (target == null) throw new IllegalArgumentException("El jugador debe estar conectado.");
                        var weapon = weapon(args, 2);
                        if (target.getInventory().firstEmpty() < 0) throw new IllegalArgumentException("El inventario del jugador está lleno.");
                        target.getInventory().addItem(plugin.getCustomItemRegistry().createItemStack(weapon.id, 1));
                        sender.sendMessage("§aEntregaste " + weapon.displayName + " a " + target.getName() + ".");
                    }
                    case "altar" -> {
                        if (!(sender instanceof Player player)) throw new IllegalArgumentException("Este comando requiere un jugador.");
                        if (args.length < 2) throw new IllegalArgumentException("Uso: /weapons altar <create|remove|locate|teleport> [arma]");
                        String sub = args[1].toLowerCase(java.util.Locale.ROOT);
                        if (sub.equals("create")) {
                            var altar = altars.create(player, weapon(args, 2), player.getLocation());
                            sender.sendMessage("§aCreaste " + altar.weapon().altarName + ".");
                        } else if (sub.equals("remove")) {
                            var altar = altars.nearest(player, null).orElseThrow(() -> new IllegalArgumentException("No hay altares en esta dimensión."));
                            if (altar.location(plugin).distanceSquared(player.getLocation()) > 100) throw new IllegalArgumentException("Acércate al altar para retirarlo.");
                            altars.remove(player, altar);
                            sender.sendMessage("§aAltar retirado. La estructura se conserva.");
                        } else if (sub.equals("locate") || sub.equals("teleport")) {
                            var altar = altars.locate(player, weapon(args, 2)).orElseThrow(() -> new IllegalArgumentException("No hay un altar registrado de esa arma."));
                            var location = altar.location(plugin);
                            sender.sendMessage("§e" + altar.weapon().altarName + " (" + location.getWorld().getName() + "): " + altar.x() + ", " + altar.y() + ", " + altar.z());
                            if (sub.equals("teleport") && !player.teleport(location.clone().add(0, 2, 1),
                                org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND)) sender.sendMessage("§cEl teletransporte fue cancelado.");
                        } else throw new IllegalArgumentException("Acción de altar desconocida.");
                    }
                    case "cooldown" -> {
                        if (args.length != 3 || !args[1].equalsIgnoreCase("reset")) throw new IllegalArgumentException("Uso: /weapons cooldown reset <jugador>");
                        var target = plugin.getServer().getOfflinePlayer(args[2]);
                        if (!target.hasPlayedBefore() && !target.isOnline()) throw new IllegalArgumentException("No se conoce a ese jugador.");
                        store.resetCooldowns(target.getUniqueId());
                        sender.sendMessage("§aCooldowns reiniciados.");
                    }
                    case "reload" -> {
                        plugin.reloadConfig();
                        sender.sendMessage("§aConfiguración recargada. Los cooldowns y devoluciones pendientes se conservan.");
                    }
                    default -> throw new IllegalArgumentException("Comando desconocido. Usa /weapons help.");
                }
            }
        } catch (IllegalArgumentException | java.io.IOException failure) {
            sender.sendMessage("§c" + (failure instanceof java.io.IOException ? "No se pudo guardar el cambio; inténtalo más tarde." : failure.getMessage()));
        }
        return true;
    }

    private static LegendaryWeapon weapon(String[] args, int index) {
        if (index >= args.length) throw new IllegalArgumentException("Indica el ID del arma; usa /weapons list.");
        return LegendaryWeapon.find(args[index]).orElseThrow(() -> new IllegalArgumentException("Arma desconocida."));
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> candidates = args.length == 1 ? List.of("help", "list", "info", "give", "altar", "cooldown", "reload")
            : args.length == 2 && args[0].equalsIgnoreCase("altar") ? List.of("create", "remove", "locate", "teleport")
            : Arrays.stream(LegendaryWeapon.values()).map(weapon -> weapon.id).toList();
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(java.util.Locale.ROOT);
        return candidates.stream().filter(value -> value.startsWith(prefix)).toList();
    }
}
