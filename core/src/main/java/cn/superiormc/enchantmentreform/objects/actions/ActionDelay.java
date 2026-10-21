package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ActionDelay<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    private final DelayedActionsFactory<S, C> runner;

    public ActionDelay(DelayedActionsFactory<S, C> runner) {
        super("delay");
        this.runner = runner;
        setRequiredArgs("time", "actions");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        long time = singleAction.getSection().getLong("time");
        DelayedActionsRunner delayedRunner = runner.create(chanceSection, singleAction, context);
        UUID playerId = player.getUniqueId();
        SchedulerUtil.runTaskLater(player, () -> {
            Player onlinePlayer = Bukkit.getPlayer(playerId);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                delayedRunner.run(onlinePlayer);
            }
        }, time);
    }

    @FunctionalInterface
    public interface DelayedActionsFactory<S, C> {
        DelayedActionsRunner create(ConfigurationSection section, S singleAction, C context);
    }

    @FunctionalInterface
    public interface DelayedActionsRunner {
        void run(Player player);
    }
}
