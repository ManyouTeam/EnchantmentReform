package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ReplaceName extends AbstractChangesRule {

    public ReplaceName() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        ConfigurationSection tempVal1 = singleChange.getConfigurationSection("replace-name");
        ItemMeta meta = singleChange.getItemMeta();
        if (tempVal1 == null || !meta.hasLore()) {
            return singleChange.getItem();
        }
        String displayName = EnchantmentReform.methodUtil.getItemName(meta);
        for (Map.Entry<String, Object> entry : tempVal1.getValues(true).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                continue;
            }
            String requiredName = TextUtil.withPAPI(singleChange.parsePlaceholder(entry.getKey()),
                    singleChange.getPlayer());
            String replacement = entry.getValue() == null ? "" : entry.getValue().toString();
            replacement = TextUtil.withPAPI(singleChange.parsePlaceholder(replacement), singleChange.getPlayer());
            displayName = displayName.replace(requiredName, replacement);
        }
        EnchantmentReform.methodUtil.setItemName(meta, displayName, singleChange.getPlayer());
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getConfigurationSection("replace-name") == null;
    }
}
