package cn.superiormc.enchantmentreform.objects.skills;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.MathUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class SkillDefinition {

    public interface SkillReward {
        String displayName();
    }

    public record AttributePointsReward(String displayName, int amount) implements SkillReward {}

    public record AttributeReward(String attribute, String displayName, int amount)
            implements SkillReward {}

    public record AttributesReward(String displayName, Map<String, Integer> attributes)
            implements SkillReward {
        public AttributesReward {
            attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        }
    }

    public record CommandReward(String displayName, String command) implements SkillReward {}

    public record ItemReward(String id, String displayName, ConfigurationSection item)
            implements SkillReward {}

    public record LevelReward(List<SkillReward> rewards) {
        public static final LevelReward EMPTY = new LevelReward(List.of());

        public LevelReward {
            rewards = List.copyOf(rewards);
        }

        public LevelReward merge(LevelReward other) {
            List<SkillReward> merged = new ArrayList<>(rewards);
            merged.addAll(other.rewards);
            return new LevelReward(List.copyOf(merged));
        }

        public int attributePoints() {
            long total = rewards.stream()
                    .filter(AttributePointsReward.class::isInstance)
                    .map(AttributePointsReward.class::cast)
                    .mapToLong(AttributePointsReward::amount).sum();
            return (int) Math.min(Integer.MAX_VALUE, total);
        }

        public Map<String, Integer> attributes() {
            Map<String, Integer> result = new LinkedHashMap<>();
            rewards.stream().filter(AttributeReward.class::isInstance)
                    .map(AttributeReward.class::cast)
                    .forEach(reward -> result.merge(
                            reward.attribute(), reward.amount(), Integer::sum));
            rewards.stream().filter(AttributesReward.class::isInstance)
                    .map(AttributesReward.class::cast)
                    .forEach(reward -> reward.attributes().forEach(
                            (attribute, amount) -> result.merge(attribute, amount, Integer::sum)));
            return Collections.unmodifiableMap(result);
        }

        public List<CommandReward> commands() {
            return rewards.stream().filter(CommandReward.class::isInstance)
                    .map(CommandReward.class::cast).toList();
        }

        public List<ItemReward> items() {
            return rewards.stream().filter(ItemReward.class::isInstance)
                    .map(ItemReward.class::cast).toList();
        }
    }

    private final String id;

    private final String name;

    private final String description;

    private final int order;

    private final int maximumLevel;

    private final String experienceFormula;

    private final YamlConfiguration config;

    private final List<SkillSource> sources;

    private final LevelReward everyLevelReward;

    private final Map<Integer, LevelReward> levelRewards;

    public SkillDefinition(String id, YamlConfiguration config) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.config = config;
        this.name = config.getString("name", this.id);
        this.description = config.getString("description", "");
        this.order = config.getInt("order", 1000);
        this.maximumLevel = Math.max(1, config.getInt("maximum-level", 100));
        this.experienceFormula = config.getString("experience-formula", "100 * {level} * {level}");
        this.sources = loadSources(config.getConfigurationSection("sources"));
        this.everyLevelReward = loadReward(config.getConfigurationSection("rewards.every-level"));
        this.levelRewards = loadLevelRewards(config.getConfigurationSection("rewards.levels"));
    }

    public String id() {
        return id;
    }

    public String name(Player player) {
        return CommonUtil.parseLang(player, name);
    }

    public String description(Player player) {
        return CommonUtil.parseLang(player, description);
    }

    public int order() {
        return order;
    }

    public int maximumLevel() {
        return maximumLevel;
    }

    public double experienceForNextLevel(int currentLevel) {
        if (currentLevel >= maximumLevel) {
            return 0.0D;
        }
        String expression = experienceFormula
                .replace("{level}", String.valueOf(Math.max(1, currentLevel + 1)))
                .replace("{current_level}", String.valueOf(Math.max(0, currentLevel)));
        double value = MathUtil.doCalculate(expression);
        return Double.isFinite(value) ? Math.max(1.0D, value) : 1.0D;
    }

    public ConfigurationSection icon() {
        return config.getConfigurationSection("icon");
    }

    public List<SkillSource> sources() {
        return sources;
    }

    public LevelReward rewardAt(int level) {
        return everyLevelReward.merge(levelRewards.getOrDefault(level, LevelReward.EMPTY));
    }

    private static List<SkillSource> loadSources(ConfigurationSection section) {
        if (section == null) {
            return List.of();
        }
        List<SkillSource> result = new ArrayList<>();
        for (String id : section.getKeys(false)) {
            ConfigurationSection source = section.getConfigurationSection(id);
            if (source != null && source.getBoolean("enabled", true)) {
                result.add(new SkillSource(id, source));
            }
        }
        return List.copyOf(result);
    }

    private static Map<Integer, LevelReward> loadLevelRewards(ConfigurationSection section) {
        if (section == null) {
            return Map.of();
        }
        Map<Integer, LevelReward> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                ConfigurationSection reward = section.getConfigurationSection(key);
                if (level > 0 && reward != null) {
                    result.put(level, loadReward(reward));
                }
            } catch (NumberFormatException ignored) {
                // The manager reports malformed files with the containing skill name.
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static LevelReward loadReward(ConfigurationSection section) {
        if (section == null) {
            return LevelReward.EMPTY;
        }
        List<SkillReward> rewards = new ArrayList<>();
        loadTypedRewards(section, rewards);
        return new LevelReward(List.copyOf(rewards));
    }

    private static void loadTypedRewards(ConfigurationSection reward,
                                         List<SkillReward> rewards) {
        int index = 0;
        for (Map<?, ?> values : reward.getMapList("rewards")) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            values.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            MemoryConfiguration holder = new MemoryConfiguration();
            ConfigurationSection configured = holder.createSection("reward", normalized);
            loadTypedReward("reward-" + ++index, configured, rewards);
        }
        ConfigurationSection section = reward.getConfigurationSection("rewards");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection configured = section.getConfigurationSection(id);
            if (configured == null) {
                continue;
            }
            loadTypedReward(id, configured, rewards);
        }
    }

    private static void loadTypedReward(String id,
                                        ConfigurationSection configured,
                                        List<SkillReward> rewards) {
        String type = configured.getString("type", "").strip().toLowerCase(Locale.ROOT);
        switch (type) {
            case "attribute-points" -> {
                int amount = Math.max(0, configured.getInt("amount", 1));
                if (amount > 0) {
                    rewards.add(new AttributePointsReward(configured.getString("display-name",
                            "{lang:skill-reward-default-attribute-points}"), amount));
                }
            }
            case "attribute" -> {
                String attribute = configured.getString("attribute", "")
                        .strip().toLowerCase(Locale.ROOT);
                int amount = configured.getInt("amount", 1);
                if (!attribute.isEmpty() && amount != 0) {
                    rewards.add(new AttributeReward(attribute,
                            configured.getString("display-name",
                                    "{lang:skill-reward-default-attribute}"), amount));
                }
            }
            case "attributes" -> {
                ConfigurationSection attributes = configured.getConfigurationSection("attributes");
                if (attributes == null) {
                    throw new IllegalArgumentException(
                            "Reward '" + id + "' with type attributes requires an attributes section");
                }
                Map<String, Integer> values = new LinkedHashMap<>();
                for (String attribute : attributes.getKeys(false)) {
                    int amount = attributes.getInt(attribute);
                    if (amount != 0) {
                        values.put(attribute.toLowerCase(Locale.ROOT), amount);
                    }
                }
                if (!values.isEmpty()) {
                    rewards.add(new AttributesReward(configured.getString("display-name",
                            "{lang:skill-reward-default-attributes}"), values));
                }
            }
            case "command" -> {
                String command = configured.getString("command", "").strip();
                if (!command.isEmpty()) {
                    rewards.add(new CommandReward(
                            configured.getString("display-name", id), command));
                }
            }
            case "item" -> rewards.add(new ItemReward(id,
                    configured.getString("display-name", id), configured));
            default -> throw new IllegalArgumentException(
                    "Unknown reward type '" + type + "' for reward '" + id + "'");
        }
    }
}
