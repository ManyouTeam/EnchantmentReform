package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.event.player.PlayerExpChangeEvent;

public final class ExpChangeTrigger extends EventTrigger<PlayerExpChangeEvent> {

    public ExpChangeTrigger() {
        super("exp_change", "on-exp-change", PlayerExpChangeEvent.class);
    }

    @Override
    protected void handle(PlayerExpChangeEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE, event.getAmount()));
    }
}
