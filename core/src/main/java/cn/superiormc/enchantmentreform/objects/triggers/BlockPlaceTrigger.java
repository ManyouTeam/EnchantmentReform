package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockPlaceTrigger extends EventTrigger<BlockPlaceEvent> {

    public BlockPlaceTrigger() {
        super("block_place", "on-block-place", BlockPlaceEvent.class);
    }

    @Override
    protected void handle(BlockPlaceEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .block(event.getBlockPlaced())
                .location(event.getBlockPlaced().getLocation().add(0.5D, 0.5D, 0.5D))
                .triggerItem(event.getItemInHand(), event.getHand()));
    }
}

