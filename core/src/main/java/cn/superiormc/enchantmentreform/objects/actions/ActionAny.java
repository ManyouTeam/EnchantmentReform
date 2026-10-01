package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ActionAny<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    private final RandomActionsRunner<S, C> runner;

    public ActionAny(RandomActionsRunner<S, C> runner) {
        super("any");
        this.runner = runner;
        setRequiredArgs("actions");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        runner.run(chanceSection, singleAction, player, context, singleAction.getInt("amount", 1));
    }

    @FunctionalInterface
    public interface RandomActionsRunner<S, C> {
        void run(ConfigurationSection section, S singleAction, Player player, C context, int amount);
    }
}
