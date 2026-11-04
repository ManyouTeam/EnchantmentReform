package cn.superiormc.enchantmentreform.objects.triggers;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

public final class CookTrigger extends EventTrigger<Event> {

    public CookTrigger() {
        super("cook", "on-cook", Event.class, FurnaceSmeltEvent.class,
                InventoryClickEvent.class, InventoryDragEvent.class, InventoryMoveItemEvent.class);
    }

    @Override
    protected void handle(Event event, TriggerRuntime runtime) {
        if (!(event instanceof FurnaceSmeltEvent cook)) {
            runtime.trackWorkstationInput(event, FurnaceInventory.class, 0);
            return;
        }
        if (cook.isCancelled()) {
            return;
        }
        Player player = runtime.workstationPlayer(cook.getBlock());
        ItemStack result = cook.getResult();
        if (player == null || result.getType().isAir() || result.getAmount() <= 0) {
            return;
        }
        ItemStack output = result.clone();
        runtime.fire(this, runtime.base(player, cook)
                .block(cook.getBlock()).location(cook.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .outputItems(List.of(output)).triggerItem(output, null));
    }
}
