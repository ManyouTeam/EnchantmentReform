package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Fires when a piglin completes a barter (throws out its reward items).
 * <p>
 * The {@code PiglinBarterEvent} does not carry the player who offered the gold ingot, so the
 * thrower is resolved through {@link TriggerRuntime#removeTarget}, which was recorded earlier
 * when the piglin picked up the gold ingot (see the {@code EntityPickupItemEvent} handling in
 * {@code EnchantmentPowerListener}).
 */
public final class PiglinBarterTrigger extends EventTrigger<PiglinBarterEvent> {

    public PiglinBarterTrigger() {
        super("piglin_barter", "on-piglin-barter", PiglinBarterEvent.class);
    }

    @Override
    protected void handle(PiglinBarterEvent event, TriggerRuntime runtime) {
        Player player = runtime.removeTarget(event.getEntity().getUniqueId());
        if (player == null) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(event.getEntity())
                .target(event.getEntity())
                .location(event.getEntity().getLocation())
                .triggerItem(event.getInput(), EquipmentSlot.HAND));
    }
}
