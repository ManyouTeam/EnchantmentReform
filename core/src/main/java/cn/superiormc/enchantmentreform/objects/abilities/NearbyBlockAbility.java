package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class NearbyBlockAbility extends AbstractAbility {


    public NearbyBlockAbility(ConfigurationSection section) {
        super("NearbyBlock", section);
    }

    @Override
    public boolean breakOtherBlock() {
        return true;
    }

    @Override
    public boolean execute(PowerContext context) {
        if (context.result() != null) {
            context.result().recordChangedBlocks(0);
        }
        ConfigurationSection abilities = section.getConfigurationSection("abilities");
        Location baseLocation = getLocation(context);
        if (abilities == null || baseLocation == null || baseLocation.getWorld() == null) {
            return false;
        }

        int radius = Math.max(0, getInt("radius", 1, context));
        int radiusX = Math.max(0, getInt("radius-x", radius, context));
        int radiusY = Math.max(0, getInt("radius-y", radius, context));
        int radiusZ = Math.max(0, getInt("radius-z", radius, context));
        int maximumBlocks = Math.max(0, getInt("max-blocks", 0, context));

        int changedBlocks = 0;
        for (Block block : nearbyBlocks(baseLocation, radiusX, radiusY, radiusZ)) {
            BlockSnapshot before = snapshot(block);
            AbilityManager.abilityManager.execute(abilities, context.withBlock(block));
            if (!before.equals(snapshot(block))) {
                changedBlocks++;
            }
            if (maximumBlocks > 0 && --maximumBlocks == 0) {
                break;
            }
        }
        if (context.result() != null) {
            context.result().recordChangedBlocks(changedBlocks);
        }
        return false;
    }

    private BlockSnapshot snapshot(Block block) {
        HookManager hooks = HookManager.hookManager;
        String blockId = hooks == null ? null : hooks.getBlockId(block);
        if (blockId == null) {
            blockId = block.getType().getKey().toString();
        }
        return new BlockSnapshot(blockId, block.getBlockData().getAsString());
    }

    private List<Block> nearbyBlocks(Location baseLocation, int radiusX, int radiusY, int radiusZ) {
        Block center = baseLocation.getBlock();
        List<Block> blocks = new ArrayList<>();
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    blocks.add(center.getRelative(x, y, z));
                }
            }
        }
        blocks.sort(Comparator.comparingDouble(block -> block.getLocation().add(
                0.5D, 0.5D, 0.5D).distanceSquared(baseLocation)));
        return blocks;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private record BlockSnapshot(String blockId, String blockData) {

        private BlockSnapshot {
            Objects.requireNonNull(blockId, "blockId");
            Objects.requireNonNull(blockData, "blockData");
        }
    }
}
