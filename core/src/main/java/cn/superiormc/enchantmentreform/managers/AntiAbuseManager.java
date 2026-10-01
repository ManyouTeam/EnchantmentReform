package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.skills.PlacedBlockTracker;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AntiAbuseManager extends AbstractManager {

    private static final class UseWindow {
        private long startedAt;
        private int uses;

        private UseWindow(long startedAt) {
            this.startedAt = startedAt;
        }
    }

    public static AntiAbuseManager antiAbuseManager;

    private final PlacedBlockTracker placedBlocks = new PlacedBlockTracker();

    private final NamespacedKey spawnerMobKey = new NamespacedKey(EnchantmentReform.instance, "skill_spawner_mob");

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private final Map<UUID, Map<String, UseWindow>> useWindows = new ConcurrentHashMap<>();

    private final Map<String, Long> targetCooldowns = new ConcurrentHashMap<>();

    public AntiAbuseManager() {
        antiAbuseManager = this;
    }

    /**
     * Checks and claims a configured use. Skill sources pass safeDefaults=true, while powers
     * opt in to every check by defining it in their trigger's anti-abuse section.
     */
    public boolean allows(ConfigurationSection anti, PowerContext context,
                          String scope, boolean safeDefaults) {
        if (context == null || context.player() == null) {
            return false;
        }
        boolean ignorePlaced = bool(anti, "ignore-player-placed", safeDefaults);
        if (ignorePlaced && context.block() != null && placedBlocks.isPlayerPlaced(context.block())) {
            return false;
        }
        boolean ignoreInternal = bool(anti, "ignore-internal-block-breaks", safeDefaults);
        if (ignoreInternal && context.triggerData() != null
                && context.triggerData().extra(BuiltinContextKeys.INTERNAL_BLOCK_BREAK).orElse(false)) {
            return false;
        }
        boolean ignoreSpawner = bool(anti, "ignore-spawner-mobs", safeDefaults);
        Entity target = context.target();
        if (ignoreSpawner && target instanceof LivingEntity living
                && living.getPersistentDataContainer().has(spawnerMobKey, PersistentDataType.BYTE)) {
            return false;
        }

        long now = System.currentTimeMillis();
        UUID playerId = context.player().getUniqueId();
        long cooldown = Math.max(0L, number(anti, "cooldown-ms", 0L));
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerId,
                ignored -> new ConcurrentHashMap<>());
        if (now < playerCooldowns.getOrDefault(scope, 0L)) {
            return false;
        }

        int maximumUses = (int) Math.max(0L, number(anti, "maximum-uses-per-minute", 0L));
        UseWindow useWindow = null;
        if (maximumUses > 0) {
            useWindow = useWindows.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                    .computeIfAbsent(scope, ignored -> new UseWindow(now));
            synchronized (useWindow) {
                if (now - useWindow.startedAt >= 60_000L) {
                    useWindow.startedAt = now;
                    useWindow.uses = 0;
                }
                if (useWindow.uses >= maximumUses) {
                    return false;
                }
            }
        }

        long targetSeconds = Math.max(0L, number(anti, "target-cooldown-seconds",
                target instanceof Player
                        ? number(anti, "player-victim-cooldown-seconds", safeDefaults ? 300L : 0L)
                        : 0L));
        String targetKey = null;
        if (target != null && targetSeconds > 0L) {
            targetKey = playerId + "/" + target.getUniqueId() + "/" + scope;
            if (now < targetCooldowns.getOrDefault(targetKey, 0L)) {
                return false;
            }
        }
        if (useWindow != null) {
            synchronized (useWindow) {
                useWindow.uses++;
            }
        }
        if (targetKey != null) {
            targetCooldowns.put(targetKey, now + targetSeconds * 1000L);
        }
        if (cooldown > 0L) {
            playerCooldowns.put(scope, now + cooldown);
        }
        return true;
    }

    public PlacedBlockTracker placedBlocks() {
        return placedBlocks;
    }

    public NamespacedKey spawnerMobKey() {
        return spawnerMobKey;
    }

    public void playerQuit(UUID playerId) {
        if (playerId == null) return;
        cooldowns.remove(playerId);
        useWindows.remove(playerId);
        String prefix = playerId + "/";
        targetCooldowns.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private boolean bool(ConfigurationSection section, String path, boolean fallback) {
        return section == null ? fallback : section.getBoolean(path, fallback);
    }

    private long number(ConfigurationSection section, String path, long fallback) {
        return section == null ? fallback : section.getLong(path, fallback);
    }

    @Override
    public void onPluginDisable() {
        cooldowns.clear();
        useWindows.clear();
        targetCooldowns.clear();
        antiAbuseManager = null;
    }
}
