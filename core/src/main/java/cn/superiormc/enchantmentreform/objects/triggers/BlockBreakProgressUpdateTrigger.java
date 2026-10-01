package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class BlockBreakProgressUpdateTrigger extends EventTrigger<BlockBreakProgressUpdateEvent> {

    public BlockBreakProgressUpdateTrigger() {
        super("block_break_progress_update", "on-block-break-progress-update",
                BlockBreakProgressUpdateEvent.class);
    }

    @Override
    protected void handle(BlockBreakProgressUpdateEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .block(event.getBlock())
                .location(event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .triggerItem(player.getInventory().getItemInMainHand(), EquipmentSlot.HAND)
                .extra(BuiltinContextKeys.BLOCK_BREAK_PROGRESS, event.getProgress()));
    }
}
