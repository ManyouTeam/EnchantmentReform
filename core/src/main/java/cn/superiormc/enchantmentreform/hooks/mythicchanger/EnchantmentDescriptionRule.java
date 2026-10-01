package cn.superiormc.enchantmentreform.hooks.mythicchanger;

import cn.superiormc.enchantmentreform.hooks.enchantmentdescription.EnchantmentDescriptionRenderer;
import cn.superiormc.enchantmentreform.hooks.enchantmentdescription.EnchantmentDescriptionRenderer.Settings;
import cn.superiormc.mythicchanger.objects.ObjectSingleChange;
import cn.superiormc.mythicchanger.objects.changes.AbstractChangesRule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public final class EnchantmentDescriptionRule extends AbstractChangesRule {

    private static final String RULE_KEY = "er-enchantment-description";

    public static void register() {
        cn.superiormc.mythicchanger.manager.ChangesManager.changesManager
                .registerNewRule(new EnchantmentDescriptionRule());
    }

    @Override
    public ItemStack setChange(ObjectSingleChange change) {
        Object configuredValue = change.get(RULE_KEY);
        boolean enabled = !(configuredValue instanceof Boolean booleanValue) || booleanValue;
        ObjectSingleChange options = change.getConfigurationSection(RULE_KEY);
        EnchantmentDescriptionRenderer.apply(
                change.getItem(),
                change.getPlayer(),
                settings(options, enabled),
                !change.isFakeOrReal(),
                change::parsePlaceholder);
        return change.setItemMeta(change.getItem().getItemMeta());
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains(RULE_KEY);
    }

    private Settings settings(ObjectSingleChange options, boolean enabled) {
        Settings defaults = Settings.defaults(enabled);
        if (options == null) {
            return defaults;
        }
        boolean first = "FIRST".equals(options.getString("position", "LAST")
                .toUpperCase(Locale.ROOT));
        List<String> format = options.contains("format")
                ? options.getStringList("format")
                : EnchantmentDescriptionRenderer.DEFAULT_FORMAT;
        List<String> separator = options.contains("separator")
                ? options.getStringList("separator")
                : List.of("");
        return new Settings(
                enabled,
                first,
                format,
                separator,
                options.getInt("wrap-length", defaults.wrapLength()));
    }
}
