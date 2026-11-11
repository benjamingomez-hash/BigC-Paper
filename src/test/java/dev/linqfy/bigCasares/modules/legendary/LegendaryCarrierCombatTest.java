package dev.linqfy.bigCasares.modules.legendary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegendaryCarrierCombatTest {
    @Test void smashKeepsTheNativeFallThresholdAndExcludesGlidingAndOtherWeapons() {
        assertFalse(LegendaryCarrierCombat.smashEligible(LegendaryWeapon.WINDWEAVER, 1.5, false));
        assertTrue(LegendaryCarrierCombat.smashEligible(LegendaryWeapon.WINDWEAVER, 1.5001, false));
        assertFalse(LegendaryCarrierCombat.smashEligible(LegendaryWeapon.WINDWEAVER, 20, true));
        assertFalse(LegendaryCarrierCombat.smashEligible(LegendaryWeapon.THUNDER_SPEAR, 20, false));
    }

    @Test void onlyFullyChargedGroundedSwordAttacksSweep() {
        assertTrue(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.BLOODLUST, 1, false, false, true));
        assertTrue(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.SHADOW_BLADE, 1, false, false, true));
        assertFalse(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.BLOODLUST, .9F, false, false, true));
        assertFalse(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.BLOODLUST, 1, true, false, true));
        assertFalse(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.BLOODLUST, 1, false, true, true));
        assertFalse(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.BLOODLUST, 1, false, false, false));
        assertFalse(LegendaryCarrierCombat.sweepEligible(LegendaryWeapon.EARTHBREAKER, 1, false, false, true));
    }
}
