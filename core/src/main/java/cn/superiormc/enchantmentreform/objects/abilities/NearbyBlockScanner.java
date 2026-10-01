package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

final class NearbyBlockScanner {

    private static final int MAX_RADIUS = 16;

    private NearbyBlockScanner() {
    }

    static List<Block> scan(Location center, int radiusX, int radiusY, int radiusZ,
                            int maximumBlocks, boolean loadChunks, Predicate<Block> filter) {
        if (center == null || center.getWorld() == null || maximumBlocks == 0) {
            return List.of();
        }
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        int limitedX = Math.min(MAX_RADIUS, Math.max(0, radiusX));
        int limitedY = Math.min(MAX_RADIUS, Math.max(0, radiusY));
        int limitedZ = Math.min(MAX_RADIUS, Math.max(0, radiusZ));

        List<BlockOffset> offsets = new ArrayList<>();
        for (int x = -limitedX; x <= limitedX; x++) {
            for (int y = -limitedY; y <= limitedY; y++) {
                for (int z = -limitedZ; z <= limitedZ; z++) {
                    offsets.add(new BlockOffset(x, y, z, x * x + y * y + z * z));
                }
            }
        }
        offsets.sort(Comparator.comparingInt(BlockOffset::distanceSquared));

        int limit = maximumBlocks < 0 ? offsets.size() : Math.min(maximumBlocks, offsets.size());
        List<Block> blocks = new ArrayList<>(limit);
        for (BlockOffset offset : offsets) {
            int x = centerX + offset.x();
            int y = centerY + offset.y();
            int z = centerZ + offset.z();
            if (y < world.getMinHeight() || y >= world.getMaxHeight()
                    || !loadChunks && !world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            Block block = world.getBlockAt(x, y, z);
            if (!filter.test(block)) {
                continue;
            }
            blocks.add(block);
            if (blocks.size() >= limit) {
                break;
            }
        }
        return blocks;
    }

    private record BlockOffset(int x, int y, int z, int distanceSquared) {
    }
}
