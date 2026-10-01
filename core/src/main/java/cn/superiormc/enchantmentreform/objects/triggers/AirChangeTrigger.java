package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityAirChangeEvent;

public final class AirChangeTrigger extends EventTrigger<EntityAirChangeEvent> {

    public AirChangeTrigger() {
        super("air_change", "on-air-change", EntityAirChangeEvent.class);
    }

    @Override
    protected void handle(EntityAirChangeEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            runtime.fire(this, runtime.base(player, event)
                    .extra(BuiltinContextKeys.ORIGINAL_AIR, event.getAmount())
                    .extra(BuiltinContextKeys.PREVIOUS_AIR, player.getRemainingAir()));
        }
    }
}
