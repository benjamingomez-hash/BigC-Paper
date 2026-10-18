package dev.linqfy.bigCasares.modules.legendary;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/** A single native damage attempt, scoped to Earthbreaker's initial pulse. */
final class LegendaryFixedDamage implements Listener {
    private Attempt active;

    private static final class Attempt {
        final Player source;
        final LivingEntity target;
        boolean accepted;

        Attempt(Player source, LivingEntity target) { this.source = source; this.target = target; }
    }

    void hit(Player source, LivingEntity target) {
        if (target.isDead() || !target.isValid() || target.isInvulnerable()) return;
        Attempt previous = active;
        Attempt attempt = new Attempt(source, target);
        int immunity = target.getNoDamageTicks();
        active = attempt;
        try {
            // One pulse must not disappear merely because another attack just landed.
            target.setNoDamageTicks(0);
            target.damage(4, DamageSource.builder(DamageType.SONIC_BOOM)
                .withCausingEntity(source).withDirectEntity(source).build());
        } finally {
            if (!attempt.accepted && target.isValid() && !target.isDead()) target.setNoDamageTicks(immunity);
            active = previous;
        }
    }

    private boolean matches(EntityDamageByEntityEvent event) {
        return active != null && event.getEntity().equals(active.target) && event.getDamager().equals(active.source)
            && event.getDamageSource().getDamageType().equals(DamageType.SONIC_BOOM);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fixedDamage(EntityDamageByEntityEvent event) {
        if (matches(event)) normalize(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void observed(EntityDamageByEntityEvent event) {
        if (matches(event)) active.accepted = !event.isCancelled() && event.getFinalDamage() > 0;
    }

    static void normalize(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, 4);
        for (var modifier : EntityDamageEvent.DamageModifier.values()) {
            if (modifier != EntityDamageEvent.DamageModifier.BASE && event.isApplicable(modifier)) event.setDamage(modifier, 0);
        }
    }
}
