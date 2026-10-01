package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class KnockbackTrigger extends EventTrigger<EntityKnockbackEvent> {

    public KnockbackTrigger() {
        super("knockback", "on-knockback", EntityKnockbackEvent.class);
    }

    @Override
    protected void handle(EntityKnockbackEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Entity source = event instanceof EntityPushedByEntityAttackEvent pushed
                ? pushed.getPushedBy() : player;
        runtime.fire(this, runtime.base(player, event)
                .source(source)
                .skill(source)
                .target(player));
    }
}
