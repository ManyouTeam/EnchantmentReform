package cn.superiormc.enchantmentreform.objects.skills;

import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class SkillSource extends AbstractConfiguredSection<PowerContext> {

    private final String trigger;

    private final boolean manualOnly;

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
        this.maximumXpPerMinute = Math.max(0.0D,
                section.getDouble("anti-abuse.maximum-xp-per-minute", 0.0D));
        this.pressureThreshold = Math.max(0,
                section.getInt("anti-abuse.pressure.threshold", 0));
        this.pressureResetMillis = Math.max(1L,
                section.getLong("anti-abuse.pressure.reset-after-seconds", 120L) * 1000L);
        this.pressureMultiplier = Math.max(0.0D, Math.min(1.0D,
                section.getDouble("anti-abuse.pressure.multiplier", 0.25D)));
    }

    public String id() { return id; }
    public String trigger() { return trigger; }
    public boolean manualOnly() { return manualOnly; }

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
