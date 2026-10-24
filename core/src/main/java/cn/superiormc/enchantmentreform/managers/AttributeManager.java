package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttributeModifier;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AttributeManager extends AbstractManager {

    public static AttributeManager attributeManager;

    public AttributeManager() {
        attributeManager = this;
    }

    public void loadAttributes() {
        ConfigManager.configManager.reloadAttributeConfigs();
    }

    public void initializePowers() {
        for (ObjectCustomAttribute attribute : ConfigManager.configManager.attributeConfigs.values()) {
            attribute.initializePower();
            if (PowerManager.powerManager != null) {
                PowerManager.powerManager.registerPower(attribute.getPower());
            }
        }
    }

    public ObjectCustomAttribute getAttribute(String id) {
        return id == null ? null : ConfigManager.configManager.getAttribute(id);
    }

    /** Resolves bare IDs and the custom_attribute:/enchantmentreform: forms. */
    public ObjectCustomAttribute resolveAttribute(String configuredId) {
        if (configuredId == null) {
            return null;
        }
        String normalized = configuredId.strip().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("custom_attribute:")) {
            normalized = normalized.substring("custom_attribute:".length());
        } else if (normalized.startsWith("enchantmentreform:")) {
            normalized = normalized.substring("enchantmentreform:".length());
        }
        return getAttribute(normalized);
    }

    public Collection<ObjectCustomAttribute> getAttributes() {
        return java.util.List.copyOf(ConfigManager.configManager.attributeConfigs.values());
    }

    public Map<String, ObjectCustomAttribute> getAttributeMap() {
        return Map.copyOf(ConfigManager.configManager.attributeConfigs);
    }

    public int getValue(Player player, String id) {
        return requireAttribute(id).getValue(player);
    }

    public int getBaseValue(Player player, String id) {
        return requireAttribute(id).getBaseValue(player);
    }

    public int setValue(Player player, String id, int value) {
        return requireAttribute(id).setValue(player, value);
    }

    public int addValue(Player player, String id, int delta) {
        return requireAttribute(id).addValue(player, delta);
    }

    public int resetValue(Player player, String id) {
        return requireAttribute(id).resetValue(player);
    }

    public Map<String, ObjectCustomAttributeModifier> getModifiers(Player player, String id) {
        return requireAttribute(id).getModifiers(player);
    }

    public boolean addModifier(Player player,
                               String id,
                               ObjectCustomAttributeModifier modifier) {
        return requireAttribute(id).addModifier(player, modifier);
    }

    public int setModifier(Player player,
                           String id,
                           ObjectCustomAttributeModifier modifier) {
        return requireAttribute(id).setModifier(player, modifier);
    }

    public boolean removeModifier(Player player, String id, String modifierId) {
        return requireAttribute(id).removeModifier(player, modifierId);
    }

    private ObjectCustomAttribute requireAttribute(String id) {
        ObjectCustomAttribute attribute = resolveAttribute(id);
        if (attribute == null) {
            throw new IllegalArgumentException("Unknown custom attribute: " + id);
        }
        return attribute;
    }

}
