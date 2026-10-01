package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MaterialTag extends AbstractMatchItemRule {

    public MaterialTag() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        for (String singleMaterial : section.getStringList("material-tag")) {
            Tag<Material> tempVal1 = Bukkit.getTag(Tag.REGISTRY_ITEMS, CommonUtil.parseNamespacedKey(singleMaterial), Material.class);
            if (tempVal1 != null && tempVal1.isTagged(item.getType())) {
                return true;
            }
            Tag<Material> tempVal2 = Bukkit.getTag(Tag.REGISTRY_BLOCKS, CommonUtil.parseNamespacedKey(singleMaterial), Material.class);
            if (tempVal2 != null && tempVal2.isTagged(item.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("material-tag").isEmpty();
    }
}
