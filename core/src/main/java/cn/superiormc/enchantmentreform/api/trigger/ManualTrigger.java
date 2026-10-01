package cn.superiormc.enchantmentreform.api.trigger;

import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import org.bukkit.NamespacedKey;

import java.util.Set;

public final class ManualTrigger extends AbstractTrigger<Object> {

    public ManualTrigger(NamespacedKey key, String configKey) {
        super(key, configKey, Object.class);
    }

    @Override
    public Set<Class<? extends Object>> getEventClasses() {
        return Set.of();
    }

    @Override
    protected void handle(Object event, TriggerRuntime runtime) {
    }
}
