package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.AbstractTrigger;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.objects.skills.SkillSource;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Loads skills, stores player progression, grants rewards, and enforces XP limits. */
public final class SkillManager extends AbstractManager {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.##");

    public enum AllocationResult {
        SUCCESS, NOT_ENOUGH_POINTS, MAXIMUM_REACHED, CONDITION_NOT_MET,
        ATTRIBUTE_NOT_FOUND, INVALID
    }

    private static final class RateWindow {
        private long startedAt;
        private double experience;

        private RateWindow(long startedAt) {
            this.startedAt = startedAt;
        }
    }

    private static final class PressureState {
        private String sourceKey;
        private int count;
        private long lastAt;
    }

    private static final class FeedbackState {
        private long lastAt;
    }

    public static SkillManager skillManager;

    private final Map<String, SkillDefinition> skills = new LinkedHashMap<>();
    private final Map<UUID, Map<String, RateWindow>> rateWindows = new ConcurrentHashMap<>();
    private final Map<UUID, PressureState> pressureStates = new ConcurrentHashMap<>();
    private final Map<UUID, FeedbackState> feedbackStates = new ConcurrentHashMap<>();
    private final NamespacedKey attributePointsKey = new NamespacedKey(
            EnchantmentReform.instance, "skill_attribute_points");
    private final NamespacedKey allExperienceMultiplierKey = new NamespacedKey(
            EnchantmentReform.instance, "skill_xp_multiplier/all");

    public SkillManager() {
        skillManager = this;
        loadSkills();
    }

