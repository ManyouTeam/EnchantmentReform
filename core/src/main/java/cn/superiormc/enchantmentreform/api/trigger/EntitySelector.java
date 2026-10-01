package cn.superiormc.enchantmentreform.api.trigger;

import java.util.Locale;

public enum EntitySelector {
    PLAYER,
    SOURCE,
    SKILL,
    TARGET;

    public static EntitySelector parse(String value, EntitySelector defaultValue) {
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
