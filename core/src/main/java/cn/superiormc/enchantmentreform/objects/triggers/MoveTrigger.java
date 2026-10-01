package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MoveTrigger extends EventTrigger<PlayerMoveEvent> {

    public MoveTrigger() {
        super("move", "on-move", PlayerMoveEvent.class);
    }

    @Override
    protected void handle(PlayerMoveEvent event, TriggerRuntime runtime) {
        if (!EnchantmentReform.methodUtil.methodID().equals("paper") || event.hasChangedBlock()) {
            runtime.fire(this, runtime.movement(event.getPlayer(), event, event.getFrom(), event.getTo()));
        }
    }
}

