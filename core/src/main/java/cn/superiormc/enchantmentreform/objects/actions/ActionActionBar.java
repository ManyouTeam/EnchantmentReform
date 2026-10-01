package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ActionActionBar<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionActionBar() {
        super("action_bar");
        setRequiredArgs("message");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        EnchantmentReform.methodUtil.sendActionBar(player, singleAction.getString("message", player, context));
    }
}
