package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Set;

public final class PowerConditionDamageCause extends AbstractPowerCondition {

    public PowerConditionDamageCause() {
        super("damage_cause");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> causes = CommonUtil.values(condition.getSection(), "causes", "cause");
        return !causes.isEmpty() && condition.getContext() != null
                && condition.getContext().event() instanceof EntityDamageEvent event
                && causes.contains(event.getCause().name());
    }
}
