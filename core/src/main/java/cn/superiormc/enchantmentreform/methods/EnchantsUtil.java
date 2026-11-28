package cn.superiormc.enchantmentreform.methods;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.objects.EnchantmentRarity;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EnchantsUtil {

    public static String getEnchantmentName(Enchantment enchantment, Player viewer) {
        NamespacedKey key = enchantment.getKey();
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        PowerEnchantmentDefinition definition = manager == null ? null : manager.getDefinition(key.toString());
        if (definition != null) {
            return definition.getLocalizedName(viewer);
        }
        return key.toString();
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

    public static List<ItemStack> enchantBooksWithLevels(int power, int amount) {
        if (power <= 0) {
            return List.of();
        }
        return generateEnchantedBooks(amount,
                () -> Bukkit.getItemFactory().enchantItem(new ItemStack(Material.BOOK), power, true));
    }

    public static List<ItemStack> randomEnchantedBooks(int amount) {
        return randomEnchantedBooks(null, amount, 0);
    }

    public static List<ItemStack> randomEnchantedBooks(String rarity, int amount, int fixedLevel) {
        return rollSingleEnchantmentBooks(candidates(rarity, Set.of()), amount, fixedLevel,
                fixedLevel == 0 ? Integer.MAX_VALUE : fixedLevel);
    }

    public static List<ItemStack> randomEnchantedBooks(String rarity, Set<Material> targetItems, int power,
                                                       int amount, int minimumLevel, int maximumLevel) {
        if (power > 0 && rarity == null && targetItems.isEmpty()) {
            return enchantBooksWithLevels(power, amount);
        }
        Stream<Enchantment> candidates = candidates(rarity, targetItems);
        if (power == 0) {
            return rollSingleEnchantmentBooks(candidates, amount, minimumLevel, maximumLevel);
        }
        List<Enchantment> enchantments = candidates.toList();
        if (enchantments.isEmpty()) {
            return List.of();
        }
        return generateEnchantedBooks(amount, EnchantmentReform.methodUtil.enchantedBookGenerator(power, enchantments));
    }

    public static List<ItemStack> booksOfRarity(String rarity, int power, int amount) {
        return randomEnchantedBooks(rarity, Set.of(), power, amount, 0, Integer.MAX_VALUE);
    }

    public static ItemStack enchantHeldItem(ItemStack held, String rarity, int power, int rolls, int minimumLevel, int maximumLevel) {
        ItemStack result = held.clone();
        boolean book = held.getType() == Material.BOOK || held.getType() == Material.ENCHANTED_BOOK;
        List<Enchantment> pool = eligibleLevels(candidates(rarity, Set.of()), minimumLevel, maximumLevel)
                .filter(enchantment -> book || enchantment.canEnchantItem(held)).toList();
        for (int index = 0; index < rolls; index++) {
            Map<Enchantment, Integer> existing = getEnchantments(result, false);
            List<Enchantment> options = pool.stream().filter(candidate -> !existing.containsKey(candidate)
                    && existing.keySet().stream().noneMatch(other -> other.conflictsWith(candidate)
                            || candidate.conflictsWith(other))).toList();
            if (options.isEmpty()) {
                return null;
            }
            Map<Enchantment, Integer> additions;
            if (power > 0) {
                ItemStack input = book ? new ItemStack(Material.BOOK) : result.clone();
                ItemStack rolled = EnchantmentReform.methodUtil.enchantedItemGenerator(input, power, options).get();
                additions = new HashMap<>(getEnchantments(rolled, false));
                additions.keySet().retainAll(options);
                if (additions.isEmpty()) {
                    return null;
                }
            } else {
                Enchantment chosen = randomEnchantment(options);
                additions = Map.of(chosen, randomLevel(chosen, minimumLevel, maximumLevel));
            }
            if (result.getType() == Material.BOOK) {
                result.setType(Material.ENCHANTED_BOOK);
            }
            if (result.getItemMeta() instanceof EnchantmentStorageMeta storage) {
                additions.forEach((enchantment, level) -> storage.addStoredEnchant(enchantment, level, true));
                result.setItemMeta(storage);
            } else {
                result.addUnsafeEnchantments(additions);
            }
        }
        return result;
    }

    private static Stream<Enchantment> candidates(String rarity, Set<Material> targetItems) {
        Stream<Enchantment> enchantments = Registry.ENCHANTMENT.stream();
        if (rarity != null) {
            EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
            enchantments = enchantments.filter(enchantment -> rarity.equalsIgnoreCase(EnchantmentRarity.idOf(
                    manager.getDefinition(enchantment.getKey().toString()), enchantment)));
        }
        if (!targetItems.isEmpty()) {
            List<ItemStack> samples = targetItems.stream().map(ItemStack::new).toList();
            enchantments = enchantments.filter(enchantment -> samples.stream().anyMatch(enchantment::canEnchantItem));
        }
        return enchantments;
    }

    private static Stream<Enchantment> eligibleLevels(Stream<Enchantment> candidates, int minimumLevel, int maximumLevel) {
        if (minimumLevel == 0) {
            return candidates;
        }
        return candidates.filter(enchantment ->
                minimumLevel <= enchantment.getMaxLevel() && maximumLevel >= enchantment.getStartLevel());
    }

    private static List<ItemStack> rollSingleEnchantmentBooks(Stream<Enchantment> candidates, int amount,
                                                              int minimumLevel, int maximumLevel) {
        List<Enchantment> enchantments = eligibleLevels(candidates, minimumLevel, maximumLevel).toList();
        if (enchantments.isEmpty()) {
            return List.of();
        }
        return generateEnchantedBooks(amount, () -> randomEnchantedBook(enchantments, minimumLevel, maximumLevel));
    }

    private static List<ItemStack> generateEnchantedBooks(int amount, Supplier<ItemStack> generator) {
        if (amount <= 0) {
            return List.of();
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

    private static ItemStack randomEnchantedBook(List<Enchantment> enchantments, int minimumLevel, int maximumLevel) {
        Enchantment enchantment = randomEnchantment(enchantments);
        return ItemUtil.generateEnchantedBook(enchantment, randomLevel(enchantment, minimumLevel, maximumLevel));
    }

    private static Enchantment randomEnchantment(List<Enchantment> enchantments) {
        return enchantments.get(ThreadLocalRandom.current().nextInt(enchantments.size()));
    }

    private static int randomLevel(Enchantment enchantment, int minimumLevel, int maximumLevel) {
        int minimum = Math.max(Math.max(1, enchantment.getStartLevel()), minimumLevel);
        int maximum = Math.min(Math.max(minimum, enchantment.getMaxLevel()), maximumLevel);
        return (int) ThreadLocalRandom.current().nextLong(minimum, (long) maximum + 1L);
    }
}
