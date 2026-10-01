package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Set;

public final class PowerConditionPotionEffectAction extends AbstractPowerCondition {

    public PowerConditionPotionEffectAction() {
        super("potion_effect_action");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> actions = CommonUtil.values(
                condition.getSection(), "actions", "action");
        return !actions.isEmpty() && condition.getContext() != null
                && condition.getContext().event() instanceof EntityPotionEffectEvent event
                && actions.contains(event.getAction().name());
    }
}
