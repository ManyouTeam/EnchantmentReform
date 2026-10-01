package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PowerEnchantmentDefinition extends AbstractPowerSourceDefinition {

    private static final Pattern LANG_PATTERN = Pattern.compile("\\{lang:(.*?)}");

    private static final Set<String> VALID_OBTAINING_SOURCES = Set.of(
            "VILLAGER_TRADE", "ENCHANTING_TABLE", "RANDOM_LOOT",
            "MOB_EQUIPMENT", "TRADED_EQUIPMENT");

    private final boolean vanillaOverride;

    private final EnchantmentKey key;

    private final EnchantmentRarity rarity;

    private final String name;

    private final String description;

    private final String registryDescription;

    private final List<String> supportedItemKeys;

    private final List<String> primaryItemKeys;

    private final List<String> exclusiveWith;

    private final Map<String, Object> nativeEffects;

    private final boolean nativeEffectsConfigured;

    private final int weight;

    private final int maxLevel;

    private final int anvilCost;

    private final ObjectCustomEnchantment.EnchantmentCost minimumCost;

    private final ObjectCustomEnchantment.EnchantmentCost maximumCost;

    private final List<String> obtainingSources;

    private final ConfigurationSection conditions;

    private final ConfigurationSection conditionsByLevel;

    protected PowerEnchantmentDefinition(String fileName,
                                          YamlConfiguration config,
                                          YamlConfiguration registryLanguage,
                                          ConfigurationSection raritySettings,
                                          boolean vanillaOverride) {
        super(fileName, (vanillaOverride ? "vanilla_enchantments/" : "enchantments/")
                + fileName + ".yml", config, vanillaOverride);
        this.vanillaOverride = vanillaOverride;

        String defaultKey = vanillaOverride ? "minecraft:" + fileName : fileName;
        this.key = EnchantmentKey.parse(config.getString("key", defaultKey));
        if (vanillaOverride && !"minecraft".equals(key.namespace())) {
            throw error("Vanilla enchantment overrides must use the minecraft namespace");
        }

        this.rarity = EnchantmentRarity.resolve(
                config.getString("rarity", "COMMON"), raritySettings);

        String languagePrefix = vanillaOverride ? "vanilla-enchantment-" : "enchantment-";
        this.name = configuredText("name", "{lang:" + languagePrefix + fileName + "-name}");
        this.description = configuredText(
                "description", "{lang:" + languagePrefix + fileName + "-description}");
        this.registryDescription = resolveRegistryDescription(registryLanguage);

        if (vanillaOverride) {
            this.supportedItemKeys = stringOrList("supported-items");
            this.primaryItemKeys = stringOrList("primary-items");
        } else {
            this.supportedItemKeys = List.of(require("supported-items"));
            String primaryItems = optionalString("primary-items");
            this.primaryItemKeys = primaryItems == null ? List.of() : List.of(primaryItems);
        }
        this.exclusiveWith = stringOrList("exclusive-with");

        Object selectedEffects;
        Map<String, Object> resolvedEffects;
        try {
            selectedEffects = NativeEffectsSelector.select(config, NativeEffectsSelector::currentVersion);
            if (config.contains("auto-migrate-effects") && !config.isBoolean("auto-migrate-effects")) {
                throw new IllegalArgumentException("'auto-migrate-effects' must be a boolean");
            }
            resolvedEffects = copyEffects(selectedEffects);
            if (!resolvedEffects.isEmpty() && config.getBoolean("auto-migrate-effects", true)) {
                resolvedEffects = Map.copyOf(NativeEffectsMigrator.migrate(
                        resolvedEffects, NativeEffectsSelector.currentVersion()));
            }
        } catch (IllegalArgumentException exception) {
            throw error(exception.getMessage());
        }
        this.nativeEffectsConfigured = selectedEffects != null;
        this.nativeEffects = resolvedEffects;

        ConfigurationSection raritySection = rarity.settings();
        this.weight = rangedInt(
                "weight", inheritedInt(raritySection, "weight", 10), 1, 1024);
        this.maxLevel = config.contains("max-level")
                ? rangedInt("max-level", 1, 1, 255)
                : vanillaOverride ? 0 : 1;
        this.anvilCost = rangedInt(
                "anvil-cost", inheritedInt(raritySection, "anvil-cost", 1),
                0, Integer.MAX_VALUE);
        this.minimumCost = new ObjectCustomEnchantment.EnchantmentCost(
                inheritedInt("minimum-cost.base", raritySection, 1),
                inheritedInt("minimum-cost.per-level", raritySection, 0));
        this.maximumCost = new ObjectCustomEnchantment.EnchantmentCost(
                inheritedInt("maximum-cost.base", raritySection, 1),
                inheritedInt("maximum-cost.per-level", raritySection, 0));

        this.obtainingSources = normalizeList(stringOrList("obtaining-sources"));
        if (config.contains("conditions")
                && !config.isConfigurationSection("conditions")) {
            throw error("'conditions' must be a section");
        }
        this.conditions = config.getConfigurationSection("conditions");
        this.conditionsByLevel = resolveConditionsByLevel(this.conditions);
    }

    public final String getTranslationKey() {
        if (vanillaOverride) {
            return "enchantmentreform.override." + key.namespace() + "." + key.value();
        }
        return "enchantment." + key.namespace() + "." + key.value();
    }

    public final EnchantmentKey getKey() {
        return key;
    }

    @Override
    public final String powerSourceId() {
        return key.asString();
    }

    public final boolean isVanillaOverride() {
        return vanillaOverride;
    }

    /** Raw configured name, before language and color processing. */
    public final String getName() {
        return name;
    }

    /** Player-localized name, including the rarity color wrapper. */
    public final String getLocalizedName(Player player) {
        return getColorPrefix() + CommonUtil.parseLang(player, name) + getColorSuffix();
    }

    /** Raw configured description, before variables and language processing. */
    public final String getDescription() {
        return description;
    }

    /** Description with level-dependent variables expanded, but not localized. */
    public final String getDescription(int level) {
        return EnchantmentDescription.render(description, getPowerVariables(), level);
    }

    /** Player-localized description with level-dependent variables expanded. */
    public final String getLocalizedDescription(Player player, int level) {
        String localized = CommonUtil.parseLang(player, description);
        return EnchantmentDescription.render(localized, getPowerVariables(), level);
    }

    public final String getRegistryDescription() {
        return registryDescription;
    }

    public final List<String> getDescriptionForDisplay(Player player, int level) {
        String localizedDescription = getLocalizedDescription(player, level);
        int wrapLength = ConfigManager.configManager == null
                ? 30
                : ConfigManager.configManager.getInt("enchantment-description.wrap-length", 30);
        return TextUtil.wrapKeepColor(localizedDescription, wrapLength);
    }

    public final String getRarity() {
        return rarity.id();
    }

    public final String getColorPrefix() {
        return rarity.colorPrefix();
    }

    public final String getColorSuffix() {
        return rarity.colorSuffix();
    }

    public final String getRarityDisplayName() {
        return rarity.displayName();
    }

    public final String getLocalizedRarityName(Player player) {
        String displayName = getRarityDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = "{lang:rarity-" + getRarity().toLowerCase(Locale.ROOT) + "}";
        }
        return CommonUtil.parseLang(player, displayName);
    }

    public final int getWeight() {
        return weight;
    }

    public final int getMaxLevel() {
        if (maxLevel > 0) {
            return maxLevel;
        }
        NamespacedKey namespacedKey = NamespacedKey.fromString(key.asString());
        Enchantment enchantment = namespacedKey == null ? null
                : Registry.ENCHANTMENT.get(namespacedKey);
        return enchantment == null ? 1 : enchantment.getMaxLevel();
    }

    public final int getAnvilCost() {
        return anvilCost;
    }

    public final ObjectCustomEnchantment.EnchantmentCost getMinimumCost() {
        return minimumCost;
    }

    public final ObjectCustomEnchantment.EnchantmentCost getMaximumCost() {
        return maximumCost;
    }

    public final String getSupportedItems() {
        return supportedItemKeys.isEmpty() ? null : supportedItemKeys.getFirst();
    }

    public final List<String> getSupportedItemKeys() {
        return supportedItemKeys;
    }

    public final String getPrimaryItems() {
        return primaryItemKeys.isEmpty() ? null : primaryItemKeys.getFirst();
    }

    public final List<String> getPrimaryItemKeys() {
        return primaryItemKeys;
    }

    public final List<String> getExclusiveWith() {
        return exclusiveWith;
    }

    public final List<String> getObtainingSources() {
        return obtainingSources;
    }

    public final ConfigurationSection getConditions() {
        return conditions;
    }

    /** Returns whether this enchantment may be used by the player in the supplied context. */
    public final boolean meetsConditions(Player player, PowerContext context) {
        if (conditions == null || conditionsByLevel != null) {
            return true;
        }
        PowerConditionsManager manager = PowerConditionsManager.powerConditions;
        return manager != null && manager.getPlayerConditions().matches(
                conditions, player, context);
    }

    /**
     * Resolves the highest usable runtime level at or below the level on the item.
     * A configured level whose conditions fail falls back to the next lower level;
     * zero means the enchantment contributes no active power.
     */
    public final int getEffectivePowerLevel(Player player, int itemLevel,
                                            ItemStack item, EquipmentSlot slot) {
        if (conditionsByLevel == null) {
            return itemLevel;
        }
        PowerConditionsManager manager = PowerConditionsManager.powerConditions;
        if (manager == null || player == null) {
            return 0;
        }
        for (int candidate = Math.min(itemLevel, getMaxLevel()); candidate >= 1; candidate--) {
            ConfigurationSection requirements = conditionsByLevel.getConfigurationSection(
                    String.valueOf(candidate));
            if (requirements == null) {
                continue;
            }
            PowerContext context = new PowerContext(
                    getPower(), candidate, null, null, item, slot, null, player);
            if (manager.matches(requirements, context, player)) {
                return candidate;
            }
        }
        return 0;
    }

    private ConfigurationSection resolveConditionsByLevel(ConfigurationSection configured) {
        if (configured == null) {
            return null;
        }
        boolean directConditions = false;
        boolean levelConditions = false;
        for (String key : configured.getKeys(false)) {
            ConfigurationSection child = configured.getConfigurationSection(key);
            if (child == null) {
                throw error("Each 'conditions' entry must be a section");
            }
            if (child.contains("type")) {
                directConditions = true;
                continue;
            }
            int level;
            try {
                level = Integer.parseInt(key);
            } catch (NumberFormatException exception) {
                throw error("A grouped 'conditions' key must be a positive enchantment level: "
                        + key);
            }
            if (level < 1 || level > getMaxLevel()) {
                throw error("A grouped 'conditions' level must be between 1 and "
                        + getMaxLevel() + ": " + level);
            }
            levelConditions = true;
        }
        if (directConditions && levelConditions) {
            throw error("'conditions' cannot mix named conditions with level groups");
        }
        return levelConditions ? configured : null;
    }

    /** Validates fields that can only be checked after every enchantment file has been read. */
    public final void validateConfiguration(Set<String> availableCustomEnchantments) {
        validateExclusiveEnchantments(availableCustomEnchantments);
        for (String source : obtainingSources) {
            if (!VALID_OBTAINING_SOURCES.contains(source)) {
                throw error("Unknown obtaining source '" + source + "'");
            }
        }
    }

    public final Map<String, Object> getNativeEffects() {
        return nativeEffects;
    }

    public final boolean hasNativeEffects() {
        return nativeEffectsConfigured;
    }

    public final boolean hasNativeEffect(String effectKey) {
        if (effectKey == null || effectKey.isBlank()) {
            return false;
        }
        String normalized = effectKey.indexOf(':') < 0
                ? "minecraft:" + effectKey
                : effectKey;
        normalized = normalized.toLowerCase(Locale.ROOT);
        for (String configuredKey : nativeEffects.keySet()) {
            String configured = configuredKey.indexOf(':') < 0
                    ? "minecraft:" + configuredKey
                    : configuredKey;
            if (configured.toLowerCase(Locale.ROOT).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> copyEffects(Object selected) {
        if (selected == null) return Map.of();
        if (selected instanceof ConfigurationSection section) {
            return Map.copyOf(copySection(section));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) selected).entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw error("effects map keys must be strings");
            }
            result.put(key, copyYamlValue(entry.getValue()));
        }
        return Map.copyOf(result);
    }

    private Map<String, Object> copySection(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String childKey : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(childKey);
            result.put(childKey, child == null
                    ? copyYamlValue(section.get(childKey))
                    : copySection(child));
        }
        return result;
    }

    private Object copyYamlValue(Object value) {
        if (value instanceof ConfigurationSection section) {
            return copySection(section);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String childKey)) {
                    throw error("effects map keys must be strings");
                }
                result.put(childKey, copyYamlValue(entry.getValue()));
            }
            return result;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::copyYamlValue).toList();
        }
        return value;
    }

    private void validateExclusiveEnchantments(Set<String> availableCustomEnchantments) {
        long tagCount = exclusiveWith.stream().filter(value -> value.startsWith("#")).count();
        if (tagCount > 0) {
            if (exclusiveWith.size() != 1) {
                throw error("'exclusive-with' cannot mix an enchantment tag with other entries");
            }
            parseReference(exclusiveWith.getFirst().substring(1), "exclusive enchantment tag");
            return;
        }

        for (String configuredReference : exclusiveWith) {
            EnchantmentKey reference = parseReference(
                    configuredReference, "exclusive enchantment");
            String referenceKey = reference.asString();
            if (referenceKey.equals(key.asString())) {
                throw error("'exclusive-with' cannot reference itself ('" + referenceKey + "')");
            }
            if (availableCustomEnchantments.contains(referenceKey)) {
                continue;
            }
            if ("enchantmentreform".equals(reference.namespace())) {
                throw error("'exclusive-with' references missing enchantment '"
                        + referenceKey + "'");
            }
            // External entries, including vanilla enchantments, are not available while
            // Paper is bootstrapping its registries. Looking them up here would initialize
            // org.bukkit.Registry too early and permanently poison that class in the JVM.
        }
    }

    private EnchantmentKey parseReference(String raw, String description) {
        try {
            return EnchantmentKey.parse(raw);
        } catch (IllegalArgumentException exception) {
            throw error("Invalid " + description + " '" + raw + "'");
        }
    }

    private String resolveRegistryDescription(YamlConfiguration registryLanguage) {
        String resolved = resolveLangPlaceholders(name, langKey -> registryLanguage == null
                ? null : registryLanguage.getString("override-lang." + langKey));
        return resolved == null || resolved.isBlank() ? fileName : resolved.strip();
    }

    private static String resolveLangPlaceholders(String text, Function<String, String> resolver) {
        if (text == null) {
            return null;
        }
        Matcher matcher = LANG_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String langKey = matcher.group(1);
            String replacement = resolver.apply(langKey);
            matcher.appendReplacement(builder,
                    Matcher.quoteReplacement(replacement == null || replacement.isBlank()
                            ? langKey : replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    @Override
    public String toString() {
        return key.asString();
    }
}
