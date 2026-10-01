package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public final class DeathTrigger extends EventTrigger<EntityDeathEvent> {

    public DeathTrigger() {
        super("death", "on-death", EntityDeathEvent.class);
    }

    @Override
    protected void handle(EntityDeathEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            Player killer = event.getEntity().getKiller();
            runtime.fire(this, runtime.base(player, event)
                    .source(killer).skill(killer).target(player).location(player.getLocation())
                    .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE, event.getDroppedExp()));
        }
        runtime.clearEntity(event.getEntity().getUniqueId());
    }
}
