package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractPowerSourceDefinition implements PowerSourceDefinition {

    private static final List<String> VALID_ACTIVE_SLOTS = List.of(
            "ANY", "ARMOR", "HAND", "OFF_HAND", "HEAD", "CHEST", "LEGS", "FEET");

    protected final String fileName;

    protected final YamlConfiguration config;

    private final String configName;

    private final boolean enabled;

    private final List<String> activeSlots;

    private final boolean emptyActiveSlotsEnabled;

    private final boolean duplicateAllowed;

    private final ConfigurationSection powers;

    private final PowerVariables variables;

    private final int executionPriority;

    private ObjectPower power;

    protected AbstractPowerSourceDefinition(String fileName,
                                            String configName,
                                            YamlConfiguration config,
                                            boolean allowEmptyActiveSlots) {
        this.fileName = fileName;
        this.configName = configName;
        this.config = config;
        validateCommonConfiguration();
        this.enabled = configuredBoolean("enabled", true);
        this.activeSlots = loadActiveSlots(allowEmptyActiveSlots);
        this.emptyActiveSlotsEnabled = allowEmptyActiveSlots;
        this.duplicateAllowed = configuredBoolean("allow-duplicate", false);
        this.powers = config.getConfigurationSection("powers");
        this.variables = PowerVariables.from(config.getConfigurationSection("variables"));
        this.executionPriority = config.getInt("execution-priority", 0);
    }

    public final String getFileName() {
        return fileName;
    }

    public final String getConfigName() {
        return configName;
    }

    public final YamlConfiguration getConfig() {
        return config;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final boolean has(String path) {
        return config.contains(path);
    }

    public final String optionalString(String path) {
        String value = config.getString(path);
        return value == null || value.isBlank() ? null : value.strip();
    }

    public final List<String> stringOrList(String path) {
        Object value = config.get(path);
        if (value == null) {
            return List.of();
        }
        if (value instanceof String text) {
            return text.isBlank() ? List.of() : List.of(text.strip());
        }
        if (value instanceof List<?> values) {
            ArrayList<String> result = new ArrayList<>();
            for (Object entry : values) {
                if (!(entry instanceof String text) || text.isBlank()) {
                    throw error("'" + path + "' entries must be non-empty strings");
                }
                result.add(text.strip());
            }
            return List.copyOf(result);
        }
        throw error("'" + path + "' must be a string or a list of strings");
    }

    public final List<String> getActiveSlots() {
        return activeSlots;
    }

    public final ConfigurationSection getPowers() {
        return powers;
    }

    public final PowerVariables getPowerVariables() {
        return variables;
    }

    public final Map<String, String> getVariables() {
        return variables.scalarValues();
    }

    public final String getVariable(String name, int level) {
        return variables.resolve(name, level);
    }

    @Override
    public final ObjectPower getPower() {
        return power;
    }

    public final void initializePower() {
        power = powers == null ? null : new ObjectPower(powerSourceId(), powers, variables);
    }

    @Override
    public final int getExecutionPriority() {
        return executionPriority;
    }

    @Override
    public final boolean isActiveOn(EquipmentSlot slot) {
        if (slot == null) {
            return false;
        }
        if (activeSlots.isEmpty()) {
            return emptyActiveSlotsEnabled;
        }
        return activeSlots.contains("ANY")
                || activeSlots.contains("ARMOR") && switch (slot) {
                    case HEAD, CHEST, LEGS, FEET -> true;
                    default -> false;
                }
                || activeSlots.contains(slot.name());
    }

    @Override
    public final boolean isDuplicateAllowed() {
        return duplicateAllowed;
    }

    protected final String require(String path) {
        String value = optionalString(path);
        if (value == null) {
            throw error("Missing required option '" + path + "'");
        }
        return value;
    }

    protected final String configuredText(String path, String defaultValue) {
        String value = config.getString(path, defaultValue);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    protected final boolean configuredBoolean(String path, boolean defaultValue) {
        if (config.contains(path) && !config.isBoolean(path)) {
            throw error("'" + path + "' must be true or false");
        }
        return config.getBoolean(path, defaultValue);
    }

    protected final int rangedInt(String path,
                                  int defaultValue,
                                  int minimum,
                                  int maximum) {
        if (config.contains(path) && !config.isInt(path)) {
            throw error("'" + path + "' must be an integer");
        }
        int value = config.getInt(path, defaultValue);
        if (value < minimum || value > maximum) {
            throw error("'" + path + "' must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    protected final int inheritedInt(String path,
                                     ConfigurationSection inheritedSection,
                                     int defaultValue) {
        if (config.contains(path) && !config.isInt(path)) {
            throw error("'" + path + "' must be an integer");
        }
        return config.contains(path)
                ? config.getInt(path, defaultValue)
                : inheritedInt(inheritedSection, path, defaultValue);
    }

    protected static int inheritedInt(ConfigurationSection inheritedSection,
                                      String path,
                                      int defaultValue) {
        return inheritedSection == null
                ? defaultValue
                : inheritedSection.getInt(path, defaultValue);
    }

    protected final List<String> normalizeList(List<String> values) {
        return values.stream()
                .map(value -> value.toUpperCase(Locale.ROOT).replace('-', '_'))
                .toList();
    }

    protected final IllegalArgumentException error(String message) {
        return new IllegalArgumentException(configName + ": " + message);
    }

    private void validateCommonConfiguration() {
        if (config.contains("enabled") && !config.isBoolean("enabled")) {
            throw error("'enabled' must be true or false");
        }
        if (config.contains("allow-duplicate") && !config.isBoolean("allow-duplicate")) {
            throw error("'allow-duplicate' must be true or false");
        }
        if (config.contains("execution-priority") && !config.isInt("execution-priority")) {
            throw error("'execution-priority' must be an integer");
        }
        if (config.contains("variables") && !config.isConfigurationSection("variables")) {
            throw error("'variables' must be a section");
        }
        if (config.contains("powers") && !config.isConfigurationSection("powers")) {
            throw error("'powers' must be a section");
        }
    }

    private List<String> loadActiveSlots(boolean allowEmpty) {
        Object configured = config.get("active-slots");
        List<?> values;
        if (configured == null) {
            values = List.of();
        } else if (configured instanceof String text) {
            values = text.isBlank() ? List.of() : List.of(text);
        } else if (configured instanceof List<?> list) {
            values = list;
        } else {
            throw error("'active-slots' must be a string or a list of strings");
        }
        if (!allowEmpty && values.isEmpty()) {
            throw error("'active-slots' must not be empty");
        }

        List<String> result = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof String text) || text.isBlank()) {
                throw error("'active-slots' entries must be non-empty strings");
            }
            String normalized = normalizeSlot(text);
            if (!VALID_ACTIVE_SLOTS.contains(normalized)) {
                throw error("Unknown active slot '" + text + "'");
            }
            if (!result.contains(normalized)) {
                result.add(normalized);
            }
        }
        return List.copyOf(result);
    }

    private String normalizeSlot(String slot) {
        String normalized = slot.strip().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "MAINHAND", "MAIN_HAND" -> "HAND";
            case "OFFHAND" -> "OFF_HAND";
            default -> normalized;
        };
    }
}
