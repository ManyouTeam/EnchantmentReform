package cn.superiormc.enchantmentreform.gui.inv;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.gui.InvGUI;
import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;
import cn.superiormc.enchantmentreform.gui.action.GUIPageController;
import cn.superiormc.enchantmentreform.gui.action.ObjectSingleGUIAction;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.GUIActionManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttributeModifier;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AttributeDetailGUI extends InvGUI implements GUIPageController {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.##");

    private static final List<String> LEGACY_DEFAULT_LAYOUT = List.of(
            "FFFFFFFFF", "FFFFIFFFF", "FFFFFFFFF",
            "FMMMMMMMF", "FMMMMMMMF", "BFFPUNFFC");

    private final ObjectCustomAttribute attribute;

    private final SkillDefinition returnSkill;

    private final String returnMenuId;

    private final ConfigurationSection config;

    private final List<Integer> modifierSlots = new ArrayList<>();

    private final Map<Integer, String> staticSlots = new LinkedHashMap<>();

    private final Map<Integer, ObjectAction<ObjectSingleGUIAction, GUIActionContext>> actions = new LinkedHashMap<>();

    private int page;

    private int maximumPage;

    public AttributeDetailGUI(Player player, ObjectCustomAttribute attribute,
                              SkillDefinition returnSkill) {
        this(player, attribute, returnSkill, null);
    }

    public AttributeDetailGUI(Player player, ObjectCustomAttribute attribute,
                              SkillDefinition returnSkill, String returnMenuId) {
        super(player);
        this.attribute = attribute;
        this.returnSkill = returnSkill;
        this.returnMenuId = returnMenuId;
        this.config = ConfigManager.configManager.getSection("menu.attribute-detail");
        parseLayout();
    }

    public AttributeDetailGUI(Player player, ObjectCustomAttribute attribute,
                              String returnMenuId) {
        super(player);
        this.attribute = attribute;
        this.returnSkill = null;
        this.returnMenuId = returnMenuId;
        this.config = ConfigManager.configManager.getSection("menu.attribute-detail");
        parseLayout();
    }

    @Override
    protected void constructGUI() {
        List<ObjectCustomAttributeModifier> modifiers = new ArrayList<>(
                attribute.getModifiers(player).values());
        maximumPage = modifierSlots.isEmpty()
                ? 0 : Math.max(0, (modifiers.size() - 1) / modifierSlots.size());
        page = Math.max(0, Math.min(page, maximumPage));
        String[] args = attributeArgs();
        title = CommonUtil.modifyString(player,
                config.getString("title", "&8{attribute_name}"), args);
        if (inv == null) {
            inv = EnchantmentReform.methodUtil.createNewInv(
                    player, config.getInt("size", 54), title, this);
        } else {
            inv.clear();
        }
        actions.clear();
        renderStaticItems(args);
        renderModifiers(modifiers, args);
    }

    @Override
    public boolean clickEventHandle(Inventory inventory, ClickType type, int slot) {
        String symbol = staticSlots.get(slot);
        if (symbol == null) {
            return true;
        }
        if (symbol.equals(config.getString("back-symbol", "B"))) {
            if (returnSkill == null) {
                new ConfiguredMenuGUI(player,
                        returnMenuId == null ? "attribute-info" : returnMenuId).openGUI();
            } else {
                new SkillDetailGUI(player, returnSkill,
                        returnMenuId == null ? "skill-info" : returnMenuId).openGUI();
            }
            return true;
        }
        if (symbol.equals(config.getString("upgrade-symbol", "U"))) {
            ConfigurationSection option = config.getConfigurationSection(
                    "upgrade-clicks." + type.name());
            if (option != null) {
                int add = option.getInt("add", 1);
                int price = attribute.getAllocationPrice(attribute.getBaseValue(player), add);
                SkillManager.AllocationResult result = SkillManager.skillManager.allocate(
                        player, attribute.getId(), add);
                sendAllocationResult(result, add, price);
                constructGUI();
            }
            return true;
        }
        if (symbol.equals(config.getString("enable-symbol", "E"))) {
            attribute.setEnabledForPlayer(player, !attribute.isEnabledForPlayer(player));
            constructGUI();
            return true;
        }
        if (symbol.equals(config.getString("effective-level-symbol", "L"))) {
            ConfigurationSection option = config.getConfigurationSection(
                    "effective-level-clicks." + type.name());
            if (option != null) {
                int current = attribute.getEffectiveBaseValue(player);
                String setTo = option.getString("set-to", "").toLowerCase(Locale.ROOT);
                int selected = switch (setTo) {
                    case "minimum", "min" -> attribute.getMinimumValue();
                    case "maximum", "max", "unlocked" -> attribute.getBaseValue(player);
                    default -> current + option.getInt("add", 0);
                };
                attribute.setEffectiveBaseValue(player, selected);
                constructGUI();
            }
            return true;
        }
        GUIActionManager.INSTANCE.execute(actions.get(slot), player,
                new GUIActionContext(this, inventory, type, slot));
        return true;
    }

    @Override
    public void openPage(int page) {
        this.page = Math.max(0, Math.min(page, maximumPage));
        constructGUI();
    }

    @Override
    public int getCurrentPage() {
        return page;
    }

    @Override
    public int getPageCount() {
        return maximumPage + 1;
    }

    @Override
    public ConfigurationSection getSection() {
        return config;
    }

    private void parseLayout() {
        int slot = 0;
        String modifierSymbol = config.getString("modifier-symbol", "M");
        List<String> layout = config.getStringList("layout");
        for (String row : layout) {
            for (int column = 0; column < 9; column++, slot++) {
                String symbol = column < row.length()
                        ? String.valueOf(row.charAt(column)) : " ";
                if (symbol.equals(modifierSymbol)) {
                    modifierSlots.add(slot);
                } else if (!symbol.isBlank() && !symbol.equals(".")) {
                    staticSlots.put(slot, symbol);
                }
            }
        }
        // Preserve the old bundled layout on upgrades while exposing the two new controls.
        if (layout.equals(LEGACY_DEFAULT_LAYOUT)) {
            staticSlots.put(46, config.getString("enable-symbol", "E"));
            staticSlots.put(52, config.getString("effective-level-symbol", "L"));
        }
    }

    private void renderStaticItems(String[] args) {
        String upgradeSymbol = config.getString("upgrade-symbol", "U");
        String enableSymbol = config.getString("enable-symbol", "E");
        for (Map.Entry<Integer, String> entry : staticSlots.entrySet()) {
            if (entry.getKey() >= inv.getSize()) continue;
            ConfigurationSection button = config.getConfigurationSection(
                    "buttons." + entry.getValue());
            if (button == null) continue;
            boolean hidden = button.getBoolean("hide-on-first-page", false) && page == 0
                    || button.getBoolean("hide-on-last-page", false) && page == maximumPage;
            ConfigurationSection shown = hidden
                    ? button.getConfigurationSection("hidden-item") : button;
            if (!hidden && entry.getValue().equals(upgradeSymbol)
                    && attribute.getBaseValue(player)
                    >= attribute.getAllocationMaximumValue()) {
                shown = button.getConfigurationSection("maximum-item");
            }
            if (!hidden && entry.getValue().equals(enableSymbol)) {
                shown = button.getConfigurationSection(attribute.isEnabledForPlayer(player)
                        ? "enabled-item" : "disabled-item");
            }
            if (shown != null) setItem(entry.getKey(), buildItem(shown, args));
            if (!hidden && !entry.getValue().equals(upgradeSymbol)
                    && !entry.getValue().equals(config.getString("back-symbol", "B"))) {
                actions.put(entry.getKey(), GUIActionManager.INSTANCE.createButtonActions(button));
            }
        }
    }

    private void renderModifiers(List<ObjectCustomAttributeModifier> modifiers, String[] baseArgs) {
        if (modifierSlots.isEmpty()) return;
        if (modifiers.isEmpty()) {
            ConfigurationSection empty = config.getConfigurationSection("no-modifiers-item");
            if (empty != null) setItem(modifierSlots.get(0), buildItem(empty, baseArgs));
            return;
        }
        ConfigurationSection template = config.getConfigurationSection("modifier-item");
        if (template == null) return;
        int first = page * modifierSlots.size();
        for (int index = 0; index < modifierSlots.size() && first + index < modifiers.size(); index++) {
            ObjectCustomAttributeModifier modifier = modifiers.get(first + index);
            String[] args = merge(baseArgs, new String[]{
                    "modifier", modifier.id(),
                    "operation", modifier.operation().name(),
                    "operation_display", operationDisplay(modifier.operation()),
                    "amount", String.valueOf(modifier.amount()),
                    "amount_display", modifierAmountDisplay(modifier)});
            setItem(modifierSlots.get(index), buildItem(template, args));
        }
    }

    private String operationDisplay(ObjectCustomAttributeModifier.Operation operation) {
        String key = switch (operation) {
            case ADD_VALUE -> "attribute-detail-operation-add-value";
            case ADD_MULTIPLIED_BASE -> "attribute-detail-operation-add-base";
            case ADD_MULTIPLIED_TOTAL -> "attribute-detail-operation-multiply-total";
        };
        return CommonUtil.parseLang(player, "{lang:" + key + "}");
    }

    private String modifierAmountDisplay(ObjectCustomAttributeModifier modifier) {
        double value = modifier.operation() == ObjectCustomAttributeModifier.Operation.ADD_VALUE
                ? modifier.amount() : modifier.amount() * 100.0D;
        return (value >= 0.0D ? "+" : "") + NUMBER.format(value)
                + (modifier.operation() == ObjectCustomAttributeModifier.Operation.ADD_VALUE
                ? "" : "%");
    }

    private String[] attributeArgs() {
        int base = attribute.getBaseValue(player);
        int effectiveBase = attribute.getEffectiveBaseValue(player);
        int value = attribute.getValue(player);
        int nextBase = Math.min(attribute.getAllocationMaximumValue(), base + 1);
        String description = attribute.getLocalizedDescription(player, value);
        String nextDescription = attribute.getLocalizedDescription(
                player, attribute.getValueAtBase(player, nextBase));
        return new String[]{
                "attribute_id", attribute.getId(),
                "attribute_name", attribute.getLocalizedName(player),
                "attribute_description", description == null ? "" : description,
                "current_description", description == null ? "" : description,
                "next_description", nextDescription == null ? "" : nextDescription,
                "base_value", String.valueOf(base),
                "unlocked_value", String.valueOf(base),
                "effective_base_value", String.valueOf(effectiveBase),
                "effective_value", String.valueOf(value),
                "attribute_enabled", String.valueOf(attribute.isEnabledForPlayer(player)),
                "attribute_status", CommonUtil.parseLang(player, attribute.isEnabledForPlayer(player)
                        ? "{lang:attribute-detail-status-enabled}"
                        : "{lang:attribute-detail-status-disabled}"),
                "value", String.valueOf(value),
                "minimum_value", String.valueOf(attribute.getMinimumValue()),
                "maximum_value", String.valueOf(attribute.getMaximumValue()),
                "allocation_maximum_value",
                String.valueOf(attribute.getAllocationMaximumValue()),
                "default_value", String.valueOf(attribute.getDefaultValue()),
                "modifier_count", String.valueOf(attribute.getModifiers(player).size()),
                "next_base", String.valueOf(nextBase),
                "next_value", String.valueOf(attribute.getValueAtBase(player, nextBase)),
                "price", String.valueOf(attribute.getAllocationPrice(base, 1)),
                "points", String.valueOf(SkillManager.skillManager.getAttributePoints(player)),
                "requirement", attribute.getAllocationRequirementDisplay(player, nextBase),
                "page", String.valueOf(page + 1),
                "pages", String.valueOf(maximumPage + 1)};
    }

    private void sendAllocationResult(SkillManager.AllocationResult result, int add, int price) {
        String path = switch (result) {
            case SUCCESS -> "skills.allocation-messages.success";
            case NOT_ENOUGH_POINTS -> "skills.allocation-messages.not-enough-points";
            case MAXIMUM_REACHED -> "skills.allocation-messages.maximum-reached";
            case CONDITION_NOT_MET -> "skills.allocation-messages.condition-not-met";
            default -> "skills.allocation-messages.invalid";
        };
        TextUtil.sendMessage(player, CommonUtil.modifyString(player,
                ConfigManager.configManager.getString(path, "&cCould not allocate points."),
                "attribute", attribute.getLocalizedName(player), "add", String.valueOf(add),
                "price", String.valueOf(price),
                "points", String.valueOf(SkillManager.skillManager.getAttributePoints(player))));
    }

    private ItemStack buildItem(ConfigurationSection section, String[] args) {
        ItemStack item = BuildItem.buildItemStack(player, section, args);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<String> lore = new ArrayList<>();
        for (String configured : CommonUtil.modifyList(
                player, section.getStringList("lore"), args)) {
            if (configured.isBlank()) {
                lore.add("");
                continue;
            }
            for (String line : configured.split("\\\\n", -1)) {
                lore.addAll(TextUtil.wrapKeepColor(line,
                        ConfigManager.configManager.getInt(
                                "enchantment-description.wrap-length", 30)));
            }
        }
        EnchantmentReform.methodUtil.setItemLore(meta, lore, player);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(meta);
        return item;
    }

    private static String[] merge(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
