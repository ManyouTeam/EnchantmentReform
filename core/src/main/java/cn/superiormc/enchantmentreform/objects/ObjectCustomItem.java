package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ChangesManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.AttributeUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ObjectCustomItem extends AbstractPowerSourceDefinition {

    private static final NamespacedKey CUSTOM_ITEM_KEY =
            new NamespacedKey(EnchantmentReform.instance, "custom_item_id");

    private final String id;

    private final ConfigurationSection baseItem;

    private final List<BaseAttribute> baseAttributes;

    private final boolean useBaseAttackModifierKeys;

    private final ConfigurationSection changeItem;

    public ObjectCustomItem(String id, YamlConfiguration config) {
        super(id.toLowerCase(Locale.ROOT),
                "items/" + id.toLowerCase(Locale.ROOT) + ".yml", config, false);
        this.id = id.toLowerCase(Locale.ROOT);
        this.baseItem = config.getConfigurationSection("base-item");
        if (baseItem == null) {
            throw error("Missing required section 'base-item'");
        }
        this.useBaseAttackModifierKeys = configuredBoolean(
                "use-base-attack-modifier-keys", false);
        this.baseAttributes = loadBaseAttributes();
        if (config.contains("change-item")
                && !config.isConfigurationSection("change-item")) {
            throw error("'change-item' must be a section");
        }
        this.changeItem = config.getConfigurationSection("change-item");
        if (getPowers() == null) {
            throw error("Missing required section 'powers'");
        }
    }

    public ItemStack build(Player player, String... args) {
        Map<BaseAttribute, Double> resolvedAttributes = resolveBaseAttributes(player, args);
        ItemStack item = BuildItem.buildItemStack(
                player, baseItem, attributeLoreArguments(resolvedAttributes, args));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw error("base-item must produce an item with metadata");
        }
        applyVanillaBaseAttributes(item, meta, resolvedAttributes);
        item.setItemMeta(meta);
        writeInternalData(item, resolvedAttributes);
        item = applyChangeItem(item, player, resolvedAttributes, args);
        writeInternalData(item, resolvedAttributes);
        return item;
    }

    public String getId() {
        return id;
    }

    /** Returns custom-attribute modifiers supplied while this item occupies the given slot. */
    public Map<String, ObjectCustomAttributeModifier> getCustomAttributeModifiers(
            ObjectCustomAttribute attribute, ItemStack item, EquipmentSlot slot) {
        if (attribute == null || item == null || slot == null
                || AttributeManager.attributeManager == null) {
            return Map.of();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Map.of();
        }
        Map<String, ObjectCustomAttributeModifier> result = new LinkedHashMap<>();
        for (BaseAttribute configured : baseAttributes) {
            ObjectCustomAttribute target = AttributeManager.attributeManager.resolveAttribute(
                    configured.attributeId());
            if (target != attribute
                    || !AttributeUtil.getAutomaticEquipmentSlotGroup(item).test(slot)) {
                continue;
            }
            Double amount = meta.getPersistentDataContainer().get(
                    attributeValueKey(configured), PersistentDataType.DOUBLE);
            if (amount == null || !Double.isFinite(amount)) {
                continue;
            }
            ObjectCustomAttributeModifier modifier = new ObjectCustomAttributeModifier(
                    modifierKey(configured).toString(), amount,
                    ObjectCustomAttributeModifier.Operation.ADD_VALUE);
            result.putIfAbsent(modifier.id(), modifier);
        }
        return Map.copyOf(result);
    }

    @Override
    public String powerSourceId() {
        return "custom_item:" + id;
    }

    public static NamespacedKey customItemKey() {
        return CUSTOM_ITEM_KEY;
    }

    private List<BaseAttribute> loadBaseAttributes() {
        if (config.contains("base-attributes")
                && !config.isConfigurationSection("base-attributes")) {
            throw error("'base-attributes' must be a section");
        }
        ConfigurationSection section = config.getConfigurationSection("base-attributes");
        if (section == null) {
            return List.of();
        }
        List<BaseAttribute> result = new ArrayList<>();
        for (String attributeId : section.getKeys(false)) {
            String normalizedAttributeId = attributeId.strip().toLowerCase(Locale.ROOT);
            if (CommonUtil.parseNamespacedKey(normalizedAttributeId) == null) {
                throw error("Invalid attribute key '" + attributeId
                        + "' in 'base-attributes'");
            }
            Object configuredValue = section.getValues(false).get(attributeId);
            if (!(configuredValue instanceof Number)
                    && (!(configuredValue instanceof String expression)
                    || expression.isBlank())) {
                throw error("'base-attributes." + attributeId
                        + "' must be a number or mathematical expression");
            }
            result.add(new BaseAttribute(
                    normalizedAttributeId, String.valueOf(configuredValue).strip()));
        }
        return List.copyOf(result);
    }

    private Map<BaseAttribute, Double> resolveBaseAttributes(Player player, String... args) {
        PowerContext context = new PowerContext(
                null, 1, null, null, null, null, null, player);
        Map<BaseAttribute, Double> result = new LinkedHashMap<>();
        for (BaseAttribute configured : baseAttributes) {
            YamlConfiguration valueConfig = new YamlConfiguration();
            valueConfig.set("value", configured.expression());
            double value = new BaseAttributeValueResolver(
                    valueConfig, getPowerVariables()).getDouble(
                    "value", 0.0D, context, args);
            if (!Double.isFinite(value)) {
                throw error("'base-attributes." + configured.attributeId()
                        + "' produced a non-finite value");
            }
            result.put(configured, value);
        }
        return Collections.unmodifiableMap(result);
    }

    private String[] attributeLoreArguments(Map<BaseAttribute, Double> values,
                                            String... originalArguments) {
        int originalLength = originalArguments == null ? 0 : originalArguments.length;
        String[] result = new String[values.size() * 2 + originalLength];
        int index = 0;
        for (Map.Entry<BaseAttribute, Double> entry : values.entrySet()) {
            result[index++] = "attribute:" + entry.getKey().attributeId();
            result[index++] = EnchantmentDescription.renderVariable(
                    String.valueOf(entry.getValue()), 1);
        }
        if (originalLength > 0) {
            System.arraycopy(originalArguments, 0, result, index, originalLength);
        }
        return result;
    }

    private void applyVanillaBaseAttributes(ItemStack item, ItemMeta meta,
                                            Map<BaseAttribute, Double> values) {
        for (Map.Entry<BaseAttribute, Double> entry : values.entrySet()) {
            BaseAttribute configured = entry.getKey();
            if (AttributeManager.attributeManager != null
                    && AttributeManager.attributeManager.resolveAttribute(
                    configured.attributeId()) != null) {
                continue;
            }
            NamespacedKey key = CommonUtil.parseNamespacedKey(configured.attributeId());
            Attribute attribute = key == null ? null : Registry.ATTRIBUTE.get(key);
            if (attribute == null) {
                continue;
            }
            AttributeModifier modifier = new AttributeModifier(
                    vanillaModifierKey(configured, key),
                    vanillaModifierAmount(key, entry.getValue()),
                    AttributeModifier.Operation.ADD_NUMBER,
                    AttributeUtil.getAutomaticEquipmentSlotGroup(item));
            AttributeUtil.copyDefaultAttributeModifiers(meta, item);
            meta.removeAttributeModifier(attribute);
            meta.addAttributeModifier(attribute, modifier);
        }
    }

    private ItemStack applyChangeItem(ItemStack item, Player player,
                                      Map<BaseAttribute, Double> values,
                                      String... args) {
        if (changeItem == null) {
            return item;
        }
        if (ChangesManager.changesManager == null) {
            throw error("Change Item manager is not initialized");
        }
        PowerContext context = new PowerContext(
                getPower(), 1, null, null, item, null, null, player);
        ObjectSingleChange singleChange = new ObjectSingleChange(
                changeItem, item, player, context,
                attributeLoreArguments(values, args));
        ItemStack changed = ChangesManager.changesManager.setChange(singleChange);
        if (changed == null || changed.getType().isAir() || changed.getItemMeta() == null) {
            throw error("'change-item' must produce a non-air item with metadata");
        }
        return changed;
    }

    private void writeInternalData(ItemStack item,
                                   Map<BaseAttribute, Double> values) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw error("Custom item must have metadata");
        }
        values.forEach((attribute, value) ->
                meta.getPersistentDataContainer().set(
                        attributeValueKey(attribute), PersistentDataType.DOUBLE, value));
        meta.getPersistentDataContainer().set(
                customItemKey(), PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    private NamespacedKey vanillaModifierKey(BaseAttribute configured,
                                              NamespacedKey attributeKey) {
        if (useBaseAttackModifierKeys
                && NamespacedKey.MINECRAFT.equals(attributeKey.getNamespace())) {
            return switch (attributeKey.getKey()) {
                case "attack_damage" -> NamespacedKey.minecraft("base_attack_damage");
                case "attack_speed" -> NamespacedKey.minecraft("base_attack_speed");
                default -> modifierKey(configured);
            };
        }
        return modifierKey(configured);
    }

    private double vanillaModifierAmount(NamespacedKey attributeKey,
                                         double configuredValue) {
        if (!useBaseAttackModifierKeys
                || !NamespacedKey.MINECRAFT.equals(attributeKey.getNamespace())) {
            return configuredValue;
        }
        return switch (attributeKey.getKey()) {
            case "attack_damage" -> configuredValue - 1.0D;
            case "attack_speed" -> configuredValue - 4.0D;
            default -> configuredValue;
        };
    }

    private NamespacedKey modifierKey(BaseAttribute configured) {
        String key = ("base_attribute/" + id + "/" + configured.attributeId())
                .replace(':', '/').replaceAll("[^a-z0-9/._-]", "_");
        return new NamespacedKey(EnchantmentReform.instance, key);
    }

    private NamespacedKey attributeValueKey(BaseAttribute configured) {
        String key = ("base_attribute_value/" + id + "/" + configured.attributeId())
                .replace(':', '/').replaceAll("[^a-z0-9/._-]", "_");
        return new NamespacedKey(EnchantmentReform.instance, key);
    }

    private record BaseAttribute(String attributeId, String expression) {
    }

    private static final class BaseAttributeValueResolver
            extends AbstractConfiguredSection<PowerContext> {

        private final PowerVariables variables;

        private BaseAttributeValueResolver(ConfigurationSection section,
                                           PowerVariables variables) {
            super(section);
            this.variables = variables;
        }

        @Override
        protected String resolvePowerVariable(String name, PowerContext context, int level) {
            return variables.resolve(name, level);
        }
    }

}
