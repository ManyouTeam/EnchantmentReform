package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class PowerConditionFoodChange extends AbstractNumericPowerCondition {

    public PowerConditionFoodChange() {
        super("food_change");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        if (!(context.event() instanceof FoodLevelChangeEvent event)
                || !(event.getEntity() instanceof Player player)) {
            return null;
        }
        return (double) (context.result().foodLevel(context.triggerData()) - player.getFoodLevel());
    }
}