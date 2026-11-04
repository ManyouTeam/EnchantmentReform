package cn.superiormc.enchantmentreform.objects.triggers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public final class BrewTrigger extends EventTrigger<Event> {

    public BrewTrigger() {
        super("brew", "on-brew", Event.class, BrewEvent.class,
                InventoryClickEvent.class, InventoryDragEvent.class, InventoryMoveItemEvent.class);
    }

    @Override
    protected void handle(Event event, TriggerRuntime runtime) {
        if (!(event instanceof BrewEvent brew)) {
            runtime.trackWorkstationInput(event, BrewerInventory.class, 0, 1, 2, 3);
            return;
        }
        if (brew.isCancelled()) {
            return;
        }
        Player player = runtime.workstationPlayer(brew.getBlock());
        if (player == null) {
            return;
        }
        List<ItemStack> results = new ArrayList<>();
        List<ItemStack> brewed = brew.getResults();
        for (int slot = 0; slot < Math.min(3, brewed.size()); slot++) {
            ItemStack result = brewed.get(slot);
            ItemStack previous = brew.getContents().getItem(slot);
            if (result != null && !result.getType().isAir() && result.getAmount() > 0
                    && (previous == null || !result.isSimilar(previous))) {
                results.add(result.clone());
            }
        }
        if (results.isEmpty()) {
            return;
        }
        runtime.fire(this, runtime.base(player, brew)
                .block(brew.getBlock()).location(brew.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .outputItems(results).triggerItem(results.getFirst(), null));
    }
}
