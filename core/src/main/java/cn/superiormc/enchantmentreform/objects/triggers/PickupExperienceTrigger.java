package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

public final class PickupExperienceTrigger extends EventTrigger<PlayerPickupExperienceEvent> {

    public PickupExperienceTrigger() {
        super("pickup_experience", "on-pickup-experience", PlayerPickupExperienceEvent.class);
    }

    @Override
    protected void handle(PlayerPickupExperienceEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .skill(event.getExperienceOrb())
                .location(event.getExperienceOrb().getLocation())
                .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE,
                        event.getExperienceOrb().getExperience()));
    }
}
