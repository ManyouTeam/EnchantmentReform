package cn.superiormc.enchantmentreform.objects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class NativeEffectsSelector {

    private NativeEffectsSelector() {
    }

    public static Object select(ConfigurationSection config, Supplier<String> serverVersion) {
        Object fallback = config.get("effects");
        if (fallback != null && !(fallback instanceof ConfigurationSection)
                && !(fallback instanceof Map<?, ?>)) {
            fallback = null;
        }
        if (!config.contains("effects-by-version")) {
            return fallback;
        }
        Object configured = config.get("effects-by-version");
        if (!(configured instanceof List<?> branches)) {
            return fallback;
        }
        int[] current = branches.isEmpty() ? null : version(serverVersion.get());
        if (current == null) {
            return fallback;
        }
        for (Object entry : branches) {
            if (!(entry instanceof Map<?, ?> branch)) {
                continue;
            }
            Object effects = branch.get("effects");
            if (!(effects instanceof Map<?, ?>) && !(effects instanceof ConfigurationSection)) {
                continue;
            }
            Object exact = branch.get("versions");
            Object min = branch.get("min-version");
            Object max = branch.get("max-version");
            if (exact == null && min == null && max == null) {
                continue;
            }
            int[] minimum = version(min);
            int[] maximum = version(max);
            if (min != null && minimum == null || max != null && maximum == null
                    || minimum != null && maximum != null && compare(minimum, maximum) > 0) {
                continue;
            }
            boolean matches = true;
            if (exact != null) {
                List<?> versions = exact instanceof List<?> list ? list : List.of(exact);
                matches = false;
                for (Object candidate : versions) {
                    int[] parsed = version(candidate);
                    if (parsed != null && compare(current, parsed) == 0) {
                        matches = true;
                    }
                }
            }
            matches &= minimum == null || compare(current, minimum) >= 0;
            matches &= maximum == null || compare(current, maximum) <= 0;
            if (matches) {
                return effects;
            }
        }
        return fallback;
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
        int[] parsedCurrent = version(current);
        int[] parsedMinimum = version(minimum);
        return parsedCurrent != null && parsedMinimum != null && compare(parsedCurrent, parsedMinimum) >= 0;
    }

    private static int[] version(Object configured) {
        if (!(configured instanceof String text) || !text.matches("\\d{1,4}(\\.\\d{1,4}){1,2}")) {
            return null;
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
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
