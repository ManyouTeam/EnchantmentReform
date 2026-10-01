package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.TempBlockManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public final class PlaceTempBlockAbility extends AbstractAbility {

    public PlaceTempBlockAbility(ConfigurationSection section) {
        super("PlaceTempBlock", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Material material = Material.matchMaterial(
                getString("block", "COBWEB", context).toUpperCase());
        if (material == null) {
            return false;
        }
        Location location = getLocation(context);
        if (location == null || location.getWorld() == null || !location.getBlock().getType().isAir()) {
            return false;
        }
        int duration = getInt("duration", 40, context);
        TempBlockManager.tempBlockManager.createTempBlock(
                location.getBlock().getLocation(), material, Math.max(1, duration));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
