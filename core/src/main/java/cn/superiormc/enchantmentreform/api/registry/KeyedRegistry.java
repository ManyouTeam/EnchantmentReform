package cn.superiormc.enchantmentreform.api.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class KeyedRegistry<K, V> {

    private final Map<K, V> entries = new LinkedHashMap<>();

    private final UnaryOperator<K> normalizer;

    public KeyedRegistry() {
        this(UnaryOperator.identity());
    }

    public KeyedRegistry(UnaryOperator<K> normalizer) {
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer");
    }

    public static <V> KeyedRegistry<String, V> stringTypes() {
        return new KeyedRegistry<>(KeyedRegistry::normalizeType);
    }

    public synchronized void register(K key, V value) {
        K normalized = normalize(key);
        Objects.requireNonNull(value, "value");
        if (entries.putIfAbsent(normalized, value) != null) {
            throw new IllegalArgumentException("Registry key already registered: " + normalized);
        }
    }

    public synchronized void replace(K key, V value) {
        entries.put(normalize(key), Objects.requireNonNull(value, "value"));
    }

    public synchronized V get(K key) {
        return entries.get(normalize(key));
    }

    public synchronized V getRequired(K key) {
        K normalized = normalize(key);
        V value = entries.get(normalized);
        if (value == null) {
            throw new IllegalArgumentException("Unknown registry key: " + normalized);
        }
        return value;
    }

    public synchronized V remove(K key) {
        return entries.remove(normalize(key));
    }

    public synchronized boolean contains(K key) {
        return entries.containsKey(normalize(key));
    }

    public synchronized Collection<V> values() {
        return List.copyOf(entries.values());
    }

    public synchronized List<K> keys() {
        return List.copyOf(entries.keySet());
    }

    public synchronized void clear() {
        entries.clear();
    }

    private K normalize(K key) {
        return normalizer.apply(Objects.requireNonNull(key, "key"));
    }

    private static String normalizeType(String type) {
        return Objects.requireNonNull(type, "type").toLowerCase(java.util.Locale.ROOT).replace('-', '_');
    }
}
