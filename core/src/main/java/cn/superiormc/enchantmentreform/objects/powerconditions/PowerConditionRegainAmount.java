package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public final class PowerConditionRegainAmount extends AbstractNumericPowerCondition {

    public PowerConditionRegainAmount() {
        super("regain_amount");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        return context.event() instanceof EntityRegainHealthEvent
                ? context.result().regainAmount(context.triggerData()) : null;
    }
}
