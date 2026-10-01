package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.Player;

import java.util.Set;

public final class PowerConditionGameMode extends AbstractPowerCondition {

    public PowerConditionGameMode() {
        super("game_mode");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Player player = context == null ? null : context.player(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        Set<String> modes = CommonUtil.values(condition.getSection(), "modes", "mode");
        return player != null && modes.contains(player.getGameMode().name());
    }
}
