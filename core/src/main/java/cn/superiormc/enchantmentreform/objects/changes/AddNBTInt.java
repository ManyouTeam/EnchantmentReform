package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.NBTUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AddNBTInt extends AbstractChangesRule {

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        NBTItem nbtItem = new NBTItem(singleChange.getItem().clone());
        List<String> tempVal1 = singleChange.getStringList("add-nbt-int");
        for (String key : tempVal1) {
            NBTUtil.setPathValue(nbtItem, key.split(";;"), "int");
        }
        singleChange.setNeedRewriteItem();
        return nbtItem.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("add-nbt-int");
    }
}
