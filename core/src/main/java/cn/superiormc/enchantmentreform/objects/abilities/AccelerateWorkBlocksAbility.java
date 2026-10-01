package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.HookManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;

public final class AccelerateWorkBlocksAbility extends AbstractAbility {

    private static final Set<Material> FURNACES = EnumSet.of(
            Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE);

    public AccelerateWorkBlocksAbility(ConfigurationSection section) {
        super("AccelerateWorkBlocks", section);
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
        int extraTicks = Math.max(0, getInt("extra-ticks", 1, context));
        if (maximumBlocks == 0 || extraTicks == 0) {
            return false;
        }
        boolean loadChunks = section.getBoolean("load-chunks", false);
        boolean checkProtection = section.getBoolean("check-protection", true);
        boolean requireActive = section.getBoolean("require-active", true);
        Player player = context.player();

        int changedBlocks = 0;
        for (Block block : NearbyBlockScanner.scan(center, radiusX, radiusY, radiusZ,
                maximumBlocks < 0 ? -1 : maximumBlocks, loadChunks,
                block -> isEnabled(block.getType()))) {
            if (!isEnabled(block.getType()) || !canUse(player, block, checkProtection)) {
                continue;
            }
            BlockState state = block.getState();
            boolean changed = state instanceof Furnace furnace
                    ? accelerateFurnace(furnace, extraTicks, requireActive)
                    : state instanceof BrewingStand brewingStand
                    && accelerateBrewingStand(brewingStand, extraTicks, requireActive);
            if (changed && state.update(true, false)) {
                changedBlocks++;
            }
        }
        if (context.result() != null) {
            context.result().recordChangedBlocks(changedBlocks);
        }
        return false;
    }

    private boolean isEnabled(Material material) {
        if (material == Material.BREWING_STAND) {
            return section.getBoolean("brewing-stand", true);
        }
        if (!FURNACES.contains(material)) {
            return false;
        }
        return switch (material) {
            case FURNACE -> section.getBoolean("furnace", true);
            case SMOKER -> section.getBoolean("smoker", true);
            case BLAST_FURNACE -> section.getBoolean("blast-furnace", true);
            default -> false;
        };
    }

    private boolean accelerateFurnace(Furnace furnace, int extraTicks, boolean requireActive) {
        int cookTime = furnace.getCookTime();
        int totalCookTime = furnace.getCookTimeTotal();
        if (totalCookTime <= 0 || requireActive && (furnace.getBurnTime() <= 0 || cookTime <= 0)) {
            return false;
        }
        int accelerated = Math.min(totalCookTime - 1, cookTime + extraTicks);
        if (accelerated <= cookTime) {
            return false;
        }
        furnace.setCookTime((short) accelerated);
        return true;
    }

    private boolean accelerateBrewingStand(BrewingStand brewingStand, int extraTicks,
                                            boolean requireActive) {
        int brewingTime = brewingStand.getBrewingTime();
        if (brewingTime <= 0 || requireActive && brewingStand.getFuelLevel() <= 0) {
            return false;
        }
        int accelerated = Math.max(1, brewingTime - extraTicks);
        if (accelerated >= brewingTime) {
            return false;
        }
        brewingStand.setBrewingTime(accelerated);
        return true;
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
