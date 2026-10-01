package cn.superiormc.enchantmentreform.hooks.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class AbstractProtectionHook {

    protected String pluginName;

    public AbstractProtectionHook(String pluginName) {
        this.pluginName = pluginName;
    }

    public abstract boolean canUse(Player player, Location location);

    public boolean canPlace(Player player, Location location) {
        return canUse(player, location);
    }

    public boolean canBreak(Player player, Location location) {
        return canUse(player, location);
    }

    public String getPluginName() {
        return pluginName;
    }
}
