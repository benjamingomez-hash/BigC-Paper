package dev.linqfy.bigCasares.duperestore.probe;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class DupeRestoreProbe extends JavaPlugin implements Listener {
    private World world;
    private JavaPlugin subject;
    private final YamlConfiguration report = new YamlConfiguration();
    private final Map<Integer, Integer> breaks = new HashMap<>();
    private final Map<Integer, Material> sources = new HashMap<>();

    @Override public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        later(5, this::begin);
    }

    private void begin() throws Exception {
        world = Bukkit.getWorlds().getFirst();
        subject = (JavaPlugin) Bukkit.getPluginManager().getPlugin("DupeRestore");
        report.set("server", Bukkit.getVersion());
        check("startup", subject != null && subject.isEnabled(), "plugin enabled");
        check("native-pistons-enabled", flag("allowPistonDuplication"), "native Paper flag");
        check("native-hooks-enabled", flag("skipTripwireHookPlacementValidation"), "native Paper flag");
        for (int x : new int[]{0, 16, 32, 48, 64, 112, 128, 144, 160}) farm(x);
        at(50, 80, 0).setType(Material.AIR, true);
        at(65, 80, 0).breakNaturally();
        later(10, () -> {
            check("attached-wire", ((Tripwire) at(1, 80, 0).getBlockData()).isAttached(), "before water");
            for (int x : new int[]{0, 16, 32, 48, 144}) water(x);
            at(113, 80, -1).setType(Material.WATER, true);
            at(129, 80, -1).setBlockData(Bukkit.createBlockData(
                    "minecraft:oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]"), true);
            at(129, 79, -1).setType(Material.COBBLESTONE, true);
            at(161, 79, -2).setType(Material.STONE, false);
            at(160, 80, -2).setType(Material.STONE, false);
            at(162, 80, -2).setType(Material.STONE, false);
            at(161, 80, -3).setType(Material.STONE, false);
            at(161, 80, -2).setBlockData(Bukkit.createBlockData(
                    "minecraft:oak_trapdoor[facing=south,half=bottom,open=true,waterlogged=true]"), true);
            at(161, 79, -2).setType(Material.COBBLESTONE, true);
            later(100, this::inspectAndReload);
        });
    }

    private void inspectAndReload() throws Exception {
        int produced = strings(0);
        check("string-farm-duplicates", produced >= 3 && breaks.getOrDefault(1, 0) >= 3 && sources.get(1) == Material.OAK_TRAPDOOR,
                "string items=" + produced + ", water breaks=" + breaks.getOrDefault(1, 0) + ", source=" + sources.get(1));
        check("cancelled-water-respected", strings(16) == 0 && at(17, 80, 0).getType() == Material.TRIPWIRE,
                "items=" + strings(16) + ", block=" + at(17, 80, 0).getType());
        check("suppressed-drops-not-restored", strings(32) == 0 && at(33, 80, 0).getType() != Material.TRIPWIRE,
                "items=" + strings(32));
        check("unconnected-wire-not-restored", strings(48) == 1 && at(49, 80, 0).getType() != Material.TRIPWIRE,
                "items=" + strings(48));
        check("manual-break-not-restored", strings(64) == 1 && at(65, 80, 0).getType() == Material.AIR,
                "items=" + strings(64));
        check("ordinary-water-not-restored", strings(112) == 1 && at(113, 80, 0).getType() != Material.TRIPWIRE,
                "items=" + strings(112) + ", block=" + at(113, 80, 0).getType());
        check("waterlogged-stairs-duplicate", strings(128) >= 3 && breaks.getOrDefault(129, 0) >= 3 && sources.get(129) == Material.OAK_STAIRS,
                "items=" + strings(128) + ", breaks=" + breaks.getOrDefault(129, 0));
        check("drained-source-not-restored", strings(144) == 1 && at(145, 80, 0).getType() != Material.TRIPWIRE,
                "items=" + strings(144));
        check("indirect-flow-not-restored", strings(160) == 1 && at(161, 80, 0).getType() != Material.TRIPWIRE,
                "items=" + strings(160) + ", source=" + sources.get(161));
        configure(false);
        check("reload-restores-native-flags", !flag("allowPistonDuplication") && !flag("skipTripwireHookPlacementValidation"), "both false");
        farm(80);
        later(10, () -> {
            water(80);
            later(60, () -> {
                check("disabled-string-is-vanilla", strings(80) == 1 && at(81, 80, 0).getType() != Material.TRIPWIRE,
                        "items=" + strings(80));
                configure(true);
                check("reload-enables-native-flags", flag("allowPistonDuplication") && flag("skipTripwireHookPlacementValidation"), "both true");
                farm(96);
                later(10, () -> {
                    water(96);
                    later(60, this::finish);
                });
            });
        });
    }

    private void finish() throws Exception {
        check("reload-restores-string-farm", strings(96) >= 3, "items=" + strings(96));
        Bukkit.getPluginManager().disablePlugin(subject);
        check("disable-restores-native-flags", !flag("allowPistonDuplication") && !flag("skipTripwireHookPlacementValidation"), "both false");
        check("disable-cleans-tasks", Bukkit.getScheduler().getPendingTasks().stream().noneMatch(t -> t.getOwner() == subject), "no remaining tasks");
        report.set("completed", true);
        saveAndStop();
    }

    private void configure(boolean enabled) {
        for (String key : new String[]{"piston-duplication", "tripwire-hook-duplication", "string-duplication"}) {
            subject.getConfig().set(key, enabled);
        }
        subject.saveConfig();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "duperestore reload");
    }

    private void farm(int x) {
        for (int cx = (x - 3) >> 4; cx <= (x + 5) >> 4; cx++) {
            for (int cz = -1; cz <= 0; cz++) world.setChunkForceLoaded(cx, cz, true);
        }
        for (int dx = -2; dx <= 4; dx++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = 68; y <= 83; y++) at(x + dx, y, z).setType(y == 68 ? Material.STONE : Material.AIR, false);
            }
        }
        at(x - 1, 80, 0).setType(Material.STONE, false);
        at(x + 3, 80, 0).setType(Material.STONE, false);
        at(x, 80, 0).setBlockData(Bukkit.createBlockData("minecraft:tripwire_hook[facing=east]"), false);
        at(x + 2, 80, 0).setBlockData(Bukkit.createBlockData("minecraft:tripwire_hook[facing=west]"), false);
        at(x + 1, 80, 0).setType(Material.TRIPWIRE, true);
        at(x + 1, 79, -1).setType(Material.STONE, false);
        at(x, 80, -1).setType(Material.STONE, false);
        at(x + 2, 80, -1).setType(Material.STONE, false);
        at(x + 1, 80, -2).setType(Material.STONE, false);
    }

    private void water(int x) {
        at(x + 1, 80, -1).setBlockData(Bukkit.createBlockData(
                "minecraft:oak_trapdoor[facing=south,half=bottom,open=true,waterlogged=true]"), true);
        // Programmatic placement needs a neighbor update to schedule fluid ticks,
        // as naturally filling a waterloggable block with a bucket does.
        at(x + 1, 79, -1).setType(Material.COBBLESTONE, true);
    }
    private Block at(int x, int y, int z) { return world.getBlockAt(x, y, z); }

    private int strings(int farmX) {
        return world.getEntitiesByClass(Item.class).stream()
                .filter(item -> item.getItemStack().getType() == Material.STRING)
                .filter(item -> Math.abs(item.getX() - (farmX + 1)) < 6)
                .mapToInt(item -> item.getItemStack().getAmount()).sum();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlow(BlockFromToEvent event) {
        if (event.getToBlock().getX() == 17 && event.getToBlock().getY() == 80 && event.getToBlock().getZ() == 0) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakBlockEvent event) {
        if (event.getBlock().getType() != Material.TRIPWIRE) return;
        if (event.getBlock().getX() == 33) event.getDrops().clear();
        breaks.merge(event.getBlock().getX(), 1, Integer::sum);
        sources.putIfAbsent(event.getBlock().getX(), event.getSource().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void drainSourceAfterDetection(BlockBreakBlockEvent event) {
        if (event.getBlock().getX() != 145 || event.getBlock().getType() != Material.TRIPWIRE) return;
        if (event.getSource().getBlockData() instanceof Waterlogged wet) {
            wet.setWaterlogged(false);
            event.getSource().setBlockData(wet, false);
        }
    }

    private boolean flag(String name) throws Exception {
        Class<?> type = Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
        Object global = type.getMethod("get").invoke(null);
        Object settings = type.getField("unsupportedSettings").get(global);
        return settings.getClass().getField(name).getBoolean(settings);
    }

    private void check(String name, boolean passed, String detail) {
        report.set("checks." + name + ".passed", passed);
        report.set("checks." + name + ".detail", detail);
        getLogger().info(name + ": " + passed + " (" + detail + ")");
    }

    private interface Step { void run() throws Exception; }
    private void later(long ticks, Step action) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try { action.run(); }
            catch (Throwable error) {
                getLogger().log(java.util.logging.Level.SEVERE, "Native probe failed", error);
                report.set("error", error.toString());
                saveAndStop();
            }
        }, ticks);
    }

    private void saveAndStop() {
        try { report.save(new File("dupe-restore-native-results.yml")); }
        catch (Exception error) { getLogger().severe(error.toString()); }
        Bukkit.shutdown();
    }
}
