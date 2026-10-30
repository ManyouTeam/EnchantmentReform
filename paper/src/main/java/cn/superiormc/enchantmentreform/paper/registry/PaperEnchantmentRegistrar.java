package cn.superiormc.enchantmentreform.paper.registry;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.VanillaEnchantmentOverride;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.RegistryEntryAddEvent;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class PaperEnchantmentRegistrar {

    private PaperEnchantmentRegistrar() {
    }

    public static void bind(BootstrapContext context,
                            Collection<ObjectCustomEnchantment> definitions,
                            Collection<VanillaEnchantmentOverride> vanillaDefinitions,
                            ConfigurationSection supportedItemsConfig) {
        List<ObjectCustomEnchantment> snapshot = List.copyOf(definitions);
        List<VanillaEnchantmentOverride> vanillaSnapshot = List.copyOf(vanillaDefinitions);
        List<PowerEnchantmentDefinition> allDefinitions = new ArrayList<>(snapshot);
        allDefinitions.addAll(vanillaSnapshot);
        Map<TagKey<ItemType>, List<TagEntry<ItemType>>> itemTags = new LinkedHashMap<>();
        Map<TagKey<ItemType>, RegistryKeySet<ItemType>> itemDefaults = new LinkedHashMap<>();
        Map<TagKey<ItemType>, Collection<TypedKey<ItemType>>> resolvedItemTags = new LinkedHashMap<>();
        boolean[] itemTagsReady = {false};
        for (PowerEnchantmentDefinition definition : allDefinitions) {
            if (!definition.isEnabled()) {
                continue;
            }
            if (!definition.isVanillaOverride() || definition.has("supported-items")) {
                configuredItemTag(definition, "supported-items", null, itemTags, itemDefaults);
            }
            if (definition.has("primary-items")) {
                configuredItemTag(definition, "primary-items", null, itemTags, itemDefaults);
            }
        }
        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
                    String eventKey = event.key().key().asString();
                    for (VanillaEnchantmentOverride definition : vanillaSnapshot) {
                        if (definition.getKey().asString().equals(eventKey)) {
                            applyVanillaOverride(event, definition, itemTags, itemDefaults,
                                    resolvedItemTags, itemTagsReady[0]);
                            if (!definition.isEnabled()) {
                                context.getLogger().info("Disabled vanilla enchantment {}.", eventKey);
                            }
                            break;
                        }
                    }
                }).filter(key -> "minecraft".equals(key.key().namespace())));
        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
                    for (ObjectCustomEnchantment definition : snapshot) {
                        TypedKey<Enchantment> key = key(definition);
                        event.registry().register(key, builder -> {
                            RegistryKeySet<ItemType> empty = RegistrySet.keySet(RegistryKey.ITEM, List.of());
                            builder.description(coloredDescription(definition))
                                    .supportedItems(itemSet(configuredItemTag(
                                            definition, "supported-items", empty, itemTags, itemDefaults),
                                            empty, resolvedItemTags, itemTagsReady[0], event::getOrCreateTag))
                                    .weight(definition.getWeight())
                                    .maxLevel(definition.getMaxLevel())
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(
                                            definition.getMinimumCost().base(),
                                            definition.getMinimumCost().perLevel()))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(
                                            definition.getMaximumCost().base(),
                                            definition.getMaximumCost().perLevel()))
                                    .anvilCost(definition.getAnvilCost())
                                    .activeSlots(parseSlots(definition.getActiveSlots()));
                            if (definition.getPrimaryItems() != null) {
                                builder.primaryItems(itemSet(configuredItemTag(
                                        definition, "primary-items", builder.supportedItems(),
                                        itemTags, itemDefaults), builder.supportedItems(),
                                        resolvedItemTags, itemTagsReady[0], event::getOrCreateTag));
                            }
                            RegistryKeySet<Enchantment> exclusive = exclusiveSet(event, definition);
                            if (exclusive != null) {
                                builder.exclusiveWith(exclusive);
                            }
                            PaperNativeEffectsBridge.apply(builder, definition);
                        });
                    }
                }));
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM).newHandler(event -> {
                    registerConfiguredItemTags(event.registrar(), supportedItemsConfig);
                    itemTags.forEach(event.registrar()::setTag);
                }));
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM).newHandler(event -> {
                    applyItemDefaults(event.registrar(), itemDefaults);
                    resolvedItemTags.clear();
                    resolvedItemTags.putAll(event.registrar().getAllTags());
                    itemTagsReady[0] = true;
                }));
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(event ->
                        registerExclusiveTags(event.registrar(), allDefinitions)));
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
                    for (ObjectCustomEnchantment definition : snapshot) {
                        for (String source : definition.getObtainingSources()) {
                            TagKey<Enchantment> tag = sourceTag(source);
                            if (tag != null) {
                                event.registrar().addToTag(tag, List.of(key(definition)));
                            }
                        }
                    }
                }));
    }

    private static void applyItemDefaults(
            PostFlattenTagRegistrar<ItemType> registrar,
            Map<TagKey<ItemType>, RegistryKeySet<ItemType>> defaults) {
        for (Map.Entry<TagKey<ItemType>, RegistryKeySet<ItemType>> entry : defaults.entrySet()) {
            TagKey<ItemType> tag = entry.getKey();
            if (registrar.hasTag(tag) && !registrar.getTag(tag).isEmpty()) {
                continue;
            }
            RegistryKeySet<ItemType> fallback = entry.getValue();
            Collection<TypedKey<ItemType>> values;
            if (fallback instanceof Tag<ItemType> fallbackTag) {
                values = registrar.hasTag(fallbackTag.tagKey())
                        ? registrar.getTag(fallbackTag.tagKey()) : List.of();
            } else {
                values = fallback == null ? List.of() : fallback.values();
            }
            registrar.setTag(tag, values);
        }
    }

    private static void registerExclusiveTags(
            PreFlattenTagRegistrar<Enchantment> registrar,
            Collection<PowerEnchantmentDefinition> definitions) {
        for (PowerEnchantmentDefinition definition : definitions) {
            if (!definition.isEnabled() || definition.getExclusiveWith().isEmpty()) {
                continue;
            }
            List<TagEntry<Enchantment>> entries = new ArrayList<>();
            for (String value : definition.getExclusiveWith()) {
                if (value.startsWith("#")) {
                    entries.add(TagEntry.tagEntry(TagKey.create(
                            RegistryKey.ENCHANTMENT, Key.key(stripTagPrefix(value))), false));
                } else {
                    entries.add(TagEntry.valueEntry(EnchantmentKeys.create(Key.key(value)), false));
                }
            }
            registrar.setTag(TagKey.create(RegistryKey.ENCHANTMENT,
                    configurationTagKey(definition, "exclusive-with")), entries);
        }
    }

    private static void registerConfiguredItemTags(
            PreFlattenTagRegistrar<ItemType> registrar,
            ConfigurationSection supportedItemsConfig) {
        ConfigurationSection tags = supportedItemsConfig == null
                ? null : supportedItemsConfig.getConfigurationSection("tags");
        if (tags == null) {
            return;
        }
        Map<String, ConfigurationSection> definitions = new LinkedHashMap<>();
        for (String id : tags.getKeys(false)) {
            ConfigurationSection definition = tags.getConfigurationSection(id);
            if (definition != null) {
                String key = normalizeTag(definition.getString("key", id));
                if (key.matches("[a-z0-9._-]+:[a-z0-9/._-]+")) {
                    definitions.put(key, definition);
                }
            }
        }
        for (Map.Entry<String, ConfigurationSection> entry : definitions.entrySet()) {
            List<TagEntry<ItemType>> values = new ArrayList<>();
            for (String value : entry.getValue().getStringList("values")) {
                boolean tag = value.startsWith("#");
                String reference = tag ? normalizeTag(value.substring(1))
                        : value.contains(":") ? value : "minecraft:" + value;
                if (!reference.matches("[a-z0-9._-]+:[a-z0-9/._-]+")) {
                    continue;
                }
                if (tag) {
                    values.add(TagEntry.tagEntry(TagKey.create(
                            RegistryKey.ITEM, Key.key(reference)), false));
                } else {
                    values.add(TagEntry.valueEntry(TypedKey.create(
                            RegistryKey.ITEM, Key.key(reference)), false));
                }
            }
            registrar.setTag(TagKey.create(RegistryKey.ITEM, Key.key(entry.getKey())), values);
        }
    }

    private static String normalizeTag(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        return normalized.contains(":") ? normalized : "enchantmentreform:" + normalized;
    }

    private static void applyVanillaOverride(
            RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.Builder> event,
            VanillaEnchantmentOverride definition,
            Map<TagKey<ItemType>, List<TagEntry<ItemType>>> itemTags,
            Map<TagKey<ItemType>, RegistryKeySet<ItemType>> itemDefaults,
            Map<TagKey<ItemType>, Collection<TypedKey<ItemType>>> resolvedItemTags,
            boolean itemTagsReady) {
        EnchantmentRegistryEntry.Builder builder = event.builder();
        if (!definition.isEnabled()) {
            builder.supportedItems(RegistrySet.keySet(RegistryKey.ITEM, List.of()))
                    .primaryItems(RegistrySet.keySet(RegistryKey.ITEM, List.of()));
            PaperNativeEffectsBridge.clear(builder, definition.getFileName());
            return;
        }

        if (definition.has("name")) {
            builder.description(coloredDescription(definition));
        }
        if (definition.has("supported-items")) {
            RegistryKeySet<ItemType> fallback = builder.supportedItems();
            builder.supportedItems(itemSet(configuredItemTag(
                    definition, "supported-items", fallback, itemTags, itemDefaults),
                    fallback, resolvedItemTags, itemTagsReady && !definition.getSupportedItemKeys().isEmpty(),
                    event::getOrCreateTag));
        }
        if (definition.has("primary-items")) {
            RegistryKeySet<ItemType> fallback = builder.primaryItems();
            if (fallback == null) {
                fallback = builder.supportedItems();
            }
            builder.primaryItems(itemSet(configuredItemTag(
                    definition, "primary-items", fallback, itemTags, itemDefaults),
                    fallback, resolvedItemTags, itemTagsReady && !definition.getPrimaryItemKeys().isEmpty(),
                    event::getOrCreateTag));
        }
        if (definition.has("weight")) {
            builder.weight(definition.getConfig().getInt("weight"));
        }
        if (definition.has("max-level")) {
            builder.maxLevel(definition.getConfig().getInt("max-level"));
        }
        if (definition.has("minimum-cost")) {
            builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(
                    definition.getConfig().getInt("minimum-cost.base", builder.minimumCost().baseCost()),
                    definition.getConfig().getInt("minimum-cost.per-level", builder.minimumCost().additionalPerLevelCost())));
        }
        if (definition.has("maximum-cost")) {
            builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(
                    definition.getConfig().getInt("maximum-cost.base", builder.maximumCost().baseCost()),
                    definition.getConfig().getInt("maximum-cost.per-level", builder.maximumCost().additionalPerLevelCost())));
        }
        if (definition.has("anvil-cost")) {
            builder.anvilCost(definition.getConfig().getInt("anvil-cost"));
        }
        if (definition.has("active-slots")) {
            builder.activeSlots(parseSlots(definition.stringOrList("active-slots")));
        }
        if (definition.has("exclusive-with")) {
            if (definition.getExclusiveWith().isEmpty()) {
                builder.exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, List.of()));
            } else {
                PaperNativeEffectsBridge.applyExclusiveTag(builder, TagKey.create(
                        RegistryKey.ENCHANTMENT, configurationTagKey(definition, "exclusive-with")));
            }
        }
        if (definition.hasNativeEffects()) {
            PaperNativeEffectsBridge.apply(builder, definition.getFileName(),
                    definition.getNativeEffects());
        }
    }

    private static RegistryKeySet<ItemType> itemSet(
            TagKey<ItemType> selected,
            RegistryKeySet<ItemType> fallback,
            Map<TagKey<ItemType>, Collection<TypedKey<ItemType>>> resolved,
            boolean ready,
            Function<TagKey<ItemType>, Tag<ItemType>> resolver) {
        if (ready && resolved.getOrDefault(selected, List.of()).isEmpty()) {
            return fallback;
        }
        return resolver.apply(selected);
    }

    private static TagKey<ItemType> configuredItemTag(
            PowerEnchantmentDefinition definition,
            String path,
            RegistryKeySet<ItemType> fallback,
            Map<TagKey<ItemType>, List<TagEntry<ItemType>>> itemTags,
            Map<TagKey<ItemType>, RegistryKeySet<ItemType>> itemDefaults) {
        List<String> values = definition.stringOrList(path);
        boolean tagReference = definition.getConfig().get(path) instanceof String;
        List<TagEntry<ItemType>> entries = new ArrayList<>();
        for (String value : values) {
            if (tagReference || value.startsWith("#")) {
                entries.add(TagEntry.tagEntry(TagKey.create(
                        RegistryKey.ITEM, Key.key(stripTagPrefix(value))), false));
            } else {
                entries.add(TagEntry.valueEntry(TypedKey.create(
                        RegistryKey.ITEM, Key.key(value)), false));
            }
        }
        TagKey<ItemType> tag = TagKey.create(RegistryKey.ITEM, configurationTagKey(definition, path));
        itemTags.put(tag, entries);
        if (!values.isEmpty()) {
            itemDefaults.put(tag, fallback);
        }
        return tag;
    }

    private static Key configurationTagKey(PowerEnchantmentDefinition definition, String path) {
        return Key.key("enchantmentreform", "configuration/" + path + "/"
                + definition.getKey().namespace() + "/" + definition.getKey().value());
    }

    private static TypedKey<Enchantment> key(ObjectCustomEnchantment definition) {
        return EnchantmentKeys.create(Key.key(definition.getKey().asString()));
    }

    private static Component coloredDescription(PowerEnchantmentDefinition definition) {
        Component description = Component.translatable(
                definition.getTranslationKey(), definition.getRegistryDescription());
        Integer color = parseColorPrefix(definition.getColorPrefix());
        return color == null ? description : description.color(TextColor.color(color));
    }

    private static Integer parseColorPrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        String text = prefix.strip();
        int hash = text.indexOf('#');
        if (hash >= 0 && text.length() >= hash + 7) {
            try {
                return Integer.parseInt(text.substring(hash + 1, hash + 7), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        for (int index = 0; index + 1 < text.length(); index++) {
            char marker = text.charAt(index);
            if (marker == '&' || marker == '\u00a7') {
                Integer color = legacyColor(text.charAt(index + 1));
                if (color != null) {
                    return color;
                }
            }
        }
        return null;
    }

    private static Integer legacyColor(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            case 'f' -> 0xFFFFFF;
            default -> null;
        };
    }

    private static RegistryKeySet<Enchantment> exclusiveSet(
            RegistryComposeEvent<Enchantment,
                    EnchantmentRegistryEntry.Builder> event,
            ObjectCustomEnchantment definition) {
        List<String> values = definition.getExclusiveWith();
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() == 1 && values.getFirst().startsWith("#")) {
            return event.getOrCreateTag(TagKey.create(
                    RegistryKey.ENCHANTMENT, configurationTagKey(definition, "exclusive-with")));
        }
        return event.getOrCreateTag(TagKey.create(
                RegistryKey.ENCHANTMENT, configurationTagKey(definition, "exclusive-with")));
    }

    private static String stripTagPrefix(String value) {
        return value.startsWith("#") ? value.substring(1) : value;
    }

    private static List<EquipmentSlotGroup> parseSlots(List<String> values) {
        List<EquipmentSlotGroup> slots = new ArrayList<>();
        for (String value : values) {
            EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(value.toLowerCase(Locale.ROOT));
            if (slot == null) {
                return slots;
            }
            slots.add(slot);
        }
        return slots;
    }

    private static TagKey<Enchantment> sourceTag(String source) {
        return switch (source) {
            case "VILLAGER_TRADE" -> EnchantmentTagKeys.TRADEABLE;
            case "ENCHANTING_TABLE" -> EnchantmentTagKeys.IN_ENCHANTING_TABLE;
            case "RANDOM_LOOT" -> EnchantmentTagKeys.ON_RANDOM_LOOT;
            case "MOB_EQUIPMENT" -> EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT;
            case "TRADED_EQUIPMENT" -> EnchantmentTagKeys.ON_TRADED_EQUIPMENT;
            default -> null;
        };
    }
}
