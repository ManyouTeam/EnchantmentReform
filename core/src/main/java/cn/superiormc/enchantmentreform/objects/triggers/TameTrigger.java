package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTameEvent;

public final class TameTrigger extends EventTrigger<EntityTameEvent> {

    public TameTrigger() {
        super("tame", "on-tame", EntityTameEvent.class);
    }

    @Override
    protected void handle(EntityTameEvent event, TriggerRuntime runtime) {
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .target(event.getEntity()).location(event.getEntity().getLocation()));
    }
}
