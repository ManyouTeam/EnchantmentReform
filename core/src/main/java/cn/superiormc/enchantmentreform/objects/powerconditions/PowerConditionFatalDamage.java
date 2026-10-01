package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public final class PowerConditionFatalDamage extends AbstractPowerCondition {

    public PowerConditionFatalDamage() {
        super("fatal_damage");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        return condition.getContext() != null && condition.getContext().source() instanceof LivingEntity living
                && condition.getContext().event() instanceof EntityDamageEvent event
                && event.getFinalDamage() >= living.getHealth();
    }
}
