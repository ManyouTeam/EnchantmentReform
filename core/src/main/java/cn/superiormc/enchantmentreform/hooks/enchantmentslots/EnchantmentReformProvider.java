package cn.superiormc.enchantmentreform.hooks.enchantmentslots;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.EnchantmentOrderUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import cn.superiormc.enchantmentslots.hooks.enchants.AbstractEnchantHook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EnchantmentReformProvider extends AbstractEnchantHook {

    private static final Pattern RESETTING_COLOR = Pattern.compile(
            "(?i)(§x(?:§[0-9a-f]){6}|§[0-9a-fr])");

    public EnchantmentReformProvider() {
        super("EnchantmentReform");
    }

    @Override
    public String getEnchantName(ItemStack item, Enchantment enchantment, Player player) {
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        PowerEnchantmentDefinition definition = manager.getDefinition(enchantment.getKey().toString());
        if (definition == null) {
            return null;
        }
        int level = getEnchantmentLevel(item, enchantment);
        PowerContext context = new PowerContext(
                definition.getPower(), level, null, null, item, null, null, player);
        if (definition.meetsConditions(player, context)) {
            return definition.getLocalizedName(player);
        }
        return strikethrough(TextUtil.colorize(definition.getLocalizedName(player)));
    }

    @Override
    public String getRawEnchantName(Enchantment enchantment) {
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        PowerEnchantmentDefinition definition = manager.getDefinition(enchantment.getKey().toString());
        return definition == null ? null : definition.getName();
    }

    @Override
    public List<String> getEnchantDescription(ItemStack item, Enchantment enchantment, Player player) {
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        String key = enchantment.getKey().toString();
        int level = getEnchantmentLevel(item, enchantment);
        PowerEnchantmentDefinition definition = manager.getDefinition(key);
        if (definition == null || definition.getDescription() == null) {
            return null;
        }
        return definition.getDescriptionForDisplay(player, level);
    }

    @Override
    public boolean hasEnchantDescription() {
        return true;
    }

    @Override
    public Map<Enchantment, Integer> sortEnchants(ItemMeta meta) {
        Map<Enchantment, Integer> source = getEnchantments(meta);
        if (source.size() < 2) {
            return new LinkedHashMap<>(source);
        }

        ConfigManager configManager = ConfigManager.configManager;
        ConfigurationSection tooltipSettings = configManager == null
                ? null
                : configManager.getSection("tooltip-order");
        if (!EnchantmentOrderUtil.isEnabled(tooltipSettings)) {
            return new LinkedHashMap<>(source);
        }

        EnchantmentConfigManager enchantmentManager = EnchantmentConfigManager.getInstance();
        Map<Enchantment, PowerEnchantmentDefinition> configuredEnchantments = new HashMap<>();
        for (Enchantment enchantment : source.keySet()) {
            String key = enchantment.getKey().toString();
            PowerEnchantmentDefinition definition = enchantmentManager.getDefinition(key);
            if (definition != null && definition.isEnabled()) {
                configuredEnchantments.put(enchantment, definition);
            }
        }
        if (configuredEnchantments.isEmpty()) {
            return new LinkedHashMap<>(source);
        }

        ConfigurationSection raritySettings = configManager == null
                ? null
                : configManager.getRarities();
        Comparator<PowerEnchantmentDefinition> comparator = EnchantmentOrderUtil.comparator(
                raritySettings, tooltipSettings);

        LinkedHashMap<Enchantment, Integer> result = new LinkedHashMap<>();
        configuredEnchantments.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(comparator))
                .forEach(entry -> result.put(entry.getKey(), source.get(entry.getKey())));
        for (Map.Entry<Enchantment, Integer> entry : source.entrySet()) {
            if (!configuredEnchantments.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private Map<Enchantment, Integer> getEnchantments(ItemMeta meta) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            Map<Enchantment, Integer> storedEnchantments = storageMeta.getStoredEnchants();
            if (!storedEnchantments.isEmpty()) {
                return storedEnchantments;
            }
        }
        return meta.getEnchants();
    }

    private int getEnchantmentLevel(ItemStack item, Enchantment enchantment) {
        int level = item.getEnchantmentLevel(enchantment);
        ItemMeta meta = item.getItemMeta();
        if (level <= 0 && meta instanceof EnchantmentStorageMeta storageMeta) {
            level = storageMeta.getStoredEnchantLevel(enchantment);
        }
        return Math.max(level, 1);
    }

    private String strikethrough(String text) {
        String reapplied = RESETTING_COLOR.matcher(text).replaceAll("$1§m");
        return "§m" + reapplied;
    }

}
