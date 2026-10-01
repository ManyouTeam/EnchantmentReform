package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Entity;

public final class TargetTickTrigger extends EventTrigger<TriggerRuntime.PlayerTick> {

    public TargetTickTrigger() {
        super("target_tick", "on-target-tick", TriggerRuntime.PlayerTick.class);
    }

    @Override
    protected void handle(TriggerRuntime.PlayerTick event, TriggerRuntime runtime) {
        if (!event.player().isOnline()) return;
        var trace = event.player().getWorld().rayTraceEntities(
                event.player().getEyeLocation(), event.player().getEyeLocation().getDirection(), 32.0D,
                entity -> entity != event.player());
        Entity target = trace == null ? null : trace.getHitEntity();
        if (target != null) {
            runtime.fire(this, runtime.base(event.player(), null)
                    .target(target).location(target.getLocation())
                    .block(target.getLocation().getBlock()).tick(event.tick()));
        }
    }
}

