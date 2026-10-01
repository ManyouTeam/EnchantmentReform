package cn.superiormc.enchantmentreform.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NativeEffectsMigrator {

    private static final Map<String, String> PROVIDERS = Map.of(
            "minecraft:dual_noise_provider", "minecraft:dual_noise",
            "minecraft:noise_provider", "minecraft:noise",
            "minecraft:noise_threshold_provider", "minecraft:noise_threshold",
            "minecraft:randomized_int_state_provider", "minecraft:randomized_int",
            "minecraft:rule_based_state_provider", "minecraft:rule_based",
            "minecraft:simple_state_provider", "minecraft:simple",
            "minecraft:weighted_state_provider", "minecraft:weighted");

    // These legacy tag names cannot name vanilla damage types. Recognizing them
    // also repairs configurations where condition was already renamed to type.
    private static final Set<String> KNOWN_DAMAGE_TAGS = Set.of(
            "minecraft:burn_from_stepping", "minecraft:bypasses_invulnerability");
    private static final Set<String> OPAQUE_FIELDS = Set.of(
            "nbt", "custom_data", "entity_data", "components");

    private NativeEffectsMigrator() {}

    public static Map<String, Object> migrate(Map<String, Object> effects, String serverVersion) {
        if (effects.isEmpty() || !NativeEffectsSelector.isAtLeast(serverVersion, "26.3")) {
            return effects;
        }
        return migrateMap(effects, false, false, "effects");
    }

    private static Object migrateValue(Object value, boolean lootPredicate,
                                       boolean blockState, String path) {
        if (value instanceof Map<?, ?> map) {
            return migrateMap(map, lootPredicate, blockState, path);
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                result.add(migrateValue(list.get(i), lootPredicate, blockState,
                        path + '[' + i + ']'));
            }
            // Older loot codecs accepted an implicit all_of list.
            return lootPredicate ? Map.of("type", "minecraft:all_of", "terms", result) : result;
        }
        return value;
    }

    private static Map<String, Object> migrateMap(Map<?, ?> input, boolean lootPredicate,
                                                boolean blockState, String path) {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean legacyPredicate = lootPredicate && input.get("condition") instanceof String
                && !input.containsKey("type");
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException(path + ": effects map keys must be strings");
            }
            Object value = entry.getValue();
            // Arbitrary NBT/component payloads are not native effect schemas.
            if (OPAQUE_FIELDS.contains(key)) {
                result.put(key, value);
                continue;
            }
            boolean childPredicate = "requirements".equals(key)
                    || lootPredicate && "term".equals(key);
            if (lootPredicate && "terms".equals(key) && value instanceof List<?> terms) {
                List<Object> converted = new ArrayList<>(terms.size());
                for (int i = 0; i < terms.size(); i++) {
                    converted.add(migrateValue(terms.get(i), true, false,
                            path + ".terms[" + i + ']'));
                }
                result.put(key, converted);
            } else {
                result.put(key, migrateValue(value, childPredicate,
                        "state".equals(key) || "block_state".equals(key), path + '.' + key));
            }
        }
        if (legacyPredicate) {
            result.put("type", result.remove("condition"));
        }
        if (blockState) {
            rename(result, "Name", "id", path);
            rename(result, "Properties", "properties", path);
        }
        Object type = result.get("type");
        if (type instanceof String identifier) {
            String namespaced = namespaced(identifier);
            if (PROVIDERS.containsKey(namespaced)) {
                result.put("type", PROVIDERS.get(namespaced));
            }
            if (lootPredicate && "minecraft:damage_source_properties".equals(namespaced)
                    && result.get("predicate") instanceof Map<?, ?> predicate) {
                result.put("predicate", migrateDamageTags(predicate, legacyPredicate));
            }
        }
        return result;
    }

    private static Map<String, Object> migrateDamageTags(Map<?, ?> predicate, boolean legacy) {
        Map<String, Object> result = new LinkedHashMap<>();
        predicate.forEach((key, value) -> result.put((String) key, value));
        if (predicate.get("tags") instanceof List<?> tags) {
            List<Object> converted = new ArrayList<>(tags.size());
            for (Object value : tags) {
                if (value instanceof Map<?, ?> tag && tag.get("id") instanceof String id
                        && !id.startsWith("#") && (legacy || KNOWN_DAMAGE_TAGS.contains(namespaced(id)))) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    tag.forEach((key, nested) -> copy.put((String) key, nested));
                    copy.put("id", "#" + namespaced(id));
                    converted.add(copy);
                } else {
                    converted.add(value);
                }
            }
            result.put("tags", converted);
        }
        return result;
    }

    private static void rename(Map<String, Object> value, String oldKey, String newKey, String path) {
        if (!value.containsKey(oldKey)) return;
        if (value.containsKey(newKey) && !java.util.Objects.equals(value.get(oldKey), value.get(newKey))) {
            throw new IllegalArgumentException(path + ": conflicting '" + oldKey + "' and '" + newKey + "'");
        }
        value.putIfAbsent(newKey, value.remove(oldKey));
    }

    private static String namespaced(String identifier) {
        return identifier.indexOf(':') < 0 ? "minecraft:" + identifier : identifier;
    }
}
