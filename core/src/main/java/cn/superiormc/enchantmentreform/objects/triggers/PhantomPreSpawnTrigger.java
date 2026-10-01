package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class PhantomPreSpawnTrigger extends EventTrigger<PhantomPreSpawnEvent> {

    public PhantomPreSpawnTrigger() {
        super("phantom_pre_spawn", "on-phantom-pre-spawn", PhantomPreSpawnEvent.class);
    }

    @Override
    protected void handle(PhantomPreSpawnEvent event, TriggerRuntime runtime) {
        Entity spawningEntity = event.getSpawningEntity();
        if (!(spawningEntity instanceof Player player)) {
            return;
        }

        TriggerResult result = runtime.fire(this, runtime.base(player, event)
                .location(event.getSpawnLocation())
                .block(event.getSpawnLocation().getBlock()));
        if (result.cancelled()) {
            event.setShouldAbortSpawn(true);
        }
    }
}
