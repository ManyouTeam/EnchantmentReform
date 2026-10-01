package cn.superiormc.enchantmentreform.gui.inv;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.gui.InvGUI;
import cn.superiormc.enchantmentreform.gui.action.GUIPageController;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
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
import java.util.Locale;
import java.util.Map;

public final class ConfiguredMenuGUI extends InvGUI implements GUIPageController {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.##");

    private record MenuEntry(SkillDefinition skill, ObjectCustomAttribute attribute,
                             ConfigurationSection content) {}

    private record MenuContent(ConfigurationSection section, List<Integer> slots,
                               List<MenuEntry> entries) {}

    private final String menuId;

    private final ConfigurationSection config;

    private final Map<Integer, MenuEntry> shownEntries = new LinkedHashMap<>();

    private final Map<Integer, ConfigurationSection> shownStaticItems = new LinkedHashMap<>();

    private int page;

    private int maximumPage;

    public ConfiguredMenuGUI(Player player, String menuId) {
        super(player);
        this.menuId = menuId == null ? "main" : menuId.toLowerCase(Locale.ROOT);
        this.config = ConfigManager.configManager.getMenu(this.menuId);
    }

    @Override
    protected void constructGUI() {
        List<String> layout = config.getStringList("layout");
        if (layout.isEmpty() || layout.size() > 6
                || layout.stream().anyMatch(row -> row.length() != 9)) {
            throw new IllegalArgumentException("Menu '" + menuId
                    + "' requires a layout containing 1-6 rows of exactly 9 characters");
        }
        Map<String, List<Integer>> layoutSlots = layoutSlots(layout);
        List<MenuContent> contents = loadContents(layoutSlots);
        maximumPage = 0;
        for (MenuContent content : contents) {
            if (!content.slots().isEmpty()) {
                maximumPage = Math.max(maximumPage,
                        Math.max(0, (content.entries().size() - 1) / content.slots().size()));
            }
        }
        page = Math.max(0, Math.min(page, maximumPage));
        String[] args = baseArgs();
        title = CommonUtil.modifyString(player, config.getString("title", "&8" + menuId), args);
        int size = layout.size() * 9;
        if (inv == null || inv.getSize() != size) {
            inv = EnchantmentReform.methodUtil.createNewInv(player, size, title, this);
        } else {
            inv.clear();
        }
        shownEntries.clear();
        shownStaticItems.clear();
        renderStaticItems(args, layoutSlots);
        for (MenuContent content : contents) renderContent(content);
    }

