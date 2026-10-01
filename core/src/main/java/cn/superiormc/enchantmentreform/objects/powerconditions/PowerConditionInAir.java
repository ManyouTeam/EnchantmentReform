package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Entity;

public final class PowerConditionInAir extends AbstractPowerCondition {

    public PowerConditionInAir() {
        super("in_air");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        boolean expected = condition.getSection().getBoolean("value", true);
        boolean actual = entity != null && !entity.isOnGround();
        return entity != null && actual == expected;
    }
}
