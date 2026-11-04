package dev.linqfy.bigCasares.duperestore;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.block.data.type.TripwireHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class StringDupeListener implements Listener, AutoCloseable {
    private record Key(UUID world, StringFarm.Pos pos) {}
    private record Pending(BlockData data, StringFarm.Line line, StringFarm.Pos source, List<ItemStack> drops) {}
    private final Plugin plugin;
    private final Map<Key, Pending> pending = new LinkedHashMap<>();
    private BukkitTask task;
    private boolean closed;

    StringDupeListener(Plugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWaterBreak(BlockBreakBlockEvent event) {
        if (closed || event.getSource().getType() != Material.WATER || !hasString(event.getDrops())) return;
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof Tripwire wire) || !wire.isAttached()) return;
        StringFarm.Pos position = position(block);
        StringFarm.Line line = StringFarm.find(view(block.getWorld()), position);
        if (line == null) return;
        pending.putIfAbsent(new Key(block.getWorld().getUID(), position),
                new Pending(wire.clone(), line, position(event.getSource()), event.getDrops()));
        if (task == null) task = Bukkit.getScheduler().runTask(plugin, this::flush);
    }

    private void flush() {
        task = null;
        Map<Key, Pending> batch = new LinkedHashMap<>(pending);
        pending.clear();
        if (closed) return;
        Map<UUID, Map<StringFarm.Pos, Pending>> worlds = new HashMap<>();
        batch.forEach((key, candidate) -> worlds.computeIfAbsent(key.world(), ignored -> new LinkedHashMap<>())
                .put(key.pos(), candidate));
        worlds.forEach((id, candidates) -> {
            World world = Bukkit.getWorld(id);
            if (world == null) return;
            StringFarm.View view = view(world);
            Map<StringFarm.Pos, StringFarm.Line> lines = new HashMap<>();
            candidates.forEach((pos, candidate) -> {
                if (hasString(candidate.drops()) && view.cell(candidate.source()) == StringFarm.Cell.WATER) {
                    lines.put(pos, candidate.line());
                }
            });
            for (StringFarm.Pos pos : StringFarm.restorable(view, lines)) {
                StringFarm.Cell current = view.cell(pos);
                if (current == StringFarm.Cell.AIR || current == StringFarm.Cell.WATER) {
                    world.getBlockAt(pos.x(), pos.y(), pos.z()).setBlockData(candidates.get(pos).data(), true);
                }
            }
        });
    }

    private static boolean hasString(List<ItemStack> drops) {
        return drops.stream().anyMatch(item -> item.getType() == Material.STRING && item.getAmount() > 0);
    }

    private static StringFarm.Pos position(Block block) {
        return new StringFarm.Pos(block.getX(), block.getY(), block.getZ());
    }

    private static StringFarm.View view(World world) {
        return pos -> {
            if (!world.isChunkLoaded(pos.x() >> 4, pos.z() >> 4)) return StringFarm.Cell.UNLOADED;
            Block block = world.getBlockAt(pos.x(), pos.y(), pos.z());
            Material material = block.getType();
            if (material.isAir()) return StringFarm.Cell.AIR;
            if (material == Material.WATER) return StringFarm.Cell.WATER;
            if (material == Material.TRIPWIRE) return StringFarm.Cell.WIRE;
            if (block.getBlockData() instanceof TripwireHook hook) {
                return switch (hook.getFacing()) {
                    case NORTH -> StringFarm.Cell.HOOK_NORTH;
                    case SOUTH -> StringFarm.Cell.HOOK_SOUTH;
                    case EAST -> StringFarm.Cell.HOOK_EAST;
                    case WEST -> StringFarm.Cell.HOOK_WEST;
                    default -> StringFarm.Cell.OTHER;
                };
            }
            return StringFarm.Cell.OTHER;
        };
    }

    @Override public void close() {
        closed = true;
        HandlerList.unregisterAll(this);
        if (task != null) task.cancel();
        task = null;
        pending.clear();
    }
}
