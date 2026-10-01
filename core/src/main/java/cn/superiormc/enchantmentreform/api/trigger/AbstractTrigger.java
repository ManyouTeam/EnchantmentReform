package cn.superiormc.enchantmentreform.api.trigger;

import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import org.bukkit.NamespacedKey;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractTrigger<E> {

    private final NamespacedKey key;

    private final String configKey;

    private final Class<E> eventClass;

    protected AbstractTrigger(String id, String configKey, Class<E> eventClass) {
        this(new NamespacedKey("enchantmentreform", id), configKey, eventClass);
    }

    protected AbstractTrigger(NamespacedKey key, String configKey, Class<E> eventClass) {
        this.key = Objects.requireNonNull(key, "key");
        if (configKey == null || configKey.isBlank()) {
            throw new IllegalArgumentException("configKey cannot be blank");
        }
        this.configKey = configKey;
        this.eventClass = Objects.requireNonNull(eventClass, "eventClass");
    }

    public final NamespacedKey key() {
        return key;
    }

    public final String configKey() {
        return configKey;
    }

    /** The payload type accepted by this trigger, also for targeted dispatch. */
    public final Class<E> eventClass() {
        return eventClass;
    }

    /**
     * Exact routing classes this trigger subscribes to; subclasses are not matched automatically.
     * Override for multiple event classes, or return an empty set for targeted dispatch only.
     */
    public Set<Class<? extends E>> getEventClasses() {
        return Set.of(eventClass);
    }

    protected abstract void handle(E event, TriggerRuntime runtime);

    public final void dispatch(Object event, TriggerRuntime runtime) {
        handle(eventClass.cast(event), runtime);
    }

    @Override
    public final boolean equals(Object other) {
        return other instanceof AbstractTrigger<?> trigger && key.equals(trigger.key);
    }

    @Override
    public final int hashCode() {
        return key.hashCode();
    }

    @Override
    public final String toString() {
        return key.toString();
    }
}
