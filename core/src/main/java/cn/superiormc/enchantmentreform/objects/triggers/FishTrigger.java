package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class FishTrigger extends EventTrigger<PlayerFishEvent> {

    public FishTrigger() {
        super("fish", "on-fish", PlayerFishEvent.class);
    }

    @Override
    protected void handle(PlayerFishEvent event, TriggerRuntime runtime) {
        EquipmentSlot hand = event.getHand() == EquipmentSlot.OFF_HAND
                ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
        ItemStack rod = hand == EquipmentSlot.OFF_HAND
                ? event.getPlayer().getInventory().getItemInOffHand()
                : event.getPlayer().getInventory().getItemInMainHand();
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .skill(event.getHook())
                .target(event.getCaught()).location(event.getHook().getLocation())
                .triggerItem(rod, hand)
                .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE, event.getExpToDrop()));
    }
}
