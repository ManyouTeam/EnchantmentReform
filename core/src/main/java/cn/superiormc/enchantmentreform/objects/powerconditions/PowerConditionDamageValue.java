package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.event.entity.EntityDamageEvent;

public final class PowerConditionDamageValue extends AbstractNumericPowerCondition {

    public PowerConditionDamageValue() {
        super("damage_value");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        return context.event() instanceof EntityDamageEvent
                ? context.result().damage(context.triggerData()) : null;
    }
}