    public void loadSkills() {
        skills.clear();
        if (!isEnabled()) {
            return;
        }
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "skills");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Could not create skill directory: " + directory);
            return;
        }
        File[] files = directory.listFiles((ignored, name) ->
                name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = InitManager.loadConfiguration(file, "skills/" + file.getName());
                if (!config.getBoolean("enabled", true)) {
                    continue;
                }
                SkillDefinition skill = new SkillDefinition(id, config);
                if (skills.putIfAbsent(id, skill) != null) {
                    throw new IllegalArgumentException("Duplicate skill id: " + id);
                }
                TextUtil.sendMessage(null, TextUtil.pluginPrefix()
                        + " §fLoaded skill: " + id + ".yml!");
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Failed to load skill "
                        + file.getName() + ": " + throwable.getMessage());
            }
        }
    }

    public Collection<SkillDefinition> getSkills() {
        return List.copyOf(skills.values());
    }

    public SkillDefinition getSkill(String id) {
        return id == null ? null : skills.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean isEnabled() {
        return ConfigManager.configManager == null
                || ConfigManager.configManager.getBoolean("modules.skills", true);
    }

    /** Returns the combined permission and persistent player XP multiplier for one skill. */
    public double getExperienceMultiplier(Player player, SkillDefinition skill) {
        if (player == null || skill == null) {
            return 1.0D;
        }
        double multiplier = permissionExperienceMultiplier(player, skill);
        PersistentDataContainer data = player.getPersistentDataContainer();
        multiplier = safeMultiply(multiplier, data.getOrDefault(
                allExperienceMultiplierKey, PersistentDataType.DOUBLE, 1.0D));
        multiplier = safeMultiply(multiplier, data.getOrDefault(
                experienceMultiplierKey(skill), PersistentDataType.DOUBLE, 1.0D));
        return multiplier;
    }

    /** Sets a persistent multiplier. A multiplier of 1 removes the corresponding override. */
    public void setExperienceMultiplier(Player player, SkillDefinition skill, double multiplier) {
        if (player == null || !Double.isFinite(multiplier) || multiplier < 0.0D) {
            throw new IllegalArgumentException("Multiplier must be a finite, non-negative number");
        }
        NamespacedKey key = skill == null ? allExperienceMultiplierKey : experienceMultiplierKey(skill);
        if (Math.abs(multiplier - 1.0D) < 1.0E-9D) {
            player.getPersistentDataContainer().remove(key);
        } else {
            player.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, multiplier);
        }
    }

    public int getLevel(Player player, SkillDefinition skill) {
        return player.getPersistentDataContainer().getOrDefault(levelKey(skill),
                PersistentDataType.INTEGER, 0);
    }

    public double getExperience(Player player, SkillDefinition skill) {
        return player.getPersistentDataContainer().getOrDefault(experienceKey(skill),
                PersistentDataType.DOUBLE, 0.0D);
    }

    /** Sets the XP progress inside the player's current level without granting level rewards. */
    public double setExperience(Player player, SkillDefinition skill, double experience) {
        if (player == null || skill == null || !Double.isFinite(experience)
                || experience < 0.0D) {
            throw new IllegalArgumentException("Experience must be a finite, non-negative number");
        }
        int level = getLevel(player, skill);
        double maximum = level >= skill.maximumLevel()
                ? 0.0D : Math.max(0.0D, skill.experienceForNextLevel(level));
        double updated = Math.min(experience, maximum);
        player.getPersistentDataContainer().set(
                experienceKey(skill), PersistentDataType.DOUBLE, updated);
        return updated;
    }

    /** Sets a skill level directly. Administrative changes do not grant configured rewards. */
    public int setLevel(Player player, SkillDefinition skill, int level) {
        if (player == null || skill == null) {
            throw new IllegalArgumentException("Player and skill are required");
        }
        int updated = Math.max(0, Math.min(skill.maximumLevel(), level));
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(levelKey(skill), PersistentDataType.INTEGER, updated);
        if (updated >= skill.maximumLevel()) {
            data.set(experienceKey(skill), PersistentDataType.DOUBLE, 0.0D);
        } else {
            setExperience(player, skill, getExperience(player, skill));
        }
        return updated;
    }

    public int addLevel(Player player, SkillDefinition skill, int amount) {
        long requested = (long) getLevel(player, skill) + amount;
        int bounded = requested < Integer.MIN_VALUE ? Integer.MIN_VALUE
                : requested > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) requested;
        return setLevel(player, skill, bounded);
    }

    public double getRequiredExperience(Player player, SkillDefinition skill) {
        return skill.experienceForNextLevel(getLevel(player, skill));
    }

    public int getAttributePoints(Player player) {
        return Math.max(0, player.getPersistentDataContainer().getOrDefault(
                attributePointsKey, PersistentDataType.INTEGER, 0));
    }

    public void addAttributePoints(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        long updated = (long) getAttributePoints(player) + amount;
        player.getPersistentDataContainer().set(attributePointsKey, PersistentDataType.INTEGER,
                (int) Math.min(Integer.MAX_VALUE, updated));
    }

    public AllocationResult allocate(Player player, String attributeId, int add) {
        if (add <= 0) {
            return AllocationResult.INVALID;
        }
        ObjectCustomAttribute attribute = AttributeManager.attributeManager == null
                ? null : AttributeManager.attributeManager.resolveAttribute(attributeId);
        if (attribute == null) {
            return AllocationResult.ATTRIBUTE_NOT_FOUND;
        }
        int base = attribute.getBaseValue(player);
        if ((long) base + add > attribute.getAllocationMaximumValue()) {
            return AllocationResult.MAXIMUM_REACHED;
        }
        if (!attribute.meetsAllocationConditions(player, add)) {
            return AllocationResult.CONDITION_NOT_MET;
        }
        int price = attribute.getAllocationPrice(base, add);
        int points = getAttributePoints(player);
        if (points < price) {
            return AllocationResult.NOT_ENOUGH_POINTS;
        }
        player.getPersistentDataContainer().set(attributePointsKey,
                PersistentDataType.INTEGER, points - price);
        attribute.setValue(player, base + add);
        return AllocationResult.SUCCESS;
    }

    /** Called from TriggerManager after a configured trigger has produced its context/result. */
    public void handleTrigger(AbstractTrigger<?> trigger, TriggerData data, TriggerResult result) {
        if (trigger == null || data == null || result == null || result.cancelled()
                || data.event() instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return;
        }
        Player player = data.player();
        for (SkillDefinition skill : skills.values()) {
            for (SkillSource source : skill.sources()) {
                if (source.manualOnly()
                        || !source.trigger().equals(trigger.key().toString())) {
                    continue;
                }
                PowerContext context = new PowerContext(null, getLevel(player, skill), trigger,
                        data, data.triggerItem(), data.triggerItemSlot(), result);
                if (!source.matches(context) || AntiAbuseManager.antiAbuseManager == null
                        || !AntiAbuseManager.antiAbuseManager.allows(
                        source.getSection("anti-abuse"), context,
                        "skill/" + skill.id() + "/" + source.id(), true)) {
                    continue;
                }
                grantSourceExperience(player, skill, source, context, sourceAmount(data));
            }
        }
    }

    public double grantSourceExperience(Player player, SkillDefinition skill,
                                        SkillSource source, PowerContext context, double amount) {
        int level = getLevel(player, skill);
        if (level >= skill.maximumLevel()) {
            return 0.0D;
        }
        long now = System.currentTimeMillis();
        String key = skill.id() + "/" + source.id();
        double xp = safeMultiply(source.calculateXp(context, amount),
                pressureMultiplier(player, source, key, now));
        xp = safeMultiply(xp, getExperienceMultiplier(player, skill));
        if (xp <= 0.0D) {
            return 0.0D;
        }
        xp = applyRateLimit(player, key, source.maximumXpPerMinute(), xp, now);
        if (xp <= 0.0D) {
            return 0.0D;
        }
        addExperienceRaw(player, skill, xp);
        sendExperienceFeedback(player, skill, xp, now);
        return xp;
    }

    /** Grants a source that is listed in the skill menu but activated by an ability. */
    public double grantManualSourceExperience(Player player, SkillDefinition skill,
                                              SkillSource source, PowerContext context,
                                              double amount) {
        if (player == null || skill == null || source == null || context == null
                || !source.manualOnly() || !source.matches(context)
                || AntiAbuseManager.antiAbuseManager == null
                || !AntiAbuseManager.antiAbuseManager.allows(
                source.getSection("anti-abuse"), context,
                "skill/" + skill.id() + "/" + source.id(), true)) {
            return 0.0D;
        }
        return grantSourceExperience(player, skill, source, context, amount);
    }

    public void addExperience(Player player, SkillDefinition skill, double amount) {
        addExperienceRaw(player, skill, safeMultiply(amount,
                getExperienceMultiplier(player, skill)));
    }

    /** Adds an exact administrative amount, bypassing XP multipliers and source rate limits. */
    public void addExperienceDirect(Player player, SkillDefinition skill, double amount) {
        addExperienceRaw(player, skill, amount);
    }

    private void addExperienceRaw(Player player, SkillDefinition skill, double amount) {
        if (amount <= 0.0D || !Double.isFinite(amount)) {
            return;
        }
        PersistentDataContainer data = player.getPersistentDataContainer();
        int level = getLevel(player, skill);
        double experience = getExperience(player, skill) + amount;
        while (level < skill.maximumLevel()) {
            double required = skill.experienceForNextLevel(level);
            if (experience + 1.0E-9D < required) {
                break;
            }
            experience -= required;
            level++;
            data.set(levelKey(skill), PersistentDataType.INTEGER, level);
            applyReward(player, skill, level);
        }
        if (level >= skill.maximumLevel()) {
            experience = 0.0D;
        }
        data.set(levelKey(skill), PersistentDataType.INTEGER, level);
        data.set(experienceKey(skill), PersistentDataType.DOUBLE, Math.max(0.0D, experience));
    }

    private double sourceAmount(TriggerData data) {
        if (data.event() instanceof EntityDamageEvent damage) {
            return Math.max(0.0D, damage.getFinalDamage());
        }
        return 1.0D;
    }

    private void applyReward(Player player, SkillDefinition skill, int level) {
        SkillDefinition.LevelReward reward = skill.rewardAt(level);
        for (SkillDefinition.SkillReward configured : reward.rewards()) {
            if (configured instanceof SkillDefinition.AttributePointsReward pointsReward) {
                addAttributePoints(player, pointsReward.amount());
            } else if (configured instanceof SkillDefinition.AttributeReward attributeReward
                    && AttributeManager.attributeManager != null) {
                ObjectCustomAttribute attribute = AttributeManager.attributeManager.resolveAttribute(
                        attributeReward.attribute());
                if (attribute != null) {
                    attribute.addValue(player, attributeReward.amount());
                }
            } else if (configured instanceof SkillDefinition.AttributesReward attributesReward
                    && AttributeManager.attributeManager != null) {
                attributesReward.attributes().forEach((id, amount) -> {
                    ObjectCustomAttribute attribute = AttributeManager.attributeManager.resolveAttribute(id);
                    if (attribute != null) {
                        attribute.addValue(player, amount);
                    }
                });
            } else if (configured instanceof SkillDefinition.CommandReward command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.command()
                        .replace("{player}", player.getName())
                        .replace("{skill}", skill.id())
                        .replace("{level}", String.valueOf(level)));
            } else if (configured instanceof SkillDefinition.ItemReward itemReward) {
                ItemStack item = BuildItem.buildItemStack(player, itemReward.item(),
                        "player", player.getName(), "skill", skill.id(),
                        "level", String.valueOf(level));
                CommonUtil.giveOrDrop(player, item);
            }
        }
        String message = ConfigManager.configManager.getString("skills.level-up-message",
                "&a{skill} reached level {level}! &e+{points} attribute points");
        TextUtil.sendMessage(player, CommonUtil.modifyString(player, message,
                "skill", skill.name(player), "level", String.valueOf(level),
                "points", String.valueOf(reward.attributePoints())));
        String title = ConfigManager.configManager.getString("skills.feedback.level-up.title", "");
        if (!title.isBlank()) {
            TextUtil.sendMessage(player, CommonUtil.modifyString(player, title,
                    "skill", skill.name(player), "level", String.valueOf(level),
                    "points", String.valueOf(reward.attributePoints())));
        }
        playConfiguredSound(player, "skills.feedback.level-up.sound");
    }

    private void sendExperienceFeedback(Player player, SkillDefinition skill, double gained, long now) {
        if (!ConfigManager.configManager.getBoolean("skills.feedback.experience.enabled")) {
            return;
        }
        long cooldown = Math.max(0L, ConfigManager.configManager.getInt(
                "skills.feedback.experience.cooldown-ticks", 5)) * 50L;
        FeedbackState state = feedbackStates.computeIfAbsent(player.getUniqueId(), ignored -> new FeedbackState());
        synchronized (state) {
            if (now - state.lastAt < cooldown) return;
            state.lastAt = now;
        }
        int level = getLevel(player, skill);
        double experience = getExperience(player, skill);
        double required = getRequiredExperience(player, skill);
        double percent = required <= 0.0D ? 100.0D
                : Math.min(100.0D, experience / required * 100.0D);
        String[] placeholders = {
                "skill", skill.name(player), "level", String.valueOf(level),
                "gained", NUMBER.format(gained), "experience", NUMBER.format(experience),
                "required_experience", NUMBER.format(required),
                "remaining_experience", NUMBER.format(Math.max(0.0D, required - experience)),
                "progress", NUMBER.format(percent),
                "progress_bar", progressBar(percent, 20)
        };
        String display = ConfigManager.configManager.getString(
                        "skills.feedback.experience.display", "BOSS_BAR")
                .trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if (display.equals("BOSS_BAR") || display.equals("BOSSBAR") || display.equals("BOTH")) {
            String title = ConfigManager.configManager.getString(
                    "skills.feedback.experience.bossbar.title", "{lang:skill-xp-gain}");
            if (!title.isBlank()) {
                EnchantmentReform.methodUtil.sendBossBar(
                        player,
                        CommonUtil.modifyString(player, title, placeholders),
                        (float) Math.max(0.0D, Math.min(1.0D, percent / 100.0D)),
                        ConfigManager.configManager.getString(
                                "skills.feedback.experience.bossbar.color", "BLUE"),
                        ConfigManager.configManager.getString(
                                "skills.feedback.experience.bossbar.style", "SOLID"),
                        "skill-experience");
            }
        }
        if (display.equals("ACTION_BAR") || display.equals("ACTIONBAR") || display.equals("BOTH")) {
            String message = ConfigManager.configManager.getString(
                    "skills.feedback.experience.actionbar", "");
            if (!message.isBlank()) {
                TextUtil.sendMessage(player, CommonUtil.modifyString(player, message, placeholders));
            }
        }
        playConfiguredSound(player, "skills.feedback.experience.sound");
    }

    private void playConfiguredSound(Player player, String path) {
        String sound = ConfigManager.configManager.getString(path + ".name", "");
        if (sound.isBlank()) return;
        float volume = (float) Math.max(0.0D, ConfigManager.configManager.getDouble(path + ".volume", 1.0D));
        float pitch = (float) Math.max(0.0D, ConfigManager.configManager.getDouble(path + ".pitch", 1.0D));
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private static String progressBar(double percent, int length) {
        int safeLength = Math.max(1, length);
        int filled = (int) Math.round(Math.max(0.0D, Math.min(100.0D, percent)) / 100.0D * safeLength);
        return "&a" + "■".repeat(filled) + "&8" + "■".repeat(safeLength - filled);
    }

    private double pressureMultiplier(Player player, SkillSource source, String key, long now) {
        if (source.pressureThreshold() <= 0) {
            return 1.0D;
        }
        PressureState state = pressureStates.computeIfAbsent(
                player.getUniqueId(), ignored -> new PressureState());
        synchronized (state) {
            if (!key.equals(state.sourceKey) || now - state.lastAt > source.pressureResetMillis()) {
                state.sourceKey = key;
                state.count = 1;
            } else {
                state.count++;
            }
            state.lastAt = now;
            return state.count > source.pressureThreshold() ? source.pressureMultiplier() : 1.0D;
        }
    }

    private double applyRateLimit(Player player, String key, double maximum,
                                  double amount, long now) {
        if (maximum <= 0.0D) {
            return amount;
        }
        RateWindow window = rateWindows.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, ignored -> new RateWindow(now));
        synchronized (window) {
            if (now - window.startedAt >= 60_000L) {
                window.startedAt = now;
                window.experience = 0.0D;
            }
            double granted = Math.min(amount, Math.max(0.0D, maximum - window.experience));
            window.experience += granted;
            return granted;
        }
    }

    private NamespacedKey levelKey(SkillDefinition skill) {
        return new NamespacedKey(EnchantmentReform.instance, "skill_level/" + skill.id());
    }

    private NamespacedKey experienceKey(SkillDefinition skill) {
        return new NamespacedKey(EnchantmentReform.instance, "skill_xp/" + skill.id());
    }

    private NamespacedKey experienceMultiplierKey(SkillDefinition skill) {
        return new NamespacedKey(EnchantmentReform.instance,
                "skill_xp_multiplier/" + skill.id());
    }

    private double permissionExperienceMultiplier(Player player, SkillDefinition skill) {
        ConfigurationSection permissions = ConfigManager.configManager.getSection(
                "skills.experience-multipliers.permissions");
        double result = 1.0D;
        for (String id : permissions.getKeys(false)) {
            ConfigurationSection entry = permissions.getConfigurationSection(id);
            if (entry == null) {
                continue;
            }
            String permission = entry.getString("permission", id).strip();
            double multiplier = entry.getDouble("multiplier", 1.0D);
            if (permission.isEmpty() || !player.hasPermission(permission)
                    || !Double.isFinite(multiplier) || multiplier < 0.0D
                    || !matchesSkill(entry, skill.id())) {
                continue;
            }
            result = safeMultiply(result, multiplier);
        }
        return result;
    }

    private boolean matchesSkill(ConfigurationSection entry, String skillId) {
        List<String> configured = entry.getStringList("skills");
        if (configured.isEmpty()) {
            String single = entry.getString("skill", "*");
            configured = List.of(single);
        }
        return configured.stream().map(String::strip).anyMatch(value ->
                value.equals("*") || value.equalsIgnoreCase("all")
                        || value.equalsIgnoreCase(skillId));
    }

    private static double safeMultiply(double left, double right) {
        if (!Double.isFinite(left) || !Double.isFinite(right) || left <= 0.0D || right <= 0.0D) {
            return 0.0D;
        }
        if (left > Double.MAX_VALUE / right) {
            return Double.MAX_VALUE;
        }
        return left * right;
    }

    public void playerQuit(UUID playerId) {
        if (playerId == null) return;
        rateWindows.remove(playerId);
        pressureStates.remove(playerId);
        feedbackStates.remove(playerId);
    }

    @Override
    public void onPluginDisable() {
        rateWindows.clear();
        pressureStates.clear();
        feedbackStates.clear();
        skillManager = null;
    }
}
