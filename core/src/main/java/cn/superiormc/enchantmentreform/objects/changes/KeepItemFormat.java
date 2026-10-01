package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.methods.DebuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

public class KeepItemFormat extends AbstractChangesRule {

    public KeepItemFormat() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        ConfigurationSection debuildSection = DebuildItem.debuildItem(singleChange.getOriginal(), new MemoryConfiguration());
        ConfigurationSection keepSection = new MemoryConfiguration();
        for (String key : singleChange.getStringList("keep-item-format")) {
            if (key.startsWith("nbt")) {
                singleChange.setNeedRewriteItem();
            }
            if (debuildSection.contains(key)) {
                keepSection.set(key, debuildSection.get(key));
            }
        }
        return BuildItem.editItemStack(singleChange.getItem(),
                singleChange.getPlayer(),
                keepSection);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("keep-item-format");
    }
}
