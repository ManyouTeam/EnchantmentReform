package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;
import cn.superiormc.enchantmentreform.utils.NBTUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class NBTDouble extends AbstractMatchItemRule {

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        NBTItem nbtItem = new NBTItem(match.getItem());
        List<String> tempVal1 = section.getStringList("nbt-double");
        for (String key : tempVal1) {
            if (NBTUtil.matchesPathValue(nbtItem, key.split(";;"), "double")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.get("nbt-double") == null;
    }
}
