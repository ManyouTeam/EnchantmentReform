package cn.superiormc.enchantmentreform.methods;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EnchantsUtil {

    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@NotNull ItemStack itemStack, boolean sort) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return new HashMap<>();
        }
        return getEnchantments(itemMeta, sort);
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@NotNull ItemMeta itemMeta, boolean sort) {
        Map<Enchantment, Integer> enchantments;
        if (itemMeta instanceof EnchantmentStorageMeta storageMeta) {
            enchantments = storageMeta.getStoredEnchants();
            if (enchantments.isEmpty()) {
                enchantments = itemMeta.getEnchants();
            }
        } else {
            enchantments = itemMeta.getEnchants();
        }
        return enchantments;
    }
}
