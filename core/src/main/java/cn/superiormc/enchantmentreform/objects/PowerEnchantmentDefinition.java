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
import java.util.ArrayList;
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
                + fileName + ".yml", normalizeConfiguration(fileName, config, vanillaOverride), vanillaOverride);
        this.vanillaOverride = vanillaOverride;

        String defaultKey = vanillaOverride ? "minecraft:" + fileName : fileName;
        this.key = EnchantmentKey.parse(config.getString("key", defaultKey));

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
            String supportedItems = optionalString("supported-items");
            this.supportedItemKeys = supportedItems == null ? List.of() : List.of(supportedItems);
            String primaryItems = optionalString("primary-items");
            this.primaryItemKeys = primaryItems == null ? List.of() : List.of(primaryItems);
        }
        this.exclusiveWith = stringOrList("exclusive-with").stream()
                .filter(value -> !value.equals(key.asString()))
                .toList();

        Object selectedEffects = NativeEffectsSelector.select(config, NativeEffectsSelector::currentVersion);
        Map<String, Object> resolvedEffects = copyEffects(selectedEffects);
        if (!resolvedEffects.isEmpty() && config.getBoolean("auto-migrate-effects", true)) {
            resolvedEffects = Map.copyOf(NativeEffectsMigrator.migrate(
                    resolvedEffects, NativeEffectsSelector.currentVersion()));
        }
        this.nativeEffectsConfigured = selectedEffects != null;
        this.nativeEffects = resolvedEffects;

        ConfigurationSection raritySection = rarity.settings();
        this.weight = registryInt("weight", raritySection, 10, 1, 1024);
        this.maxLevel = config.contains("max-level")
                ? rangedInt("max-level", 1, 1, 255)
                : vanillaOverride ? 0 : 1;
        this.anvilCost = registryInt("anvil-cost", raritySection, 1, 0, Integer.MAX_VALUE);
        this.minimumCost = new ObjectCustomEnchantment.EnchantmentCost(
                registryInt("minimum-cost.base", raritySection, 1, 0, Integer.MAX_VALUE),
                registryInt("minimum-cost.per-level", raritySection, 0, 0, Integer.MAX_VALUE));
        this.maximumCost = new ObjectCustomEnchantment.EnchantmentCost(
                registryInt("maximum-cost.base", raritySection, 1, 0, Integer.MAX_VALUE),
                registryInt("maximum-cost.per-level", raritySection, 0, 0, Integer.MAX_VALUE));

        this.obtainingSources = normalizeList(stringOrList("obtaining-sources")).stream()
                .filter(VALID_OBTAINING_SOURCES::contains)
                .toList();
        this.conditions = config.getConfigurationSection("conditions");
        this.conditionsByLevel = resolveConditionsByLevel(this.conditions);
    }

    private static YamlConfiguration normalizeConfiguration(
            String fileName, YamlConfiguration config, boolean vanillaOverride) {
        for (String path : List.of("enabled", "allow-duplicate", "auto-migrate-effects")) {
            if (config.contains(path) && !config.isBoolean(path)) {
                config.set(path, null);
            }
        }
        for (String path : List.of("variables", "powers", "conditions", "minimum-cost", "maximum-cost")) {
            if (config.contains(path) && !config.isConfigurationSection(path)) {
                config.set(path, null);
            }
        }
        for (String path : List.of("name", "description", "rarity")) {
            if (config.contains(path) && !config.isString(path)) {
                config.set(path, null);
            }
        }
        String defaultId = fileName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        if (defaultId.isBlank()) {
            defaultId = "unnamed";
        }
        String namespace = vanillaOverride ? "minecraft" : "enchantmentreform";
        String configuredKey = normalizeReference(config.get("key"), namespace, false);
        if (configuredKey == null || vanillaOverride && !configuredKey.startsWith("minecraft:")) {
            configuredKey = namespace + ":" + defaultId;
        }
        config.set("key", configuredKey);
        for (String path : List.of("supported-items", "primary-items", "exclusive-with", "obtaining-sources")) {
            Object raw = config.get(path);
            List<?> entries = raw instanceof List<?> list ? list : raw == null ? List.of() : List.of(raw);
            List<String> valid = new ArrayList<>();
            for (Object entry : entries) {
                if ("exclusive-with".equals(path) && entry instanceof String text
                        && "*".equals(text.strip())) {
                    valid.add("*");
                    continue;
                }
                String reference = "obtaining-sources".equals(path)
                        ? entry instanceof String text && !text.isBlank() ? text.strip() : null
                        : normalizeReference(entry, "minecraft", true);
                if (reference != null) {
                    valid.add(reference);
                }
            }
            if (raw instanceof String && !valid.isEmpty()) {
                config.set(path, valid.getFirst());
            } else if (raw instanceof List<?> && (!valid.isEmpty() || entries.isEmpty())) {
                config.set(path, valid);
            } else {
                config.set(path, null);
            }
            if (!vanillaOverride && !"exclusive-with".equals(path)
                    && !"obtaining-sources".equals(path) && config.isList(path)) {
                config.set(path, valid.isEmpty() ? null : valid.getFirst());
            }
        }
        for (String path : List.of("execution-priority", "weight", "max-level", "anvil-cost",
                "minimum-cost.base", "minimum-cost.per-level", "maximum-cost.base", "maximum-cost.per-level")) {
            int minimum = "weight".equals(path) || "max-level".equals(path) ? 1
                    : "execution-priority".equals(path) ? Integer.MIN_VALUE : 0;
            int maximum = "weight".equals(path) ? 1024 : "max-level".equals(path) ? 255 : Integer.MAX_VALUE;
            if (config.contains(path) && (!config.isInt(path)
                    || config.getInt(path) < minimum || config.getInt(path) > maximum)) {
                config.set(path, null);
            }
        }
        Object rawSlots = config.get("active-slots");
        List<?> slots = rawSlots instanceof List<?> list ? list
                : rawSlots instanceof String text ? List.of(text) : List.of();
        List<String> validSlots = new ArrayList<>();
        for (Object value : slots) {
            if (!(value instanceof String text)) {
                continue;
            }
            String slot = text.strip().toUpperCase(Locale.ROOT).replace('-', '_');
            slot = switch (slot) {
                case "MAINHAND", "MAIN_HAND" -> "HAND";
                case "OFFHAND" -> "OFF_HAND";
                default -> slot;
            };
            if (List.of("ANY", "ARMOR", "HAND", "OFF_HAND", "HEAD", "CHEST", "LEGS", "FEET").contains(slot)) {
                validSlots.add(slot);
            }
        }
        if (!validSlots.isEmpty()) {
            config.set("active-slots", validSlots);
        } else if (!vanillaOverride) {
            config.set("active-slots", List.of("ANY"));
        } else if (!(rawSlots instanceof List<?> list && list.isEmpty())) {
            config.set("active-slots", null);
        }
        return config;
    }

    private static String normalizeReference(Object value, String defaultNamespace, boolean allowTag) {
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        String reference = text.strip().toLowerCase(Locale.ROOT);
        boolean tag = allowTag && reference.startsWith("#");
        if (tag) {
            reference = reference.substring(1);
        }
        if (!reference.contains(":")) {
            reference = defaultNamespace + ":" + reference;
        }
        if (!reference.matches("[a-z0-9._-]+:[a-z0-9/._-]+")) {
            return null;
        }
        return (tag ? "#" : "") + reference;
    }

    private int registryInt(String path, ConfigurationSection inherited, int fallback, int minimum, int maximum) {
        int value = inheritedInt(path, inherited, fallback);
        return value < minimum || value > maximum ? fallback : value;
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

    public final int getOrder() {
        return config.getInt("order", 1000);
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
        if (manager == null) {
            return false;
        }
        return manager.getPlayerConditions().matches(conditions, player, context);
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
                configured.set(key, null);
                continue;
            }
            if (child.contains("type")) {
                directConditions = true;
                continue;
            }
            if (!key.matches("[0-9]{1,9}")) {
                configured.set(key, null);
                continue;
            }
            int level = Integer.parseInt(key);
            if (level < 1 || level > getMaxLevel()) {
                configured.set(key, null);
                continue;
            }
            levelConditions = true;
        }
        if (directConditions && levelConditions) {
            for (String key : List.copyOf(configured.getKeys(false))) {
                ConfigurationSection child = configured.getConfigurationSection(key);
                if (child != null && !child.contains("type")) {
                    configured.set(key, null);
                }
            }
            return null;
        }
        return levelConditions ? configured : null;
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
        if (selected == null) {
            return Map.of();
        }
        if (selected instanceof ConfigurationSection section) {
            return Map.copyOf(copySection(section));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) selected).entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                continue;
            }
            if (entry.getValue() == null) {
                continue;
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
                    continue;
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
