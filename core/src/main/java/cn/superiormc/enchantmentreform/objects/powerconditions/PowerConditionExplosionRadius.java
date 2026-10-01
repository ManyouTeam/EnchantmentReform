package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public final class PowerConditionExplosionRadius extends AbstractNumericPowerCondition {

    public PowerConditionExplosionRadius() {
        super("explosion_radius");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        return context.event() instanceof ExplosionPrimeEvent
                ? (double) context.result().explosionRadius(context.triggerData()) : null;
    }
}
