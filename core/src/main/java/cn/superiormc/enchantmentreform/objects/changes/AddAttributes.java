package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.AttributeUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import com.google.common.base.Enums;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class AddAttributes extends AbstractChangesRule {

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        ItemMeta meta = singleChange.getItemMeta();
        ConfigurationSection attributesKey = singleChange.section.getConfigurationSection("add-attributes");
        if (attributesKey != null) {
            for (String attribute : attributesKey.getKeys(false)) {
                Attribute attributeInst = Registry.ATTRIBUTE.get(CommonUtil.parseNamespacedKey(attribute));
                if (attributeInst == null) {
                    continue;
                }
                ConfigurationSection subSection = attributesKey.getConfigurationSection(attribute);
                if (subSection == null) {
                    continue;
                }

                String attribName = subSection.getString("name");
                double attribAmount = subSection.getDouble("amount");
                String attribOperation = subSection.getString("operation");

                String attribSlot = subSection.getString("slot");
                EquipmentSlotGroup slot = AttributeUtil.getAutomaticEquipmentSlotGroup(singleChange.getItem());
                if (attribSlot != null) {
                    EquipmentSlotGroup targetSlot = EquipmentSlotGroup.getByName(attribSlot);
                    slot = targetSlot != null ? targetSlot : EquipmentSlotGroup.ANY;
                }
                if (attribName != null && attribOperation != null) {
                    AttributeModifier modifier = new AttributeModifier(
                                CommonUtil.parseNamespacedKey(attribName),
                                attribAmount,
                                Enums.getIfPresent(AttributeModifier.Operation.class, attribOperation)
                                        .or(AttributeModifier.Operation.ADD_NUMBER),
                                slot);
                    AttributeUtil.copyDefaultAttributeModifiers(meta, singleChange.getItem());
                    AttributeUtil.addOrMergeAttributeModifier(meta, attributeInst, modifier);
                }

            }
        }
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("add-attributes");
    }
}
