package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import org.bukkit.entity.Player;

public final class SubSetAttribute extends AbstractAttributeCommand {

    public SubSetAttribute() {
        super("setattribute", true);
    }

    @Override
    protected int update(ObjectCustomAttribute attribute, Player player, int value,
                         boolean ignoreLimits) {
        return attribute.setValue(player, value, ignoreLimits);
    }

    @Override
    protected String successMessage() {
        return "attribute.set";
    }
}
