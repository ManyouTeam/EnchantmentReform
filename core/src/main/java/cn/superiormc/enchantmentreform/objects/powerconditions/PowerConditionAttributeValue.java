package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.entity.Player;

/** Compares a player's final custom-attribute value. */
public final class PowerConditionAttributeValue extends AbstractNumericPowerCondition {

    public PowerConditionAttributeValue() {
        super("attribute_value");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Player player = condition.player();
        if (player == null || AttributeManager.attributeManager == null) {
            return null;
        }
        String id = condition.getString("attribute",
                condition.getString("id", ""));
        ObjectCustomAttribute attribute = AttributeManager.attributeManager.resolveAttribute(id);
        return attribute == null ? null : (double) attribute.getValue(player);
    }
}
