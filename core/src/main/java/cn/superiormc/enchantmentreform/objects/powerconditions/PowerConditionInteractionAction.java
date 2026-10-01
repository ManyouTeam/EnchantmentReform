package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

public final class PowerConditionInteractionAction extends AbstractPowerCondition {

    public PowerConditionInteractionAction() {
        super("interaction_action");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> actions = CommonUtil.values(condition.getSection(), "actions", "action");
        return !actions.isEmpty() && condition.getContext() != null
                && condition.getContext().event() instanceof PlayerInteractEvent event
                && actions.contains(event.getAction().name());
    }
}
