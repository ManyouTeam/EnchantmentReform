package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class ObjectSingleAction extends AbstractConfiguredSection<PowerContext> {

    public ObjectSingleAction(ObjectAction<ObjectSingleAction, PowerContext> parent,
                              ConfigurationSection section) {
        super(section);
    }

    @Override
    protected String replacePlaceholder(String content, Player player, PowerContext context) {
        if (content == null) {
            return "";
        }
        int level = context == null ? 1 : context.level();
        return TextUtil.withPAPI(
                CommonUtil.modifyString(player, content, "level", String.valueOf(level)), player);
    }
}
