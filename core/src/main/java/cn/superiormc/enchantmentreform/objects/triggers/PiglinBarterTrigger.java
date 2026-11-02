package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class PiglinBarterTrigger extends EventTrigger<PiglinBarterEvent> {

    public PiglinBarterTrigger() {
        super("piglin_barter", "on-piglin-barter", PiglinBarterEvent.class);
    }

    @Override
    protected void handle(PiglinBarterEvent event, TriggerRuntime runtime) {
        Player player = runtime.takeBarterPlayer(event.getEntity().getUniqueId());
        if (player == null) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .outputItems(event.getOutcome())
                .source(event.getEntity())
                .target(event.getEntity())
                .location(event.getEntity().getLocation())
                .triggerItem(event.getInput(), EquipmentSlot.HAND));
    }
}
