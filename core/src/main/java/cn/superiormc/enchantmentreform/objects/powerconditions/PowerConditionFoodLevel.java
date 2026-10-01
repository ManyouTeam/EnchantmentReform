package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.entity.Player;

public final class PowerConditionFoodLevel extends AbstractNumericPowerCondition {

    public PowerConditionFoodLevel() {
        super("food_level");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Player player = context.player(
                condition.getSection().getString("target"), EntitySelector.PLAYER);
        return player == null ? null : (double) player.getFoodLevel();
    }
}