    @Override
    public boolean clickEventHandle(Inventory inventory, ClickType type, int slot) {
        MenuEntry entry = shownEntries.get(slot);
        if (entry != null) {
            executeActions(clickActions(entry.content(), type), entry);
            return true;
        }
        ConfigurationSection item = shownStaticItems.get(slot);
        if (item != null) {
            executeActions(clickActions(item, type), null);
        }
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

    private List<MenuContent> loadContents(Map<String, List<Integer>> layoutSlots) {
        ConfigurationSection contents = config.getConfigurationSection("contents");
        if (contents == null) return List.of();
        List<MenuContent> result = new ArrayList<>();
        for (String id : contents.getKeys(false)) {
            ConfigurationSection section = contents.getConfigurationSection(id);
            if (section == null) continue;
            List<Integer> slots = symbolSlots(id, layoutSlots);
            String type = section.getString("type", "").toLowerCase(Locale.ROOT);
            List<MenuEntry> entries = switch (type) {
                case "skills" -> skillEntries(section);
                case "attributes" -> attributeEntries(section);
                default -> List.of();
            };
            result.add(new MenuContent(section, slots, entries));
        }
        return result;
    }

    private List<MenuEntry> skillEntries(ConfigurationSection content) {
        if (SkillManager.skillManager == null) {
            return List.of();
        }
        List<String> include = normalizedList(content, "include");
        List<String> exclude = normalizedList(content, "exclude");
        return SkillManager.skillManager.getSkills().stream()
                .filter(skill -> include.isEmpty() || include.contains(skill.id()))
                .filter(skill -> !exclude.contains(skill.id()))
                .sorted(Comparator.comparingInt(SkillDefinition::order)
                        .thenComparing(SkillDefinition::id))
                .map(skill -> new MenuEntry(skill, null, content)).toList();
    }

    private List<MenuEntry> attributeEntries(ConfigurationSection content) {
        if (AttributeManager.attributeManager == null) {
            return List.of();
        }
        List<String> include = normalizedList(content, "include");
        List<String> exclude = normalizedList(content, "exclude");
        String mode = content.getString("mode", "all").toLowerCase(Locale.ROOT);
        String skill = content.getString("skill", "");
        return AttributeManager.attributeManager.getAttributes().stream()
                .filter(attribute -> include.isEmpty() || include.contains(attribute.getId()))
                .filter(attribute -> !exclude.contains(attribute.getId()))
                .filter(attribute -> switch (mode) {
                    case "allocation" -> attribute.isShownInAllocationMenu();
                    case "information", "info" -> attribute.isShownInAttributeInfoMenu();
                    case "skill" -> attribute.isShownInSkillMenu(skill);
                    default -> true;
                })
                .sorted(Comparator.comparing(ObjectCustomAttribute::getId))
                .map(attribute -> new MenuEntry(null, attribute, content)).toList();
    }

    private void renderContent(MenuContent content) {
        if (content.slots().isEmpty()) return;
        int first = page * content.slots().size();
        for (int index = 0;
             index < content.slots().size() && first + index < content.entries().size(); index++) {
            int slot = content.slots().get(index);
            if (slot < 0 || slot >= inv.getSize()) continue;
            MenuEntry entry = content.entries().get(first + index);
            ItemStack item = entry.skill() == null
                    ? buildAttributeItem(entry.attribute(), content.section())
                    : buildSkillItem(entry.skill(), content.section());
            setItem(slot, item);
            shownEntries.put(slot, entry);
        }
    }

    private void renderStaticItems(String[] args,
                                   Map<String, List<Integer>> layoutSlots) {
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) return;
        for (String id : items.getKeys(false)) {
            ConfigurationSection definition = items.getConfigurationSection(id);
            if (definition == null) continue;
            boolean hidden = definition.getBoolean("hide-on-first-page", false) && page == 0
                    || definition.getBoolean("hide-on-last-page", false) && page == maximumPage;
            ConfigurationSection displayed = hidden
                    ? definition.getConfigurationSection("hidden-item")
                    : definition.getConfigurationSection("item");
            if (displayed == null && !hidden) displayed = definition;
            if (displayed == null) continue;
            ItemStack item = buildItem(displayed, args);
            for (int slot : symbolSlots(id, layoutSlots)) {
                if (slot < 0 || slot >= inv.getSize()) continue;
                setItem(slot, item.clone());
                if (!hidden) shownStaticItems.put(slot, definition);
            }
        }
    }

    private ItemStack buildSkillItem(SkillDefinition skill, ConfigurationSection content) {
        SkillManager manager = SkillManager.skillManager;
        int level = manager.getLevel(player, skill);
        double xp = manager.getExperience(player, skill);
        double required = manager.getRequiredExperience(player, skill);
        double percent = required <= 0.0D ? 100.0D : Math.min(100.0D, xp / required * 100.0D);
        String[] args = merge(baseArgs(), new String[]{
                "id", skill.id(), "name", skill.name(player),
                "description", skill.description(player), "level", String.valueOf(level),
                "maximum_level", String.valueOf(skill.maximumLevel()),
                "experience", NUMBER.format(xp), "required_experience", NUMBER.format(required),
                "remaining_experience", NUMBER.format(Math.max(0.0D, required - xp)),
                "progress", NUMBER.format(percent),
                "progress_bar", progressBar(percent, 20),
                "source_count", String.valueOf(skill.sources().size())});
        ConfigurationSection template = content.getConfigurationSection("item");
        ConfigurationSection icon = content.getBoolean("use-skill-icon", true) ? skill.icon() : null;
        return buildTemplatedItem(icon, template, Material.EXPERIENCE_BOTTLE, args);
    }

