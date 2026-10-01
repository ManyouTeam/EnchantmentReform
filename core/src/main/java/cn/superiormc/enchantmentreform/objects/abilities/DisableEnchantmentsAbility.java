package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Activation-only ability that prevents matching enchantments on the same item from becoming active.
 * Patterns are matched against the logical file name, key value, and full namespaced key.
 */
public final class DisableEnchantmentsAbility extends AbstractAbility {

    private final List<Pattern> enchantments;
    private final List<Pattern> excludedEnchantments;

    public DisableEnchantmentsAbility(ConfigurationSection section) {
        super("DisableEnchantments", section);
        enchantments = compilePatterns(section, "enchantments");
        excludedEnchantments = compilePatterns(section, "exclude-enchantments");
    }

    public boolean disables(PowerEnchantmentDefinition enchantment) {
        return matches(enchantments, enchantment)
                && !matches(excludedEnchantments, enchantment);
    }

    @Override
    public boolean execute(PowerContext context) {
        // This ability is evaluated by ActiveEnchantmentManager before trigger execution.
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }

    private static boolean matches(List<Pattern> patterns, PowerEnchantmentDefinition enchantment) {
        if (patterns.isEmpty() || enchantment == null) {
            return false;
        }
        String fileName = normalize(enchantment.getFileName());
        String keyValue = normalize(enchantment.getKey().value());
        String fullKey = normalize(enchantment.getKey().asString());
        return patterns.stream().anyMatch(pattern -> pattern.matcher(fileName).matches()
                || pattern.matcher(keyValue).matches()
                || pattern.matcher(fullKey).matches());
    }

    private static List<Pattern> compilePatterns(ConfigurationSection section, String path) {
        Object configured = section.get(path);
        if (configured == null) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        if (configured instanceof String value) {
            if (!value.isBlank()) {
                values.add(value);
            }
        } else if (configured instanceof List<?> list) {
            for (Object entry : list) {
                if (entry instanceof String value && !value.isBlank()) {
                    values.add(value);
                }
            }
        }
        return values.stream()
                .map(DisableEnchantmentsAbility::globPattern)
                .toList();
    }

    private static Pattern globPattern(String glob) {
        String normalized = normalize(glob);
        StringBuilder regex = new StringBuilder("^");
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            switch (character) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append('.');
                case '\\', '.', '[', ']', '{', '}', '(', ')', '+', '-', '^', '$', '|' ->
                        regex.append('\\').append(character);
                default -> regex.append(character);
            }
        }
        return Pattern.compile(regex.append('$').toString());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
