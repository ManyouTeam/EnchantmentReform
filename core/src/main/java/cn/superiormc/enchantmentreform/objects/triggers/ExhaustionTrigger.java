package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExhaustionEvent;

public final class ExhaustionTrigger extends EventTrigger<EntityExhaustionEvent> {

    public ExhaustionTrigger() {
        super("exhaustion", "on-exhaustion", EntityExhaustionEvent.class);
    }

    @Override
    protected void handle(EntityExhaustionEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            runtime.fire(this, runtime.base(player, event));
        }
    }
}
