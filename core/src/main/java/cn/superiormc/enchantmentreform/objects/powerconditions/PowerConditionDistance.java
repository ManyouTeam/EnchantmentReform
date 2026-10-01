package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.entity.Entity;

public final class PowerConditionDistance extends AbstractNumericPowerCondition {

    public PowerConditionDistance() {
        super("distance");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Entity source = context.entity(EntitySelector.parse(
                condition.getSection().getString("source"), EntitySelector.SOURCE));
        Entity target = context.entity(EntitySelector.parse(
                condition.getSection().getString("target"), EntitySelector.TARGET));
        if (source == null || target == null || !source.getWorld().equals(target.getWorld())) {
            return null;
        }
        return source.getLocation().distance(target.getLocation());
    }
}
