package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Predicate;

public class ActionDelay<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    private final Predicate<S> canRun;

    private final DelayedActionsFactory<S, C> runner;

    public ActionDelay(ActionChance.ActionsRunner<S, C> runner) {
        this(singleAction -> true, (section, singleAction, context) ->
                onlinePlayer -> runner.run(section, singleAction, onlinePlayer, context));
    }

    public ActionDelay(Predicate<S> canRun, DelayedActionsFactory<S, C> runner) {
        super("delay");
        this.canRun = canRun;
        this.runner = runner;
        setRequiredArgs("time", "actions");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        if (!canRun.test(singleAction)) {
            return;
        }
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        long time = singleAction.getSection().getLong("time");
        DelayedActionsRunner delayedRunner = runner.create(chanceSection, singleAction, context);
        UUID playerId = player.getUniqueId();
        SchedulerUtil.runTaskLater(() -> {
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
