package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.gui.action.ActionGUIPage;
import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;
import cn.superiormc.enchantmentreform.gui.action.ObjectSingleGUIAction;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public final class GUIActionManager {

    public static final GUIActionManager INSTANCE = new GUIActionManager();

    private final ActionManager<ObjectSingleGUIAction, GUIActionContext> actionManager;

    private GUIActionManager() {
        actionManager = new ActionManager<>(
                (section, parent, player, context) -> createActions(section).runAllActions(player, context),
                (section, parent, player, context, amount) ->
                        createActions(section).runRandomEveryActions(player, context, amount),
                (section, parent, context) ->
                        player -> createActions(section).runAllActions(player, context),
                true);
        actionManager.registerNewAction("previous_page", new ActionGUIPage("previous_page", -1));
        actionManager.registerNewAction("next_page", new ActionGUIPage("next_page", 1));
        actionManager.registerNewAction("refresh", new ActionGUIPage("refresh", 0));
        for (String type : List.of("open_menu", "open_skill", "open_attribute", "allocate_attribute", "command")) {
            actionManager.registerNewAction(type, new ActionGUIPage(type, 0));
        }
    }

    public ObjectAction<ObjectSingleGUIAction, GUIActionContext> createButtonActions(ConfigurationSection button) {
        return createButtonActions(button, null);
    }

    public ObjectAction<ObjectSingleGUIAction, GUIActionContext> createButtonActions(
            ConfigurationSection button, ClickType clickType) {
        if (button == null) {
            return createActions(null);
        }
        ConfigurationSection specific = clickType == null ? null
                : button.getConfigurationSection("click-actions." + clickType.name());
        return createActions(specific == null ? button.getConfigurationSection("actions") : specific);
    }

    public void executeButtonActions(ConfigurationSection button, Player player, GUIActionContext context) {
        execute(createButtonActions(button, context == null ? null : context.clickType()), player, context);
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
