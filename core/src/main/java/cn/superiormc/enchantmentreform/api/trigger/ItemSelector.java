package cn.superiormc.enchantmentreform.api.trigger;

import java.util.Locale;

public enum ItemSelector {
    CONTEXT,
    TRIGGER_ITEM,
    MAIN_HAND,
    OFF_HAND,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    ARMOR,
    ALL_EQUIPMENT;

    public static ItemSelector parse(String value, ItemSelector defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        normalized = switch (normalized) {
            case "HAND" -> "MAIN_HAND";
            case "OFFHAND" -> "OFF_HAND";
            case "HEAD" -> "HELMET";
            case "CHEST" -> "CHESTPLATE";
            case "LEGS" -> "LEGGINGS";
            case "FEET" -> "BOOTS";
            default -> normalized;
        };
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }
}
