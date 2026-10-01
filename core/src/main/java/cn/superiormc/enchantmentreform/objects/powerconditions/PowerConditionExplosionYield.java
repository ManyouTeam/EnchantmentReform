package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class PowerConditionExplosionYield extends AbstractNumericPowerCondition {

    public PowerConditionExplosionYield() {
        super("explosion_yield");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        return context.event() instanceof EntityExplodeEvent
                ? (double) context.result().explosionYield(context.triggerData()) : null;
    }
}
