package dev.linqfy.legendary;

import com.sun.net.httpserver.HttpServer;
import dev.linqfy.bigCasares.modules.legendary.LegendaryWeaponsModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LegendaryWeaponsPlugin extends JavaPlugin implements Listener {
    private final LegendaryItemRegistry items = new LegendaryItemRegistry();
    private LegendaryWeaponsModule weapons;
    private HttpServer packServer;
    private ExecutorService packExecutor;
    private byte[] packHash;

    public LegendaryItemRegistry getCustomItemRegistry() { return items; }

    @Override public void onEnable() {
        saveDefaultConfig();
        try {
            weapons = new LegendaryWeaponsModule(this);
            weapons.onEnable();
            if (getConfig().getBoolean("resource-pack.enabled", true)) {
                saveResource("legendary-pack.zip", true);
                byte[] pack = Files.readAllBytes(getDataFolder().toPath().resolve("legendary-pack.zip"));
                packHash = MessageDigest.getInstance("SHA-1").digest(pack);
                packServer = HttpServer.create(new InetSocketAddress(
                    getConfig().getString("resource-pack.bind-address", "127.0.0.1"),
                    getConfig().getInt("resource-pack.port", 8124)), 0);
                packExecutor = Executors.newFixedThreadPool(2);
                packServer.setExecutor(packExecutor);
                packServer.createContext("/legendary-pack.zip", exchange -> {
                    try (exchange) {
                        if (!exchange.getRequestMethod().equals("GET")) {
                            exchange.sendResponseHeaders(405, -1);
                            return;
                        }
                        exchange.getResponseHeaders().set("Content-Type", "application/zip");
                        exchange.sendResponseHeaders(200, pack.length);
                        exchange.getResponseBody().write(pack);
                    }
                });
                packServer.start();
                getServer().getPluginManager().registerEvents(this, this);
            }
            getLogger().info("Ocho armas legendarias listas. Usa /weapons help.");
        } catch (Exception failure) {
            getLogger().log(java.util.logging.Level.SEVERE, "No se pudieron iniciar las armas legendarias.", failure);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @EventHandler public void onJoin(PlayerJoinEvent event) {
        getServer().getScheduler().runTaskLater(this, () -> {
            if (event.getPlayer().isOnline() && packHash != null) {
                event.getPlayer().setResourcePack(getConfig().getString("resource-pack.url",
                    "http://127.0.0.1:8124/legendary-pack.zip"), packHash);
            }
        }, 40);
    }

    @Override public void onDisable() {
        if (weapons != null) weapons.onDisable();
        if (packServer != null) packServer.stop(0);
        if (packExecutor != null) packExecutor.shutdownNow();
        getServer().getScheduler().cancelTasks(this);
    }
}
