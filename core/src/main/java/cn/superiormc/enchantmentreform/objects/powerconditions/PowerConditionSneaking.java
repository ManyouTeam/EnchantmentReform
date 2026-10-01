package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Player;

public final class PowerConditionSneaking extends AbstractPowerCondition {

    public PowerConditionSneaking() {
        super("sneaking");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Player player = context == null ? null : context.player(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return player != null && player.isSneaking() == condition.getSection().getBoolean("value", true);
    }
}
