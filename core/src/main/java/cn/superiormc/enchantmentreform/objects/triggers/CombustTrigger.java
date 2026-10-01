package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;

public final class CombustTrigger extends EventTrigger<EntityCombustEvent> {

    public CombustTrigger() {
        super("combust", "on-combust", EntityCombustEvent.class);
    }

    @Override
    protected void handle(EntityCombustEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            runtime.fire(this, runtime.base(player, event)
                    .extra(BuiltinContextKeys.ORIGINAL_DURATION, event.getDuration()));
        }
    }
}

