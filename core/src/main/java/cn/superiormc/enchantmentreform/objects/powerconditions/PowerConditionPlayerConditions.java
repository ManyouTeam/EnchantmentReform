package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Player;

public final class PowerConditionPlayerConditions extends AbstractPowerCondition {

    public PowerConditionPlayerConditions() {
        super("player_conditions");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Player player = resolvePlayer(condition);
        return player != null && PowerConditionsManager.powerConditions.getPlayerConditions().matches(
                condition.getSection().getConfigurationSection("conditions"), player, condition.getContext());
    }

    private Player resolvePlayer(ObjectSingleCondition condition) {
        if (condition.getContext() == null) {
            return null;
        }
        return condition.getContext().player(
                condition.getSection().getString("target"), EntitySelector.SOURCE);
    }
}
