package cn.superiormc.enchantmentreform.hooks;

import cn.superiormc.enchantmentreform.hooks.blocks.AbstractBlockHook;
import cn.superiormc.enchantmentreform.managers.HookManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared block matcher and placer.
 *
 * <p>Supported matcher formats:</p>
 * <ul>
 *     <li>{@code STONE}</li>
 *     <li>{@code minecraft:stone}</li>
 *     <li>{@code craftengine:test}</li>
 *     <li>{@code #minecraft:logs}</li>
 *     <li>{@code minecraft:farmland[moisture=7]}</li>
 *     <li>{@code minecraft:farmland[moisture=(5~7)]}</li>
 * </ul>
 *
 * <p>State ranges are inclusive. Multiple states are combined with AND.</p>
 */
public final class BlockPriceUtil {

    private static final Pattern NUMBER_RANGE = Pattern.compile(
            "^\\(\\s*(-?\\d+(?:\\.\\d+)?)\\s*~\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\)$");

    private BlockPriceUtil() {
    }

    public static boolean matches(Block block, String specification) {
        if (block == null) {
            return false;
        }
        return matches(block.getType(), block.getBlockData(), block, block.getLocation(), specification);
    }

    /**
     * Matches against a captured {@link BlockState}. Useful after the world block has changed
     * (for example in {@code BlockDropItemEvent}, where the live block is already air).
     */
    public static boolean matches(BlockState state, String specification) {
        if (state == null) {
            return false;
        }
        return matches(state.getType(), state.getBlockData(), state.getBlock(),
                state.getLocation(), specification);
    }

    private static boolean matches(Material type, BlockData blockData, Block block,
                                   Location location, String specification) {
        if (type == null || specification == null || specification.isBlank()) {
            return false;
        }

        ParsedBlock parsed = parse(specification);
        if (parsed == null) {
            return false;
        }
        if (parsed.tag()) {
            return parsed.states().isEmpty() && matchesTag(type, parsed.id());
        }

        Material material = resolveVanillaMaterial(parsed.id());
        if (material != null) {
            return type == material && matchesStates(blockData, parsed.states());
        }

        if (!parsed.states().isEmpty() || block == null) {
            return false;
        }
        AbstractBlockHook hook = getHook(parsed.id());
        return hook != null && hook.check(block, parsed.id(), location);
    }

    public static boolean matchesAny(Block block, Collection<String> specifications) {
        return findMatch(block, specifications) != null;
    }

    public static boolean matchesAny(BlockState state, Collection<String> specifications) {
        if (state == null || specifications == null) {
            return false;
        }
        for (String specification : specifications) {
            if (matches(state, specification)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String findMatch(Block block, Collection<String> specifications) {
        if (specifications == null) {
            return null;
        }
        for (String specification : specifications) {
            if (matches(block, specification)) {
                return specification;
            }
        }
        return null;
    }

    public static boolean place(Location location, String specification) {
        if (location == null || location.getWorld() == null
                || specification == null || specification.isBlank()) {
            return false;
        }

        ParsedBlock parsed = parse(specification);
        if (parsed == null || parsed.tag() || containsRange(parsed.states())) {
            return false;
        }

        Material material = resolveVanillaMaterial(parsed.id());
        if (material != null) {
            try {
                if (parsed.states().isEmpty()) {
                    location.getBlock().setType(material, false);
                } else {
                    String dataString = material.getKey() + serializeStates(parsed.states());
                    location.getBlock().setBlockData(Bukkit.createBlockData(dataString), false);
                }
                return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }

        if (!parsed.states().isEmpty()) {
            return false;
        }
        AbstractBlockHook hook = getHook(parsed.id());
        if (hook == null) {
            return false;
        }
        hook.placeBlock(parsed.id(), location);
        return true;
    }

    private static boolean matchesStates(BlockData blockData, Map<String, String> expectedStates) {
        if (expectedStates.isEmpty()) {
            return true;
        }
        Map<String, String> actualStates = parseSerializedStates(blockData.getAsString());
        for (Map.Entry<String, String> expected : expectedStates.entrySet()) {
            String actual = actualStates.get(expected.getKey());
            if (actual == null || !matchesStateValue(actual, expected.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesStateValue(String actual, String expected) {
        Matcher matcher = NUMBER_RANGE.matcher(expected);
        if (!matcher.matches()) {
            return actual.equalsIgnoreCase(expected);
        }
        try {
            double current = Double.parseDouble(actual);
            double first = Double.parseDouble(matcher.group(1));
            double second = Double.parseDouble(matcher.group(2));
            double minimum = Math.min(first, second);
            double maximum = Math.max(first, second);
            return current >= minimum && current <= maximum;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean containsRange(Map<String, String> states) {
        for (String value : states.values()) {
            if (NUMBER_RANGE.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesTag(Material material, String rawTag) {
        NamespacedKey key = NamespacedKey.fromString(rawTag.toLowerCase(Locale.ROOT));
        if (key == null) {
            key = NamespacedKey.minecraft(rawTag.toLowerCase(Locale.ROOT));
        }
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);
        return tag != null && tag.isTagged(material);
    }

    @Nullable
    private static AbstractBlockHook getHook(String id) {
        HookManager manager = HookManager.hookManager;
        return manager == null ? null : manager.getSuitableChecker(id);
    }

    @Nullable
    private static Material resolveVanillaMaterial(String id) {
        String value = id;
        if (value.regionMatches(true, 0, "minecraft:", 0, "minecraft:".length())) {
            value = value.substring("minecraft:".length());
        } else if (value.contains(":")) {
            return null;
        }
        return Material.matchMaterial(value, false);
    }

    @Nullable
    private static ParsedBlock parse(String raw) {
        String input = raw.trim();
        boolean tag = input.startsWith("#");
        if (tag) {
            input = input.substring(1).trim();
        }

        int statesStart = input.indexOf('[');
        String id = statesStart < 0 ? input : input.substring(0, statesStart).trim();
        if (id.isEmpty()) {
            return null;
        }

        Map<String, String> states = new LinkedHashMap<>();
        if (statesStart >= 0) {
            if (!input.endsWith("]")) {
                return null;
            }
            String stateContent = input.substring(statesStart + 1, input.length() - 1).trim();
            if (!stateContent.isEmpty() && !parseStateContent(stateContent, states)) {
                return null;
            }
        }
        return new ParsedBlock(normalizeId(id), Map.copyOf(states), tag);
    }

    private static boolean parseStateContent(String content, Map<String, String> target) {
        int start = 0;
        int parentheses = 0;
        for (int index = 0; index <= content.length(); index++) {
            char current = index == content.length() ? ',' : content.charAt(index);
            if (current == '(') {
                parentheses++;
            } else if (current == ')') {
                parentheses--;
                if (parentheses < 0) {
                    return false;
                }
            }
            if (current == ',' && parentheses == 0) {
                String entry = content.substring(start, index).trim();
                int equals = entry.indexOf('=');
                if (equals <= 0 || equals == entry.length() - 1) {
                    return false;
                }
                String key = entry.substring(0, equals).trim().toLowerCase(Locale.ROOT);
                String value = entry.substring(equals + 1).trim().toLowerCase(Locale.ROOT);
                if (key.isEmpty() || value.isEmpty()) {
                    return false;
                }
                target.put(key, value);
                start = index + 1;
            }
        }
        return parentheses == 0;
    }

    private static Map<String, String> parseSerializedStates(String serialized) {
        int start = serialized.indexOf('[');
        if (start < 0 || !serialized.endsWith("]")) {
            return Map.of();
        }
        Map<String, String> states = new LinkedHashMap<>();
        return parseStateContent(serialized.substring(start + 1, serialized.length() - 1), states)
                ? states : Map.of();
    }

    private static String normalizeId(String id) {
        String trimmed = id.trim();
        int separator = trimmed.indexOf(':');
        if (separator < 0) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        String provider = trimmed.substring(0, separator).toLowerCase(Locale.ROOT);
        String value = trimmed.substring(separator + 1);
        return provider + ":" + (provider.equals("minecraft")
                ? value.toLowerCase(Locale.ROOT) : value);
    }

    private static String serializeStates(Map<String, String> states) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, String> state : states.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(state.getKey()).append('=').append(state.getValue());
            first = false;
        }
        return builder.append(']').toString();
    }

    private record ParsedBlock(String id, Map<String, String> states, boolean tag) {
    }
}
