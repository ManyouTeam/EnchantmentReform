package cn.superiormc.enchantmentreform.spigot.nms.v1_21_11;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import com.mojang.serialization.JavaOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_21_R7.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftNamespacedKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Native custom-enchantment registrar for Spigot 1.21.11 (CraftBukkit v1_21_R7).
 *
 * <p>This class is compiled against Mojang mappings and remapped to Spigot mappings
 * by SpecialSource. Do not move it into the API-only Spigot module.</p>
 */
public final class SpigotEnchantmentRegistrar12111 {

    private static final MinecraftServer SERVER;
    private static final MappedRegistry<Enchantment> ENCHANTMENTS;
    private static final MappedRegistry<Item> ITEMS;

    // Runtime Spigot names used by the registry freeze implementation in 1.21.11.
    private static final String REGISTRY_FROZEN_TAGS_FIELD = "j";
    private static final String REGISTRY_ALL_TAGS_FIELD = "k";
    private static final String TAG_SET_UNBOUND_METHOD = "a";
    private static final String TAG_SET_MAP_FIELD = "a";

    static {
        SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        ENCHANTMENTS = (MappedRegistry<Enchantment>) SERVER.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .orElseThrow();
        ITEMS = (MappedRegistry<Item>) SERVER.registryAccess()
                .lookup(Registries.ITEM)
                .orElseThrow();
    }

    private SpigotEnchantmentRegistrar12111() {
    }

    public static int registerAll(Collection<ObjectCustomEnchantment> definitions,
                                  ConfigurationSection supportedItems,
                                  Logger logger) {
        List<ObjectCustomEnchantment> snapshot = List.copyOf(definitions);
        unfreeze(ENCHANTMENTS);
        unfreeze(ITEMS);

        try {
            ItemTagResolver itemTagResolver = new ItemTagResolver(supportedItems);
            itemTagResolver.registerConfiguredTags();

            Map<String, Holder.Reference<Enchantment>> registered = new LinkedHashMap<>();
            Map<String, TagKey<Enchantment>> exclusiveTags = new LinkedHashMap<>();
            int count = 0;

            for (ObjectCustomEnchantment definition : snapshot) {
                ResourceKey<Enchantment> key = enchantmentKey(definition.getKey().asString());
                Holder.Reference<Enchantment> existing = ENCHANTMENTS.get(key).orElse(null);
                if (existing != null) {
                    registered.put(definition.getKey().asString(), existing);
                    logger.info("Spigot enchantment already exists, keeping registry entry: "
                            + definition.getKey().asString());
                    continue;
                }

                HolderSet.Named<Item> supportedSet = itemTagResolver.requireTag(
                        definition.getSupportedItems(), definition.getFileName(), "supported-items");
                HolderSet.Named<Item> primarySet = definition.getPrimaryItems() == null
                        ? supportedSet
                        : itemTagResolver.requireTag(
                                definition.getPrimaryItems(), definition.getFileName(), "primary-items");

                TagKey<Enchantment> exclusiveTag = customEnchantmentTag(
                        "exclusive_set/" + definition.getKey().value());
                ENCHANTMENTS.bindTag(exclusiveTag, List.of());
                HolderSet.Named<Enchantment> exclusiveSet = requireFrozenTag(
                        ENCHANTMENTS, exclusiveTag,
                        definition.getFileName() + ".yml: failed to create exclusive set");

                MutableComponent description = Component.translatableWithFallback(
                        definition.getTranslationKey(), definition.getRegistryDescription());
                Integer color = parseColorPrefix(definition.getColorPrefix());
                if (color != null) {
                    description.setStyle(description.getStyle().withColor(color));
                }

                Enchantment.EnchantmentDefinition nativeDefinition = Enchantment.definition(
                        supportedSet,
                        primarySet,
                        definition.getWeight(),
                        definition.getMaxLevel(),
                        new Enchantment.Cost(
                                definition.getMinimumCost().base(),
                                definition.getMinimumCost().perLevel()),
                        new Enchantment.Cost(
                                definition.getMaximumCost().base(),
                                definition.getMaximumCost().perLevel()),
                        definition.getAnvilCost(),
                        activeSlots(definition, logger));

                DataComponentMap nativeEffects = decodeEffects(definition);
                Enchantment enchantment = new Enchantment(
                        description, nativeDefinition, exclusiveSet, nativeEffects);

                ENCHANTMENTS.createIntrusiveHolder(enchantment);
                Registry.register(ENCHANTMENTS, key, enchantment);

                Holder.Reference<Enchantment> holder = ENCHANTMENTS.get(key).orElseThrow();
                registered.put(definition.getKey().asString(), holder);
                exclusiveTags.put(definition.getKey().asString(), exclusiveTag);
                CraftEnchantment.minecraftToBukkit(enchantment);
                count++;
            }

            for (ObjectCustomEnchantment definition : snapshot) {
                TagKey<Enchantment> targetTag = exclusiveTags.get(definition.getKey().asString());
                if (targetTag == null) {
                    continue;
                }
                ENCHANTMENTS.bindTag(targetTag, resolveExclusives(definition));
            }

            for (ObjectCustomEnchantment definition : snapshot) {
                Holder.Reference<Enchantment> holder = registered.get(definition.getKey().asString());
                if (holder == null) {
                    continue;
                }
                applyObtainingTags(definition, holder, logger);
                applyRarityTags(definition, holder, supportedItems);
                addToTag(ENCHANTMENTS, minecraftEnchantmentTag("tooltip_order"), holder);
            }

            return count;
        } finally {
            freeze(ITEMS);
            freeze(ENCHANTMENTS);
        }
    }

