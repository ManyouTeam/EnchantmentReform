package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.entity.EntityExhaustionEvent;

import java.util.Set;

public final class PowerConditionExhaustionReason extends AbstractPowerCondition {

    public PowerConditionExhaustionReason() {
        super("exhaustion_reason");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> reasons = CommonUtil.values(
                condition.getSection(), "reasons", "reason");
        return !reasons.isEmpty() && condition.getContext() != null
                && condition.getContext().event() instanceof EntityExhaustionEvent event
                && reasons.contains(event.getExhaustionReason().name());
    }
}
