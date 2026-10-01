package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class EnchantItemTrigger extends EventTrigger<EnchantItemEvent> {

    public EnchantItemTrigger() {
        super("enchant_item", "on-enchant-item", EnchantItemEvent.class);
    }

    @Override
    protected void handle(EnchantItemEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getEnchanter(), event)
                .triggerItem(event.getItem(), EquipmentSlot.HAND)
                .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE, event.getExpLevelCost())
                .extra(BuiltinContextKeys.ENCHANTMENT_LEVEL_COST,
                        Math.max(0, event.whichButton() + 1))
                .extra(BuiltinContextKeys.ORIGINAL_LAPIS,
                        Math.max(0, event.whichButton() + 1)));
    }
}
