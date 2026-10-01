package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ActionEntitySpawn<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionEntitySpawn() {
        super("entity_spawn");
        setRequiredArgs("entity");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        EntityType entity = EntityType.valueOf(singleAction.getString("entity").toUpperCase());
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
        EnchantmentReform.methodUtil.spawnEntity(location, entity);
    }
}
