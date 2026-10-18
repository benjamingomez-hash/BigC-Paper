package dev.linqfy.bigCasares.modules.legendary;

import com.google.common.base.Function;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegendaryFixedDamageTest {
    private EntityDamageEvent event(boolean reductions) {
        var amounts = new EnumMap<EntityDamageEvent.DamageModifier, Double>(EntityDamageEvent.DamageModifier.class);
        var functions = new EnumMap<EntityDamageEvent.DamageModifier, Function<Double, Double>>(EntityDamageEvent.DamageModifier.class);
        amounts.put(EntityDamageEvent.DamageModifier.BASE, 20.0);
        functions.put(EntityDamageEvent.DamageModifier.BASE, value -> 0.0);
        if (reductions) for (var modifier : EntityDamageEvent.DamageModifier.values()) {
            if (modifier == EntityDamageEvent.DamageModifier.BASE) continue;
            amounts.put(modifier, -2.0);
            functions.put(modifier, value -> -2.0);
        }
        var source = (DamageSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{DamageSource.class},
            (proxy, method, args) -> { throw new UnsupportedOperationException(method.getName()); });
        return new EntityDamageEvent(null, EntityDamageEvent.DamageCause.SONIC_BOOM, source, amounts, functions);
    }

    @Test
    void armorEnchantmentsResistanceBlockingAndAbsorptionCannotReduceFourHealthPoints() {
        var event = event(true);
        LegendaryFixedDamage.normalize(event);
        assertEquals(4, event.getFinalDamage());
        for (var modifier : EntityDamageEvent.DamageModifier.values()) {
            assertEquals(modifier == EntityDamageEvent.DamageModifier.BASE ? 4 : 0, event.getDamage(modifier));
        }
        assertFalse(event.isCancelled());
    }

    @Test
    void targetsWithoutArmorOrOptionalModifiersStillTakeExactlyFourPoints() {
        var event = event(false);
        LegendaryFixedDamage.normalize(event);
        assertEquals(4, event.getFinalDamage());
        assertFalse(event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR));
    }

    @Test
    void cancelledProtectionIsNeverReenabledOrAltered() {
        var event = event(true);
        event.setCancelled(true);
        double previous = event.getFinalDamage();
        LegendaryFixedDamage.normalize(event);
        assertTrue(event.isCancelled());
        assertEquals(previous, event.getFinalDamage());
    }
}
