package cn.superiormc.enchantmentreform.objects.buttons;

import org.bukkit.configuration.ConfigurationSection;

public final class ObjectContentButton extends ObjectButton {

    public ObjectContentButton(String id, ConfigurationSection config) {
        super(id, config);
    }

    public String getContentType() {
        return config.getString("type", "");
    }

}
