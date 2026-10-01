package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.changes.*;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class ChangesManager extends AbstractManager {

    public static ChangesManager changesManager;

    private final Collection<AbstractChangesRule> rules = new ArrayList<>();

    private final Map<String, AbstractChangesRule> rulesByConfigKey = new HashMap<>();

    public ChangesManager() {
        changesManager = this;
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        register(new AddEnchants());
        register(new ModifyEnchants());
        register(new AddFlags());
        register(new AddLoreFirst());
        register(new AddLoreLast());
        register(new AddLorePrefix());
        register(new AddLoreSuffix());
        register(new AddNameFirst());
        register(new AddNameLast());
        register(new AddAttributes());
        register(new AddStoredEnchants());
        register(new DeleteEnchants());
        register(new Empty());
        register(new RemoveName());
        register(new RemoveAllLore());
        register(new RemoveAllEnchants());
        register(new RemoveAllStoredEnchants());
        register(new RemoveFlags());
        register(new RemoveAttributes());
        register(new RemoveAttributeByName());
        register(new RemoveAttributeContainsName());
        register(new RemoveEnchants());
        register(new RemoveStoredEnchants());
        register(new ReplaceName());
        register(new ReplaceItem());
        register(new ReplaceLore());
        register(new RemoveColor());
        register(new RemoveCustomModelData());
        register(new RemoveItemModel());
        register(new SetCustomModelData());
        register(new SetItemModel());
        register(new SetLore());
        register(new SetName());
        register(new SetType());
        register(new SetAmount());
        register(new FixHideAttributes());
        register(new SetColor());
        register(new ParsePAPIName());
        register(new ParsePAPILore());
        register(new EditItem());
        register(new AddDamage());
        register(new Damage());
        register(new RepairDamage());
        if (EnchantmentReform.methodUtil.methodID().equals("paper")) {
            register(new RemoveData());
            register(new ResetData());
            register(new AddTool());
        }
        if (CommonUtil.checkPluginLoad("NBTAPI")) {
            register(new AddNBTString());
            register(new AddNBTByte());
            register(new AddNBTInt());
            register(new AddNBTDouble());
            register(new RemoveNBT());
        }
        register(new KeepEnchants());
        register(new KeepDamage());
        register(new KeepName());
        register(new KeepLore());
        register(new KeepFlags());
        if (CommonUtil.getMinorVersion(20, 5)) {
            register(new KeepItemName());
        }
        register(new KeepItemFormat());
        register(new EditLore());
        register(new EditLoreLine());
        register(new ReplaceEnchants());
        register(new ReplaceStoredEnchants());
        register(new ReplaceRandomItem());
        register(new Deapply());
        register(new ResetApplyLimit());
        register(new RandomChange());
        register(new SubChange());
    }

    public void register(AbstractChangesRule rule) {
        rules.add(rule);
        rulesByConfigKey.clear();
    }

    public ItemStack setChange(ConfigurationSection section, ItemStack item, Player player, PowerContext context) {
        if (section == null || item == null || item.getType().isAir() || item.getItemMeta() == null) {
            return item;
        }
        ObjectSingleChange singleChange = new ObjectSingleChange(section, item, player, context);
        return setChange(singleChange);
    }

    public ItemStack setChange(ObjectSingleChange singleChange) {
        for (String configKey : singleChange.section.getKeys(false)) {
            AbstractChangesRule rule = getRuleByConfigKey(singleChange.section, configKey);
            if (rule == null) {
                continue;
            }
            if (ConfigManager.configManager.getBoolean("debug")) {
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fApply fake rule: " + rule.getClass().getSimpleName());
            }
            ItemStack result = rule.setChange(singleChange);
            if (singleChange.isRewritten()) {
                singleChange.replaceItem(result);
            }
        }
        return singleChange.getItem();
    }

    private AbstractChangesRule getRuleByConfigKey(ConfigurationSection section, String configKey) {
        AbstractChangesRule cachedRule = rulesByConfigKey.get(configKey);
        if (cachedRule != null) {
            return cachedRule;
        }
        ConfigurationSection singleKeySection = new MemoryConfiguration();
        singleKeySection.set(configKey, section.get(configKey));
        for (AbstractChangesRule rule : rules) {
            if (!rule.configNotContains(singleKeySection)) {
                rulesByConfigKey.put(configKey, rule);
                return rule;
            }
        }
        return null;
    }
}
