package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.block.PlayerShearBlockEvent;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class HoneyCombExtractTrigger extends EventTrigger<PlayerShearBlockEvent> {

    public HoneyCombExtractTrigger() {
        super("honey_comb_extract", "on-honey-comb-extract", PlayerShearBlockEvent.class);
    }

    @Override
    protected void handle(PlayerShearBlockEvent event, TriggerRuntime runtime) {
        Material type = event.getBlock().getType();
        if (event.isCancelled() || type != Material.BEE_NEST && type != Material.BEEHIVE) {
            return;
        }
        List<ItemStack> output = event.getDrops().stream()
                .filter(item -> item != null && item.getType() == Material.HONEYCOMB && item.getAmount() > 0)
                .map(ItemStack::clone).toList();
        if (output.isEmpty()) {
            return;
        }
        runtime.fire(this, runtime.base(event.getPlayer(), event).block(event.getBlock())
                .location(event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .outputItems(output).triggerItem(event.getItem(), event.getHand()));
    }
}
