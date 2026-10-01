package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;

import java.util.Set;

public final class PowerConditionTargetReason extends AbstractPowerCondition {

    public PowerConditionTargetReason() {
        super("target_reason");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null || condition.getContext().triggerData() == null) {
            return false;
        }
        Set<String> reasons = CommonUtil.values(condition.getSection(), "reasons", "reason");
        return !reasons.isEmpty()
                && condition.getContext().triggerData().extra(BuiltinContextKeys.TARGET_REASON)
                .map(reason -> reasons.contains(reason.name())).orElse(false);
    }
}
