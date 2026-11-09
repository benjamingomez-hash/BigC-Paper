package dev.linqfy.bigCasares.modules.schizophrenia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpisodeMathTest {

    private static final long MINUTE = 60_000L;

    @Test
    void intensityEscalatesAndCapsAtFive() {
        assertEquals(1, EpisodeMath.intensity(1, 0, 3.0));
        assertEquals(1, EpisodeMath.intensity(1, 2 * MINUTE, 3.0));
        assertEquals(2, EpisodeMath.intensity(1, 3 * MINUTE, 3.0));
        assertEquals(4, EpisodeMath.intensity(2, 6 * MINUTE, 3.0));
        assertEquals(5, EpisodeMath.intensity(1, 500 * MINUTE, 3.0));
        assertEquals(5, EpisodeMath.intensity(9, 0, 3.0));
        assertEquals(1, EpisodeMath.intensity(-3, -10, 3.0));
    }

    @Test
    void hallucinationsComeFasterAsItGetsWorse() {
        double calm = EpisodeMath.intervalSeconds(1, 14, 3, 0.5);
        double breakdown = EpisodeMath.intervalSeconds(5, 14, 3, 0.5);
        assertEquals(14.0, calm, 1.0E-9);
        assertEquals(3.0, breakdown, 1.0E-9);
        assertTrue(EpisodeMath.intervalSeconds(3, 14, 3, 0.0) < EpisodeMath.intervalSeconds(3, 14, 3, 1.0));
        assertTrue(EpisodeMath.intervalSeconds(5, 1, 1, 0.0) >= 1.0);
    }

    @Test
    void zeroMaxMinutesMeansOnlyTheLobotomyEndsIt() {
        assertFalse(EpisodeMath.expired(10_000 * MINUTE, 0));
        assertFalse(EpisodeMath.expired(24 * MINUTE, 25));
        assertTrue(EpisodeMath.expired(25 * MINUTE, 25));
    }

    @Test
    void naturalOnsetNeedsSolitudeAndDarknessOrDepth() {
        SchizophreniaSettings.Natural natural = SchizophreniaSettings.Natural.defaults();
        assertTrue(EpisodeMath.naturalOnsetConditions(true, 0, 64, natural));
        assertTrue(EpisodeMath.naturalOnsetConditions(true, 15, -30, natural));
        assertFalse(EpisodeMath.naturalOnsetConditions(true, 15, 64, natural));
        assertFalse(EpisodeMath.naturalOnsetConditions(false, 0, -30, natural));
    }
}
