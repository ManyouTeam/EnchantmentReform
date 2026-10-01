package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;

public final class PowerConditionTargetCount extends AbstractNumericPowerCondition {

    public PowerConditionTargetCount() {
        super("target_count");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        return context.triggerData().extra(BuiltinContextKeys.TARGET_COUNT)
                .map(Integer::doubleValue)
                .orElse(null);
    }
}