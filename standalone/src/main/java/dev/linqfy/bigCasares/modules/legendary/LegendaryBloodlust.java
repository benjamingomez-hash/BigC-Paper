package dev.linqfy.bigCasares.modules.legendary;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/** Healing from one primary melee hit, bounded by the active skill and real health capacity. */
final class LegendaryBloodlust {
    private LegendaryBloodlust() {}

    static double healing(int age, DamageCause cause, double damage, double health, double maximum) {
        if (age < 0 || age >= 200 || cause != DamageCause.ENTITY_ATTACK || !Double.isFinite(damage) || damage <= 0
            || !Double.isFinite(health) || !Double.isFinite(maximum) || health <= 0 || health >= maximum) return 0;
        return Math.min(1, maximum - health);
    }
}
