package cn.superiormc.enchantmentreform.utils;

import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class EnchantmentOrderUtil {

    private EnchantmentOrderUtil() {
    }

    public static Comparator<PowerEnchantmentDefinition> comparator(
            ConfigurationSection sortSettings) {
        return comparator(sortSettings,
                PowerEnchantmentDefinition::getRarity,
                PowerEnchantmentDefinition::getOrder,
                PowerEnchantmentDefinition::getWeight,
                definition -> definition.getKey().asString());
    }

    public static <T> Comparator<T> comparator(
            ConfigurationSection sortSettings,
            Function<T, String> rarity,
            ToIntFunction<T> order,
            ToIntFunction<T> weight,
            Function<T, String> key) {
        Comparator<T> result = Comparator.comparing(rarity, rarityComparator(sortSettings))
                .thenComparingInt(order);
        String weightDirection = readWeightDirection(sortSettings);
        if (!weightDirection.equals("DISABLED")) {
            Comparator<T> weightComparator = Comparator.comparingInt(weight);
            if (weightDirection.equals("DESCENDING")) {
                weightComparator = weightComparator.reversed();
            }
            result = result.thenComparing(weightComparator);
        }
        return result.thenComparing(key);
    }

    public static Comparator<String> rarityComparator(
            ConfigurationSection sortSettings) {
        Map<String, Integer> rarityRanks = createRarityRanks(sortSettings);
        return Comparator
                .comparingInt((String rarity) -> rarityRank(rarity, rarityRanks))
                .thenComparing(String.CASE_INSENSITIVE_ORDER);
    }

    private static Map<String, Integer> createRarityRanks(
            ConfigurationSection sortSettings) {
        List<String> configuredOrder = sortSettings == null
                ? List.of()
                : sortSettings.getStringList("rarity-sort-rule");
        Map<String, Integer> result = new LinkedHashMap<>();

        for (String rarity : configuredOrder) {
            addRarity(result, rarity);
        }
        return result;
    }

    private static void addRarity(Map<String, Integer> ranks, String rarity) {
        if (rarity == null || rarity.isBlank()) {
            return;
        }
        String normalized = rarity.strip().toUpperCase(Locale.ROOT).replace('-', '_');
        ranks.putIfAbsent(normalized, ranks.size());
    }

    private static int rarityRank(String rarity, Map<String, Integer> rarityRanks) {
        return rarityRanks.getOrDefault(
                rarity.toUpperCase(Locale.ROOT),
                Integer.MAX_VALUE);
    }

    private static String readWeightDirection(ConfigurationSection settings) {
        String configured = settings == null ? "DESCENDING" : settings.getString("weight-order", "DESCENDING");
        String normalized = configured == null ? "DESCENDING" : configured.strip().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ASC", "ASCENDING" -> "ASCENDING";
            case "DESC", "DESCENDING" -> "DESCENDING";
            case "DISABLED" -> "DISABLED";
            default -> throw new IllegalArgumentException(
                    "weight-order must be ASCENDING, DESCENDING or DISABLED");
        };
    }
}
