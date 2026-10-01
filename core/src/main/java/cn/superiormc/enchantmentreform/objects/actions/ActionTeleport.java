package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActionTeleport<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionTeleport() {
        super("teleport");
        setRequiredArgs("world", "x", "y", "z");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        Location loc = new Location(Bukkit.getWorld(singleAction.getString("world")),
                singleAction.getDouble("x", player, context),
                singleAction.getDouble("y", player, context),
                singleAction.getDouble("z", player, context),
                singleAction.getInt("yaw", (int) player.getLocation().getYaw()),
                singleAction.getInt("pitch", (int) player.getLocation().getPitch()));
        EnchantmentReform.methodUtil.playerTeleport(player, loc);
    }
}
