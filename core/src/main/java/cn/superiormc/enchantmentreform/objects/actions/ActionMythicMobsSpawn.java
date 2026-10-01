package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ActionMythicMobsSpawn<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionMythicMobsSpawn() {
        super("mythicmobs_spawn");
        setRequiredArgs("entity");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        String mobName = singleAction.getString("entity");
        String worldName = singleAction.getString("world");
        Location location;
        if (worldName == null) {
            location = player.getLocation();
        } else {
            World world = Bukkit.getWorld(worldName);
            location = new Location(world,
                    singleAction.getDouble("x", player, context),
                    singleAction.getDouble("y", player, context),
                    singleAction.getDouble("z", player, context));
        }
        CommonUtil.summonMythicMobs(location, mobName, singleAction.getInt("level", 1));
    }
}
