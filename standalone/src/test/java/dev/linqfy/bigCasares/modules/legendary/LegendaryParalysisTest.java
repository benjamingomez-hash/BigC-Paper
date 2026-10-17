package dev.linqfy.bigCasares.modules.legendary;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegendaryParalysisTest {
    private final UUID world = UUID.randomUUID();

    @Test void repeatedScansPreserveTheOriginalAnchorInsteadOfFollowingEscapeAttempts() {
        var locks = new LegendaryParalysis();
        UUID field = UUID.randomUUID(), target = UUID.randomUUID();
        var start = point(1, 2, 3);
        locks.sync(field, Map.of(target, start));
        locks.sync(field, Map.of(target, point(4, 5, 6)));
        assertEquals(start, locks.anchor(target).orElseThrow());
    }

    @Test void casterAndEnemyCanBeLockedTogetherAndReleasedAtTheEnd() {
        var locks = new LegendaryParalysis();
        UUID caster = UUID.randomUUID(), enemy = UUID.randomUUID();
        locks.sync(caster, Map.of(caster, point(0, 0, 0), enemy, point(2, 0, 0)));
        assertTrue(locks.anchor(caster).isPresent());
        assertTrue(locks.anchor(enemy).isPresent());
        locks.release(caster);
        assertTrue(locks.targets().isEmpty());
    }

    @Test void overlappingFieldsDoNotReleaseEachOthersTargets() {
        var locks = new LegendaryParalysis();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID(), target = UUID.randomUUID();
        locks.acquire(first, target, point(1, 0, 1));
        locks.acquire(second, target, point(2, 0, 2));
        locks.release(first);
        assertEquals(point(1, 0, 1), locks.anchor(target).orElseThrow());
        locks.release(second);
        assertTrue(locks.anchor(target).isEmpty());
    }

    @Test void leavingTheFieldOrDisconnectingRemovesOnlyTheRelevantBindings() {
        var locks = new LegendaryParalysis();
        UUID field = UUID.randomUUID(), first = UUID.randomUUID(), second = UUID.randomUUID();
        locks.sync(field, Map.of(first, point(1, 0, 1), second, point(2, 0, 2)));
        locks.sync(field, Map.of(second, point(2, 0, 2)));
        assertTrue(locks.anchor(first).isEmpty());
        assertTrue(locks.anchor(second).isPresent());
        locks.forget(second);
        assertTrue(locks.targets().isEmpty());
        locks.release(field);
        locks.clear();
    }

    @Test void circularFieldUsesHorizontalRadiusAndBoundedHeightInTheSameWorld() {
        var center = point(0, 10, 0);
        assertTrue(LegendaryParalysis.inside(center, point(7, 6, 0), 7));
        assertTrue(LegendaryParalysis.inside(center, point(0, 14, 7), 7));
        assertFalse(LegendaryParalysis.inside(center, point(7.01, 10, 0), 7));
        assertFalse(LegendaryParalysis.inside(center, point(0, 14.01, 0), 7));
        assertFalse(LegendaryParalysis.inside(center, new LegendaryParalysis.Point(UUID.randomUUID(), 0, 10, 0), 7));
    }

    private LegendaryParalysis.Point point(double x, double y, double z) {
        return new LegendaryParalysis.Point(world, x, y, z);
    }
}
