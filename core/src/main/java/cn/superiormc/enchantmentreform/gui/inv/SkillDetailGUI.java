package cn.superiormc.enchantmentreform.gui.inv;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.gui.InvGUI;
import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;
import cn.superiormc.enchantmentreform.gui.action.GUIPageController;
import cn.superiormc.enchantmentreform.gui.action.ObjectSingleGUIAction;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.GUIActionManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.objects.skills.SkillSource;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillDetailGUI extends InvGUI implements GUIPageController {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.##");

    private final SkillDefinition skill;

    private final String returnMenuId;

    private final ConfigurationSection config;

    private final List<Integer> sourceSlots = new ArrayList<>();

    private final List<Integer> attributeSlots = new ArrayList<>();

    private final List<Integer> rewardSlots = new ArrayList<>();

    private final Map<Integer, String> sourceStaticSlots = new LinkedHashMap<>();

    private final Map<Integer, String> rewardStaticSlots = new LinkedHashMap<>();

    private final Map<Integer, ObjectAction<ObjectSingleGUIAction, GUIActionContext>> actions = new LinkedHashMap<>();

    private final Map<Integer, ObjectCustomAttribute> shownAttributes = new LinkedHashMap<>();

    private int page;

    private int maximumPage;

    private View view = View.SOURCES;

    private enum View {
        SOURCES,
        REWARDS
    }

    public SkillDetailGUI(Player player, SkillDefinition skill) {
        this(player, skill, "skill-info");
    }

    public SkillDetailGUI(Player player, SkillDefinition skill, String returnMenuId) {
        super(player);
        this.skill = skill;
        this.returnMenuId = returnMenuId;
        this.config = ConfigManager.configManager.getSection("menu.skill-detail");
        parseLayouts();
    }

    @Override
    protected void constructGUI() {
        List<Integer> entrySlots = currentEntrySlots();
        if (entrySlots.isEmpty()) return;
        int entryCount = view == View.SOURCES ? skill.sources().size() : skill.maximumLevel();
        maximumPage = Math.max(0, (entryCount - 1) / entrySlots.size());
        if (view == View.SOURCES && !attributeSlots.isEmpty()) {
            maximumPage = Math.max(maximumPage,
                    Math.max(0, (skillAttributes().size() - 1) / attributeSlots.size()));
        }
        page = Math.min(page, maximumPage);

        String[] skillArgs = skillArgs();
        title = CommonUtil.modifyString(player,
                config.getString(view == View.SOURCES ? "source-title" : "reward-title",
                        config.getString("title", "&8{name}")), skillArgs);
        int size = config.getInt("size", 54);
        if (inv == null) {
            inv = EnchantmentReform.methodUtil.createNewInv(player, size, title, this);
        } else {
            inv.clear();
        }
        actions.clear();
        shownAttributes.clear();
        renderButtons(skillArgs);
        if (view == View.SOURCES) {
            renderSkillAttributes();
        }
        int first = page * entrySlots.size();
        for (int index = 0; index < entrySlots.size() && first + index < entryCount; index++) {
            int entryIndex = first + index;
            ItemStack item = view == View.SOURCES
                    ? buildSourceItem(skill.sources().get(entryIndex), entryIndex + 1)
                    : buildRewardItem(entryIndex + 1);
            setItem(entrySlots.get(index), item);
        }
    }

    @Override
    public boolean clickEventHandle(Inventory inventory, ClickType type, int slot) {
        ObjectCustomAttribute attribute = shownAttributes.get(slot);
        if (attribute != null) {
            new AttributeDetailGUI(player, attribute, skill, returnMenuId).openGUI();
            return true;
        }
        String symbol = currentStaticSlots().get(slot);
        if (symbol != null && symbol.equals(config.getString("back-symbol", "B"))) {
            new ConfiguredMenuGUI(player, returnMenuId).openGUI();
            return true;
        }
        if (symbol != null && symbol.equals(config.getString("view-symbol", "V"))) {
            view = view == View.SOURCES ? View.REWARDS : View.SOURCES;
            page = 0;
            inv = null;
            openGUI();
            return true;
        }
        GUIActionManager.INSTANCE.execute(actions.get(slot), player,
                new GUIActionContext(this, inventory, type, slot));
        return true;
    }

    @Override
    public void openPage(int page) {
        this.page = Math.max(0, Math.min(page, maximumPage));
        inv = null;
        openGUI();
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

    private void parseLayouts() {
        parseLayout(config.getStringList("layout"), config.getString("source-symbol", "S"),
                sourceSlots, sourceStaticSlots, false);
        parseAttributeSlots(config.getStringList("layout"),
                config.getString("attribute-symbol", "A"));
        List<String> rewardLayout = config.getStringList("reward-layout");
        if (rewardLayout.isEmpty()) rewardLayout = config.getStringList("layout");
        parseLayout(rewardLayout, config.getString("reward-symbol", "R"),
                rewardSlots, rewardStaticSlots, true);
    }

    private void parseAttributeSlots(List<String> layout, String symbol) {
        for (int rowIndex = 0; rowIndex < layout.size(); rowIndex++) {
            String row = layout.get(rowIndex);
            for (int column = 0; column < 9; column++) {
                if (column < row.length()
                        && String.valueOf(row.charAt(column)).equals(symbol)) {
                    int slot = rowIndex * 9 + column;
                    attributeSlots.add(slot);
                    sourceStaticSlots.remove(slot);
                }
            }
        }
    }

    private void parseLayout(List<String> layout, String symbol, List<Integer> entrySlots,
                             Map<Integer, String> staticSlots, boolean snake) {
        int rewardBand = 0;
        for (int rowIndex = 0; rowIndex < layout.size(); rowIndex++) {
            String row = layout.get(rowIndex);
            List<Integer> rowEntries = new ArrayList<>();
            for (int column = 0; column < 9; column++) {
                String value = column < row.length() ? String.valueOf(row.charAt(column)) : " ";
                int slot = rowIndex * 9 + column;
                if (value.equals(symbol)) rowEntries.add(slot);
                else if (!value.isBlank() && !value.equals(".")) staticSlots.put(slot, value);
            }
            if (snake && rowEntries.size() > 1 && rewardBand++ % 2 == 1) {
                java.util.Collections.reverse(rowEntries);
            }
            entrySlots.addAll(rowEntries);
        }
    }

    private List<Integer> currentEntrySlots() {
        return view == View.SOURCES ? sourceSlots : rewardSlots;
    }

    private Map<Integer, String> currentStaticSlots() {
        return view == View.SOURCES ? sourceStaticSlots : rewardStaticSlots;
    }

    private void renderButtons(String[] args) {
        for (Map.Entry<Integer, String> entry : currentStaticSlots().entrySet()) {
            if (entry.getKey() >= inv.getSize()) continue;
            ConfigurationSection button = config.getConfigurationSection("buttons." + entry.getValue());
            if (button == null) continue;
            boolean hidden = button.getBoolean("hide-on-first-page", false) && page == 0
                    || button.getBoolean("hide-on-last-page", false) && page == maximumPage;
            ConfigurationSection shown = hidden ? button.getConfigurationSection("hidden-item") : button;
            if (!hidden && entry.getValue().equals(config.getString("view-symbol", "V"))
                    && view == View.REWARDS && button.getConfigurationSection("reward-view-item") != null) {
                shown = button.getConfigurationSection("reward-view-item");
            }
            if (shown != null) setItem(entry.getKey(), buildItem(shown, args));
            if (!hidden && !entry.getValue().equals(config.getString("view-symbol", "V"))) {
                actions.put(entry.getKey(), GUIActionManager.INSTANCE.createButtonActions(button));
            }
        }
    }

    private ItemStack buildSourceItem(SkillSource source, int index) {
        String unit = source.unit(player);
        String[] args = merge(skillArgs(), new String[]{
                "source_id", source.id(), "source_name", source.name(player),
                "source_description", source.description(player), "source_xp", source.configuredXp(),
                "source_unit", unit.isBlank() ? "" : " " + unit,
                "source_index", String.valueOf(index)});
        ConfigurationSection itemConfig = source.icon() != null ? source.icon()
                : config.getConfigurationSection("source-item");
        ItemStack item = itemConfig == null ? new ItemStack(Material.EXPERIENCE_BOTTLE)
                : BuildItem.buildItemStack(player, itemConfig, args);
        ItemMeta meta = item.getItemMeta();
        ConfigurationSection template = config.getConfigurationSection("source-item");
        if (meta != null && template != null) {
            EnchantmentReform.methodUtil.setItemName(meta, CommonUtil.modifyString(player,
                    template.getString("name", "&f{source_name}"), args), player);
            List<String> lore = CommonUtil.modifyList(player, template.getStringList("lore"), args);
            EnchantmentReform.methodUtil.setItemLore(meta, wrapLore(lore), player);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void renderSkillAttributes() {
        List<ObjectCustomAttribute> attributes = skillAttributes();
        int first = page * attributeSlots.size();
        for (int index = 0;
             index < attributeSlots.size() && first + index < attributes.size(); index++) {
            int slot = attributeSlots.get(index);
            ObjectCustomAttribute attribute = attributes.get(first + index);
            shownAttributes.put(slot, attribute);
            setItem(slot, buildSkillAttributeItem(attribute));
        }
    }

    private List<ObjectCustomAttribute> skillAttributes() {
        if (AttributeManager.attributeManager == null) {
            return List.of();
        }
        return AttributeManager.attributeManager.getAttributes().stream()
                .filter(attribute -> attribute.isShownInSkillMenu(skill.id()))
                .sorted(Comparator.comparing(ObjectCustomAttribute::getId)).toList();
    }

    private ItemStack buildSkillAttributeItem(ObjectCustomAttribute attribute) {
        ConfigurationSection template = config.getConfigurationSection("attribute-item");
        if (template == null) {
            return new ItemStack(Material.NETHER_STAR);
        }
        int baseValue = attribute.getBaseValue(player);
        int value = attribute.getValue(player);
        String nextCost = baseValue >= attribute.getAllocationMaximumValue()
                ? CommonUtil.parseLang(player, "{lang:skill-attribute-summary-maximum}")
                : CommonUtil.modifyString(player,
                "{lang:skill-attribute-summary-next-cost}",
                "price", String.valueOf(attribute.getAllocationPrice(baseValue, 1)));
        String description = attribute.getLocalizedDescription(player, value);
        String[] args = merge(skillArgs(), new String[]{
                "attribute_id", attribute.getId(),
                "attribute_name", attribute.getLocalizedName(player),
                "attribute_description", description == null ? "" : description,
                "base_value", String.valueOf(baseValue),
                "value", String.valueOf(value),
                "maximum_value", String.valueOf(attribute.getAllocationMaximumValue()),
                "price", String.valueOf(attribute.getAllocationPrice(baseValue, 1)),
                "next_cost", nextCost,
                "points", String.valueOf(SkillManager.skillManager.getAttributePoints(player)),
                "requirement", attribute.getAllocationRequirementDisplay(player, baseValue + 1)});
        return buildItem(template, args);
    }

    private ItemStack buildRewardItem(int rewardLevel) {
        SkillDefinition.LevelReward reward = skill.rewardAt(rewardLevel);
        int currentLevel = SkillManager.skillManager.getLevel(player, skill);
        String statusKey = rewardLevel <= currentLevel ? "skill-reward-status-claimed"
                : rewardLevel == currentLevel + 1
                ? "skill-reward-status-next" : "skill-reward-status-locked";
        String attributes = formatRewardAttributes(reward);
        String commands = formatRewardCommands(reward, rewardLevel);
        String items = formatRewardItems(reward, rewardLevel);
        String rewardLines = formatRewardLines(reward, rewardLevel);
        String[] args = merge(skillArgs(), new String[]{
                "reward_level", String.valueOf(rewardLevel),
                "reward_status", CommonUtil.parseLang(player, "{lang:" + statusKey + "}"),
                "reward_count", String.valueOf(reward.rewards().size()),
                "reward_lines", rewardLines,
                "reward_attribute_points", String.valueOf(reward.attributePoints()),
                "reward_attributes", attributes,
                "reward_command_count", String.valueOf(reward.commands().size()),
                "reward_commands", commands,
                "reward_item_count", String.valueOf(reward.items().size()),
                "reward_items", items});
        String itemKey = rewardLevel <= currentLevel ? "reward-item-claimed"
                : rewardLevel == currentLevel + 1 ? "reward-item-next" : "reward-item";
        ConfigurationSection template = config.getConfigurationSection("reward-item");
        ConfigurationSection itemStyle = config.getConfigurationSection(itemKey);
        if (itemStyle == null) itemStyle = template;
        ItemStack item = itemStyle == null ? new ItemStack(Material.CHEST)
                : BuildItem.buildItemStack(player, itemStyle, args);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && template != null) {
            EnchantmentReform.methodUtil.setItemName(meta, CommonUtil.modifyString(player,
                    template.getString("name", "&eLevel {reward_level}"), args), player);
            EnchantmentReform.methodUtil.setItemLore(meta,
                    wrapLore(CommonUtil.modifyList(player, template.getStringList("lore"), args)), player);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatRewardLines(SkillDefinition.LevelReward reward, int rewardLevel) {
        if (reward.rewards().isEmpty()) {
            return CommonUtil.parseLang(player, "{lang:skill-reward-none}");
        }
        List<String> lines = new ArrayList<>();
        for (SkillDefinition.SkillReward configured : reward.rewards()) {
            String displayName;
            if (configured instanceof SkillDefinition.AttributePointsReward pointsReward) {
                displayName = formatRewardName(pointsReward.displayName(), rewardLevel,
                        pointsReward.amount(), "");
            } else if (configured instanceof SkillDefinition.AttributeReward attributeReward) {
                String attributeName = localizedAttributeName(attributeReward.attribute());
                displayName = formatRewardName(attributeReward.displayName(), rewardLevel,
                        attributeReward.amount(), attributeName);
            } else if (configured instanceof SkillDefinition.AttributesReward attributesReward) {
                String summary = formatAttributeSummary(attributesReward.attributes());
                displayName = CommonUtil.modifyString(player, attributesReward.displayName(),
                        "player", player.getName(), "skill", skill.id(),
                        "level", String.valueOf(rewardLevel), "attributes", summary);
            } else if (configured instanceof SkillDefinition.CommandReward commandReward) {
                displayName = formatRewardName(commandReward.displayName(), rewardLevel, 1, "");
            } else if (configured instanceof SkillDefinition.ItemReward itemReward) {
                String[] itemArgs = {"player", player.getName(), "skill", skill.id(),
                        "level", String.valueOf(rewardLevel)};
                ItemStack item = BuildItem.buildItemStack(player, itemReward.item(), itemArgs);
                String itemName = CommonUtil.modifyString(player, itemReward.displayName(),
                        rewardArgs(rewardLevel, item.getAmount(), ""));
                displayName = CommonUtil.modifyString(player,
                        "{lang:skill-reward-default-item}",
                        "item", itemName, "amount", String.valueOf(item.getAmount()));
            } else {
                continue;
            }
            lines.add(CommonUtil.modifyString(player, "{lang:skill-reward-line}",
                    "reward", displayName));
        }
        return String.join("\\n", lines);
    }

    private String formatAttributeSummary(Map<String, Integer> attributes) {
        List<String> values = new ArrayList<>();
        attributes.forEach((id, amount) -> values.add(CommonUtil.modifyString(player,
                "{lang:skill-reward-attribute-summary}",
                "attribute", localizedAttributeName(id), "amount", String.valueOf(amount))));
        return String.join(CommonUtil.parseLang(player,
                "{lang:skill-reward-attribute-separator}"), values);
    }

    private String formatRewardName(String configured, int rewardLevel,
                                    int amount, String attributeName) {
        return CommonUtil.modifyString(player, configured,
                rewardArgs(rewardLevel, amount, attributeName));
    }

    private String[] rewardArgs(int rewardLevel, int amount, String attributeName) {
        return new String[]{"player", player.getName(), "skill", skill.id(),
                "level", String.valueOf(rewardLevel), "amount", String.valueOf(amount),
                "attribute", attributeName};
    }

    private String localizedAttributeName(String id) {
        if (AttributeManager.attributeManager != null) {
            ObjectCustomAttribute attribute = AttributeManager.attributeManager.resolveAttribute(id);
            if (attribute != null) {
                return attribute.getLocalizedName(player);
            }
        }
        return id;
    }

    private String formatRewardAttributes(SkillDefinition.LevelReward reward) {
        if (reward.attributes().isEmpty()) {
            return CommonUtil.parseLang(player, "{lang:skill-reward-none}");
        }
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : reward.attributes().entrySet()) {
            String name = localizedAttributeName(entry.getKey());
            lines.add(CommonUtil.modifyString(player, "{lang:skill-reward-attribute-line}",
                    "attribute", name, "amount", String.valueOf(entry.getValue())));
        }
        return String.join("\\n", lines);
    }

    private String formatRewardCommands(SkillDefinition.LevelReward reward, int rewardLevel) {
        if (reward.commands().isEmpty()) {
            return CommonUtil.parseLang(player, "{lang:skill-reward-none}");
        }
        List<String> lines = new ArrayList<>();
        for (SkillDefinition.CommandReward command : reward.commands()) {
            String displayName = CommonUtil.modifyString(player, command.displayName(),
                    "player", player.getName(), "skill", skill.id(),
                    "level", String.valueOf(rewardLevel));
            lines.add(CommonUtil.modifyString(player, "{lang:skill-reward-command-line}",
                    "command", displayName));
        }
        return String.join("\\n", lines);
    }

    private String formatRewardItems(SkillDefinition.LevelReward reward, int rewardLevel) {
        if (reward.items().isEmpty()) {
            return CommonUtil.parseLang(player, "{lang:skill-reward-none}");
        }
        List<String> lines = new ArrayList<>();
        for (SkillDefinition.ItemReward itemReward : reward.items()) {
            String[] args = {"player", player.getName(), "skill", skill.id(),
                    "level", String.valueOf(rewardLevel)};
            ItemStack item = BuildItem.buildItemStack(player, itemReward.item(), args);
            String displayName = CommonUtil.modifyString(player, itemReward.displayName(), args);
            lines.add(CommonUtil.modifyString(player, "{lang:skill-reward-item-line}",
                    "item", displayName, "amount", String.valueOf(item.getAmount())));
        }
        return String.join("\\n", lines);
    }

    private String[] skillArgs() {
        SkillManager manager = SkillManager.skillManager;
        int level = manager.getLevel(player, skill);
        double xp = manager.getExperience(player, skill);
        double required = manager.getRequiredExperience(player, skill);
        double percent = required <= 0.0D ? 100.0D : Math.min(100.0D, xp / required * 100.0D);
        return new String[]{"id", skill.id(), "name", skill.name(player),
                "description", skill.description(player), "level", String.valueOf(level),
                "maximum_level", String.valueOf(skill.maximumLevel()), "experience", NUMBER.format(xp),
                "required_experience", NUMBER.format(required), "remaining_experience",
                NUMBER.format(Math.max(0.0D, required - xp)), "progress", NUMBER.format(percent),
                "progress_bar", progressBar(percent, 20),
                "source_count", String.valueOf(skill.sources().size()),
                "view", view.name().toLowerCase(),
                "page", String.valueOf(page + 1), "pages", String.valueOf(maximumPage + 1)};
    }

    private ItemStack buildItem(ConfigurationSection section, String[] args) {
        ItemStack item = BuildItem.buildItemStack(player, section, args);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && !section.getStringList("lore").isEmpty()) {
            EnchantmentReform.methodUtil.setItemLore(meta,
                    wrapLore(CommonUtil.modifyList(player, section.getStringList("lore"), args)), player);
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> wrapLore(List<String> lore) {
        List<String> result = new ArrayList<>();
        for (String configured : lore) {
            for (String line : configured.split("\\\\n", -1)) {
                result.addAll(TextUtil.wrapKeepColor(line,
                        ConfigManager.configManager.getInt("enchantment-description.wrap-length", 30)));
            }
        }
        return result;
    }

    private static String[] merge(String[] first, String[] second) {
        String[] merged = new String[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }

    private static String progressBar(double percent, int length) {
        int filled = (int) Math.round(Math.max(0.0D, Math.min(100.0D, percent))
                / 100.0D * length);
        return "&a" + "|".repeat(filled) + "&8" + "|".repeat(length - filled);
    }
}
