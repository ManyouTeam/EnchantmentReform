package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public final class LevelValueResolver {

    private LevelValueResolver() {
    }

    public static Object resolve(Object raw, int level) {
        if (raw instanceof ConfigurationSection section) {
            for (String selector : section.getKeys(false)) {
                if (matches(selector, level)) {
                    return section.get(selector);
                }
            }
            return null;
        }
        if (raw instanceof Map<?, ?> values) {
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                if (entry.getKey() != null && matches(String.valueOf(entry.getKey()), level)) {
                    return entry.getValue();
                }
            }
            return null;
        }
        return raw;
    }

    public static boolean matches(String expression, int level) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        for (String rawPart : expression.split(";;")) {
            String part = rawPart.trim();
            try {
                if (part.startsWith(">=")) {
                    if (level < Integer.parseInt(part.substring(2).trim())) return false;
                } else if (part.startsWith("<=")) {
                    if (level > Integer.parseInt(part.substring(2).trim())) return false;
                } else if (part.startsWith(">")) {
                    if (level <= Integer.parseInt(part.substring(1).trim())) return false;
                } else if (part.startsWith("<")) {
                    if (level >= Integer.parseInt(part.substring(1).trim())) return false;
                } else if (part.startsWith("==")) {
                    if (level != Integer.parseInt(part.substring(2).trim())) return false;
                } else if (part.startsWith("=")) {
                    if (level != Integer.parseInt(part.substring(1).trim())) return false;
                } else if (level != Integer.parseInt(part)) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
    }
}
