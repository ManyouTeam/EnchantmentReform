package cn.superiormc.enchantmentreform.power;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockBreakProtectionUtil {

    private BlockBreakProtectionUtil() {
    }

    public enum MatchBlock {
        STATE,
        TYPE
    }

    private static final Map<Key, Protection> PROTECTIONS = new ConcurrentHashMap<>();

    private static volatile long tick;

    public static boolean protect(UUID player, Block block, long duration, MatchBlock match) {
        if (block == null || duration <= 0L || match == null) {
            return false;
        }
        Material material = block.getType();
        if (material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR) {
            return false;
        }
        Protection next = new Protection(material, block.getBlockData().getAsString(),
                match, tick + duration);
        PROTECTIONS.compute(key(player, block), (key, previous) -> {
            if (previous != null && previous.match() == match && previous.matches(block)
                    && previous.expiresAtTick() > next.expiresAtTick()) {
                return previous;
            }
            return next;
        });
        return true;
    }

    public static boolean shouldCancel(UUID player, Block block) {
        return protectedBy(key(null, block), block) || protectedBy(key(player, block), block);
    }

    private static boolean protectedBy(Key key, Block block) {
        Protection protection = PROTECTIONS.get(key);
        if (protection == null) {
            return false;
        }
        if (tick >= protection.expiresAtTick() || !protection.matches(block)) {
            PROTECTIONS.remove(key, protection);
            return false;
        }
        return true;
    }

    public static void advanceTick() {
        long current = ++tick;
        PROTECTIONS.forEach((key, protection) -> {
            if (current >= protection.expiresAtTick()) {
                PROTECTIONS.remove(key, protection);
            }
        });
    }

    public static void clearProtection() {
        PROTECTIONS.clear();
    }

    private static Key key(UUID player, Block block) {
        return new Key(player, block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    private record Key(UUID player, UUID world, int x, int y, int z) {
    }

    private record Protection(Material type, String blockData, MatchBlock match, long expiresAtTick) {
        boolean matches(Block block) {
            return block.getType() == type
                    && (match == MatchBlock.TYPE || blockData.equals(block.getBlockData().getAsString()));
        }
    }
}
