package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerData;

public final class ActivateTrigger extends EventTrigger<TriggerRuntime.Activation> {

    public ActivateTrigger() {
        super("activate", "on-activate", TriggerRuntime.Activation.class);
    }

    @Override
    protected void handle(TriggerRuntime.Activation event, TriggerRuntime runtime) {
        TriggerData data = runtime.base(event.player(), event.event())
                .block(event.player().getLocation().getBlock())
                .triggerItem(event.active().item(), event.active().slot()).build();
        runtime.fireActive(this, data, event.active());
    }
}
