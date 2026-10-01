package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;

public final class ProjectileTickTrigger extends EventTrigger<TriggerRuntime.ProjectileTick> {

    public ProjectileTickTrigger() {
        super("projectile_tick", "on-projectile-tick", TriggerRuntime.ProjectileTick.class);
    }

    @Override
    protected void handle(TriggerRuntime.ProjectileTick event, TriggerRuntime runtime) {
        TriggerResult result = runtime.fireTracked(this, runtime.base(event.player(), null)
                .source(event.shooter()).skill(event.projectile()).target(event.projectile())
                .location(event.projectile().getLocation()).block(event.projectile().getLocation().getBlock())
                .triggerItem(event.item(), event.slot()).tick(event.tick())
                .extra(BuiltinContextKeys.PROJECTILE, event.projectile()).build(), event.sources());
        event.complete(result);
    }
}
