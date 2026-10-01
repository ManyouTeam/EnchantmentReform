package cn.superiormc.enchantmentreform.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the furnace/cooking result for an input material, caching lookups across the server's
 * recipe set. Shared by drop-rewriting and fishing-catch smelting so both agree on results and
 * cache invalidation.
 */
public final class SmeltUtil {

    /** Material -> smelted result, resolved from the server's cooking recipes on first use. */
    private static final Map<Material, ItemStack> SMELT_CACHE = new ConcurrentHashMap<>();

    private static final ItemStack NO_SMELT_RESULT = new ItemStack(Material.AIR);

    private SmeltUtil() {
    }

    /**
     * Returns the furnace-smelted form of {@code input}, preserving the stack size. Falls back to a
     * clone of the input when no cooking recipe consumes it.
     */
    public static ItemStack smelt(ItemStack input) {
        if (input == null) {
            return null;
        }
        ItemStack recipeResult = resolveSmeltResult(input.getType());
        if (recipeResult == null) {
            return input.clone();
        }
        ItemStack result = recipeResult.clone();
        result.setAmount(Math.max(1, recipeResult.getAmount()) * input.getAmount());
        return result;
    }

    /** True when {@code input} has a cooking recipe (i.e. {@link #smelt} would change its type). */
    public static boolean hasSmeltResult(ItemStack input) {
        return input != null && resolveSmeltResult(input.getType()) != null;
    }

    /** Drops cached lookups; recipes may change across a reload so callers rebuild lazily. */
    public static void clearCache() {
        SMELT_CACHE.clear();
    }

    private static ItemStack resolveSmeltResult(Material material) {
        ItemStack cached = SMELT_CACHE.computeIfAbsent(material, SmeltUtil::lookupSmeltResult);
        return cached == NO_SMELT_RESULT ? null : cached;
    }

    /**
     * Scans the server recipes for one that consumes {@code material}. {@link Bukkit#getRecipesFor}
     * matches on the recipe <em>result</em>, not its ingredient, so it cannot be used here.
     */
    private static ItemStack lookupSmeltResult(Material material) {
        ItemStack probe = new ItemStack(material);
        FurnaceRecipe furnaceMatch = null;
        CookingRecipe<?> cookingMatch = null;
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            if (!(recipes.next() instanceof CookingRecipe<?> cooking)) {
                continue;
            }
            if (!cooking.getInputChoice().test(probe)) {
                continue;
            }
            if (cooking instanceof FurnaceRecipe furnace) {
                furnaceMatch = furnace;
                break;
            }
            if (cookingMatch == null) {
                cookingMatch = cooking;
            }
        }
        CookingRecipe<?> selected = furnaceMatch != null ? furnaceMatch : cookingMatch;
        return selected == null ? NO_SMELT_RESULT : selected.getResult().clone();
    }
}
