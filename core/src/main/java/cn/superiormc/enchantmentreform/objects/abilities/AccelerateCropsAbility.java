package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.HookManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class AccelerateCropsAbility extends AbstractAbility {

    private static final Set<Material> CROPS = EnumSet.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.NETHER_WART, Material.COCOA,
            Material.SWEET_BERRY_BUSH, Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP);

    public AccelerateCropsAbility(ConfigurationSection section) {
        super("AccelerateCrops", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location center = getLocation(context);
        if (center == null || center.getWorld() == null) {
            return false;
        }
        int radius = Math.max(0, getInt("radius", 1, context));
        int radiusX = Math.max(0, getInt("radius-x", radius, context));
        int radiusY = Math.max(0, getInt("radius-y", radius, context));
        int radiusZ = Math.max(0, getInt("radius-z", radius, context));
        int maximumBlocks = getInt("max-blocks", 128, context);
        if (maximumBlocks == 0) {
            return false;
        }
        int stages = Math.max(1, getInt("stages", 1, context));
        double chance = Math.max(0.0D, Math.min(100.0D, getDouble("chance", 100.0D, context)));
        boolean loadChunks = section.getBoolean("load-chunks", false);
        boolean checkProtection = section.getBoolean("check-protection", true);
        Player player = context.player();

        int changedBlocks = 0;
        for (Block block : NearbyBlockScanner.scan(center, radiusX, radiusY, radiusZ,
                maximumBlocks < 0 ? -1 : maximumBlocks, loadChunks,
                block -> CROPS.contains(block.getType()))) {
            if (!(block.getBlockData() instanceof Ageable ageable)
                    || ageable.getAge() >= ageable.getMaximumAge()
                    || chance < 100.0D && ThreadLocalRandom.current().nextDouble(100.0D) >= chance
                    || !canUse(player, block, checkProtection)) {
                continue;
            }
            ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + stages));
            block.setBlockData(ageable);
            changedBlocks++;
        }
        if (context.result() != null) {
            context.result().recordChangedBlocks(changedBlocks);
        }
        return false;
    }

    private boolean canUse(Player player, Block block, boolean checkProtection) {
        return !checkProtection || player == null || HookManager.hookManager == null
                || HookManager.hookManager.getProtectionCanUse(player, block.getLocation());
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
