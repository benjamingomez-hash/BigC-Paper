package dev.linqfy.bigCasares.modules.legendary;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LegendaryHyperionSalvoTest {
    @Test void oneActivationFiresExactlyFiveShotsWithTimeToAimBetweenThem() {
        var shots = new ArrayList<Integer>();
        for (int age = 0; age <= LegendaryHyperionSalvo.MAX_AGE; age++) {
            if (LegendaryHyperionSalvo.firesAt(age)) shots.add(age);
        }
        assertEquals(List.of(1, 6, 11, 16, 21), shots);
    }

    @Test void expiredOrInvalidSessionsNeverEmitExtraProjectiles() {
        for (int age : List.of(-10, -4, -1, 0, 22, 26, 101, 121, 1001)) {
            assertFalse(LegendaryHyperionSalvo.firesAt(age));
        }
    }
}
