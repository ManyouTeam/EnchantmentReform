package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;

/** Reads a numeric value from the configured power state pool. */
public final class PowerConditionStateValue extends AbstractNumericPowerCondition {

    public PowerConditionStateValue() {
        super("state_value");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        PowerStateStore.Key key = PowerStateStore.key(context,
                condition.getString("key", "default"),
                condition.getSection().getBoolean("per-target", false),
                EntitySelector.parse(condition.getString("owner"), EntitySelector.SOURCE),
                EntitySelector.parse(condition.getString("state-target"), EntitySelector.TARGET));
        return PowerStateStore.get(key);
    }
}
