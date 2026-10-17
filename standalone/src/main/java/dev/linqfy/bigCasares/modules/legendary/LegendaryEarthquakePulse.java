package dev.linqfy.bigCasares.modules.legendary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/** Three ground waves; a denied hit never consumes a target's hit for that wave. */
final class LegendaryEarthquakePulse {
    static final int DURATION = 60;
    static final int SLOWNESS_TICKS = 80;
    private static final int WAVE_TICKS = 20;
    private final Set<UUID> hit = new HashSet<>();
    private int wave = -1;

    static boolean startsWave(int age) { return (age - 1) % WAVE_TICKS == 0; }

    static double radius(int age) {
        return Math.min(9, (((age - 1) % WAVE_TICKS) + 1) * .45);
    }

    static boolean inRange(double x, double y, double z, double radius) {
        return Math.abs(y) <= 2.5 && x * x + z * z <= radius * radius;
    }

    <T> List<T> strike(int age, Collection<T> targets, Function<T, UUID> identity, Predicate<T> damage) {
        if (age < 1 || age > DURATION) return List.of();
        int currentWave = (age - 1) / WAVE_TICKS;
        if (currentWave != wave) {
            wave = currentWave;
            hit.clear();
        }
        var accepted = new ArrayList<T>();
        for (T target : targets) {
            UUID id = identity.apply(target);
            if (!hit.contains(id) && damage.test(target)) {
                hit.add(id);
                accepted.add(target);
            }
        }
        return accepted;
    }
}
