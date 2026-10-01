package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SetAttributeAbility extends AbstractAbility {

    public SetAttributeAbility(ConfigurationSection section) {
        super("SetAttribute", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (entity == null) {
            return false;
        }
        String attributeName = section.getString("attribute", "max_health");
        ObjectCustomAttribute customAttribute = AttributeManager.attributeManager == null
                ? null : AttributeManager.attributeManager.resolveAttribute(attributeName);
        if (customAttribute != null) {
            if (!(entity instanceof Player player)) {
                return false;
            }
            double value = getDouble("value", 1, context,
                    "now", String.valueOf(customAttribute.getBaseValue(player)),
                    "max", String.valueOf(customAttribute.getMaximumValue()));
            customAttribute.setValue(player, (int) Math.round(value));
            return false;
        }
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        try {
            Attribute attribute = Registry.ATTRIBUTE.get(
                    CommonUtil.parseNamespacedKey(attributeName));
            if (attribute == null) {
                return false;
            }
            AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                double maxValue = instance.getAttribute().equals(Attribute.MAX_HEALTH)
                        ? instance.getValue()
                        : (living.getAttribute(Attribute.MAX_HEALTH) == null ? instance.getValue() : living.getAttribute(Attribute.MAX_HEALTH).getValue());
                double value = getDouble("value", 1, context,
                        "now", String.valueOf(instance.getBaseValue()),
                        "max", String.valueOf(maxValue));
                instance.setBaseValue(value);
                if (instance.getAttribute().equals(Attribute.MAX_HEALTH)) {
                    living.setHealth(Math.max(living.getHealth(), value));
                }
            }
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
