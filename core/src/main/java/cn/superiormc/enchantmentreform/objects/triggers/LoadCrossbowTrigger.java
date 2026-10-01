package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.entity.Player;

public final class LoadCrossbowTrigger extends EventTrigger<EntityLoadCrossbowEvent> {

    public LoadCrossbowTrigger() {
        super("load_crossbow", "on-load-crossbow", EntityLoadCrossbowEvent.class);
    }

    @Override
    protected void handle(EntityLoadCrossbowEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        runtime.fire(this, runtime.base(player, event)
                .triggerItem(event.getCrossbow(), event.getHand()));
    }
}
