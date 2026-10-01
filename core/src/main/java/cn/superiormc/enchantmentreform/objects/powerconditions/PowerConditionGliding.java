package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionGliding extends AbstractPowerCondition {

    public PowerConditionGliding() {
        super("gliding");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        LivingEntity living = context == null ? null : context.livingEntity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return living != null && living.isGliding() == condition.getSection().getBoolean("value", true);
    }
}
