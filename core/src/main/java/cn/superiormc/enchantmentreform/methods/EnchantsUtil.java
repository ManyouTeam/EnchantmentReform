package cn.superiormc.enchantmentreform.methods;

import cn.superiormc.enchantmentreform.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class EnchantsUtil {

    public static List<ItemStack> enchantBooksWithLevels(int power, int amount) {
        if (power <= 0) {
            return null;
        }
        return generateEnchantedBooks(amount,
                () -> Bukkit.getItemFactory().enchantItem(new ItemStack(Material.BOOK), power, true));
    }

    public static List<ItemStack> randomEnchantedBooks(int amount) {
        List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().toList();
        if (enchantments.isEmpty()) {
            return List.of();
        }
        return generateEnchantedBooks(amount, () -> randomEnchantedBook(enchantments));
    }

    private static List<ItemStack> generateEnchantedBooks(int amount, Supplier<ItemStack> generator) {
        if (amount <= 0) {
            return null;
        }
        List<ItemStack> books = new ArrayList<>(amount);
        for (int index = 0; index < amount; index++) {
            ItemStack book = generator.get();
            if (book == null || book.getType() != Material.ENCHANTED_BOOK
                    || getEnchantments(book, false).isEmpty()) {
                return List.of();
            }
            books.add(book);
        }
        return books;
    }

    private static ItemStack randomEnchantedBook(List<Enchantment> enchantments) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Enchantment enchantment = enchantments.get(random.nextInt(enchantments.size()));
        int minimum = Math.max(1, enchantment.getStartLevel());
        int maximum = Math.max(minimum, enchantment.getMaxLevel());
        int level = (int) random.nextLong(minimum, (long) maximum + 1L);
        return ItemUtil.generateEnchantedBook(enchantment, level);
    }

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
