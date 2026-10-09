package cn.superiormc.enchantmentreform.power;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AttackCooldownUtil {

    private static final long MAX_AGE_MILLIS = 1_000L;

    private static final Map<Key, Entry> VALUES = new ConcurrentHashMap<>();

    private AttackCooldownUtil() {
    }

    public static void record(Player player, Entity target, float strength) {
        if (player == null || target == null || !Float.isFinite(strength)) {
            return;
        }
        VALUES.put(new Key(player.getUniqueId(), target.getUniqueId()),
                new Entry(Math.max(0.0D, Math.min(1.0D, strength)), System.currentTimeMillis()));
    }

    public static double consume(Player player, Entity target) {
        if (player == null || target == null) {
            return player == null ? 0.0D : player.getAttackCooldown();
        }
        Entry entry = VALUES.remove(new Key(player.getUniqueId(), target.getUniqueId()));
        if (entry == null || System.currentTimeMillis() - entry.createdAt > MAX_AGE_MILLIS) {
            return player.getAttackCooldown();
        }
        return entry.strength;
    }

    public static void clear(UUID entityId) {
        if (entityId != null) {
            VALUES.keySet().removeIf(key -> key.player.equals(entityId)
                    || key.target.equals(entityId));
        }
    }

    private record Key(UUID player, UUID target) {
    }

    private record Entry(double strength, long createdAt) {
    }
}
