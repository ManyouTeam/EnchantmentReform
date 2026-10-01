package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Entity;

public final class PowerConditionRiding extends AbstractPowerCondition {

    public PowerConditionRiding() {
        super("riding");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        boolean expected = condition.getSection().getBoolean("value", true);
        return entity != null && (entity.getVehicle() != null) == expected;
    }
}
