package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttributeModifier;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AttributeManager extends AbstractManager {

    public static AttributeManager attributeManager;

    private final Map<String, ObjectCustomAttribute> attributes = new LinkedHashMap<>();

    public AttributeManager() {
        attributeManager = this;
        loadAttributes();
    }

    public void loadAttributes() {
        attributes.clear();
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "attributes");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not create custom attribute directory: " + directory);
            return;
        }
        for (File file : findAttributeFiles(directory)) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = InitManager.loadConfiguration(file, "attributes/" + file.getName().toLowerCase(Locale.ROOT));
                ObjectCustomAttribute attribute = new ObjectCustomAttribute(id, config);
                if (!attribute.isEnabled()) {
                    continue;
                }
                if (attributes.putIfAbsent(id, attribute) != null) {
                    throw new IllegalArgumentException("Duplicate custom attribute id: " + id);
                }
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded custom attribute: " + id + ".yml!");
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Failed to load custom attribute " + file.getName() + ": " + throwable.getMessage());
            }
        }
    }

    private List<File> findAttributeFiles(File directory) {
        List<File> result = new ArrayList<>();
        File[] entries = directory.listFiles();
        if (entries == null) {
            return result;
        }
        for (File entry : entries) {
            if (entry.isFile()
                    && entry.getName().toLowerCase(Locale.ROOT).endsWith(".yml")) {
                result.add(entry);
            }
        }
        result.sort(Comparator.comparing(File::getName));
        return result;
    }

    public void initializePowers() {
        for (ObjectCustomAttribute attribute : attributes.values()) {
            attribute.initializePower();
            if (PowerManager.powerManager != null) {
                PowerManager.powerManager.registerPower(attribute.getPower());
            }
        }
    }

    public ObjectCustomAttribute getAttribute(String id) {
        return id == null ? null : attributes.get(id.toLowerCase(Locale.ROOT));
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
        return java.util.List.copyOf(attributes.values());
    }

    public Map<String, ObjectCustomAttribute> getAttributeMap() {
        return Map.copyOf(attributes);
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
