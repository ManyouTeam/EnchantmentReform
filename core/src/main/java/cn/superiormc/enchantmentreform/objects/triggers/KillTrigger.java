package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public final class KillTrigger extends EventTrigger<EntityDeathEvent> {

    public KillTrigger() {
        super("kill", "on-kill", EntityDeathEvent.class);
    }

    @Override
    protected void handle(EntityDeathEvent event, TriggerRuntime runtime) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        runtime.fire(this, runtime.base(killer, event)
                .source(killer).skill(killer).target(event.getEntity())
                .location(event.getEntity().getLocation())
                .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE, event.getDroppedExp()));
    }
}
