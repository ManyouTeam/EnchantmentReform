package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.event.player.PlayerItemDamageEvent;

public final class ItemDamageTrigger extends EventTrigger<PlayerItemDamageEvent> {

    public ItemDamageTrigger() {
        super("item_damage", "on-item-damage", PlayerItemDamageEvent.class);
    }

    @Override
    protected void handle(PlayerItemDamageEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .triggerItem(event.getItem(), runtime.findSlot(event.getPlayer(), event.getItem()))
                .extra(BuiltinContextKeys.ORIGINAL_ITEM_DAMAGE, event.getDamage()));
    }
}
