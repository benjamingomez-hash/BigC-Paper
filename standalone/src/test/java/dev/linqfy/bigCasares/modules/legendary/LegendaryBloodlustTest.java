package dev.linqfy.bigCasares.modules.legendary;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegendaryBloodlustTest {
    @Test
    void eachSuccessfulPrimaryHitRestoresHalfAHeartRegardlessOfDamageAmount() {
        assertEquals(1, LegendaryBloodlust.healing(0, DamageCause.ENTITY_ATTACK, .1, 10, 20));
        assertEquals(1, LegendaryBloodlust.healing(100, DamageCause.ENTITY_ATTACK, 35, 10, 20));
        assertEquals(1, LegendaryBloodlust.healing(199, DamageCause.ENTITY_ATTACK, 4, 10, 20));
    }

    @Test
    void healingEndsExactlyAtTwoHundredTicksAndCannotStartBeforeActivation() {
        assertEquals(0, LegendaryBloodlust.healing(-1, DamageCause.ENTITY_ATTACK, 4, 10, 20));
        assertEquals(0, LegendaryBloodlust.healing(200, DamageCause.ENTITY_ATTACK, 4, 10, 20));
        assertEquals(0, LegendaryBloodlust.healing(220, DamageCause.ENTITY_ATTACK, 4, 10, 20));
    }

    @Test
    void sweepingTargetsProjectilesAndSpellsCannotMultiplyHealingFromOneSwing() {
        for (DamageCause cause : new DamageCause[]{DamageCause.ENTITY_SWEEP_ATTACK, DamageCause.PROJECTILE,
            DamageCause.MAGIC, DamageCause.SONIC_BOOM, DamageCause.ENTITY_EXPLOSION}) {
            assertEquals(0, LegendaryBloodlust.healing(20, cause, 4, 10, 20));
        }
    }

    @Test
    void blockedOrInvalidDamageAndDeadPlayersNeverHeal() {
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, LegendaryBloodlust.healing(20, DamageCause.ENTITY_ATTACK, damage, 10, 20));
        }
        assertEquals(0, LegendaryBloodlust.healing(20, DamageCause.ENTITY_ATTACK, 4, 0, 20));
    }

    @Test
    void healingStopsAtTheActualMaximumIncludingNonstandardHealthLimits() {
        assertEquals(.25, LegendaryBloodlust.healing(20, DamageCause.ENTITY_ATTACK, 4, 19.75, 20));
        assertEquals(0, LegendaryBloodlust.healing(20, DamageCause.ENTITY_ATTACK, 4, 20, 20));
        assertEquals(.5, LegendaryBloodlust.healing(20, DamageCause.ENTITY_ATTACK, 4, 39.5, 40));
    }
}
