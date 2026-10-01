package cn.superiormc.enchantmentreform.objects.triggers;

import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

public final class EndermanAttackPlayerTrigger extends EventTrigger<EndermanAttackPlayerEvent> {

    public EndermanAttackPlayerTrigger() {
        super("enderman_attack_player", "on-enderman-attack-player", EndermanAttackPlayerEvent.class);
    }

    @Override
    protected void handle(EndermanAttackPlayerEvent event, TriggerRuntime runtime) {
        Player player = event.getPlayer();
        Enderman enderman = event.getEntity();
        runtime.fire(this, runtime.base(player, event)
                .source(enderman).skill(enderman).target(player)
                .location(player.getLocation()));
    }
}
