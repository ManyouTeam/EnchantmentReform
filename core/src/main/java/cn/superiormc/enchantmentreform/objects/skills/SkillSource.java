package cn.superiormc.enchantmentreform.objects.skills;

import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillSource extends AbstractConfiguredSection<PowerContext> {

    private final String trigger;

    private final boolean manualOnly;

    private final Statistic movementStatistic;

    private final int distancePerReward;

    private final int triggerCountPerReward;

    private final Map<UUID, Integer> movementStatistics = new ConcurrentHashMap<>();

    private final Map<UUID, Long> movementDistances = new ConcurrentHashMap<>();

    private final Map<UUID, Integer> triggerCounts = new ConcurrentHashMap<>();

    private final double maximumXpPerMinute;

    private final int pressureThreshold;

    private final long pressureResetMillis;

    private final double pressureMultiplier;

    public SkillSource(String id, ConfigurationSection section) {
        super(id.toLowerCase(Locale.ROOT), section);
        String configuredTrigger = section.getString("trigger");
        if (configuredTrigger == null || configuredTrigger.isBlank()) {
            throw new IllegalArgumentException("Missing required option 'trigger'");
        }
        if (!section.contains("xp")) {
            throw new IllegalArgumentException("Missing required option 'xp'");
        }
        this.trigger = normalizeTrigger(configuredTrigger);
        this.manualOnly = section.getBoolean("manual", false);
        String statistic = section.getString("movement-statistic", "").strip();
        this.movementStatistic = statistic.isEmpty() ? null
                : Statistic.valueOf(statistic.toUpperCase(Locale.ROOT));
        double distance = section.getDouble("distance-per-reward", 10.0D);
        if (movementStatistic != null && (!this.trigger.equals("enchantmentreform:move")
                || manualOnly || !movementStatistic.name().endsWith("_ONE_CM")
                || !Double.isFinite(distance) || distance < 0.01D
                || distance > Integer.MAX_VALUE / 100.0D)) {
            throw new IllegalArgumentException("Movement sources require trigger 'move', manual: false, "
                    + "a distance statistic ending in '_ONE_CM', and a positive distance-per-reward");
        }
        this.distancePerReward = movementStatistic == null ? 0 : (int) Math.round(distance * 100.0D);
        this.triggerCountPerReward = section.getInt("trigger-count-per-reward", 1);
        if (triggerCountPerReward < 1
                || (movementStatistic != null || manualOnly) && triggerCountPerReward != 1) {
            throw new IllegalArgumentException("trigger-count-per-reward must be positive "
                    + "and cannot be combined with movement-statistic or manual sources");
        }
        this.maximumXpPerMinute = Math.max(0.0D,
                section.getDouble("anti-abuse.maximum-xp-per-minute", 0.0D));
        this.pressureThreshold = Math.max(0,
                section.getInt("anti-abuse.pressure.threshold", 0));
        this.pressureResetMillis = Math.max(1L,
                section.getLong("anti-abuse.pressure.reset-after-seconds", 120L) * 1000L);
        this.pressureMultiplier = Math.max(0.0D, Math.min(1.0D,
                section.getDouble("anti-abuse.pressure.multiplier", 0.25D)));
    }

    public String id() {
        return id;
    }

    public String trigger() {
        return trigger;
    }

    public boolean manualOnly() {
        return manualOnly;
    }

    public boolean tracksMovement() {
        return movementStatistic != null;
    }

    public boolean batchesTriggers() {
        return triggerCountPerReward > 1;
    }

    public void trackMovement(Player player) {
        clearProgress(player.getUniqueId());
        if (movementStatistic != null) {
            movementStatistics.put(player.getUniqueId(), player.getStatistic(movementStatistic));
        }
    }

    public void clearProgress(UUID playerId) {
        movementStatistics.remove(playerId);
        movementDistances.remove(playerId);
        triggerCounts.remove(playerId);
    }

    public double batchAmount(Player player, double amount) {
        if (triggerCountPerReward == 1) {
            return amount;
        }
        UUID playerId = player.getUniqueId();
        int count = triggerCounts.getOrDefault(playerId, 0) + 1;
        if (count < triggerCountPerReward) {
            triggerCounts.put(playerId, count);
            return 0.0D;
        }
        triggerCounts.remove(playerId);
        return 1.0D;
    }

    public double movementAmount(Player player, boolean matches) {
        UUID playerId = player.getUniqueId();
        int current = player.getStatistic(movementStatistic);
        Integer previous = movementStatistics.put(playerId, current);
        if (previous == null || current < previous) {
            movementDistances.remove(playerId);
            return 0.0D;
        }
        if (!matches) {
            return 0.0D;
        }
        long distance = movementDistances.getOrDefault(playerId, 0L) + (long) current - previous;
        long rewards = distance / distancePerReward;
        movementDistances.put(playerId, distance % distancePerReward);
        return rewards;
    }

    public String name(Player player) {
        return CommonUtil.parseLang(player, section.getString("name", id));
    }

    public String description(Player player) {
        return CommonUtil.parseLang(player, section.getString("description", ""));
    }

    public String unit(Player player) {
        return CommonUtil.parseLang(player, section.getString("unit", ""));
    }

    public String configuredXp() {
        return section.getString("display-xp", section.getString("xp", "0"));
    }

    public ConfigurationSection icon() {
        return section.getConfigurationSection("icon");
    }

    public boolean matches(PowerContext context) {
        return PowerConditionsManager.powerConditions.matches(
                section.getConfigurationSection("conditions"), context);
    }

    public double calculateXp(PowerContext context, double amount) {
        double result = getDouble("xp", 0.0D, context, "amount", String.valueOf(amount));
        return Double.isFinite(result) ? Math.max(0.0D, result) : 0.0D;
    }

    public double maximumXpPerMinute() {
        return maximumXpPerMinute;
    }

    public int pressureThreshold() {
        return pressureThreshold;
    }

    public long pressureResetMillis() {
        return pressureResetMillis;
    }

    public double pressureMultiplier() {
        return pressureMultiplier;
    }

    private static String normalizeTrigger(String trigger) {
        String normalized = trigger.strip().toLowerCase(Locale.ROOT).replace('-', '_');
        return normalized.contains(":") ? normalized : "enchantmentreform:" + normalized;
    }
}
