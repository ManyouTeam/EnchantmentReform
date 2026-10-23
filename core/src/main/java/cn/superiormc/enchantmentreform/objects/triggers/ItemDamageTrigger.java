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
                .extra(BuiltinContextKeys.ORIGINAL_ITEM_DAMAGE, event.getDamage())
                .extra(BuiltinContextKeys.EFFECTIVE_ITEM_DAMAGE, effectiveDamage(event)));
    }

    private static int effectiveDamage(PlayerItemDamageEvent event) {
        var item = event.getItem();
        var meta = item.getItemMeta();
        if (!(meta instanceof org.bukkit.inventory.meta.Damageable damage) || meta.isUnbreakable()) return 0;
        int maximum = damage.hasMaxDamage() ? damage.getMaxDamage() : item.getType().getMaxDurability();
        return Math.max(0, Math.min(event.getDamage(), Math.max(0, maximum - damage.getDamage())));
    }
}
