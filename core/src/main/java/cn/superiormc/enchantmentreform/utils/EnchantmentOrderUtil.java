package cn.superiormc.enchantmentreform.utils;

import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EnchantmentOrderUtil {

    private static final String DESCENDING = "DESCENDING";

    private EnchantmentOrderUtil() {
    }

    public static boolean isEnabled(ConfigurationSection settings) {
        return settings == null || settings.getBoolean("enabled", true);
    }

    public static Comparator<PowerEnchantmentDefinition> comparator(
            ConfigurationSection raritySettings,
            ConfigurationSection tooltipSettings) {
        boolean weightDescending = readWeightDirection(tooltipSettings).equals(DESCENDING);

        Comparator<PowerEnchantmentDefinition> weightComparator =
                Comparator.comparingInt(PowerEnchantmentDefinition::getWeight);
        if (weightDescending) {
            weightComparator = weightComparator.reversed();
        }

        return Comparator
                .comparing(PowerEnchantmentDefinition::getRarity,
                        rarityComparator(raritySettings, tooltipSettings))
                .thenComparing(weightComparator)
                .thenComparing(definition -> definition.getKey().asString());
    }

    public static Comparator<String> rarityComparator(
            ConfigurationSection raritySettings,
            ConfigurationSection sortSettings) {
        Map<String, Integer> rarityRanks = createRarityRanks(
                raritySettings, sortSettings);
        return Comparator
                .comparingInt((String rarity) -> rarityRank(rarity, rarityRanks))
                .thenComparing(String.CASE_INSENSITIVE_ORDER);
    }

    private static Map<String, Integer> createRarityRanks(
            ConfigurationSection raritySettings,
            ConfigurationSection sortSettings) {
        List<String> configuredOrder = sortSettings == null
                ? List.of()
                : sortSettings.getStringList("rarity-sort-rule");
        Map<String, Integer> result = new LinkedHashMap<>();

        if (!configuredOrder.isEmpty()) {
            for (String rarity : configuredOrder) {
                addRarity(result, rarity);
            }
            return result;
        }

        if (raritySettings != null) {
            for (String rarity : raritySettings.getKeys(false)) {
                addRarity(result, rarity);
            }
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
        String configured = settings == null
                ? DESCENDING
                : settings.getString("weight-order", DESCENDING);
        String normalized = configured == null
                ? DESCENDING
                : configured.strip().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "ASC", "ASCENDING" -> "ASCENDING";
            case "DESC", DESCENDING -> DESCENDING;
            default -> throw new IllegalArgumentException(
                    "tooltip-order.weight-order must be ASCENDING or DESCENDING");
        };
    }
}
