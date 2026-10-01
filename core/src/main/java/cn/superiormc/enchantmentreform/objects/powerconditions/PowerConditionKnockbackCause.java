package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import io.papermc.paper.event.entity.EntityKnockbackEvent;

import java.util.HashSet;
import java.util.Set;

public final class PowerConditionKnockbackCause extends AbstractPowerCondition {

    public PowerConditionKnockbackCause() {
        this("knockback_cause");
    }

    public PowerConditionKnockbackCause(String type) {
        super(type);
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null
                || !(condition.getContext().event() instanceof EntityKnockbackEvent event)) {
            return false;
        }
        Set<String> causes = new HashSet<>(
                CommonUtil.values(condition.getSection(), "causes", "cause"));
        causes.addAll(CommonUtil.values(condition.getSection(), "reasons", "reason"));
        return !causes.isEmpty() && causes.contains(event.getCause().name());
    }
}
