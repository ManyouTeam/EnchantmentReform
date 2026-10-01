package cn.superiormc.enchantmentreform.gui.inv;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.gui.InvGUI;
import cn.superiormc.enchantmentreform.gui.action.GUIActionContext;
import cn.superiormc.enchantmentreform.gui.action.GUIPageController;
import cn.superiormc.enchantmentreform.gui.action.ObjectSingleGUIAction;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.managers.GUIActionManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.VanillaEnchantmentOverride;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.EnchantmentOrderUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class EnchantmentInfoGUI extends InvGUI implements GUIPageController {

    private static final String ALL_FILTER = "*";

    private final ConfigurationSection config;

    private final List<Integer> enchantmentSlots = new ArrayList<>();

    private final Map<Integer, String> staticSlots = new LinkedHashMap<>();

    private final Map<Integer, FilterType> filterSlots = new LinkedHashMap<>();

    private final Map<String, Set<Material>> itemTagCache = new LinkedHashMap<>();

    private String rarityFilter = ALL_FILTER;

    private String supportedItemsFilter = ALL_FILTER;

    private int page;

    private int maximumPage;

    private final Map<Integer, ObjectAction<ObjectSingleGUIAction, GUIActionContext>> slotActions =
            new LinkedHashMap<>();

    public EnchantmentInfoGUI(Player player) {
        super(player);
        this.config = ConfigManager.configManager.getEnchantmentMenu();
        this.page = 0;
        this.maximumPage = Math.max(0, maximumPage);
        parseLayout();
    }

    @Override
    protected void constructGUI() {
        List<EnchantmentInfoEntry> allEnchantments = getAllEnchantments();
        List<EnchantmentInfoEntry> enchantments = filterEnchantments(allEnchantments);
        if (enchantmentSlots.isEmpty()) {
            return;
        }
        setMaximumPage(Math.max(0, (enchantments.size() - 1) / enchantmentSlots.size()));
        int size = config.getInt("size", 54);
        title = CommonUtil.modifyString(player, config.getString("title", "{lang:enchantment-menu-title}"),
                "page", String.valueOf(getPage() + 1),
                "pages", String.valueOf(getMaximumPage() + 1));
        if (inv == null) {
            inv = EnchantmentReform.methodUtil.createNewInv(player, size, title, this);
        } else {
            inv.clear();
        }
        slotActions.clear();
        filterSlots.clear();
        renderStaticItems(enchantments.size());
        renderEnchantments(enchantments);
    }

    @Override
    public boolean clickEventHandle(Inventory inventory, ClickType type, int slot) {
        FilterType filterType = filterSlots.get(slot);
        if (filterType != null) {
            cycleFilter(filterType, type.isRightClick() ? -1 : 1);
            return true;
        }
        GUIActionManager.INSTANCE.execute(slotActions.get(slot), player,
                new GUIActionContext(this, inventory, type, slot));
        return true;
    }

    @Override
    public void openPage(int page) {
        setPage(page);
        constructGUI();
    }

    @Override
    public int getCurrentPage() {
        return getPage();
    }

    @Override
    public int getPageCount() {
        return getMaximumPage() + 1;
    }

    @Override
    public ConfigurationSection getSection() {
        return config;
    }

    private void parseLayout() {
        int slot = 0;
        String enchantmentSymbol = config.getString("enchantment-symbol", "E");
        for (String row : config.getStringList("layout")) {
            for (int column = 0; column < 9; column++) {
                String symbol = column < row.length() ? String.valueOf(row.charAt(column)) : " ";
                if (symbol.equals(enchantmentSymbol)) {
                    enchantmentSlots.add(slot);
                } else if (!symbol.isBlank() && !symbol.equals(".")) {
                    staticSlots.put(slot, symbol);
                }
                slot++;
            }
        }
    }

    private void renderStaticItems(int enchantmentAmount) {
        for (Map.Entry<Integer, String> entry : staticSlots.entrySet()) {
            if (entry.getKey() >= inv.getSize()) {
                continue;
            }
            ConfigurationSection button = config.getConfigurationSection("buttons." + entry.getValue());
            if (button == null) {
                continue;
            }
            boolean hidden = button.getBoolean("hide-on-first-page", false) && getPage() == 0
                    || button.getBoolean("hide-on-last-page", false) && getPage() == getMaximumPage();
            if (hidden) {
                ConfigurationSection hiddenItem = button.getConfigurationSection("hidden-item");
                if (hiddenItem != null) {
                    setItem(entry.getKey(), buildGuiItem(hiddenItem,
                            "page", String.valueOf(getPage() + 1),
                            "pages", String.valueOf(getMaximumPage() + 1),
                            "amount", String.valueOf(enchantmentAmount)));
                }
                continue;
            }
            FilterType filterType = FilterType.parse(button.getString("filter-type"));
            if (filterType != null) {
                filterSlots.put(entry.getKey(), filterType);
            }
            setItem(entry.getKey(), buildGuiItem(button,
                    "page", String.valueOf(getPage() + 1),
                    "pages", String.valueOf(getMaximumPage() + 1),
                    "amount", String.valueOf(enchantmentAmount),
                    "rarity_filter", displayRarityFilter(),
                    "supported_item_filter", displaySupportedItemsFilter()));
            slotActions.put(entry.getKey(), GUIActionManager.INSTANCE.createButtonActions(button));
        }
    }

    private void renderEnchantments(List<EnchantmentInfoEntry> enchantments) {
        int first = getPage() * enchantmentSlots.size();
        for (int index = 0; index < enchantmentSlots.size() && first + index < enchantments.size(); index++) {
            int slot = enchantmentSlots.get(index);
            if (slot < inv.getSize()) {
                setItem(slot, buildEnchantmentItem(enchantments.get(first + index)));
            }
        }
    }

    private ItemStack buildEnchantmentItem(EnchantmentInfoEntry enchantment) {
        ConfigurationSection itemSection = config.getConfigurationSection("enchantment-item");
        if (itemSection == null) {
            return new ItemStack(Material.BOOK);
        }
        String name = displayName(enchantment);
        String description = "";
        List<String> levelDescriptions = new ArrayList<>();
        PowerEnchantmentDefinition definition = enchantment.definition();
        if (definition != null && definition.getDescription() != null) {
            description = definition.getLocalizedDescription(player, definition.getMaxLevel());
            for (int level = 1; level <= definition.getMaxLevel(); level++) {
                levelDescriptions.add(CommonUtil.modifyString(player,
                        "{lang:enchantment-menu-level-description}",
                        "level", String.valueOf(level),
                        "description", definition.getLocalizedDescription(player, level)));
            }
        }

        ItemStack item = buildGuiItem(itemSection,
                "name", name,
                "description", description,
                "level-descriptions", String.join("\\n", levelDescriptions),
                "max_level", String.valueOf(enchantment.maxLevel()),
                "rarity", displayRarity(enchantment),
                "weight", String.valueOf(weight(enchantment)),
                "supported_items", displaySupportedItems(enchantment),
                "key", enchantment.key());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            EnchantmentReform.methodUtil.setItemName(meta,
                    CommonUtil.modifyString(player, itemSection.getString("name", "&a{name}"),
                            "name", name,
                            "key", enchantment.key()), player);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int resolveWrapLength() {
        return ConfigManager.configManager.getInt("enchantment-description.wrap-length", 30);
    }

    private ItemStack buildGuiItem(ConfigurationSection section, String... args) {
        ItemStack item = BuildItem.buildItemStack(player, section, args);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        List<String> configuredLore = section.getStringList("lore");
        if (configuredLore.isEmpty()) {
            return item;
        }
        List<String> wrappedLore = new ArrayList<>();
        for (String configuredLine : CommonUtil.modifyList(player, configuredLore, args)) {
            for (String line : configuredLine.split("\\\\n", -1)) {
                wrappedLore.addAll(TextUtil.wrapKeepColor(line, resolveWrapLength()));
            }
        }
        EnchantmentReform.methodUtil.setItemLore(meta, wrappedLore, player);
        item.setItemMeta(meta);
        return item;
    }

    private List<EnchantmentInfoEntry> getAllEnchantments() {
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        if (manager == null) {
            return List.of();
        }
        List<EnchantmentInfoEntry> result = new ArrayList<>();
        manager.getEnchantments().forEach(enchantment ->
                result.add(new EnchantmentInfoEntry(enchantment, null)));
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (!"minecraft".equals(enchantment.getKey().getNamespace())) {
                continue;
            }
            VanillaEnchantmentOverride override = manager.getVanillaEnchantment(enchantment.getKey().toString());
            if (override == null || override.isEnabled()) {
                result.add(new EnchantmentInfoEntry(override, enchantment));
            }
        }
        result.sort(Comparator
                .comparing(this::rarity, rarityComparator())
                .thenComparing(EnchantmentInfoEntry::key));
        return result;
    }

    private List<EnchantmentInfoEntry> filterEnchantments(
            List<EnchantmentInfoEntry> enchantments) {
        SupportedItemsFilter supportedFilter = findSupportedItemsFilter(supportedItemsFilter);
        return enchantments.stream()
                .filter(enchantment -> ALL_FILTER.equals(rarityFilter)
                        || rarity(enchantment).equalsIgnoreCase(rarityFilter))
                .filter(enchantment -> ALL_FILTER.equals(supportedItemsFilter)
                        || supportedFilter != null && matchesFilter(enchantment, supportedFilter))
                .toList();
    }

    private void cycleFilter(FilterType type, int direction) {
        List<String> values = Stream.concat(
                        Stream.of(ALL_FILTER),
                        type == FilterType.RARITY
                                ? getAllEnchantments().stream()
                                        .map(this::rarity)
                                        .filter(Objects::nonNull)
                                        .filter(value -> !value.isBlank())
                                        .distinct()
                                        .sorted(rarityComparator())
                                : supportedItemsFilters().stream().map(SupportedItemsFilter::id))
                .toList();
        String current = type == FilterType.RARITY ? rarityFilter : supportedItemsFilter;
        int currentIndex = values.indexOf(current);
        int nextIndex = Math.floorMod((currentIndex < 0 ? 0 : currentIndex) + direction,
                values.size());
        if (type == FilterType.RARITY) {
            rarityFilter = values.get(nextIndex);
        } else {
            supportedItemsFilter = values.get(nextIndex);
        }
        page = 0;
        constructGUI();
    }

    private String displayRarityFilter() {
        if (ALL_FILTER.equals(rarityFilter)) {
            return CommonUtil.parseLang(player, "{lang:enchantment-menu-filter-all}");
        }
        return displayRarityName(rarityFilter);
    }

    private String displaySupportedItemsFilter() {
        return ALL_FILTER.equals(supportedItemsFilter)
                ? CommonUtil.parseLang(player, "{lang:enchantment-menu-filter-all}")
                : supportedItemsFilters().stream()
                .filter(filter -> filter.id().equalsIgnoreCase(supportedItemsFilter))
                .findFirst()
                .map(filter -> CommonUtil.parseLang(player, filter.displayName()))
                .orElse(supportedItemsFilter);
    }

    private String displayRarity(EnchantmentInfoEntry enchantment) {
        return enchantment.definition() == null
                ? displayRarityName(rarity(enchantment))
                : enchantment.definition().getLocalizedRarityName(player);
    }

    private String displayRarityName(String rarity) {
        ConfigurationSection section = findSectionIgnoreCase(
                ConfigManager.configManager.getRarities(), rarity);
        String configured = section == null ? null : section.getString("display-name");
        if (configured == null || configured.isBlank()) {
            configured = "{lang:rarity-" + rarity.toLowerCase(Locale.ROOT) + "}";
        }
        return CommonUtil.parseLang(player, configured);
    }

    private String displaySupportedItems(EnchantmentInfoEntry enchantment) {
        List<String> names = supportedItemsFilters().stream()
                .filter(filter -> matchesFilter(enchantment, filter))
                .map(filter -> CommonUtil.parseLang(player, filter.displayName()))
                .toList();
        String value;
        if (!names.isEmpty()) {
            value = String.join(", ", names);
        } else {
            value = enchantment.definition() == null
                    ? "-"
                    : enchantment.definition().getSupportedItemKeys().stream()
                    .map(EnchantmentInfoGUI::normalizeTag)
                    .findFirst().orElse("-");
        }
        return value;
    }

    private String displayName(EnchantmentInfoEntry enchantment) {
        String name = enchantment.definition() == null
                ? null : enchantment.definition().getLocalizedName(player);
        return name == null ? enchantment.vanilla().getKey().getKey() : name;
    }

    private List<SupportedItemsFilter> supportedItemsFilters() {
        ConfigurationSection section = ConfigManager.configManager.getSupportedItems()
                .getConfigurationSection("filters");
        if (section == null) {
            return List.of();
        }
        List<SupportedItemsFilter> result = new ArrayList<>();
        for (String id : section.getKeys(false)) {
            ConfigurationSection option = section.getConfigurationSection(id);
            if (option == null) {
                continue;
            }
            List<String> tags = stringOrList(option, "tags").stream()
                    .map(EnchantmentInfoGUI::normalizeTag)
                    .filter(value -> !value.isBlank())
                    .toList();
            if (tags.isEmpty()) {
                continue;
            }
            result.add(new SupportedItemsFilter(id, option.getString("display-name", id), tags));
        }
        return result;
    }

    private SupportedItemsFilter findSupportedItemsFilter(String id) {
        return supportedItemsFilters().stream()
                .filter(filter -> filter.id().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    private boolean matchesFilter(EnchantmentInfoEntry enchantment, SupportedItemsFilter filter) {
        Set<Material> filterItems = new LinkedHashSet<>();
        for (String tag : filter.tags()) {
            filterItems.addAll(resolveItemTag(tag, new LinkedHashSet<>()));
        }
        if (filterItems.isEmpty()) {
            return enchantment.definition() != null && filter.tags().stream().anyMatch(tag ->
                    enchantment.definition().getSupportedItemKeys().stream().anyMatch(supported ->
                            tag.equalsIgnoreCase(normalizeTag(supported))));
        }
        if (enchantment.vanilla() != null) {
            return filterItems.stream()
                    .filter(Material::isItem)
                    .anyMatch(material -> enchantment.vanilla().canEnchantItem(new ItemStack(material)));
        }
        Set<Material> supported = enchantment.definition().getSupportedItemKeys().stream()
                .flatMap(tag -> resolveItemTag(normalizeTag(tag), new LinkedHashSet<>()).stream())
                .collect(java.util.stream.Collectors.toSet());
        return filterItems.stream().anyMatch(supported::contains);
    }

    private Set<Material> resolveItemTag(String tag, Set<String> resolving) {
        String normalized = normalizeTag(tag);
        Set<Material> cached = itemTagCache.get(normalized);
        if (cached != null) {
            return cached;
        }
        if (!resolving.add(normalized)) {
            return Set.of();
        }
        ConfigurationSection definition = configuredTagDefinitions().get(normalized);
        LinkedHashSet<Material> result = new LinkedHashSet<>();
        if (definition != null) {
            for (String value : definition.getStringList("values")) {
                if (value.startsWith("#")) {
                    result.addAll(resolveItemTag(value.substring(1), resolving));
                } else {
                    Material material = Material.matchMaterial(value);
                    if (material != null && material.isItem()) {
                        result.add(material);
                    }
                }
            }
        } else {
            NamespacedKey key = NamespacedKey.fromString(normalized);
            Tag<Material> bukkitTag = key == null ? null
                    : Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material.class);
            if (bukkitTag != null) {
                result.addAll(bukkitTag.getValues());
            }
        }
        resolving.remove(normalized);
        Set<Material> resolved = Set.copyOf(result);
        itemTagCache.put(normalized, resolved);
        return resolved;
    }

    private Map<String, ConfigurationSection> configuredTagDefinitions() {
        ConfigurationSection tags = ConfigManager.configManager.getSupportedItems()
                .getConfigurationSection("tags");
        if (tags == null) {
            return Map.of();
        }
        Map<String, ConfigurationSection> result = new LinkedHashMap<>();
        for (String id : tags.getKeys(false)) {
            ConfigurationSection definition = tags.getConfigurationSection(id);
            if (definition != null) {
                result.put(normalizeTag(definition.getString("key", id)), definition);
            }
        }
        return result;
    }

    private String rarity(EnchantmentInfoEntry enchantment) {
        if (enchantment.definition() != null) {
            return enchantment.definition().getRarity();
        }
        if (enchantment.vanilla().isCursed()) {
            return "CURSE";
        }
        Object paperRarity = invokeNoArgs(enchantment.vanilla(), "getRarity");
        if (paperRarity != null) {
            String value = paperRarity.toString().toUpperCase(Locale.ROOT);
            return value.equals("VERY_RARE") ? "LEGENDARY" : value;
        }
        return enchantment.vanilla().isTreasure() ? "RARE" : "COMMON";
    }

    private int weight(EnchantmentInfoEntry enchantment) {
        if (enchantment.definition() != null) {
            return enchantment.definition().getWeight();
        }
        Object value = invokeNoArgs(enchantment.vanilla(), "getWeight");
        if (value instanceof Number number) {
            return number.intValue();
        }
        ConfigurationSection rarity = findSectionIgnoreCase(ConfigManager.configManager.getRarities(), rarity(enchantment));
        return rarity == null ? 0 : rarity.getInt("weight", 0);
    }

    private static Object invokeNoArgs(Object target, String name) {
        try {
            Method method = target.getClass().getMethod(name);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static ConfigurationSection findSectionIgnoreCase(ConfigurationSection parent, String expected) {
        ConfigurationSection direct = parent.getConfigurationSection(expected);
        if (direct != null) {
            return direct;
        }
        for (String key : parent.getKeys(false)) {
            if (key.equalsIgnoreCase(expected)) {
                return parent.getConfigurationSection(key);
            }
        }
        return null;
    }

    private static List<String> stringOrList(ConfigurationSection section, String path) {
        Object value = section.get(path);
        if (value instanceof String text) {
            return text.isBlank() ? List.of() : List.of(text);
        }
        return value instanceof Collection<?> collection
                ? collection.stream().map(String::valueOf).toList() : List.of();
    }

    private static String normalizeTag(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        return normalized.contains(":") ? normalized : "enchantmentreform:" + normalized;
    }

    private int getPage() {
        return page;
    }

    private void setPage(int page) {
        this.page = Math.max(0, Math.min(page, maximumPage));
    }

    private int getMaximumPage() {
        return maximumPage;
    }

    private void setMaximumPage(int maximumPage) {
        this.maximumPage = Math.max(0, maximumPage);
        setPage(page);
    }

    private enum FilterType {
        RARITY,
        SUPPORTED_ITEM;

        private static FilterType parse(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return valueOf(value.strip().toUpperCase(Locale.ROOT).replace('-', '_'));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    private record SupportedItemsFilter(String id, String displayName, List<String> tags) {}

    private record EnchantmentInfoEntry(PowerEnchantmentDefinition definition,
                                        Enchantment vanilla) {
        private String key() {
            return definition == null ? vanilla.getKey().toString() : definition.getKey().asString();
        }

        private int maxLevel() {
            return definition == null ? vanilla.getMaxLevel() : definition.getMaxLevel();
        }
    }

    private Comparator<String> rarityComparator() {
        return EnchantmentOrderUtil.rarityComparator(
                ConfigManager.configManager.getRarities(), config);
    }
}
