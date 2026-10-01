package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

public final class UntagTrigger extends EventTrigger<EntityTargetEvent> {

    public UntagTrigger() {
        super("untag", "on-untag", EntityTargetEvent.class);
    }

    @Override
    protected void handle(EntityTargetEvent event, TriggerRuntime runtime) {
        if (event.getTarget() instanceof Player) return;
        Player player = runtime.removeTarget(event.getEntity().getUniqueId());
        if (player != null) {
            runtime.fire(this, runtime.base(player, event)
                    .source(event.getEntity()).skill(event.getEntity()).target(player)
                    .location(player.getLocation()).extra(BuiltinContextKeys.TARGET_REASON, event.getReason()));
        }
    }
}

