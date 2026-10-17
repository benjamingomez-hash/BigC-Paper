package dev.linqfy.bigCasares.modules.legendary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Temporary movement anchors; overlapping fields share a target's first anchor. */
final class LegendaryParalysis {
    record Point(UUID world, double x, double y, double z) { }
    private final Map<UUID, Set<UUID>> fields = new HashMap<>();
    private final Map<UUID, Point> anchors = new HashMap<>();

    static boolean inside(Point center, Point position, double radius) {
        double x = position.x() - center.x(), z = position.z() - center.z();
        return center.world().equals(position.world()) && Math.abs(position.y() - center.y()) <= 4
            && x * x + z * z <= radius * radius;
    }

    void acquire(UUID field, UUID target, Point position) {
        fields.computeIfAbsent(field, ignored -> new HashSet<>()).add(target);
        anchors.putIfAbsent(target, position);
    }

    void sync(UUID field, Map<UUID, Point> targets) {
        var previous = new HashSet<>(fields.getOrDefault(field, Set.of()));
        for (UUID target : previous) if (!targets.containsKey(target)) {
            fields.get(field).remove(target);
            releaseUnreferenced(target);
        }
        targets.forEach((target, position) -> acquire(field, target, position));
    }

    Optional<Point> anchor(UUID target) { return Optional.ofNullable(anchors.get(target)); }
    Set<UUID> targets() { return Set.copyOf(anchors.keySet()); }

    void release(UUID field) {
        Set<UUID> targets = fields.remove(field);
        if (targets != null) targets.forEach(this::releaseUnreferenced);
    }

    void forget(UUID target) {
        fields.values().forEach(targets -> targets.remove(target));
        anchors.remove(target);
    }

    void clear() { fields.clear(); anchors.clear(); }

    private void releaseUnreferenced(UUID target) {
        if (fields.values().stream().noneMatch(targets -> targets.contains(target))) anchors.remove(target);
    }
}
