package dev.linqfy.bigCasares.modules.legendary;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegendaryEarthquakePulseTest {
    @Test void standingInTheWaveCanBeHitOnceInEachOfThreeWaves() {
        var pulse = new LegendaryEarthquakePulse();
        UUID enemy = UUID.randomUUID();
        var hits = new AtomicInteger();
        for (int age = 1; age <= LegendaryEarthquakePulse.DURATION; age++) {
            pulse.strike(age, List.of(enemy), id -> id, id -> { hits.incrementAndGet(); return true; });
        }
        assertEquals(3, hits.get());
    }

    @Test void cancelledOrInvulnerableHitCanRetryWithoutApplyingSecondaryEffects() {
        var pulse = new LegendaryEarthquakePulse();
        UUID enemy = UUID.randomUUID();
        assertTrue(pulse.strike(5, List.of(enemy), id -> id, id -> false).isEmpty());
        assertEquals(List.of(enemy), pulse.strike(6, List.of(enemy), id -> id, id -> true));
        assertTrue(pulse.strike(7, List.of(enemy), id -> id, id -> true).isEmpty());
    }

    @Test void newWaveReachesDistantEnemiesAgainAndNeverConsumesMissingTargets() {
        var pulse = new LegendaryEarthquakePulse();
        UUID enemy = UUID.randomUUID();
        assertEquals(.45, LegendaryEarthquakePulse.radius(1), .0001);
        assertEquals(9, LegendaryEarthquakePulse.radius(20), .0001);
        assertEquals(.45, LegendaryEarthquakePulse.radius(21), .0001);
        pulse.strike(1, List.<UUID>of(), id -> id, id -> true);
        assertEquals(List.of(enemy), pulse.strike(20, List.of(enemy), id -> id, id -> true));
        assertEquals(List.of(enemy), pulse.strike(40, List.of(enemy), id -> id, id -> true));
        assertEquals(List.of(enemy), pulse.strike(60, List.of(enemy), id -> id, id -> true));
    }

    @Test void groundWaveUsesHorizontalRangeAndOnlyNearbyGroundLevels() {
        assertTrue(LegendaryEarthquakePulse.inRange(8.9, 1.8, 0, 9));
        assertFalse(LegendaryEarthquakePulse.inRange(9.1, 0, 0, 9));
        assertFalse(LegendaryEarthquakePulse.inRange(2, 3, 0, 9));
        assertFalse(LegendaryEarthquakePulse.inRange(2, -3, 0, 9));
    }

    @Test void waitingForDebuffsToExpireDoesNotCreateAdditionalWaves() {
        var pulse = new LegendaryEarthquakePulse();
        UUID enemy = UUID.randomUUID();
        var hits = new AtomicInteger();
        for (int age = 1; age <= LegendaryEarthquakePulse.DURATION + LegendaryEarthquakePulse.SLOWNESS_TICKS; age++) {
            pulse.strike(age, List.of(enemy), id -> id, id -> { hits.incrementAndGet(); return true; });
        }
        assertEquals(3, hits.get());
    }
}
