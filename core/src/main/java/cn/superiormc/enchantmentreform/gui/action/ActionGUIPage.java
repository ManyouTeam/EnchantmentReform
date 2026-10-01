package cn.superiormc.enchantmentreform.gui.action;

import cn.superiormc.enchantmentreform.objects.actions.AbstractRunAction;
import org.bukkit.entity.Player;

public final class ActionGUIPage extends AbstractRunAction<ObjectSingleGUIAction, GUIActionContext> {

    private final int pageOffset;

    public ActionGUIPage(String type, int pageOffset) {
        super(type);
        this.pageOffset = pageOffset;
    }

    @Override
    protected void onDoAction(ObjectSingleGUIAction singleAction,
                              Player player,
                              GUIActionContext context) {
        if (context != null) {
            context.gui().openPage(context.gui().getCurrentPage() + pageOffset);
        }
    }
}
