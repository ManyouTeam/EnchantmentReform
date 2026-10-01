package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.event.entity.EntityCombustEvent;

public final class PowerConditionCombustDuration extends AbstractNumericPowerCondition {

    public PowerConditionCombustDuration() {
        super("combust_duration");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        return context.event() instanceof EntityCombustEvent
                ? (double) context.result().combustDuration(context.triggerData()) : null;
    }
}
