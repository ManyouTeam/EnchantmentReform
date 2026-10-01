package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class RespawnTrigger extends EventTrigger<PlayerRespawnEvent> {

    public RespawnTrigger() {
        super("respawn", "on-respawn", PlayerRespawnEvent.class);
    }

    @Override
    protected void handle(PlayerRespawnEvent event, TriggerRuntime runtime) {
        Player player = event.getPlayer();
        SchedulerUtil.runTaskLater(player, () -> runtime.fire(this,
                runtime.base(player, event).location(event.getRespawnLocation())
                        .block(event.getRespawnLocation().getBlock())), 1L);
    }
}

