package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.entity.Entity;

public final class PowerConditionFallDistance extends AbstractNumericPowerCondition {

    public PowerConditionFallDistance() {
        super("fall_distance");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Entity entity = context.entity(EntitySelector.parse(
                condition.getSection().getString("target"), EntitySelector.TARGET));
        return entity == null ? null : (double) entity.getFallDistance();
    }
}
