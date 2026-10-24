package cn.superiormc.enchantmentreform.gui.inv;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.gui.InvGUI;
import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;
import cn.superiormc.enchantmentreform.gui.action.GUIPageController;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.buttons.ObjectButton;
import cn.superiormc.enchantmentreform.objects.menus.ObjectMenu;
import cn.superiormc.enchantmentreform.objects.menus.ObjectMenuContent;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ConfiguredMenuGUI extends InvGUI implements GUIPageController {

    private record MenuEntry(SkillDefinition skill, ObjectCustomAttribute attribute,
                             ConfigurationSection content) {
    }

    private record MenuContent(ConfigurationSection section, List<Integer> slots,
                               List<MenuEntry> entries) {
    }

    private final String menuId;

    private final ObjectMenu menu;

    private final ConfigurationSection config;

    private final Map<Integer, MenuEntry> shownEntries = new LinkedHashMap<>();

    private final Map<Integer, ObjectButton> shownStaticItems = new LinkedHashMap<>();

    private int page;

    private int maximumPage;

    public ConfiguredMenuGUI(Player player, String menuId) {
        super(player);
        this.menuId = menuId == null ? "main" : menuId.toLowerCase(Locale.ROOT);
        this.menu = ConfigManager.configManager.getMenuObject(this.menuId);
        this.config = menu.getConfig();
    }

    @Override
    protected void constructGUI() {
        List<String> layout = menu.getLayout();
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
        title = CommonUtil.modifyString(player, config.getString("title", "&8" + menuId + " &8• &7{page}/{pages}"), args);
        int size = layout.size() * 9;
        if (inv == null || inv.getSize() != size) {
            inv = EnchantmentReform.methodUtil.createNewInv(player, size, title, this);
        } else {
            inv.clear();
        }
        shownEntries.clear();
        shownStaticItems.clear();
        renderStaticItems(args, layoutSlots);
        for (MenuContent content : contents) {
            renderContent(content);
        }
    }

    @Override
    public boolean clickEventHandle(Inventory inventory, ClickType type, int slot) {
        MenuEntry entry = shownEntries.get(slot);
        ObjectButton button = entry == null ? shownStaticItems.get(slot) : menu.getContentButton(entry.content());
        if (button == null) {
            return true;
        }
        button.clickEvent(player,
                new GUIActionContext(this, inventory, type, slot, menuId, menuId,
                        entry == null ? null : entry.skill(), entry == null ? null : entry.attribute(),
                        bypassPermission));
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
        List<MenuContent> result = new ArrayList<>();
        for (ObjectMenuContent content : menu.getContents().values()) {
            String id = content.getId();
            ConfigurationSection section = content.getConfig();
            if (section == null) {
                continue;
            }
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
        if (content.slots().isEmpty()) {
            return;
        }
        int first = page * content.slots().size();
        for (int index = 0;
             index < content.slots().size() && first + index < content.entries().size(); index++) {
            int slot = content.slots().get(index);
            if (slot < 0 || slot >= inv.getSize()) {
                continue;
            }
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
        for (ObjectButton button : menu.getButtons().values()) {
            String id = button.getId();
            ConfigurationSection definition = button.getConfig();
            if (definition == null) {
                continue;
            }
            boolean hidden = button.isHidden(page, maximumPage);
            ConfigurationSection displayed = button.getDisplayConfig(hidden);
            if (displayed == null) {
                continue;
            }
            ItemStack item = buildItem(displayed, args);
            for (int slot : symbolSlots(id, layoutSlots)) {
                if (slot < 0 || slot >= inv.getSize()) {
                    continue;
                }
                setItem(slot, item.clone());
                if (!hidden) {
                    shownStaticItems.put(slot, button);
                }
            }
        }
    }

    private ItemStack buildSkillItem(SkillDefinition skill, ConfigurationSection content) {
        SkillManager manager = SkillManager.skillManager;
        String[] args = merge(baseArgs(), manager.getProgressPlaceholders(player, skill));
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
        ConfigurationSection icon = content.getBoolean("use-attribute-icon", true)
                ? attribute.getConfig().getConfigurationSection("icon") : null;
        return buildTemplatedItem(icon, content.getConfigurationSection("item"),
                Material.NETHER_STAR, args);
    }

    private ItemStack buildTemplatedItem(ConfigurationSection base,
                                         ConfigurationSection template,
                                         Material fallback, String[] args) {
        ItemStack item = base == null ? new ItemStack(fallback)
                : BuildItem.buildItemStack(player, base, args);
        if (template == null) {
            return item;
        }
        ConfigurationSection materialSource = template.contains("material") ? template : null;
        if (materialSource != null) {
            item = BuildItem.buildItemStack(player, template, args);
        }
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

    private String[] baseArgs() {
        int points = SkillManager.skillManager == null
                ? 0 : SkillManager.skillManager.getAttributePoints(player);
        return new String[]{"menu", menuId, "player", player.getName(),
                "page", String.valueOf(page + 1), "pages", String.valueOf(maximumPage + 1),
                "points", String.valueOf(points), "attribute_points", String.valueOf(points)};
    }

    private Map<String, List<Integer>> layoutSlots(List<String> layout) {
        return menu.layoutSlots(layout);
    }

    private List<Integer> symbolSlots(String id, Map<String, List<Integer>> layoutSlots) {
        return menu.symbolSlots(id, layoutSlots);
    }

    private List<String> normalizedList(ConfigurationSection section, String path) {
        return section.getStringList(path).stream()
                .map(value -> value.toLowerCase(Locale.ROOT)).toList();
    }

}