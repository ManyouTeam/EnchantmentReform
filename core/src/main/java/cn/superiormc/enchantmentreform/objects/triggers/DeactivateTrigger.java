package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerData;

public final class DeactivateTrigger extends EventTrigger<TriggerRuntime.Deactivation> {

    public DeactivateTrigger() {
        super("deactivate", "on-deactivate", TriggerRuntime.Deactivation.class);
    }

    @Override
    protected void handle(TriggerRuntime.Deactivation event, TriggerRuntime runtime) {
        TriggerData data = runtime.base(event.player(), event.event())
                .block(event.player().getLocation().getBlock())
                .triggerItem(event.active().item(), event.active().slot()).build();
        runtime.fireActive(this, data, event.active());
    }
}
