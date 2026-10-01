package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ActionClose<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionClose() {
        super("close");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        player.closeInventory();
    }
}
