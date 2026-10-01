package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class BlockDropItemTrigger extends EventTrigger<BlockDropItemEvent> {

    public BlockDropItemTrigger() {
        super("block_drop_item", "on-block-drop-item", BlockDropItemEvent.class);
    }

    @Override
    protected void handle(BlockDropItemEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .block(event.getBlock()).location(event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .triggerItem(event.getPlayer().getInventory().getItemInMainHand(), EquipmentSlot.HAND)
                .extra(BuiltinContextKeys.BROKEN_BLOCK_STATE, event.getBlockState()));
    }
}

