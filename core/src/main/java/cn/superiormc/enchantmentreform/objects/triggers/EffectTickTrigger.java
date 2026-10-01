package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.entity.EntityEffectTickEvent;
import org.bukkit.entity.Player;

public final class EffectTickTrigger extends EventTrigger<EntityEffectTickEvent> {

    public EffectTickTrigger() {
        super("effect_tick", "on-effect-tick", EntityEffectTickEvent.class);
    }

    @Override
    protected void handle(EntityEffectTickEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            runtime.fire(this, runtime.base(player, event));
        }
    }
}
