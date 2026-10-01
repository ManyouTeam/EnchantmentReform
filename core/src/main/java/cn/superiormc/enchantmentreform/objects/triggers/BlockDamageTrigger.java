package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class BlockDamageTrigger extends EventTrigger<BlockDamageEvent> {

    public BlockDamageTrigger() {
        super("block_damage", "on-block-damage", BlockDamageEvent.class);
    }

    @Override
    protected void handle(BlockDamageEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .block(event.getBlock()).location(event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .triggerItem(event.getItemInHand(), EquipmentSlot.HAND));
    }
}

