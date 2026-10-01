package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;

public final class GrowCropAbility extends AbstractAbility {

    public GrowCropAbility(ConfigurationSection section) {
        super("GrowCrop", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Block block = context.block();
        if (block == null || !(block.getBlockData() instanceof Ageable ageable)) {
            return false;
        }
        int stages = Math.max(1, getInt("stages", 1, context));
        ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + stages));
        block.setBlockData(ageable);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
