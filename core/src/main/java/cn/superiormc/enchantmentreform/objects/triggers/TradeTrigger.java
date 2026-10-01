package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class TradeTrigger extends EventTrigger<PlayerTradeEvent> {

    public TradeTrigger() {
        super("trade", "on-trade", PlayerTradeEvent.class);
    }

    @Override
    protected void handle(PlayerTradeEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .target(event.getMerchant())
                .location(event.getMerchant().getLocation())
                .triggerItem(event.getTrade().getResult(), EquipmentSlot.HAND));
    }
}
