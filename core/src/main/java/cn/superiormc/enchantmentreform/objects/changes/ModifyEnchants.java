package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.MathUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

/** Modifies current enchantment levels without rebuilding the whole item format. */
public final class ModifyEnchants extends AbstractChangesRule {

    private static final String CONFIG_KEY = "modify-enchants";

    /**
     * Change configuration is normally reused for many items. Cache registry lookups so repeated
     * executions only perform a concurrent-map lookup and ItemMeta#getEnchantLevel.
     */
    private static final Map<String, Optional<Enchantment>> ENCHANTMENT_CACHE =
            new ConcurrentHashMap<>();

    @Override
    public ItemStack setChange(ObjectSingleChange change) {
        ConfigurationSection enchantments = change.getConfigurationSection(CONFIG_KEY);
        ItemMeta meta = change.getItemMeta();
        if (enchantments == null || meta == null) {
            return change.getItem();
        }

        boolean changed = false;
        for (Map.Entry<String, Object> entry : enchantments.getValues(false).entrySet()) {
            Enchantment enchantment = resolveEnchantment(entry.getKey());
            if (enchantment == null) {
                continue;
            }

            int currentLevel = meta.getEnchantLevel(enchantment);
            Object configured = entry.getValue();
            String operation = "ADD";
            Object rawValue = configured;
            if (configured instanceof ConfigurationSection rule) {
                String configuredOperation = rule.getString("operation", "ADD");
                operation = configuredOperation == null
                        ? "ADD" : configuredOperation.trim().toUpperCase(Locale.ROOT);
                rawValue = rule.get("value");
            }

            Double operand = evaluate(rawValue, change, currentLevel, enchantment.getMaxLevel());
            if (operand == null) {
                continue;
            }
            double result = switch (operation) {
                case "SET" -> operand;
                case "SUBTRACT" -> currentLevel - operand;
                case "MULTIPLY" -> currentLevel * operand;
                default -> currentLevel + operand;
            };

            Integer targetLevel = normalizeLevel(result);
            if (targetLevel == null || targetLevel == currentLevel) {
                continue;
            }

            if (targetLevel <= 0) {
                if (currentLevel > 0) {
                    changed |= meta.removeEnchant(enchantment);
                }
                continue;
            }

            changed |= meta.addEnchant(enchantment, targetLevel, true);
        }

        return changed ? change.setItemMeta(meta) : change.getItem();
    }

    private Double evaluate(Object rawValue, ObjectSingleChange change, int currentLevel, int maxLevel) {
        if (rawValue instanceof Number number) {
            return number.doubleValue();
        }
        if (rawValue == null) {
            return 0.0D;
        }

        String expression = change.parsePlaceholder(String.valueOf(rawValue));
        expression = expression.replace("{current-level}", Integer.toString(currentLevel))
                .replace("{max-level}", Integer.toString(maxLevel));
        OptionalDouble result = MathUtil.tryCalculate(expression);
        if (result.isPresent()) {
            return result.getAsDouble();
        }

        // Keep the normal configuration error message, but skip this enchantment instead of
        // treating an invalid expression as level zero and removing it.
        MathUtil.doCalculate(expression);
        return null;
    }

    /** Returns null for NaN/infinite results so invalid expressions never corrupt the item. */
    private Integer normalizeLevel(double value) {
        if (!Double.isFinite(value)) {
            return null;
        }
        if (value <= 0.0D) {
            return 0;
        }
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(value);
    }

    private Enchantment resolveEnchantment(String configuredKey) {
        if (configuredKey == null || configuredKey.isBlank()) {
            return null;
        }
        String normalized = configuredKey.trim().toLowerCase(Locale.ROOT);
        return ENCHANTMENT_CACHE.computeIfAbsent(normalized, this::lookupEnchantment).orElse(null);
    }

    private Optional<Enchantment> lookupEnchantment(String value) {
        try {
            NamespacedKey key = CommonUtil.parseNamespacedKey(value);
            return Optional.ofNullable(key == null ? null : Registry.ENCHANTMENT.get(key));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getConfigurationSection(CONFIG_KEY) == null;
    }
}
