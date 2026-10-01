package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import org.bukkit.entity.Player;

public final class WardenAngerTrigger extends EventTrigger<WardenAngerChangeEvent> {

    public WardenAngerTrigger() {
        super("warden_anger_change", "on-warden-anger-change", WardenAngerChangeEvent.class);
    }

    @Override
    protected void handle(WardenAngerChangeEvent event, TriggerRuntime runtime) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(player)
                .skill(event.getEntity())
                .target(event.getEntity())
                .location(event.getEntity().getLocation()));
    }
}