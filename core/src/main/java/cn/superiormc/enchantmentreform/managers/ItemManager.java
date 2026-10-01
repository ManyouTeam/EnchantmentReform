package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.methods.DebuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectCustomItem;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttributeModifier;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemManager extends AbstractManager {

    private static final List<EquipmentSlot> ATTRIBUTE_SLOTS = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);

    private final Map<String, ObjectCustomItem> customItems = new LinkedHashMap<>();

    public static ItemManager itemManager;

    public ItemManager() {
        itemManager = this;
        initSavedItems();
    }

    public void initSavedItems() {
        customItems.clear();
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "items");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not create custom item directory: " + directory);
            return;
        }
        File[] files = directory.listFiles((ignored, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(file);
                ObjectCustomItem customItem = new ObjectCustomItem(id, config);
                if (!customItem.isEnabled()) {
                    continue;
                }
                if (customItems.putIfAbsent(id, customItem) != null) {
                    throw new IllegalArgumentException("Duplicate custom item id: " + id);
                }
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded custom item: " + id + ".yml!");
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage(
                        "§cError: Failed to load custom item " + file.getName() + ": "
                                + throwable.getMessage());
            }
        }
    }

    public void initializePowers() {
        for (ObjectCustomItem customItem : customItems.values()) {
            customItem.initializePower();
            if (PowerManager.powerManager != null) {
                PowerManager.powerManager.registerPower(customItem.getPower());
            }
        }
    }

    public boolean saveMainHandItem(Player player, String key) {
        YamlConfiguration config = newDefinition();
        ItemStack item = player.getInventory().getItemInMainHand();
        config.getConfigurationSection("base-item").set(
                "item", EnchantmentReform.methodUtil.makeItemToObject(item));
        config.getConfigurationSection("base-item").set("amount", item.getAmount());
        return saveDefinition(key, config);
    }

    public boolean saveMainHandItemFormat(Player player, String key) {
        YamlConfiguration config = newDefinition();
        DebuildItem.debuildItem(player.getInventory().getItemInMainHand(),
                config.getConfigurationSection("base-item"));
        return saveDefinition(key, config);
    }

    public ItemStack getItemByKey(Player player, String key) {
        ObjectCustomItem customItem = getCustomItem(key);
        if (customItem == null) {
            return null;
        }
        try {
            return customItem.build(player);
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Failed to build custom item " + customItem.getId() + ": "
                            + throwable.getMessage());
            return null;
        }
    }

    public ObjectCustomItem getCustomItem(String key) {
        return key == null ? null : customItems.get(key.toLowerCase(Locale.ROOT));
    }

    public ObjectCustomItem getCustomItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String id = meta.getPersistentDataContainer().get(
                ObjectCustomItem.customItemKey(), PersistentDataType.STRING);
        return getCustomItem(id);
    }

    public Collection<String> getItemKeys() {
        return List.copyOf(customItems.keySet());
    }

    public Map<String, ObjectCustomItem> getCustomItems() {
        return Map.copyOf(customItems);
    }

    /** Collects non-persistent custom-attribute modifiers from equipped custom items. */
    public Map<String, ObjectCustomAttributeModifier> getBaseAttributeModifiers(
            Player player, ObjectCustomAttribute attribute) {
        if (player == null || attribute == null) {
            return Map.of();
        }
        PlayerInventory inventory = player.getInventory();
        Map<String, ObjectCustomAttributeModifier> result = new LinkedHashMap<>();
        for (EquipmentSlot slot : ATTRIBUTE_SLOTS) {
            ItemStack item = switch (slot) {
                case HEAD -> inventory.getHelmet();
                case CHEST -> inventory.getChestplate();
                case LEGS -> inventory.getLeggings();
                case FEET -> inventory.getBoots();
                case HAND -> inventory.getItemInMainHand();
                case OFF_HAND -> inventory.getItemInOffHand();
                default -> null;
            };
            ObjectCustomItem customItem = getCustomItem(item);
            if (customItem == null) {
                continue;
            }
            customItem.getCustomAttributeModifiers(attribute, item, slot)
                    .forEach(result::putIfAbsent);
        }
        return Map.copyOf(result);
    }

    private YamlConfiguration newDefinition() {
        YamlConfiguration config = new YamlConfiguration();
        config.createSection("base-item");
        config.set("active-slots", List.of("HAND"));
        config.createSection("powers");
        return config;
    }

    private boolean saveDefinition(String key, YamlConfiguration config) {
        final String id;
        final ObjectCustomItem customItem;
        try {
            id = normalizeNewId(key);
            customItem = new ObjectCustomItem(id, config);
            customItem.initializePower();
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Failed to save custom item " + key + ": " + throwable.getMessage());
            return false;
        }

        File directory = new File(EnchantmentReform.instance.getDataFolder(), "items");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Could not create custom item directory: " + directory);
            return false;
        }

        customItems.put(id, customItem);
        if (PowerManager.powerManager != null) {
            PowerManager.powerManager.registerPower(customItem.getPower());
        }
        if (TriggerManager.triggerManager != null) {
            TriggerManager.triggerManager.activeEnchantments().clearAll();
        }
        String yaml = config.saveToString();
        Path path = new File(directory, id + ".yml").toPath();
        SchedulerUtil.runTaskAsynchronously(() -> {
            try {
                Files.writeString(path, yaml, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                SchedulerUtil.runSync(() -> ErrorManager.errorManager.sendErrorMessage(
                        "§cError: Failed to save custom item " + id + ".yml: "
                                + exception.getMessage()));
            }
        });
        return true;
    }

    private String normalizeNewId(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Custom item id cannot be null");
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Custom item id may only contain lowercase letters, numbers, '_' and '-'");
        }
        return normalized;
    }
}
