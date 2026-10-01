package cn.superiormc.enchantmentreform.objects.triggers;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;

public final class ElytraBoostTrigger extends EventTrigger<PlayerElytraBoostEvent> {

    public ElytraBoostTrigger() {
        super("elytra_boost", "on-elytra-boost", PlayerElytraBoostEvent.class);
    }

    @Override
    protected void handle(PlayerElytraBoostEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .source(event.getPlayer())
                .skill(event.getFirework())
                .target(event.getPlayer())
                .triggerItem(event.getItemStack(), event.getHand()));
    }
}
