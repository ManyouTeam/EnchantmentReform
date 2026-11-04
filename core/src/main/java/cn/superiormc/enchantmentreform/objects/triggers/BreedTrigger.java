package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityBreedEvent;

public final class BreedTrigger extends EventTrigger<EntityBreedEvent> {

    public BreedTrigger() {
        super("breed", "on-breed", EntityBreedEvent.class);
    }

    @Override
    protected void handle(EntityBreedEvent event, TriggerRuntime runtime) {
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .target(event.getEntity()).location(event.getEntity().getLocation())
                .triggerItem(event.getBredWith(), null));
    }
}
