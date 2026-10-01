package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Set;

public final class PowerConditionEventState extends AbstractPowerCondition {

    public PowerConditionEventState() {
        super("event_state");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> states = CommonUtil.values(condition.getSection(), "states", "state");
        return !states.isEmpty() && condition.getContext() != null
                && condition.getContext().event() instanceof PlayerFishEvent event
                && states.contains(event.getState().name());
    }
}