    private static EquipmentSlotGroup[] activeSlots(ObjectCustomEnchantment definition,
                                                     Logger logger) {
        List<EquipmentSlotGroup> slots = new ArrayList<>();
        for (String configured : definition.getActiveSlots()) {
            org.bukkit.inventory.EquipmentSlotGroup bukkitGroup =
                    org.bukkit.inventory.EquipmentSlotGroup.getByName(
                            configured.toLowerCase(Locale.ROOT));
            if (bukkitGroup == null) {
                logger.warning(definition.getFileName() + ".yml: unknown active slot group '"
                        + configured + "'");
                continue;
            }
            slots.add(CraftEquipmentSlot.getNMSGroup(bukkitGroup));
        }
        if (slots.isEmpty()) {
            slots.add(CraftEquipmentSlot.getNMSGroup(
                    org.bukkit.inventory.EquipmentSlotGroup.ANY));
        }
        return slots.toArray(EquipmentSlotGroup[]::new);
    }

    private static DataComponentMap decodeEffects(ObjectCustomEnchantment definition) {
        if (!definition.hasNativeEffects()) {
            return DataComponentMap.EMPTY;
        }

        RegistryOps<Object> operations = RegistryOps.create(
                JavaOps.INSTANCE, SERVER.registryAccess());
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : definition.getNativeEffects().entrySet()) {
            String key = entry.getKey().contains(":")
                    ? entry.getKey()
                    : "minecraft:" + entry.getKey();
            normalized.put(key, normalizeNativeValue(entry.getKey(), entry.getValue(), operations));
        }

