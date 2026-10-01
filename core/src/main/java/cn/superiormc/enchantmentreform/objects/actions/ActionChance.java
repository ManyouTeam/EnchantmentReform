package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ActionChance<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    private final ActionsRunner<S, C> runner;

    public ActionChance(ActionsRunner<S, C> runner) {
        super("chance");
        this.runner = runner;
        setRequiredArgs("rate", "actions");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        ConfigurationSection chanceSection = singleAction.getSection().getConfigurationSection("actions");
        if (chanceSection == null) {
            return;
        }
        double rate = singleAction.getDouble("rate", player, context);
        if (RandomUtils.nextDouble(0, 100) <= rate) {
            runner.run(chanceSection, singleAction, player, context);
        }
    }

    @FunctionalInterface
    public interface ActionsRunner<S, C> {
        void run(ConfigurationSection section, S singleAction, Player player, C context);
    }
}
