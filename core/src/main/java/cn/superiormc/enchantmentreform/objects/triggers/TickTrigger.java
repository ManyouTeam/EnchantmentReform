package cn.superiormc.enchantmentreform.objects.triggers;


public final class TickTrigger extends EventTrigger<TriggerRuntime.PlayerTick> {

    public TickTrigger() {
        super("tick", "on-tick", TriggerRuntime.PlayerTick.class);
    }

    @Override
    protected void handle(TriggerRuntime.PlayerTick event, TriggerRuntime runtime) {
        if (!event.player().isOnline()) return;
        runtime.fire(this, runtime.base(event.player(), null)
                .block(event.player().getLocation().getBlock()).tick(event.tick()));
    }
}

