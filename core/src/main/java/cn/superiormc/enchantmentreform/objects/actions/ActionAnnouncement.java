package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionAnnouncement<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionAnnouncement() {
        super("announcement");
        setRequiredArgs("message");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            TextUtil.sendMessage(target, singleAction.getString("message", player, context));
        }
    }
}
