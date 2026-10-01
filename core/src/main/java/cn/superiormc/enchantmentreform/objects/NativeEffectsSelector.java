package cn.superiormc.enchantmentreform.objects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class NativeEffectsSelector {

    private NativeEffectsSelector() {}

    public static Object select(ConfigurationSection config, Supplier<String> serverVersion) {
        Object fallback = config.get("effects");
        if (fallback != null && !(fallback instanceof ConfigurationSection)
                && !(fallback instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("'effects' must be a section");
        }
        if (!config.contains("effects-by-version")) {
            return fallback;
        }
        Object configured = config.get("effects-by-version");
        if (!(configured instanceof List<?> branches)) {
            throw new IllegalArgumentException("'effects-by-version' must be a list");
        }
        int[] current = branches.isEmpty() ? null : version(serverVersion.get());
        Object selected = fallback;
        boolean matched = false;
        for (int index = 0; index < branches.size(); index++) {
            String path = "effects-by-version[" + index + "]";
            if (!(branches.get(index) instanceof Map<?, ?> branch)) {
                throw new IllegalArgumentException(path + " must be a section");
            }
            for (Object key : branch.keySet()) {
                if (!List.of("versions", "min-version", "max-version", "effects").contains(key)) {
                    throw new IllegalArgumentException(path + ": unknown field '" + key + "'");
                }
            }
            Object effects = branch.get("effects");
            if (!(effects instanceof Map<?, ?>) && !(effects instanceof ConfigurationSection)) {
                throw new IllegalArgumentException(path + ".effects must be a section (use {} to clear)");
            }
            Object exact = branch.get("versions");
            Object min = branch.get("min-version");
            Object max = branch.get("max-version");
            if (exact == null && min == null && max == null) {
                throw new IllegalArgumentException(path + " requires versions, min-version or max-version");
            }
            try {
                int[] minimum = min == null ? null : version(min);
                int[] maximum = max == null ? null : version(max);
                if (minimum != null && maximum != null && compare(minimum, maximum) > 0) {
                    throw new IllegalArgumentException("min-version must not exceed max-version");
                }
                boolean matches = true;
                if (exact != null) {
                    List<?> versions = exact instanceof List<?> list ? list : List.of(exact);
                    if (versions.isEmpty()) {
                        throw new IllegalArgumentException("versions must not be empty");
                    }
                    matches = false;
                    for (Object candidate : versions) {
                        matches |= compare(current, version(candidate)) == 0;
                    }
                }
                matches &= minimum == null || compare(current, minimum) >= 0;
                matches &= maximum == null || compare(current, maximum) <= 0;
                if (matches && !matched) {
                    selected = effects;
                    matched = true;
                }
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(path + ": " + exception.getMessage(), exception);
            }
        }
        return selected;
    }

    static String currentVersion() {
        // Paper bootstrap runs before Bukkit has a Server instance. Build info is
        // available then; reflectively access it so Spigot can also load this class.
        try {
            Class<?> buildInfo = Class.forName("io.papermc.paper.ServerBuildInfo");
            Object info = buildInfo.getMethod("buildInfo").invoke(null);
            return (String) buildInfo.getMethod("minecraftVersionId").invoke(info);
        } catch (ClassNotFoundException ignored) {
            return Bukkit.getBukkitVersion().split("-", 2)[0];
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not determine Minecraft version during bootstrap", exception);
        }
    }

    static boolean isAtLeast(String current, String minimum) {
        return compare(version(current), version(minimum)) >= 0;
    }

    private static int[] version(Object configured) {
        if (!(configured instanceof String text) || !text.matches("\\d+(\\.\\d+){1,2}")) {
            throw new IllegalArgumentException("Minecraft versions must be quoted release strings, e.g. '26.3' or '1.21.11': " + configured);
        }
        String[] parts = text.split("\\.");
        int[] result = new int[3];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }

    private static int compare(int[] left, int[] right) {
        for (int i = 0; i < left.length; i++) {
            int result = Integer.compare(left[i], right[i]);
            if (result != 0) return result;
        }
        return 0;
    }
}
