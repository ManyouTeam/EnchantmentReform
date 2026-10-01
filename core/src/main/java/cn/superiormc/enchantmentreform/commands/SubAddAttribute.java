package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import org.bukkit.entity.Player;

public final class SubAddAttribute extends AbstractAttributeCommand {

    public SubAddAttribute() {
        super("addattribute", false);
    }

    @Override
    protected int update(ObjectCustomAttribute attribute, Player player, int value,
                         boolean ignoreLimits) {
        return attribute.addValue(player, value);
    }

    @Override
    protected String successMessage() {
        return "attribute.add";
    }
}
