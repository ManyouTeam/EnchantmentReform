package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.NamespacedKey;

import java.util.Locale;
import java.util.Objects;

public record ObjectCustomAttributeModifier(
        String id,
        double amount,
        Operation operation
) {

    public ObjectCustomAttributeModifier {
        id = normalizeId(id);
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Custom attribute modifier amount must be finite");
        }
        Objects.requireNonNull(operation, "operation");
    }

    public static String normalizeId(String configuredId) {
        if (configuredId == null || configuredId.isBlank()) {
            throw new IllegalArgumentException("Custom attribute modifier id cannot be empty");
        }
        NamespacedKey key = NamespacedKey.fromString(
                configuredId.strip().toLowerCase(Locale.ROOT), EnchantmentReform.instance);
        if (key == null) {
            throw new IllegalArgumentException(
                    "Invalid custom attribute modifier id: " + configuredId);
        }
        return key.toString();
    }

    public enum Operation {
        ADD_VALUE,
        ADD_MULTIPLIED_BASE,
        ADD_MULTIPLIED_TOTAL;

        public static Operation parse(String configured) {
            if (configured == null) {
                throw new IllegalArgumentException("Custom attribute modifier operation cannot be null");
            }
            String normalized = configured.strip().toUpperCase(Locale.ROOT).replace('-', '_');
            normalized = switch (normalized) {
                case "ADD_NUMBER" -> "ADD_VALUE";
                case "ADD_SCALAR" -> "ADD_MULTIPLIED_BASE";
                case "MULTIPLY_SCALAR_1" -> "ADD_MULTIPLIED_TOTAL";
                default -> normalized;
            };
            try {
                return valueOf(normalized);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Unknown custom attribute modifier operation: " + configured);
            }
        }
    }
}
