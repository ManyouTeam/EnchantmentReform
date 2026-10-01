package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

public final class TargetTrigger extends EventTrigger<EntityTargetEvent> {

    public TargetTrigger() {
        super("target", "on-target", EntityTargetEvent.class);
    }

    @Override
    protected void handle(EntityTargetEvent event, TriggerRuntime runtime) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        runtime.rememberTarget(
                event.getEntity().getUniqueId(),
                player.getUniqueId(),
                event.getEntity() instanceof Monster);
        var result = runtime.fire(this, runtime.base(player, event)
                .source(event.getEntity()).skill(event.getEntity()).target(player)
                .location(player.getLocation())
                .extra(BuiltinContextKeys.TARGET_REASON, event.getReason())
                .extra(BuiltinContextKeys.TARGET_COUNT, runtime.countTargets(player.getUniqueId())));
        if (result.cancelled()) {
            runtime.removeTarget(event.getEntity().getUniqueId());
        }
    }
}

