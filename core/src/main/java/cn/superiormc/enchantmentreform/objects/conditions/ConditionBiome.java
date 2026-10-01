package cn.superiormc.enchantmentreform.objects.conditions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ConditionBiome<S extends AbstractConfiguredSection<C>, C> extends AbstractCheckCondition<S, C> {

    public ConditionBiome() {
        super("biome");
        setRequiredArgs("biome");
    }

    @Override
    protected boolean onCheckCondition(S singleCondition, Player player, C context) {
        return player.getLocation().getBlock().getBiome().name().equals(singleCondition.getString("biome").toUpperCase());
    }
}
