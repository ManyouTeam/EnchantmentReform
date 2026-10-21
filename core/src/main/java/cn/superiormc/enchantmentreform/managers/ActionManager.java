package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.actions.AbstractRunAction;
import cn.superiormc.enchantmentreform.objects.actions.ActionActionBar;
import cn.superiormc.enchantmentreform.objects.actions.ActionAnnouncement;
import cn.superiormc.enchantmentreform.objects.actions.ActionAny;
import cn.superiormc.enchantmentreform.objects.actions.ActionChance;
import cn.superiormc.enchantmentreform.objects.actions.ActionClose;
import cn.superiormc.enchantmentreform.objects.actions.ActionConsoleCommand;
import cn.superiormc.enchantmentreform.objects.actions.ActionDelay;
import cn.superiormc.enchantmentreform.objects.actions.ActionEffect;
import cn.superiormc.enchantmentreform.objects.actions.ActionEntitySpawn;
import cn.superiormc.enchantmentreform.objects.actions.ActionMessage;
import cn.superiormc.enchantmentreform.objects.actions.ActionOPCommand;
import cn.superiormc.enchantmentreform.objects.actions.ActionParticle;
import cn.superiormc.enchantmentreform.objects.actions.ActionPlayerCommand;
import cn.superiormc.enchantmentreform.objects.actions.ActionSound;
import cn.superiormc.enchantmentreform.objects.actions.ActionTeleport;
import cn.superiormc.enchantmentreform.objects.actions.ActionTitle;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class ActionManager<S extends AbstractConfiguredSection<C>, C> {

    private final KeyedRegistry<String, AbstractRunAction<S, C>> actions = KeyedRegistry.stringTypes();

    public ActionManager() {
        registerNewAction("message", new ActionMessage<>());
    }

    public ActionManager(ActionChance.ActionsRunner<S, C> actionsRunner,
                         ActionAny.RandomActionsRunner<S, C> randomActionsRunner,
                         ActionDelay.DelayedActionsFactory<S, C> delayedActionsFactory,
                         boolean close) {
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
        if (close) {
            registerNewAction("close", new ActionClose<>());
        }
        registerNewAction("chance", new ActionChance<>(actionsRunner));
        registerNewAction("delay", new ActionDelay<>(delayedActionsFactory));
        registerNewAction("any", new ActionAny<>(randomActionsRunner));
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
        return type == null ? "" : type.toLowerCase(Locale.ROOT).replace('-', '_');
    }

}
