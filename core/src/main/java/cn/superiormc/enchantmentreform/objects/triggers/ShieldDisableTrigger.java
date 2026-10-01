package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import org.bukkit.entity.Player;

public final class ShieldDisableTrigger extends EventTrigger<PlayerShieldDisableEvent> {

    public ShieldDisableTrigger() {
        super("shield_disable", "on-shield-disable", PlayerShieldDisableEvent.class);
    }

    @Override
    protected void handle(PlayerShieldDisableEvent event, TriggerRuntime runtime) {
        Player player = event.getPlayer();
        runtime.fire(this, runtime.base(player, event)
                .source(event.getDamager())
                .skill(event.getDamager())
                .target(player)
                .location(player.getLocation())
                .triggerItem());
    }
}
