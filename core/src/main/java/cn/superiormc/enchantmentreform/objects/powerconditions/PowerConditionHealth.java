package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;

public final class PowerConditionHealth extends AbstractNumericPowerCondition {

    public PowerConditionHealth() {
        super("health");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        LivingEntity entity = context.livingEntity(
                condition.getSection().getString("target"), EntitySelector.PLAYER);
        if (entity == null) {
            return null;
        }
        String mode = condition.getSection().getString("mode", "CURRENT").toUpperCase(Locale.ROOT);
        return mode.equals("MAX") || mode.equals("MAXIMUM")
                ? CommonUtil.getMaxHealth(entity) : entity.getHealth();
    }
}
