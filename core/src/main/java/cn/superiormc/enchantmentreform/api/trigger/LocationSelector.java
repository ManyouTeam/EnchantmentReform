package cn.superiormc.enchantmentreform.api.trigger;

import java.util.Locale;

public enum LocationSelector {
    CONTEXT,
    FROM,
    TO,
    PLAYER,
    SOURCE,
    SKILL,
    TARGET,
    BLOCK,
    LOOK;

    public static LocationSelector parse(String value, LocationSelector defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }
}
