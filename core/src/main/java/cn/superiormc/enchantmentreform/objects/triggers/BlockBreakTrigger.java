package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class BlockBreakTrigger extends EventTrigger<BlockBreakEvent> {

    public BlockBreakTrigger() {
        super("block_break", "on-block-break", BlockBreakEvent.class);
    }

    @Override
    protected void handle(BlockBreakEvent event, TriggerRuntime runtime) {
        boolean internalBlockBreak = PowerManager.powerManager != null
                && PowerManager.powerManager.consumeInternalBlockBreak(event.getBlock());
        TriggerData.Builder data = runtime.base(event.getPlayer(), event)
                .block(event.getBlock()).location(event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .triggerItem(event.getPlayer().getInventory().getItemInMainHand(), EquipmentSlot.HAND)
                .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE, event.getExpToDrop());
        if (internalBlockBreak) {
            data.extra(BuiltinContextKeys.INTERNAL_BLOCK_BREAK, true);
        }
        runtime.fire(this, data);
    }
}
