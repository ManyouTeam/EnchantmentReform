package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.actions.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;

public class ActionManager<S extends AbstractConfiguredSection<C>, C> {

    private final KeyedRegistry<String, AbstractRunAction<S, C>> actions = KeyedRegistry.stringTypes();

    public ActionManager() {
    }

    public ActionManager(BuiltInActionOptions<S, C> options) {
        registerBuiltInActions(options);
    }

    public void registerBuiltInActions(BuiltInActionOptions<S, C> options) {
        registerNewAction("message", new ActionMessage<>());
        registerNewAction("title", new ActionTitle<>());
        registerNewAction("action_bar", new ActionActionBar<>());
        registerNewAction("particle", new ActionParticle<>());
        registerNewAction("sound", new ActionSound<>());
        registerNewAction("announcement", new ActionAnnouncement<>());
        registerNewAction("effect", new ActionEffect<>());
        registerNewAction("console_command", new ActionConsoleCommand<>());
        registerNewAction("op_command", new ActionOPCommand<>());
        registerNewAction("player_command", new ActionPlayerCommand<>());
        registerNewAction("teleport", new ActionTeleport<>());
        registerNewAction("entity_spawn", new ActionEntitySpawn<>());
        if (options.close()) {
            registerNewAction("close", new ActionClose<>());
        }
        if (options.mythicMobsSpawn()) {
            registerNewAction("mythicmobs_spawn", new ActionMythicMobsSpawn<>());
        }
        registerNewAction("chance", new ActionChance<>(options.actionsRunner()));
        registerNewAction("delay", new ActionDelay<>(options.canRunDelay(), options.delayedActionsFactory()));
        registerNewAction("any", new ActionAny<>(options.randomActionsRunner()));
        registerNewAction("conditional", new ActionConditional<>(
                options.canRunConditional(),
                options.conditionsRunner(),
                options.actionsRunner()));
    }

    public void registerNewAction(String actionID, AbstractRunAction<S, C> action) {
        actions.register(normalize(actionID), action);
    }

    public void doAction(S action, Player player, C context) {
        if (action == null || player == null) {
            return;
        }
        AbstractRunAction<S, C> runAction = actions.get(normalize(action.getString("type")));
        if (runAction != null) {
            runAction.runAction(action, player, context);
        }
    }

    public void doAction(S action, Player player) {
        doAction(action, player, null);
    }

    public List<String> getActionTypes() {
        return actions.keys();
    }

    private String normalize(String type) {
        return type == null ? "" : type.toLowerCase().replace('-', '_');
    }

    public record BuiltInActionOptions<S extends AbstractConfiguredSection<C>, C>(
            ActionChance.ActionsRunner<S, C> actionsRunner,
            ActionAny.RandomActionsRunner<S, C> randomActionsRunner,
            ActionDelay.DelayedActionsFactory<S, C> delayedActionsFactory,
            ActionConditional.ConditionsRunner<S, C> conditionsRunner,
            Predicate<S> canRunDelay,
            Predicate<S> canRunConditional,
            boolean close,
            boolean mythicMobsSpawn
    ) {

        public BuiltInActionOptions(
                ActionChance.ActionsRunner<S, C> actionsRunner,
                ActionAny.RandomActionsRunner<S, C> randomActionsRunner,
                ActionDelay.DelayedActionsFactory<S, C> delayedActionsFactory,
                ActionConditional.ConditionsRunner<S, C> conditionsRunner) {
            this(actionsRunner, randomActionsRunner, delayedActionsFactory, conditionsRunner,
                    singleAction -> true, singleAction -> true, false, false);
        }
    }
}
