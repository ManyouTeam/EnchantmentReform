package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ItemManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public final class ObjectCustomAttribute extends AbstractPowerSourceDefinition {

    private final String id;

    private final String name;

    private final String description;

    private final int minimumValue;

    private final int maximumValue;

    private final int defaultValue;

    private final int allocationMaximumValue;

    private final boolean skipPowerAtDefaultValue;

    private final boolean skipPowerAtZero;

    private final NamespacedKey persistentDataKey;

    private final NamespacedKey ignoreLimitsPersistentDataKey;

    private final NamespacedKey enabledPersistentDataKey;

    private final NamespacedKey effectiveLevelPersistentDataKey;

    private final NamespacedKey modifiersPersistentDataKey;

    private final Map<Player, Map<String, ObjectCustomAttributeModifier>> modifierCache =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<Player, Map<String, ObjectCustomAttributeModifier>> transientModifierCache =
            Collections.synchronizedMap(new WeakHashMap<>());

    public ObjectCustomAttribute(String id, YamlConfiguration config) {
        super(normalizeId(id), "attributes/" + normalizeId(id) + ".yml",
                config, true);
        this.id = normalizeId(id);
        this.name = configuredText("name", this.id);
        this.description = optionalString("description");
        this.minimumValue = rangedInt(
                "minimum-value", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        this.maximumValue = rangedInt(
                "maximum-value", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (minimumValue > maximumValue) {
            throw error("'minimum-value' must not be greater than 'maximum-value'");
        }
        this.defaultValue = rangedInt(
                "default-value", 0, minimumValue, maximumValue);
        this.allocationMaximumValue = rangedInt(
                "allocation.maximum-value", maximumValue, minimumValue, maximumValue);
        this.skipPowerAtDefaultValue = configuredBoolean(
                "skip-power-at-default-value", false);
        this.skipPowerAtZero = configuredBoolean("skip-power-at-zero", false);
        this.persistentDataKey = new NamespacedKey(
                EnchantmentReform.instance, "attribute/" + this.id);
        this.ignoreLimitsPersistentDataKey = new NamespacedKey(
                EnchantmentReform.instance, "attribute_ignore_limits/" + this.id);
        this.enabledPersistentDataKey = new NamespacedKey(
                EnchantmentReform.instance, "attribute_enabled/" + this.id);
        this.effectiveLevelPersistentDataKey = new NamespacedKey(
                EnchantmentReform.instance, "attribute_effective_level/" + this.id);
        this.modifiersPersistentDataKey = new NamespacedKey(
                EnchantmentReform.instance, "attribute_modifiers/" + this.id);
    }

    public String getId() {
        return id;
    }

    /** Raw configured name before resolving language placeholders. */
    public String getName() {
        return name;
    }

    public String getLocalizedName(Player player) {
        return CommonUtil.parseLang(player, name);
    }

    /** Raw configured description before resolving variables and language placeholders. */
    public String getDescription() {
        return description;
    }

    /** Description with variables expanded at the supplied attribute value. */
    public String getDescription(int value) {
        return EnchantmentDescription.render(description, getPowerVariables(), value);
    }

    /** Player-localized description with variables expanded at the supplied attribute value. */
    public String getLocalizedDescription(Player player, int value) {
        String localized = description == null ? null : CommonUtil.parseLang(player, description);
        return EnchantmentDescription.render(localized, getPowerVariables(), value);
    }

    public int getMinimumValue() {
        return minimumValue;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    /** Maximum persistent base value that may be purchased through allocation menus. */
    public int getAllocationMaximumValue() {
        return allocationMaximumValue;
    }

    public boolean isPowerSkippedAtDefaultValue() {
        return skipPowerAtDefaultValue;
    }

    public boolean isPowerSkippedAtZero() {
        return skipPowerAtZero;
    }

    public NamespacedKey getPersistentDataKey() {
        return persistentDataKey;
    }

    public boolean hasStoredValue(Player player) {
        return player != null && player.getPersistentDataContainer().has(
                persistentDataKey, PersistentDataType.INTEGER);
    }

    public int getBaseValue(Player player) {
        if (player == null) {
            return defaultValue;
        }
        Integer stored = player.getPersistentDataContainer().get(
                persistentDataKey, PersistentDataType.INTEGER);
        if (stored == null) {
            return defaultValue;
        }
        return ignoresLimits(player) ? stored : clamp(stored);
    }

    /** Returns the player-selected base level, capped by the unlocked base value. */
    public int getEffectiveBaseValue(Player player) {
        if (player == null) {
            return defaultValue;
        }
        Integer selected = player.getPersistentDataContainer().get(
                effectiveLevelPersistentDataKey, PersistentDataType.INTEGER);
        int unlocked = getBaseValue(player);
        return selected == null ? unlocked
                : Math.max(minimumValue, Math.min(unlocked, selected));
    }

    /** Stores and returns the selected base level, capped by the unlocked base value. */
    public int setEffectiveBaseValue(Player player, int value) {
        requirePlayer(player);
        int selected = Math.max(minimumValue, Math.min(getBaseValue(player), value));
        player.getPersistentDataContainer().set(
                effectiveLevelPersistentDataKey, PersistentDataType.INTEGER, selected);
        return selected;
    }

    /** Removes the explicit selection so the effective level follows the unlocked level. */
    public void resetEffectiveBaseValue(Player player) {
        if (player != null) {
            player.getPersistentDataContainer().remove(effectiveLevelPersistentDataKey);
        }
    }

    /** Whether this player's powers from the attribute are enabled. Defaults to true. */
    public boolean isEnabledForPlayer(Player player) {
        if (player == null) {
            return true;
        }
        Byte enabled = player.getPersistentDataContainer().get(
                enabledPersistentDataKey, PersistentDataType.BYTE);
        return enabled == null || enabled != 0;
    }

    public void setEnabledForPlayer(Player player, boolean enabled) {
        requirePlayer(player);
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (enabled) {
            container.remove(enabledPersistentDataKey);
        } else {
            container.set(enabledPersistentDataKey, PersistentDataType.BYTE, (byte) 0);
        }
    }

    /** Returns the effective final value after applying all modifiers. */
    public int getValue(Player player) {
        return getValueAtBase(player, getEffectiveBaseValue(player));
    }

    /** Calculates the final value with the supplied hypothetical base value. */
    public int getValueAtBase(Player player, int baseValue) {
        boolean ignoreLimits = ignoresLimits(player);
        if (!ignoreLimits) {
            baseValue = clamp(baseValue);
        }
        List<ObjectCustomAttributeModifier> modifiers = new ArrayList<>(
                getModifiers(player).values());
        double value = baseValue;
        for (ObjectCustomAttributeModifier modifier : modifiers) {
            if (modifier.operation() == ObjectCustomAttributeModifier.Operation.ADD_VALUE) {
                value += modifier.amount();
            }
        }
        for (ObjectCustomAttributeModifier modifier : modifiers) {
            if (modifier.operation()
                    == ObjectCustomAttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                value += baseValue * modifier.amount();
            }
        }
        for (ObjectCustomAttributeModifier modifier : modifiers) {
            if (modifier.operation()
                    == ObjectCustomAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                value *= 1.0D + modifier.amount();
            }
        }
        return ignoreLimits ? roundToInteger(value) : clampRounded(value);
    }

    public boolean isShownInAllocationMenu() {
        return config.getBoolean("allocation.show-in-menu", true);
    }

    public boolean isShownInAttributeInfoMenu() {
        return config.getBoolean("show-in-attribute-gui", true);
    }

    public boolean isShownInSkillMenu(String skillId) {
        return skillId != null && config.getBoolean("skill-menu.show", true);
    }

    public String getAllocationRequirementDisplay(Player player, int targetLevel) {
        if (ConfigManager.configManager != null
                && !ConfigManager.configManager.getBoolean("modules.skills", true)) {
            return "";
        }
        String levelPath = "allocation.levels." + targetLevel;
        String configured = config.getString(levelPath + ".requirement-display", "").strip();
        if (!configured.isEmpty()) {
            return CommonUtil.parseLang(player, configured);
        }
        ConfigurationSection conditions = config.getConfigurationSection(
                levelPath + ".conditions");
        if (conditions == null) {
            return "";
        }
        for (String id : conditions.getKeys(false)) {
            ConfigurationSection condition = conditions.getConfigurationSection(id);
            if (condition == null || !"skill_level".equalsIgnoreCase(
                    condition.getString("type", ""))) {
                continue;
            }
            String skillId = condition.getString("skill", "").strip();
            if (skillId.isEmpty()) {
                continue;
            }
            if (!condition.contains("min")) {
                continue;
            }
            int requiredLevel = condition.getInt("min");
            SkillDefinition skill = SkillManager.skillManager == null
                    ? null : SkillManager.skillManager.getSkill(skillId);
            String skillName = skill == null ? skillId : skill.name(player);
            return CommonUtil.parseLang(player,
                            "{lang:skill-allocation-skill-level-requirement}")
                    .replace("{skill}", skillName)
                    .replace("{level}", String.valueOf(requiredLevel));
        }
        return "";
    }

    /** Returns the total point price for every target base level in this increase. */
    public int getAllocationPrice(int currentBaseValue, int add) {
        if (add <= 0 || currentBaseValue >= allocationMaximumValue) {
            return 0;
        }
        long total = 0L;
        long lastTarget = Math.min(
                (long) allocationMaximumValue, (long) currentBaseValue + add);
        for (long target = (long) currentBaseValue + 1;
             target <= lastTarget; target++) {
            total += allocationPriceAt((int) target);
            if (total >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) total;
    }

    private int allocationPriceAt(int targetLevel) {
        Object selected = LevelValueResolver.resolve(config.get("allocation.price"), targetLevel);
        if (selected instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        if (selected != null) {
            try {
                return Math.max(0, Integer.parseInt(String.valueOf(selected).strip()));
            } catch (NumberFormatException ignored) {
                // Invalid values fall back to the documented default.
            }
        }
        return 1;
    }

    /** Checks common conditions and each exact target-level condition in the increase. */
    public boolean meetsAllocationConditions(Player player, int add) {
        if (player == null || add <= 0 || PowerConditionsManager.powerConditions == null) {
            return player != null && add > 0;
        }
        int current = getBaseValue(player);
        if ((long) current + add > allocationMaximumValue) {
            return false;
        }
        for (long target = (long) current + 1; target <= (long) current + add; target++) {
            TriggerData data = TriggerData.builder(player)
                    .source(player).skill(player).target(player).build();
            PowerContext context = new PowerContext(
                    null, (int) target, null, data, null, null, null);
            if (!PowerConditionsManager.powerConditions.matches(
                    config.getConfigurationSection("allocation.conditions"), context, player)
                    || !PowerConditionsManager.powerConditions.matches(
                    config.getConfigurationSection(
                            "allocation.levels." + target + ".conditions"), context, player)) {
                return false;
            }
        }
        return true;
    }

    /** Stores and returns the clamped value. */
    public int setValue(Player player, int value) {
        return setValue(player, value, false);
    }

    /** Stores a base value, optionally allowing an administrator to bypass configured limits. */
    public int setValue(Player player, int value, boolean ignoreLimits) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        boolean outsideLimits = value < minimumValue || value > maximumValue;
        int storedValue = ignoreLimits && outsideLimits ? value : clamp(value);
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(persistentDataKey, PersistentDataType.INTEGER, storedValue);
        if (ignoreLimits && outsideLimits) {
            container.set(ignoreLimitsPersistentDataKey, PersistentDataType.BYTE, (byte) 1);
            container.remove(effectiveLevelPersistentDataKey);
        } else {
            container.remove(ignoreLimitsPersistentDataKey);
        }
        Integer selected = container.get(
                effectiveLevelPersistentDataKey, PersistentDataType.INTEGER);
        if (selected != null && selected > storedValue) {
            container.set(effectiveLevelPersistentDataKey,
                    PersistentDataType.INTEGER, storedValue);
        }
        return storedValue;
    }

    public int addValue(Player player, int delta) {
        long updated = (long) getBaseValue(player) + delta;
        int bounded = updated < Integer.MIN_VALUE
                ? Integer.MIN_VALUE
                : updated > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) updated;
        return setValue(player, bounded);
    }

    public int resetValue(Player player) {
        return setValue(player, defaultValue);
    }

    public void clearStoredValue(Player player) {
        if (player != null) {
            PersistentDataContainer container = player.getPersistentDataContainer();
            container.remove(persistentDataKey);
            container.remove(ignoreLimitsPersistentDataKey);
            container.remove(effectiveLevelPersistentDataKey);
        }
    }

    public Map<String, ObjectCustomAttributeModifier> getModifiers(Player player) {
        Map<String, ObjectCustomAttributeModifier> persistent = getPersistentModifiers(player);
        Map<String, ObjectCustomAttributeModifier> transientModifiers =
                getTransientModifiers(player);
        Map<String, ObjectCustomAttributeModifier> itemModifiers =
                ItemManager.itemManager == null
                        ? Map.of()
                        : ItemManager.itemManager.getBaseAttributeModifiers(player, this);
        if (transientModifiers.isEmpty() && itemModifiers.isEmpty()) {
            return persistent;
        }
        Map<String, ObjectCustomAttributeModifier> combined =
                new LinkedHashMap<>(persistent);
        combined.putAll(transientModifiers);
        itemModifiers.forEach(combined::putIfAbsent);
        return Collections.unmodifiableMap(combined);
    }

    private Map<String, ObjectCustomAttributeModifier> getPersistentModifiers(Player player) {
        if (player == null) {
            return Map.of();
        }
        Map<String, ObjectCustomAttributeModifier> cached = modifierCache.get(player);
        if (cached != null) {
            return cached;
        }
        String serialized = player.getPersistentDataContainer().get(
                modifiersPersistentDataKey, PersistentDataType.STRING);
        if (serialized == null || serialized.isBlank()) {
            modifierCache.put(player, Map.of());
            return Map.of();
        }
        try {
            YamlConfiguration data = new YamlConfiguration();
            data.loadFromString(serialized);
            List<Map<?, ?>> entries = data.getMapList("modifiers");
            Map<String, ObjectCustomAttributeModifier> result = new LinkedHashMap<>();
            for (Map<?, ?> entry : entries) {
                Object idValue = entry.get("id");
                Object amountValue = entry.get("amount");
                Object operationValue = entry.get("operation");
                if (idValue == null || !(amountValue instanceof Number amount)
                        || operationValue == null) {
                    continue;
                }
                ObjectCustomAttributeModifier modifier = new ObjectCustomAttributeModifier(
                        String.valueOf(idValue), amount.doubleValue(),
                        ObjectCustomAttributeModifier.Operation.parse(
                                String.valueOf(operationValue)));
                result.put(modifier.id(), modifier);
            }
            Map<String, ObjectCustomAttributeModifier> immutable =
                    Collections.unmodifiableMap(new LinkedHashMap<>(result));
            modifierCache.put(player, immutable);
            return immutable;
        } catch (Exception ignored) {
            modifierCache.put(player, Map.of());
            return Map.of();
        }
    }

    /** Adds a modifier and returns false when its ID already exists. */
    public boolean addModifier(Player player, ObjectCustomAttributeModifier modifier) {
        requirePlayer(player);
        Map<String, ObjectCustomAttributeModifier> modifiers = new LinkedHashMap<>(
                getPersistentModifiers(player));
        if (getModifiers(player).containsKey(modifier.id())) {
            return false;
        }
        modifiers.put(modifier.id(), modifier);
        saveModifiers(player, modifiers);
        return true;
    }

    /** Creates or replaces a modifier and returns the resulting final value. */
    public int setModifier(Player player, ObjectCustomAttributeModifier modifier) {
        requirePlayer(player);
        Map<String, ObjectCustomAttributeModifier> modifiers = new LinkedHashMap<>(
                getPersistentModifiers(player));
        modifiers.put(modifier.id(), modifier);
        removeTransientModifier(player, modifier.id());
        saveModifiers(player, modifiers);
        return getValue(player);
    }

    public ObjectCustomAttributeModifier getModifier(Player player, String modifierId) {
        return getModifiers(player).get(ObjectCustomAttributeModifier.normalizeId(modifierId));
    }

    public boolean removeModifier(Player player, String modifierId) {
        requirePlayer(player);
        String normalizedId = ObjectCustomAttributeModifier.normalizeId(modifierId);
        Map<String, ObjectCustomAttributeModifier> modifiers = new LinkedHashMap<>(
                getPersistentModifiers(player));
        boolean removed = modifiers.remove(normalizedId) != null;
        if (removed) {
            saveModifiers(player, modifiers);
        }
        return removeTransientModifier(player, normalizedId) || removed;
    }

    public ObjectCustomAttributeModifier getTransientModifier(Player player,
                                                               String modifierId) {
        return getTransientModifiers(player).get(
                ObjectCustomAttributeModifier.normalizeId(modifierId));
    }

    public int setTransientModifier(Player player,
                                    ObjectCustomAttributeModifier modifier) {
        requirePlayer(player);
        Map<String, ObjectCustomAttributeModifier> modifiers = new LinkedHashMap<>(
                getTransientModifiers(player));
        modifiers.put(modifier.id(), modifier);
        transientModifierCache.put(player, Collections.unmodifiableMap(modifiers));
        return getValue(player);
    }

    public boolean removeTransientModifier(Player player, String modifierId) {
        if (player == null) {
            return false;
        }
        String normalizedId = ObjectCustomAttributeModifier.normalizeId(modifierId);
        Map<String, ObjectCustomAttributeModifier> modifiers = new LinkedHashMap<>(
                getTransientModifiers(player));
        if (modifiers.remove(normalizedId) == null) {
            return false;
        }
        transientModifierCache.put(player, modifiers.isEmpty()
                ? Map.of() : Collections.unmodifiableMap(modifiers));
        return true;
    }

    public void clearModifiers(Player player) {
        if (player != null) {
            player.getPersistentDataContainer().remove(modifiersPersistentDataKey);
            modifierCache.put(player, Map.of());
            transientModifierCache.put(player, Map.of());
        }
    }

    public boolean shouldExecutePower(Player player) {
        if (!isEnabledForPlayer(player)) {
            return false;
        }
        int value = getValue(player);
        return !(skipPowerAtDefaultValue && value == defaultValue)
                && !(skipPowerAtZero && value == 0);
    }

    @Override
    public String powerSourceId() {
        return "custom_attribute:" + id;
    }

    private int clamp(int value) {
        return Math.max(minimumValue, Math.min(maximumValue, value));
    }

    private int clampRounded(double value) {
        if (Double.isNaN(value)) {
            return defaultValue;
        }
        if (value <= minimumValue) {
            return minimumValue;
        }
        if (value >= maximumValue) {
            return maximumValue;
        }
        long rounded = Math.round(value);
        return clamp(rounded < Integer.MIN_VALUE
                ? Integer.MIN_VALUE
                : rounded > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rounded);
    }

    private int roundToInteger(double value) {
        if (Double.isNaN(value)) {
            return defaultValue;
        }
        if (value <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(value);
    }

    private boolean ignoresLimits(Player player) {
        if (player == null) {
            return false;
        }
        Byte ignored = player.getPersistentDataContainer().get(
                ignoreLimitsPersistentDataKey, PersistentDataType.BYTE);
        return ignored != null && ignored != 0;
    }

    private void saveModifiers(Player player,
                               Map<String, ObjectCustomAttributeModifier> modifiers) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (modifiers.isEmpty()) {
            container.remove(modifiersPersistentDataKey);
            modifierCache.put(player, Map.of());
            return;
        }
        YamlConfiguration data = new YamlConfiguration();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (ObjectCustomAttributeModifier modifier : modifiers.values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", modifier.id());
            entry.put("amount", modifier.amount());
            entry.put("operation", modifier.operation().name());
            entries.add(entry);
        }
        data.set("modifiers", entries);
        container.set(modifiersPersistentDataKey, PersistentDataType.STRING,
                data.saveToString());
        modifierCache.put(player, Collections.unmodifiableMap(
                new LinkedHashMap<>(modifiers)));
    }

    private Map<String, ObjectCustomAttributeModifier> getTransientModifiers(Player player) {
        if (player == null) {
            return Map.of();
        }
        return transientModifierCache.getOrDefault(player, Map.of());
    }

    private void requirePlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
    }

    private static String normalizeId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Custom attribute id cannot be null");
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Custom attribute id may only contain lowercase letters, numbers, '_' and '-'");
        }
        return normalized;
    }
}