    private ItemStack buildAttributeItem(ObjectCustomAttribute attribute,
                                         ConfigurationSection content) {
        int base = attribute.getBaseValue(player);
        int value = attribute.getValue(player);
        int next = Math.min(attribute.getAllocationMaximumValue(), base + 1);
        String description = attribute.getLocalizedDescription(player, value);
        String nextDescription = attribute.getLocalizedDescription(
                player, attribute.getValueAtBase(player, next));
        String[] args = merge(baseArgs(), new String[]{
                "id", attribute.getId(), "name", attribute.getLocalizedName(player),
                "description", description == null ? "" : description,
                "current_description", description == null ? "" : description,
                "next_description", nextDescription == null ? "" : nextDescription,
                "base_value", String.valueOf(base), "value", String.valueOf(value),
                "minimum_value", String.valueOf(attribute.getMinimumValue()),
                "maximum_value", String.valueOf(attribute.getAllocationMaximumValue()),
                "next_base", String.valueOf(next),
                "next_value", String.valueOf(attribute.getValueAtBase(player, next)),
                "next_price", String.valueOf(attribute.getAllocationPrice(base, 1)),
                "price", String.valueOf(attribute.getAllocationPrice(base, 1)),
                "requirement", attribute.getAllocationRequirementDisplay(player, next),
                "modifier_count", String.valueOf(attribute.getModifiers(player).size())});
        return buildTemplatedItem(null, content.getConfigurationSection("item"),
                Material.NETHER_STAR, args);
    }