        Function<String, RuntimeException> errorFactory = message ->
                new IllegalArgumentException(definition.getFileName()
                        + ".yml: invalid native enchantment effects: " + message);
        return EnchantmentEffectComponents.CODEC
                .parse(operations, normalized)
                .getOrThrow(errorFactory);
    }

    private static Object normalizeNativeValue(String key,
                                               Object value,
                                               RegistryOps<Object> operations) {
        if ("offset".equals(key)
                && value instanceof List<?> values
                && values.size() == 3
                && values.stream().allMatch(Number.class::isInstance)) {
            return operations.createIntList(IntStream.of(
                    ((Number) values.get(0)).intValue(),
                    ((Number) values.get(1)).intValue(),
                    ((Number) values.get(2)).intValue()));
        }
        if (value instanceof Map<?, ?> values) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                String nestedKey = String.valueOf(entry.getKey());
                normalized.put(nestedKey,
                        normalizeNativeValue(nestedKey, entry.getValue(), operations));
            }
            return normalized;
        }
        if (value instanceof List<?> values) {
            List<Object> normalized = new ArrayList<>(values.size());
            for (Object nested : values) {
                normalized.add(normalizeNativeValue(null, nested, operations));
            }
            return List.copyOf(normalized);
        }
        return value;
    }

    private static List<Holder<Enchantment>> resolveExclusives(
            ObjectCustomEnchantment definition) {
        List<String> configured = definition.getExclusiveWith();
        if (configured.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Holder<Enchantment>> holders = new LinkedHashSet<>();
        if (configured.size() == 1 && configured.getFirst().startsWith("#")) {
            TagKey<Enchantment> tag = TagKey.create(
                    ENCHANTMENTS.key(), identifier(configured.getFirst().substring(1)));
            HolderSet.Named<Enchantment> set = ENCHANTMENTS.get(tag).orElseThrow(() ->
                    new IllegalArgumentException(definition.getFileName()
                            + ".yml: unknown exclusive enchantment tag '"
                            + configured.getFirst() + "'"));
            set.forEach(holders::add);
            return List.copyOf(holders);
        }

        if (configured.stream().anyMatch(value -> value.startsWith("#"))) {
            throw new IllegalArgumentException(definition.getFileName()
                    + ".yml: exclusive-with cannot mix tags and enchantment keys");
        }

        for (String id : configured) {
            Holder.Reference<Enchantment> holder = ENCHANTMENTS
                    .get(enchantmentKey(id))
                    .orElseThrow(() -> new IllegalArgumentException(
                            definition.getFileName()
                                    + ".yml: unknown exclusive enchantment '" + id + "'"));
            holders.add(holder);
        }
        return List.copyOf(holders);
    }

    private static void applyObtainingTags(ObjectCustomEnchantment definition,
                                            Holder.Reference<Enchantment> holder,
                                            Logger logger) {
        for (String source : definition.getObtainingSources()) {
            TagKey<Enchantment> tag = switch (source) {
                case "VILLAGER_TRADE" -> EnchantmentTags.TRADEABLE;
                case "ENCHANTING_TABLE" -> EnchantmentTags.IN_ENCHANTING_TABLE;
                case "RANDOM_LOOT" -> EnchantmentTags.ON_RANDOM_LOOT;
                case "MOB_EQUIPMENT" -> EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT;
                case "TRADED_EQUIPMENT" -> EnchantmentTags.ON_TRADED_EQUIPMENT;
                default -> null;
            };
            if (tag == null) {
                logger.warning(definition.getFileName()
                        + ".yml: unknown obtaining source '" + source + "'");
                continue;
            }
            addToTag(ENCHANTMENTS, tag, holder);
        }
    }

    private static void applyRarityTags(ObjectCustomEnchantment definition,
                                        Holder.Reference<Enchantment> holder,
                                        ConfigurationSection supportedItems) {
        ConfigurationSection root = supportedItems == null ? null : supportedItems.getRoot();
        ConfigurationSection rarities = root == null
                ? null
                : root.getConfigurationSection("rarity");
        ConfigurationSection rarity = findSectionIgnoreCase(rarities, definition.getRarity());
        if (rarity == null) {
            return;
        }
        if (rarity.getBoolean("curse", false)) {
            addToTag(ENCHANTMENTS, EnchantmentTags.CURSE, holder);
        }
        if (rarity.getBoolean("double-trade-multiplier", false)) {
            addToTag(ENCHANTMENTS, EnchantmentTags.DOUBLE_TRADE_PRICE, holder);
        }
    }

    private static ConfigurationSection findSectionIgnoreCase(ConfigurationSection parent,
                                                               String key) {
        if (parent == null || key == null) {
            return null;
        }
        ConfigurationSection direct = parent.getConfigurationSection(key);
        if (direct != null) {
            return direct;
        }
        for (String child : parent.getKeys(false)) {
            if (child.equalsIgnoreCase(key)) {
                return parent.getConfigurationSection(child);
            }
        }
        return null;
    }

    private static ResourceKey<Enchantment> enchantmentKey(String value) {
        return ResourceKey.create(ENCHANTMENTS.key(), identifier(value));
    }

    private static TagKey<Enchantment> customEnchantmentTag(String path) {
        return TagKey.create(
                ENCHANTMENTS.key(),
                CraftNamespacedKey.toMinecraft(new NamespacedKey("enchantmentreform", path)));
    }

    private static TagKey<Enchantment> minecraftEnchantmentTag(String path) {
        return TagKey.create(ENCHANTMENTS.key(), identifier("minecraft:" + path));
    }

    private static Identifier identifier(String value) {
        String normalized = value.contains(":") ? value : "minecraft:" + value;
        NamespacedKey key = NamespacedKey.fromString(normalized);
        if (key == null) {
            throw new IllegalArgumentException("Invalid namespaced key: " + value);
        }
        return CraftNamespacedKey.toMinecraft(key);
    }

    private static <T> void addToTag(MappedRegistry<T> registry,
                                     TagKey<T> tagKey,
                                     Holder.Reference<T> reference) {
        HolderSet.Named<T> named = registry.get(tagKey).orElse(null);
        if (named == null) {
            registry.bindTag(tagKey, List.of());
            named = requireFrozenTag(registry, tagKey,
                    "Could not create registry tag " + tagKey.location());
        }
        List<Holder<T>> contents = new ArrayList<>(named.stream().toList());
        if (!contents.contains(reference)) {
            contents.add(reference);
            registry.bindTag(tagKey, contents);
        }
    }

    private static final class ItemTagResolver {

        private final Map<String, ConfigurationSection> definitions = new LinkedHashMap<>();
        private final Map<String, List<Holder<Item>>> resolved = new LinkedHashMap<>();
        private final Set<String> resolving = new LinkedHashSet<>();

        private ItemTagResolver(ConfigurationSection supportedItems) {
            ConfigurationSection tags = supportedItems == null
                    ? null
                    : supportedItems.getConfigurationSection("tags");
            if (tags == null) {
                return;
            }
            for (String logicalName : tags.getKeys(false)) {
                ConfigurationSection definition = tags.getConfigurationSection(logicalName);
                if (definition == null) {
                    continue;
                }
                definitions.put(normalizeTag(
                        definition.getString("key", logicalName)), definition);
                definitions.putIfAbsent(normalizeTag(logicalName), definition);
            }
        }

        private void registerConfiguredTags() {
            for (String key : List.copyOf(definitions.keySet())) {
                resolve(key);
            }
        }

        private HolderSet.Named<Item> requireTag(String configured,
                                                 String fileName,
                                                 String path) {
            String key = normalizeTag(configured);
            if (definitions.containsKey(key)) {
                resolve(key);
            }
            TagKey<Item> tagKey = TagKey.create(ITEMS.key(), identifier(key));
            return ITEMS.get(tagKey).orElseThrow(() ->
                    new IllegalArgumentException(fileName + ".yml: " + path
                            + " references unknown item tag '" + configured + "'"));
        }

        private List<Holder<Item>> resolve(String tag) {
            List<Holder<Item>> cached = resolved.get(tag);
            if (cached != null) {
                return cached;
            }
            if (!resolving.add(tag)) {
                throw new IllegalArgumentException(
                        "Circular supported-items tag reference: " + tag);
            }

            ConfigurationSection definition = definitions.get(tag);
            if (definition == null) {
                resolving.remove(tag);
                return List.of();
            }

            LinkedHashSet<Holder<Item>> contents = new LinkedHashSet<>();
            for (String configured : definition.getStringList("values")) {
                if (configured.startsWith("#")) {
                    String referenced = normalizeTag(configured.substring(1));
                    if (definitions.containsKey(referenced)) {
                        contents.addAll(resolve(referenced));
                    } else {
                        TagKey<Item> vanillaTag = TagKey.create(
                                ITEMS.key(), identifier(referenced));
                        HolderSet.Named<Item> named = ITEMS.get(vanillaTag).orElseThrow(() ->
                                new IllegalArgumentException(tag
                                        + " references unknown item tag #" + referenced));
                        named.forEach(contents::add);
                    }
                } else {
                    Identifier itemId = identifier(configured);
                    Holder.Reference<Item> holder = ITEMS.get(itemId).orElseThrow(() ->
                            new IllegalArgumentException(tag
                                    + " references unknown item " + configured));
                    contents.add(holder);
                }
            }

            resolving.remove(tag);
            List<Holder<Item>> result = List.copyOf(contents);
            resolved.put(tag, result);
            ITEMS.bindTag(TagKey.create(ITEMS.key(), identifier(tag)), result);
            return result;
        }

        private static String normalizeTag(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Item tag key cannot be empty");
            }
            String normalized = value.strip().toLowerCase(Locale.ROOT);
            return normalized.contains(":")
                    ? normalized
                    : "enchantmentreform:" + normalized;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<TagKey<T>, HolderSet.Named<T>> getFrozenTags(
            MappedRegistry<T> registry) {
        return (Map<TagKey<T>, HolderSet.Named<T>>) getFieldValue(
                registry, REGISTRY_FROZEN_TAGS_FIELD);
    }

    private static <T> Object getAllTags(MappedRegistry<T> registry) {
        return getFieldValue(registry, REGISTRY_ALL_TAGS_FIELD);
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<TagKey<T>, HolderSet.Named<T>> getTagsMap(Object tagSet) {
        return new HashMap<>((Map<TagKey<T>, HolderSet.Named<T>>)
                getFieldValue(tagSet, TAG_SET_MAP_FIELD));
    }

    private static <T> HolderSet.Named<T> requireFrozenTag(MappedRegistry<T> registry,
                                                            TagKey<T> key,
                                                            String message) {
        HolderSet.Named<T> value = getFrozenTags(registry).get(key);
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private static <T> void unfreeze(MappedRegistry<T> registry) {
        setFieldValue(registry, "l", false);
        setFieldValue(registry, "m", new IdentityHashMap<>());
    }

    private static <T> void freeze(MappedRegistry<T> registry) {
        Object originalTagSet = getAllTags(registry);
        Map<TagKey<T>, HolderSet.Named<T>> tagsMap = getTagsMap(originalTagSet);
        Map<TagKey<T>, HolderSet.Named<T>> frozenTags = getFrozenTags(registry);
        tagsMap.forEach(frozenTags::putIfAbsent);

        unbindTags(registry);
        registry.freeze();

        frozenTags.forEach(tagsMap::putIfAbsent);
        setFieldValue(originalTagSet, TAG_SET_MAP_FIELD, tagsMap);
        setFieldValue(registry, REGISTRY_ALL_TAGS_FIELD, originalTagSet);
    }

    private static <T> void unbindTags(MappedRegistry<T> registry) {
        Class<?> tagSetClass = innerClass(MappedRegistry.class, "a");
        Method method = declaredMethod(tagSetClass, TAG_SET_UNBOUND_METHOD);
        Object unbound = invoke(method, registry);
        setFieldValue(registry, REGISTRY_ALL_TAGS_FIELD, unbound);
    }

    private static Class<?> innerClass(Class<?> owner, String simpleName) {
        for (Class<?> inner : owner.getDeclaredClasses()) {
            if (inner.getSimpleName().equals(simpleName)) {
                return inner;
            }
        }
        throw new IllegalStateException("Missing inner class "
                + owner.getName() + '$' + simpleName);
    }

    private static Method declaredMethod(Class<?> type, String name) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Missing method " + type.getName() + '#' + name);
    }

    private static Object invoke(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not invoke " + method, exception);
        }
    }

    private static Object getFieldValue(Object target, String name) {
        Field field = field(target.getClass(), name);
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not read " + field, exception);
        }
    }

    private static void setFieldValue(Object target, String name, Object value) {
        Field field = field(target.getClass(), name);
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not write " + field, exception);
        }
    }

    private static Field field(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new IllegalStateException("Missing field " + type.getName() + '#' + name);
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
        return null;
    }
}
