package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionOnGround extends AbstractPowerCondition {

    public PowerConditionOnGround() {
        super("on_ground");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        LivingEntity entity = context == null ? null : context.livingEntity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return entity != null && entity.isOnGround() == condition.getSection().getBoolean("value", true);
    }
}
