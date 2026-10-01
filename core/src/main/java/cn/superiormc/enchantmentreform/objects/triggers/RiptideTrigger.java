package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class RiptideTrigger extends EventTrigger<PlayerRiptideEvent> {

    public RiptideTrigger() {
        super("riptide", "on-riptide", PlayerRiptideEvent.class);
    }

    @Override
    protected void handle(PlayerRiptideEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .triggerItem(event.getItem(), EquipmentSlot.HAND));
    }
}

