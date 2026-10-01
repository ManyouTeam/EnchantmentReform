package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class InsideBlockTrigger extends EventTrigger<EntityInsideBlockEvent> {

    public InsideBlockTrigger() {
        super("inside_block", "on-inside-block", EntityInsideBlockEvent.class);
    }

    @Override
    protected void handle(EntityInsideBlockEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Block block = event.getBlock();
        runtime.fire(this, runtime.base(player, event)
                .block(block)
                .location(block.getLocation().add(0.5D, 0.5D, 0.5D)));
    }
}
