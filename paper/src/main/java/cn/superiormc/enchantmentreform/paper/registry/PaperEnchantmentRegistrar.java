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
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PaperEnchantmentRegistrar {

    private PaperEnchantmentRegistrar() {}

    public static void bind(BootstrapContext context,
                            Collection<ObjectCustomEnchantment> definitions,
                            Collection<VanillaEnchantmentOverride> vanillaDefinitions,
                            ConfigurationSection supportedItemsConfig) {
        List<ObjectCustomEnchantment> snapshot = List.copyOf(definitions);
        List<VanillaEnchantmentOverride> vanillaSnapshot = List.copyOf(vanillaDefinitions);
        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
                    String eventKey = event.key().key().asString();
                    for (VanillaEnchantmentOverride definition : vanillaSnapshot) {
                        if (definition.getKey().asString().equals(eventKey)) {
                            applyVanillaOverride(event, definition);
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
                            builder.description(coloredDescription(definition))
                                    .supportedItems(event.getOrCreateTag(TagKey.create(
                                            RegistryKey.ITEM, Key.key(stripTagPrefix(
                                                    definition.getSupportedItems())))))
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
                                builder.primaryItems(event.getOrCreateTag(TagKey.create(
                                        RegistryKey.ITEM,
                                        Key.key(stripTagPrefix(definition.getPrimaryItems())))));
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
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM).newHandler(event ->
                        registerConfiguredItemTags(event.registrar(), supportedItemsConfig)));
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

    private static void registerConfiguredItemTags(
            io.papermc.paper.tag.PostFlattenTagRegistrar<ItemType> registrar,
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
                definitions.put(normalizeTag(definition.getString("key", id)), definition);
            }
        }
        Map<String, Collection<TypedKey<ItemType>>> resolved = new LinkedHashMap<>();
        for (String tag : definitions.keySet()) {
            Collection<TypedKey<ItemType>> values = resolveConfiguredTag(
                    tag, definitions, resolved, new LinkedHashSet<>(), registrar);
            registrar.setTag(TagKey.create(RegistryKey.ITEM, Key.key(tag)), values);
        }
    }

    private static Collection<TypedKey<ItemType>> resolveConfiguredTag(
            String tag,
            Map<String, ConfigurationSection> definitions,
            Map<String, Collection<TypedKey<ItemType>>> resolved,
            Set<String> resolving,
            io.papermc.paper.tag.PostFlattenTagRegistrar<ItemType> registrar) {
        Collection<TypedKey<ItemType>> cached = resolved.get(tag);
        if (cached != null) {
            return cached;
        }
        if (!resolving.add(tag)) {
            throw new IllegalArgumentException("Circular supported-items tag reference: " + tag);
        }
        LinkedHashSet<TypedKey<ItemType>> values = new LinkedHashSet<>();
        ConfigurationSection definition = definitions.get(tag);
        if (definition != null) {
            for (String value : definition.getStringList("values")) {
                if (value.startsWith("#")) {
                    String referenced = normalizeTag(value.substring(1));
                    if (definitions.containsKey(referenced)) {
                        values.addAll(resolveConfiguredTag(
                                referenced, definitions, resolved, resolving, registrar));
                    } else {
                        TagKey<ItemType> key = TagKey.create(RegistryKey.ITEM, Key.key(referenced));
                        if (registrar.hasTag(key)) {
                            values.addAll(registrar.getTag(key));
                        }
                    }
                } else {
                    values.add(TypedKey.create(RegistryKey.ITEM, Key.key(value)));
                }
            }
        }
        resolving.remove(tag);
        Collection<TypedKey<ItemType>> result = List.copyOf(values);
        resolved.put(tag, result);
        return result;
    }

    private static String normalizeTag(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        return normalized.contains(":") ? normalized : "enchantmentreform:" + normalized;
    }

    private static void applyVanillaOverride(
            RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.Builder> event,
            VanillaEnchantmentOverride definition) {
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
            builder.supportedItems(itemSet(event, definition.stringOrList("supported-items"),
                    definition, "supported-items"));
        }
        if (definition.has("primary-items")) {
            builder.primaryItems(itemSet(event, definition.stringOrList("primary-items"),
                    definition, "primary-items"));
        }
        if (definition.has("weight")) {
            builder.weight(definition.getConfig().getInt("weight"));
        }
        if (definition.has("max-level")) {
            builder.maxLevel(definition.getConfig().getInt("max-level"));
        }
        if (definition.has("minimum-cost")) {
            builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(
                    definition.getConfig().getInt("minimum-cost.base"),
                    definition.getConfig().getInt("minimum-cost.per-level")));
        }
        if (definition.has("maximum-cost")) {
            builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(
                    definition.getConfig().getInt("maximum-cost.base"),
                    definition.getConfig().getInt("maximum-cost.per-level")));
        }
        if (definition.has("anvil-cost")) {
            builder.anvilCost(definition.getConfig().getInt("anvil-cost"));
        }
        if (definition.has("active-slots")) {
            builder.activeSlots(parseSlots(definition.stringOrList("active-slots")));
        }
        if (definition.has("exclusive-with")) {
            builder.exclusiveWith(exclusiveSet(event, definition));
        }
        if (definition.hasNativeEffects()) {
            PaperNativeEffectsBridge.apply(builder, definition.getFileName(),
                    definition.getNativeEffects());
        }
    }

    private static RegistryKeySet<ItemType> itemSet(
            RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.Builder> event,
            List<String> values,
            VanillaEnchantmentOverride definition,
            String path) {
        if (definition.getConfig().get(path) instanceof String && values.size() == 1) {
            return event.getOrCreateTag(TagKey.create(
                    RegistryKey.ITEM, Key.key(stripTagPrefix(values.getFirst()))));
        }
        if (values.stream().anyMatch(value -> value.startsWith("#"))) {
            throw vanillaError(definition, path + " cannot mix a tag with item keys");
        }
        List<TypedKey<ItemType>> keys = values.stream()
                .map(Key::key)
                .map(key -> TypedKey.create(RegistryKey.ITEM, key))
                .toList();
        return RegistrySet.keySet(RegistryKey.ITEM, keys);
    }

    private static RegistryKeySet<Enchantment> exclusiveSet(
            RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.Builder> event,
            VanillaEnchantmentOverride definition) {
        List<String> values = definition.stringOrList("exclusive-with");
        if (values.size() == 1 && values.getFirst().startsWith("#")) {
            return event.getOrCreateTag(TagKey.create(
                    RegistryKey.ENCHANTMENT, Key.key(stripTagPrefix(values.getFirst()))));
        }
        if (values.stream().anyMatch(value -> value.startsWith("#"))) {
            throw vanillaError(definition, "exclusive-with cannot mix a tag with enchantment keys");
        }
        List<TypedKey<Enchantment>> keys = values.stream()
                .map(Key::key)
                .map(EnchantmentKeys::create)
                .toList();
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT, keys);
    }

    private static IllegalArgumentException vanillaError(
            VanillaEnchantmentOverride definition, String message) {
        return new IllegalArgumentException("vanilla_enchantments/"
                + definition.getFileName() + ".yml: " + message);
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
            io.papermc.paper.registry.event.RegistryComposeEvent<Enchantment,
                    EnchantmentRegistryEntry.Builder> event,
            ObjectCustomEnchantment definition) {
        List<String> values = definition.getExclusiveWith();
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() == 1 && values.getFirst().startsWith("#")) {
            return event.getOrCreateTag(TagKey.create(
                    RegistryKey.ENCHANTMENT, Key.key(stripTagPrefix(values.getFirst()))));
        }
        if (values.stream().anyMatch(value -> value.startsWith("#"))) {
            throw new IllegalArgumentException(definition.getFileName()
                    + ".yml: exclusive-with cannot mix a tag with enchantment keys");
        }
        List<TypedKey<Enchantment>> keys = values.stream()
                .map(Key::key)
                .map(EnchantmentKeys::create)
                .toList();
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT, keys);
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
