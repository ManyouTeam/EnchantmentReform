package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class ActionConditional<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    private final Predicate<S> canRun;
    private final ConditionsRunner<S, C> conditionsRunner;
    private final ActionChance.ActionsRunner<S, C> actionsRunner;

    public ActionConditional(ConditionsRunner<S, C> conditionsRunner,
                             ActionChance.ActionsRunner<S, C> actionsRunner) {
        this(singleAction -> true, conditionsRunner, actionsRunner);
    }

    public ActionConditional(Predicate<S> canRun,
                             ConditionsRunner<S, C> conditionsRunner,
                             ActionChance.ActionsRunner<S, C> actionsRunner) {
        super("conditional");
        this.canRun = canRun;
        this.conditionsRunner = conditionsRunner;
        this.actionsRunner = actionsRunner;
        setRequiredArgs("actions", "conditions");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        if (!canRun.test(singleAction)) {
            return;
        }
        ConfigurationSection conditionSection = singleAction.getSection().getConfigurationSection("conditions");
        if (conditionSection == null || !conditionsRunner.check(conditionSection, singleAction, player, context)) {
            return;
        }
        ConfigurationSection actionSection = singleAction.getSection().getConfigurationSection("actions");
        if (actionSection != null) {
            actionsRunner.run(actionSection, singleAction, player, context);
        }
    }

    @FunctionalInterface
    public interface ConditionsRunner<S, C> {
        boolean check(ConfigurationSection section, S singleAction, Player player, C context);
    }
}