    private ItemStack buildTemplatedItem(ConfigurationSection base,
                                         ConfigurationSection template,
                                         Material fallback, String[] args) {
        ItemStack item = base == null ? new ItemStack(fallback)
                : BuildItem.buildItemStack(player, base, args);
        if (template == null) return item;
        ConfigurationSection materialSource = template.contains("material") ? template : null;
        if (materialSource != null) item = BuildItem.buildItemStack(player, template, args);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            EnchantmentReform.methodUtil.setItemName(meta, CommonUtil.modifyString(player,
                    template.getString("name", "&f{name}"), args), player);
            EnchantmentReform.methodUtil.setItemLore(meta,
                    wrapLore(CommonUtil.modifyList(player, template.getStringList("lore"), args)), player);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildItem(ConfigurationSection section, String[] args) {
        ItemStack item = BuildItem.buildItemStack(player, section, args);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && !section.getStringList("lore").isEmpty()) {
            EnchantmentReform.methodUtil.setItemLore(meta,
                    wrapLore(CommonUtil.modifyList(player, section.getStringList("lore"), args)), player);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void executeActions(ConfigurationSection actions, MenuEntry entry) {
        if (actions == null) return;
        if (actions.contains("type")) {
            executeAction(actions, entry);
            return;
        }
        for (String id : actions.getKeys(false)) {
            ConfigurationSection action = actions.getConfigurationSection(id);
            if (action != null) executeAction(action, entry);
        }
    }

    private void executeAction(ConfigurationSection action, MenuEntry entry) {
        String type = action.getString("type", "").toLowerCase(Locale.ROOT);
        switch (type) {
            case "previous_page" -> openPage(page - 1);
            case "next_page" -> openPage(page + 1);
            case "refresh" -> constructGUI();
            case "close" -> player.closeInventory();
            case "open_menu" -> openMenu(action.getString("menu", "main"));
            case "open_skill" -> {
                if (entry != null && entry.skill() != null) {
                    new SkillDetailGUI(player, entry.skill(), menuId).openGUI();
                }
            }
            case "open_attribute" -> {
                if (entry != null && entry.attribute() != null) {
                    new AttributeDetailGUI(player, entry.attribute(), menuId).openGUI();
                }
            }
            case "allocate_attribute" -> {
                if (entry != null && entry.attribute() != null) {
                    int add = Math.max(1, action.getInt("add", 1));
                    int price = entry.attribute().getAllocationPrice(
                            entry.attribute().getBaseValue(player), add);
                    SkillManager.AllocationResult result = SkillManager.skillManager.allocate(
                            player, entry.attribute().getId(), add);
                    sendAllocationResult(result, entry.attribute(), add, price);
                    constructGUI();
                }
            }
            case "command" -> {
                String command = CommonUtil.modifyString(player,
                        action.getString("command", ""),
                        "player", player.getName(), "menu", menuId);
                if (!command.isBlank()) {
                    if (action.getBoolean("as-console", true)) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } else {
                        player.performCommand(command);
                    }
                }
            }
            default -> { }
        }
    }

    private void openMenu(String targetId) {
        if (!ConfigManager.configManager.hasMenu(targetId)) {
            TextUtil.sendMessage(player, CommonUtil.parseLang(player,
                    "{lang:menu-not-found}"));
            return;
        }
        if ("enchantment-info".equalsIgnoreCase(targetId)) {
            new EnchantmentInfoGUI(player).openGUI();
        } else {
            new ConfiguredMenuGUI(player, targetId).openGUI();
        }
    }

    private void sendAllocationResult(SkillManager.AllocationResult result,
                                      ObjectCustomAttribute attribute, int add, int price) {
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

    private ConfigurationSection clickActions(ConfigurationSection section, ClickType click) {
        ConfigurationSection specific = section.getConfigurationSection(
                "click-actions." + click.name());
        return specific == null ? section.getConfigurationSection("actions") : specific;
    }

    private String[] baseArgs() {
        int points = SkillManager.skillManager == null
                ? 0 : SkillManager.skillManager.getAttributePoints(player);
        return new String[]{"menu", menuId, "player", player.getName(),
                "page", String.valueOf(page + 1), "pages", String.valueOf(maximumPage + 1),
                "points", String.valueOf(points), "attribute_points", String.valueOf(points)};
    }

    private Map<String, List<Integer>> layoutSlots(List<String> layout) {
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        for (int row = 0; row < layout.size(); row++) {
            for (int column = 0; column < 9; column++) {
                String symbol = String.valueOf(layout.get(row).charAt(column));
                result.computeIfAbsent(symbol, ignored -> new ArrayList<>())
                        .add(row * 9 + column);
            }
        }
        return result;
    }

    private List<Integer> symbolSlots(String id,
                                      Map<String, List<Integer>> layoutSlots) {
        if (id.length() != 1) {
            throw new IllegalArgumentException("Menu '" + menuId
                    + "' layout entry '" + id + "' must be one character");
        }
        return layoutSlots.getOrDefault(id, List.of());
    }

    private static String progressBar(double percent, int length) {
        int filled = (int) Math.round(Math.max(0.0D, Math.min(100.0D, percent))
                / 100.0D * length);
        return "&a" + "|".repeat(filled) + "&8" + "|".repeat(length - filled);
    }

    private List<String> normalizedList(ConfigurationSection section, String path) {
        return section.getStringList(path).stream()
                .map(value -> value.toLowerCase(Locale.ROOT)).toList();
    }

    private List<String> wrapLore(List<String> configured) {
        List<String> result = new ArrayList<>();
        for (String value : configured) {
            if (value.isBlank()) {
                result.add("");
                continue;
            }
            for (String line : value.split("\\\\n", -1)) {
                result.addAll(TextUtil.wrapKeepColor(line,
                        ConfigManager.configManager.getInt(
                                "enchantment-description.wrap-length", 30)));
            }
        }
        return result;
    }

    private static String[] merge(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
