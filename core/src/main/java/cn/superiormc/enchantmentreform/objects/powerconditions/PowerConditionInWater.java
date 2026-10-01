package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionInWater extends AbstractPowerCondition {

    public PowerConditionInWater() {
        super("in_water");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return entity != null && entity.isInWater() == condition.getSection().getBoolean("value", true);
    }
}
