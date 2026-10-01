package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.AttributeUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public final class RemoveAttributes extends AbstractChangesRule {

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        ItemMeta meta = singleChange.getItemMeta();
        Set<Attribute> attributes = new HashSet<>();
        for (String attribute : singleChange.getStringList("remove-attributes")) {
            Attribute attributeInst = Registry.ATTRIBUTE.get(CommonUtil.parseNamespacedKey(attribute));
            if (attributeInst != null) {
                attributes.add(attributeInst);
            }
        }
        AttributeUtil.removeCustomAttributeModifiers(meta, singleChange.getItem(),
                (attribute, modifier) -> attributes.contains(attribute));
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("remove-attributes").isEmpty();
    }
}
