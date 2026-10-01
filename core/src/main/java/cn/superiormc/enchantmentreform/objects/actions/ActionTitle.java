package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ActionTitle<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionTitle() {
        super("title");
        setRequiredArgs("main-title", "sub-title", "fade-in", "stay", "fade-out");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        EnchantmentReform.methodUtil.sendTitle(player,
                singleAction.getString("main-title", player, context),
                singleAction.getString("sub-title", player, context),
                singleAction.getInt("fade-in"),
                singleAction.getInt("stay"),
                singleAction.getInt("fade-out"));
    }
}
