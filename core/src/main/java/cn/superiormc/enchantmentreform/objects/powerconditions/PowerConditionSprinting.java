package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Player;

public final class PowerConditionSprinting extends AbstractPowerCondition {

    public PowerConditionSprinting() {
        super("sprinting");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Player player = context == null ? null : context.player(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return player != null && player.isSprinting() == condition.getSection().getBoolean("value", true);
    }
}
