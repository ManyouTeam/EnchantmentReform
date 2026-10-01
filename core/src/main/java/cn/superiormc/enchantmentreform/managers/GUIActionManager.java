package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.gui.action.ActionGUIPage;
import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;
import cn.superiormc.enchantmentreform.gui.action.ObjectSingleGUIAction;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class GUIActionManager {

    public static final GUIActionManager INSTANCE = new GUIActionManager();

    private final ActionManager<ObjectSingleGUIAction, GUIActionContext> actionManager;

    private GUIActionManager() {
        actionManager = new ActionManager<>(new ActionManager.BuiltInActionOptions<>(
                (section, parent, player, context) -> createActions(section).runAllActions(player, context),
                (section, parent, player, context, amount) ->
                        createActions(section).runRandomEveryActions(player, context, amount),
                (section, parent, context) ->
                        player -> createActions(section).runAllActions(player, context),
                (section, parent, player, context) -> false,
                action -> true,
                action -> false,
                true,
                false));
        actionManager.registerNewAction("previous_page", new ActionGUIPage("previous_page", -1));
        actionManager.registerNewAction("next_page", new ActionGUIPage("next_page", 1));
        actionManager.registerNewAction("refresh", new ActionGUIPage("refresh", 0));
    }

    public ObjectAction<ObjectSingleGUIAction, GUIActionContext> createButtonActions(ConfigurationSection button) {
        return createActions(button.getConfigurationSection("actions"));
    }

    public void execute(ObjectAction<ObjectSingleGUIAction, GUIActionContext> actions,
                        Player player,
                        GUIActionContext context) {
        if (actions != null) {
            actions.runAllActions(player, context);
        }
    }

    public ActionManager<ObjectSingleGUIAction, GUIActionContext> getActionManager() {
        return actionManager;
    }

    private ObjectAction<ObjectSingleGUIAction, GUIActionContext> createActions(ConfigurationSection section) {
        return new ObjectAction<>(section, ObjectSingleGUIAction::new, actionManager::doAction);
    }
}
