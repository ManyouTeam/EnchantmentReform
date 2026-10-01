package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TempBlockManager extends AbstractManager {

    private static final String CRACK_CONFIG_PATH =
            "powers.temp-block-crack-animation.";

    private static final AtomicInteger NEXT_CRACK_SOURCE_ID =
            new AtomicInteger(-1);

    public static TempBlockManager tempBlockManager;

    private final Map<String, TempBlockData> tempBlocks = new ConcurrentHashMap<>();

    private final boolean crackAnimationEnabled;

    private final double crackAnimationStartAt;

    private final long crackAnimationUpdateInterval;

    private final double crackAnimationViewDistanceSquared;

    private volatile long animationClock;

    private SchedulerUtil crackAnimationTask;

    public TempBlockManager() {
        tempBlockManager = this;
        ConfigManager config = ConfigManager.configManager;
        crackAnimationEnabled = config != null
                && config.getBoolean(CRACK_CONFIG_PATH + "enabled", true);
        crackAnimationStartAt = clamp(config == null
                ? 0.5D : config.getDouble(CRACK_CONFIG_PATH + "start-at", 0.5D),
                0.0D, 0.99D);
        crackAnimationUpdateInterval = Math.max(1L, config == null
                ? 5L : config.getInt(CRACK_CONFIG_PATH + "update-interval", 5));
        int viewDistance = Math.max(1, config == null
                ? 64 : config.getInt(CRACK_CONFIG_PATH + "view-distance", 64));
        crackAnimationViewDistanceSquared = (double) viewDistance * viewDistance;
        if (crackAnimationEnabled) {
            crackAnimationTask = SchedulerUtil.runTaskTimer(() -> {
                animationClock += crackAnimationUpdateInterval;
                updateCrackAnimations();
            }, crackAnimationUpdateInterval, crackAnimationUpdateInterval);
        }
    }

    public void createTempBlock(Location location, Material material, int duration) {
        if (location == null || material == null) {
            return;
        }

        String key = toKey(location);

        // 已存在则不重复创建
        if (tempBlocks.containsKey(key)) {
            return;
        }

        Block block = location.getBlock();

        // 保存原始数据
        BlockData originalData = block.getBlockData().clone();

        // 设置为临时方块
        block.setType(material, false);

        // 保存临时方块数据，以免到期任务覆盖第三方插件后续放置的方块
        BlockData temporaryData = block.getBlockData().clone();

        registerTempBlock(location, originalData, temporaryData, duration);
    }

    /**
     * Tracks a block that has already been formed by a vanilla enchantment effect.
     * The caller must capture both states while handling the corresponding form event.
     */
    public void trackFormedBlock(Location location, BlockData originalData,
                                 BlockData temporaryData, int duration) {
        if (location == null || originalData == null || temporaryData == null) {
            return;
        }
        registerTempBlock(location, originalData.clone(), temporaryData.clone(), duration);
    }

    private void registerTempBlock(Location location, BlockData originalData,
                                   BlockData temporaryData, int duration) {
        String key = toKey(location);

        // 创建定时任务
        SchedulerUtil task = SchedulerUtil.runTaskLater(
                location, () -> removeTempBlock(location), Math.max(1, duration));

        TempBlockData previous = tempBlocks.putIfAbsent(
                key, new TempBlockData(location.clone(), originalData,
                        temporaryData, task, animationClock,
                        Math.max(1, duration), NEXT_CRACK_SOURCE_ID.getAndDecrement()));
        if (previous != null) {
            task.cancel();
        }
    }

    public void removeTempBlock(Location location) {
        String key = toKey(location);

        TempBlockData data = tempBlocks.remove(key);
        if (data == null) {
            return;
        }

        Block block = location.getBlock();

        // 只恢复仍保持临时状态的方块，避免覆盖第三方插件后续修改
        if (sameBlockData(block.getBlockData(), data.temporaryData)) {
            block.setBlockData(data.originalData, false);
        }

        // 取消任务
        if (data.task != null) {
            data.task.cancel();
        }
        clearCrackAnimation(data);
    }

    public boolean isTempBlock(Location location) {
        return tempBlocks.containsKey(toKey(location));
    }

    public void clearAll() {
        if (crackAnimationTask != null) {
            crackAnimationTask.cancel();
            crackAnimationTask = null;
        }
        for (String key : tempBlocks.keySet()) {
            Location location = fromKey(key);
            removeTempBlock(location);
        }
        tempBlocks.clear();
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName() + ";"
                + loc.getBlockX() + ";"
                + loc.getBlockY() + ";"
                + loc.getBlockZ();
    }

    private Location fromKey(String key) {
        String[] split = key.split(";");
        return new Location(
                Bukkit.getWorld(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2]),
                Integer.parseInt(split[3])
        );
    }

    @Override
    public void onPluginDisable() {
        clearAll();
        tempBlockManager = null;
    }

    private boolean sameBlockData(BlockData first, BlockData second) {
        return first.getAsString().equals(second.getAsString());
    }

    private void updateCrackAnimations() {
        if (tempBlocks.isEmpty()) {
            return;
        }
        List<CrackAnimationFrame> frames = new ArrayList<>();
        for (Map.Entry<String, TempBlockData> entry : tempBlocks.entrySet()) {
            TempBlockData data = entry.getValue();
            double elapsedRatio = clamp(
                    (double) (animationClock - data.createdAtTick) / data.duration,
                    0.0D, 1.0D);
            if (elapsedRatio < crackAnimationStartAt) {
                continue;
            }
            double crackRatio = clamp(
                    (elapsedRatio - crackAnimationStartAt)
                            / (1.0D - crackAnimationStartAt),
                    0.0D, 1.0D);
            int stage = Math.min(9, (int) Math.floor(crackRatio * 10.0D));
            boolean stageChanged = data.lastCrackStage != stage;
            data.lastCrackStage = stage;
            frames.add(new CrackAnimationFrame(
                    entry.getKey(), data, (stage + 1) / 10.0F, stageChanged));
        }
        if (frames.isEmpty()) {
            return;
        }
        List<CrackAnimationFrame> snapshot = List.copyOf(frames);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (EnchantmentReform.isFolia) {
                SchedulerUtil.runSync(player, () -> sendCrackFrames(player, snapshot));
            } else {
                sendCrackFrames(player, snapshot);
            }
        }
    }

    private void sendCrackFrames(Player player, List<CrackAnimationFrame> frames) {
        if (!player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        Location playerLocation = player.getLocation();
        for (CrackAnimationFrame frame : frames) {
            TempBlockData data = frame.data;
            if (tempBlocks.get(frame.key) != data) {
                if (data.viewers.remove(playerId)
                        && player.getWorld().equals(data.location.getWorld())) {
                    player.sendBlockDamage(data.location, 0.0F, data.crackSourceId);
                }
                continue;
            }
            boolean sameWorld = player.getWorld().equals(data.location.getWorld());
            boolean visible = sameWorld && playerLocation.distanceSquared(data.location)
                    <= crackAnimationViewDistanceSquared;
            if (!visible) {
                if (data.viewers.remove(playerId) && sameWorld) {
                    player.sendBlockDamage(data.location, 0.0F, data.crackSourceId);
                }
                continue;
            }
            boolean newViewer = data.viewers.add(playerId);
            if (newViewer || frame.stageChanged) {
                player.sendBlockDamage(
                        data.location, frame.progress, data.crackSourceId);
            }
        }
    }

    private void clearCrackAnimation(TempBlockData data) {
        if (!crackAnimationEnabled || data.viewers.isEmpty()) {
            return;
        }
        for (UUID viewerId : List.copyOf(data.viewers)) {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null || !player.isOnline()) {
                continue;
            }
            Runnable clear = () -> {
                if (player.isOnline()
                        && player.getWorld().equals(data.location.getWorld())) {
                    player.sendBlockDamage(
                            data.location, 0.0F, data.crackSourceId);
                }
            };
            if (EnchantmentReform.isFolia) {
                SchedulerUtil.runSync(player, clear);
            } else {
                clear.run();
            }
        }
        data.viewers.clear();
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static final class TempBlockData {

        private final Location location;
        private final BlockData originalData;
        private final BlockData temporaryData;
        private final SchedulerUtil task;
        private final long createdAtTick;
        private final int duration;
        private final int crackSourceId;
        private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
        private volatile int lastCrackStage = -1;

        private TempBlockData(Location location, BlockData originalData,
                              BlockData temporaryData, SchedulerUtil task,
                              long createdAtTick, int duration,
                              int crackSourceId) {
            this.location = location;
            this.originalData = originalData;
            this.temporaryData = temporaryData;
            this.task = task;
            this.createdAtTick = createdAtTick;
            this.duration = duration;
            this.crackSourceId = crackSourceId;
        }

    }

    private record CrackAnimationFrame(String key, TempBlockData data,
                                       float progress, boolean stageChanged) {

    }
}
