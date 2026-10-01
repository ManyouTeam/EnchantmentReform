package cn.superiormc.enchantmentreform.utils;

import cn.superiormc.enchantmentreform.methods.DebuildItem;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemUtil {

    public static boolean isValid(ItemStack item) {
        return item != null && !item.getType().isAir();
    }


    public static boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (ConfigManager.configManager.getString(
                "powers.cost-price.check-method", "Bukkit").equalsIgnoreCase("Bukkit")) {
            return item1.isSimilar(item2);
        }
        Map<String, Object> item1Result = DebuildItem.debuildItem(item1, new MemoryConfiguration()).getValues(true);
        Map<String, Object> item2Result = DebuildItem.debuildItem(item2, new MemoryConfiguration()).getValues(true);
        if (ConfigManager.configManager.getBoolean(
                "powers.cost-price.item-format.require-same-key", false)) {
            for (String key : item1Result.keySet()) {
                if (canIgnore(key)) {
                    continue;
                }
                if (!item2Result.containsKey(key)) {
                    return false;
                }
            }
        }
        for (String key : item2Result.keySet()) {
            if (canIgnore(key)) {
                continue;
            }
            Object object = item1Result.get(key);
            if (object == null) {
                return false;
            }
            if (object instanceof MemorySection) {
                continue;
            }
            if (!object.equals(item2Result.get(key))) {
                if (object instanceof String && item2Result.get(key) instanceof String) {
                    String tempVal1 = (String) object;
                    String tempVal2 = (String) item2Result.get(key);
                    if (tempVal1.equalsIgnoreCase(tempVal2)) {
                        continue;
                    }
                }
                return false;
            }
        }
        return true;
    }

    public static boolean canIgnore(String key) {
        if (key == null) {
            return true;
        }
        if (key.equals("amount")) {
            return true;
        }
        for (String tempVal1 : ConfigManager.configManager.config.getStringList(
                "powers.cost-price.item-format.ignore-key")) {
            if (tempVal1.equals(key) || key.startsWith(tempVal1 + ".")) {
                return true;
            }
        }
        return false;
    }

}
