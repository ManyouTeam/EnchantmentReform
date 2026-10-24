package cn.superiormc.enchantmentreform.objects.buttons;

import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class AbstractButton {

    protected final ConfigurationSection config;

    protected AbstractButton(ConfigurationSection config) {
        this.config = config;
    }

    public abstract void clickEvent(Player player, GUIActionContext context);

    public ConfigurationSection getConfig() {
        return config;
    }

}
