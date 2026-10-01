package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ObjectSingleMatchItem {

    public final ConfigurationSection section;

    private final ItemStack item;

    private final ItemMeta meta;

    private final Player player;

    private final PowerContext context;

    public ObjectSingleMatchItem(ConfigurationSection section, ItemStack item, ItemMeta meta,
                                 Player player, PowerContext context) {
        this.section = section;
        this.item = item;
        this.meta = meta;
        this.player = player;
        this.context = context;
    }

    /** Derives a context for a nested section (used by the {@code any}/{@code not} rules). */
    public ObjectSingleMatchItem withSection(ConfigurationSection section) {
        return new ObjectSingleMatchItem(section, item, meta, player, context);
    }

    public String parsePlaceholder(String value) {
        if (value == null) {
            return null;
        }
        int level = context == null ? 1 : context.level();
        return CommonUtil.modifyString(player, value,
                "level", String.valueOf(level),
                "amount", String.valueOf(item == null ? 0 : item.getAmount()));
    }

    public List<String> parsePlaceholder(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(parsePlaceholder(value));
        }
        return result;
    }

    public ConfigurationSection getSection() {
        return section;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemMeta getItemMeta() {
        return meta;
    }

    public Player getPlayer() {
        return player;
    }

    public PowerContext getContext() {
        return context;
    }
}
