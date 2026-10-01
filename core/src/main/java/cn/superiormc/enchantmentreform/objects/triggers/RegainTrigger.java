package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public final class RegainTrigger extends EventTrigger<EntityRegainHealthEvent> {

    public RegainTrigger() {
        super("regain", "on-regain", EntityRegainHealthEvent.class);
    }

    @Override
    protected void handle(EntityRegainHealthEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            runtime.fire(this, runtime.base(player, event)
                    .extra(BuiltinContextKeys.ORIGINAL_AMOUNT, event.getAmount()));
        }
    }
}

