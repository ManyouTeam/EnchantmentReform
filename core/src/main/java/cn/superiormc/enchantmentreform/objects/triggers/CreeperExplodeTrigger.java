package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public final class CreeperExplodeTrigger extends EventTrigger<ExplosionPrimeEvent> {

    public CreeperExplodeTrigger() {
        super("creeper_explode", "on-creeper-explode", ExplosionPrimeEvent.class);
    }

    @Override
    protected void handle(ExplosionPrimeEvent event, TriggerRuntime runtime) {
        Player owner = event.getEntity() instanceof Player player ? player
                : event.getEntity() instanceof Creeper creeper
                && creeper.getTarget() instanceof Player player ? player : null;
        if (owner != null) {
            runtime.fire(this, runtime.base(owner, event)
                    .source(event.getEntity()).skill(event.getEntity()).target(owner)
                    .location(event.getEntity().getLocation())
                    .extra(BuiltinContextKeys.ORIGINAL_RADIUS, event.getRadius()));
        }
    }
}

