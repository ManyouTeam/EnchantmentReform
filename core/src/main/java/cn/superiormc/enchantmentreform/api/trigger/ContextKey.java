package cn.superiormc.enchantmentreform.api.trigger;

import org.bukkit.NamespacedKey;

import java.util.Objects;

public record ContextKey<T>(NamespacedKey key, Class<T> type) {

    public ContextKey{
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
    }

    void validate(Object value) {
        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException(
                    key + " requires " + type.getName() + ", got " + value.getClass().getName());
        }
    }
}
