package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;

import java.util.Locale;
import java.util.Set;

public final class PowerConditionEnvironment extends AbstractPowerCondition {

    public PowerConditionEnvironment() {
        super("environment");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null) {
            return false;
        }
        Entity entity = condition.getContext().entity(EntitySelector.parse(
                condition.getSection().getString("target"), EntitySelector.SOURCE));
        if (entity == null) {
            return false;
        }
        String dimension = condition.getSection().getString("dimension", "").trim();
        if (!dimension.isEmpty()
                && !entity.getWorld().getEnvironment().name().equalsIgnoreCase(dimension)) {
            return false;
        }
        Set<String> biomes = CommonUtil.normalized(
                condition.getSection().getStringList("biomes"));
        if (biomes.isEmpty()) {
            return !dimension.isEmpty();
        }
        Biome biome = entity.getLocation().getBlock().getBiome();
        return biomes.contains(biome.name())
                || biomes.contains(biome.getKey().toString().toUpperCase(Locale.ROOT));
    }
}
