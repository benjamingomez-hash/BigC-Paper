package dev.linqfy.bigCasares.modules.schizophrenia;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HallucinationPickerTest {

    @Test
    void nastyHallucinationsUnlockWithIntensity() {
        HallucinationPicker picker = new HallucinationPicker(Set.of());
        assertTrue(picker.eligible(1).contains(Hallucination.FOOTSTEPS));
        assertTrue(picker.eligible(1).contains(Hallucination.CREEPER_HISS));
        assertTrue(picker.eligible(1).contains(Hallucination.PHANTOM_CLONE));
        assertFalse(picker.eligible(1).contains(Hallucination.FAKE_DEATH));
        assertFalse(picker.eligible(4).contains(Hallucination.SWARM));
        assertEquals(Hallucination.values().length, picker.eligible(5).size());
    }

    @Test
    void neverRepeatsBackToBackAndRespectsDisabledOnes() {
        HallucinationPicker picker = new HallucinationPicker(Set.of(Hallucination.WHISPERS));
        SplittableRandom random = new SplittableRandom(7);
        Hallucination previous = null;
        EnumSet<Hallucination> seen = EnumSet.noneOf(Hallucination.class);
        for (int i = 0; i < 2000; i++) {
            Hallucination next = picker.pick(5, previous, random).orElseThrow();
            assertNotEquals(previous, next);
            assertNotEquals(Hallucination.WHISPERS, next);
            seen.add(next);
            previous = next;
        }
        assertEquals(Hallucination.values().length - 1, seen.size());
    }

    @Test
    void everythingDisabledPicksNothing() {
        HallucinationPicker picker = new HallucinationPicker(EnumSet.allOf(Hallucination.class));
        assertTrue(picker.pick(5, null, new SplittableRandom(1)).isEmpty());
    }

    @Test
    void configIdsRoundTrip() {
        for (Hallucination hallucination : Hallucination.values()) {
            assertEquals(hallucination, Hallucination.parse(hallucination.id()).orElseThrow());
        }
        assertTrue(Hallucination.parse(" ").isEmpty());
    }
}
