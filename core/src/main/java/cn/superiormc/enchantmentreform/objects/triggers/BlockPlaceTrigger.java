package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockPlaceTrigger extends EventTrigger<BlockPlaceEvent> {

    public BlockPlaceTrigger() {
        super("block_place", "on-block-place", BlockPlaceEvent.class);
    }

    @Override
    protected void handle(BlockPlaceEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .placedBlocks(event instanceof org.bukkit.event.block.BlockMultiPlaceEvent multi
                        ? multi.getReplacedBlockStates().stream().map(state -> state.getBlock().getState()).toList()
                        : java.util.List.of(event.getBlockPlaced().getState()))
                .block(event.getBlockPlaced())
                .location(event.getBlockPlaced().getLocation().add(0.5D, 0.5D, 0.5D))
                .triggerItem(event.getItemInHand(), event.getHand()));
    }
}

