package cn.superiormc.enchantmentreform.gui.action;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class ObjectSingleGUIAction extends AbstractConfiguredSection<GUIActionContext> {

    public ObjectSingleGUIAction(ObjectAction<ObjectSingleGUIAction, GUIActionContext> parent,
                                 ConfigurationSection section) {
        super(section);
    }

    @Override
    protected String replacePlaceholder(String content, Player player, GUIActionContext context) {
        if (content == null) {
            return "";
        }
        if (context == null) {
            return TextUtil.withPAPI(CommonUtil.modifyString(player, content), player);
        }
        return TextUtil.withPAPI(CommonUtil.modifyString(player, content,
                "page", String.valueOf(context.gui().getCurrentPage() + 1),
                "pages", String.valueOf(context.gui().getPageCount()),
                "slot", String.valueOf(context.slot()),
                "click", context.clickType().name()), player);
    }
}
