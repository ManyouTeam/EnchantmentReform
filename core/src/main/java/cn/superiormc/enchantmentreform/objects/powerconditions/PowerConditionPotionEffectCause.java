package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Set;

public final class PowerConditionPotionEffectCause extends AbstractPowerCondition {

    public PowerConditionPotionEffectCause() {
        super("potion_effect_cause");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> causes = CommonUtil.values(
                condition.getSection(), "causes", "cause");
        return !causes.isEmpty() && condition.getContext() != null
                && condition.getContext().event() instanceof EntityPotionEffectEvent event
                && causes.contains(event.getCause().name());
    }
}
