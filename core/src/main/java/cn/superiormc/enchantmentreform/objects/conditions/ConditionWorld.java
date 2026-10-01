package cn.superiormc.enchantmentreform.objects.conditions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ConditionWorld<S extends AbstractConfiguredSection<C>, C> extends AbstractCheckCondition<S, C> {

    public ConditionWorld() {
        super("world");
        setRequiredArgs("world");
    }

    @Override
    protected boolean onCheckCondition(S singleCondition, Player player, C context) {
        return player.getWorld().getName().equals(singleCondition.getString("world", player, context));
    }
}
