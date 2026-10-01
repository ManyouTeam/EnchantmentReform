package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class SpawnTrigger extends EventTrigger<Event> {

    public SpawnTrigger() {
        super("spawn", "on-spawn", Event.class, PlayerJoinEvent.class, PlayerRespawnEvent.class);
    }

    @Override
    protected void handle(Event event, TriggerRuntime runtime) {
        Player player;
        if (event instanceof PlayerJoinEvent join) {
            player = join.getPlayer();
            SchedulerUtil.runTaskLater(player, () -> runtime.fire(this,
                    runtime.base(player, join).block(player.getLocation().getBlock())), 1L);
            return;
        }
        if (event instanceof PlayerRespawnEvent respawn) {
            player = respawn.getPlayer();
            SchedulerUtil.runTaskLater(player, () -> runtime.fire(this,
                    runtime.base(player, respawn).location(respawn.getRespawnLocation())
                            .block(respawn.getRespawnLocation().getBlock())), 1L);
        }
    }
}

