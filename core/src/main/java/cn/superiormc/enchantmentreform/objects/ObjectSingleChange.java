package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.MathUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ObjectSingleChange extends AbstractConfiguredSection<PowerContext> {

    private final ItemStack original;

    private ItemStack item;

    private ItemMeta itemMeta;

    private final Player player;

    private final PowerContext context;

    private final String[] arguments;

    private boolean rewritten;

    public ObjectSingleChange(ConfigurationSection section, ItemStack item, Player player, PowerContext context) {
        this(section, item, player, context, new String[0]);
    }

    public ObjectSingleChange(ConfigurationSection section, ItemStack item, Player player,
                              PowerContext context, String... arguments) {
        super(section);
        this.original = item.clone();
        this.item = item;
        this.itemMeta = item.getItemMeta();
        this.player = player;
        this.context = context;
        this.arguments = arguments == null ? new String[0] : arguments.clone();
    }

    public ObjectSingleChange(ConfigurationSection section, ObjectSingleChange parent) {
        super(section);
        this.original = parent.original;
        this.item = parent.item;
        this.itemMeta = parent.itemMeta;
        this.player = parent.player;
        this.context = parent.context;
        this.arguments = parent.arguments;
    }

    public double getDouble(String path, double defaultValue) {
        Object value = section.get(path);
        if (value instanceof Number number) return number.doubleValue();
        if (value == null) return defaultValue;
        return MathUtil.doCalculate(parsePlaceholder(value.toString()));
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public int getInt(String path, int defaultValue) {
        return (int) Math.round(getDouble(path, defaultValue));
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public String getString(String path, String defaultValue) {
        String value = section.getString(path);
        return value == null ? defaultValue : parsePlaceholder(value);
    }

    @Override
    public String getString(String path) {
        return getString(path, "");
    }

    public Object get(String path) {
        return section.get(path);
    }

    @Override
    public List<String> getStringList(String path) {
        List<String> result = new ArrayList<>();
        for (String value : section.getStringList(path)) result.add(parsePlaceholder(value));
        return result;
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return section.getConfigurationSection(path);
    }

    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        return section.getBoolean(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return section.getBoolean(path);
    }

    public String parsePlaceholder(String value) {
        if (value == null) return null;
        int level = context == null ? 1 : context.level();
        return resolveText(value, player, context, level, arguments);
    }

    public List<String> parsePlaceholder(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(parsePlaceholder(value));
        }
        return result;
    }

    @Override
    protected String replacePlaceholder(String content, Player player, PowerContext context) {
        content = super.replacePlaceholder(content, player, context);
        int damage = itemMeta instanceof Damageable damageable ? damageable.getDamage() : 0;
        return CommonUtil.modifyString(player, content,
                "amount", String.valueOf(item.getAmount()),
                "max-stack", String.valueOf(item.getMaxStackSize()),
                "damage", String.valueOf(damage),
                "max-damage", String.valueOf(item.getType().getMaxDurability()),
                "original-damage", String.valueOf(originalDamage()));
    }

    private int originalDamage() {
        return original.getItemMeta() instanceof Damageable damageable ? damageable.getDamage() : 0;
    }

    public ItemStack updateItemMeta() {
        this.itemMeta = item.getItemMeta();
        return item;
    }

    public ItemStack setItemMeta(ItemMeta meta) {
        this.itemMeta = meta;
        item.setItemMeta(meta);
        return item;
    }

    public void replaceItem(ItemStack replacement) {
        if (replacement == null) {
            return;
        }
        this.item = replacement;
        this.itemMeta = replacement.getItemMeta();
        this.rewritten = true;
    }

    public void setNeedRewriteItem() {
        rewritten = true;
    }

    public boolean isFakeOrReal() {
        return false;
    }

    public boolean isPlayerInventory() {
        return player != null;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack getOriginal() {
        return original;
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemMeta getOriginalMeta() {
        return original.getItemMeta();
    }

    public Player getPlayer() {
        return player;
    }

    public PowerContext getContext() {
        return context;
    }

    public boolean isRewritten() {
        return rewritten;
    }
}
