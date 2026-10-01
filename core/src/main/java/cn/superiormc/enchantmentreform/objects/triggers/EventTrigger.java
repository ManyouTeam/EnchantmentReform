package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.AbstractTrigger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public abstract class EventTrigger<E> extends AbstractTrigger<E> {

    private final Set<Class<? extends E>> eventClasses;

    protected EventTrigger(String id, String configKey, Class<E> eventClass) {
        super(id, configKey, eventClass);
        eventClasses = Set.of(eventClass);
    }

    /** Explicit subscriptions; an empty array enables targeted dispatch only. */
    @SafeVarargs
    protected EventTrigger(String id, String configKey, Class<E> eventClass,
                           Class<? extends E>... subscriptions) {
        this(id, configKey, eventClass, Arrays.asList(subscriptions));
    }

    protected EventTrigger(String id, String configKey, Class<E> eventClass,
                           Collection<Class<? extends E>> subscriptions) {
        super(id, configKey, eventClass);
        Set<Class<? extends E>> classes = new LinkedHashSet<>();
        for (Class<? extends E> subscription : subscriptions) {
            Objects.requireNonNull(subscription, "subscription");
            if (!eventClass.isAssignableFrom(subscription)) {
                throw new IllegalArgumentException("Subscription must extend " + eventClass.getName());
            }
            classes.add(subscription);
        }
        eventClasses = Collections.unmodifiableSet(classes);
    }

    @Override
    public final Set<Class<? extends E>> getEventClasses() {
        return eventClasses;
    }
}
