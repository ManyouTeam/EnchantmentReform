package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PowerVariables {

    private static final Pattern REFERENCE = Pattern.compile("\\{([^{}]+)}");

    private static final PowerVariables EMPTY = new PowerVariables(Map.of());

    private final Map<String, Object> values;

    private PowerVariables(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static PowerVariables empty() {
        return EMPTY;
    }

    public static PowerVariables from(ConfigurationSection section) {
        return section == null ? EMPTY : new PowerVariables(copySection(section));
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public String resolve(String name, int level) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return resolve(name, level, new LinkedHashSet<>());
    }

    public String replace(String content, int level, UnaryOperator<String> renderer) {
        if (content == null || content.isEmpty() || values.isEmpty()) {
            return content;
        }
        String result = content;
        for (String name : values.keySet()) {
            String resolved = resolve(name, level);
            if (resolved != null) {
                result = result.replace("{" + name + "}", renderer.apply(resolved));
            }
        }
        return result;
    }

    public Map<String, String> scalarValues() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?>)) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private String resolve(String name, int level, Set<String> resolving) {
        if (!values.containsKey(name) || !resolving.add(name)) {
            return null;
        }
        Object selected = LevelValueResolver.resolve(values.get(name), level);
        if (selected == null || selected instanceof Map<?, ?> || selected instanceof ConfigurationSection) {
            resolving.remove(name);
            return null;
        }
        String formula = String.valueOf(selected);
        Matcher matcher = REFERENCE.matcher(formula);
        StringBuffer expanded = new StringBuffer();
        while (matcher.find()) {
            String nested = resolve(matcher.group(1), level, resolving);
            matcher.appendReplacement(expanded, Matcher.quoteReplacement(
                    nested == null ? matcher.group() : nestedReplacement(nested)));
        }
        matcher.appendTail(expanded);
        resolving.remove(name);
        return expanded.toString();
    }

    private String nestedReplacement(String value) {
        if (value.contains("{level}")
                || value.matches(".*[+*/%^~()].*")
                || value.matches(".*(?<=[0-9})])\\s*-.*")
                || value.matches(".*-\\s*(?=[0-9({]).*")) {
            return "(" + value + ")";
        }
        return value;
    }

    private static Map<String, Object> copySection(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(key);
            result.put(key, child == null ? copyValue(section.get(key)) : copySection(child));
        }
        return result;
    }

    private static Object copyValue(Object value) {
        if (value instanceof ConfigurationSection section) {
            return copySection(section);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), copyValue(entry.getValue()));
                }
            }
            return result;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            for (Object entry : list) {
                result.add(copyValue(entry));
            }
            return List.copyOf(result);
        }
        return value;
    }
}
