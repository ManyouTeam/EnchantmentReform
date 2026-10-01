package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ActionOPCommand<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionOPCommand() {
        super("op_command");
        setRequiredArgs("command");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        EnchantmentReform.methodUtil.dispatchOpCommand(player, singleAction.getString("command", player, context));
    }
}
