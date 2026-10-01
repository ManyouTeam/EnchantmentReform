package cn.superiormc.enchantmentreform.spigot.registry;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Native custom-enchantment registration for named-mapped Spigot 26.2.
 *
 * <p>Spigot 26.2 exposes Mojang-named server classes at runtime, but the public
 * API still does not provide a writable enchantment registry. This registrar is
 * therefore reflection-only and runs during {@code JavaPlugin#onLoad()}.</p>
 */
public final class SpigotEnchantmentRegistrar261 {

    private SpigotEnchantmentRegistrar261() {
    }

    public static int registerAll(Collection<ObjectCustomEnchantment> definitions,
                                  ConfigurationSection supportedItems,
                                  Logger logger) {
        try {
            return new Registrar(supportedItems, logger).register(List.copyOf(definitions));
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Native Spigot 26.2 enchantment registration failed", cause);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "This Spigot 26.2 build is not compatible with the native enchantment registrar",
                    exception);
        }
    }

    private static final class Registrar {

        private final ConfigurationSection supportedItems;
        private final Logger logger;

        private final Class<?> registriesClass = type("net.minecraft.core.registries.Registries");
        private final Class<?> registryClass = type("net.minecraft.core.Registry");
        private final Class<?> resourceKeyClass = type("net.minecraft.resources.ResourceKey");
        private final Class<?> identifierClass = type("net.minecraft.resources.Identifier");
        private final Class<?> tagKeyClass = type("net.minecraft.tags.TagKey");
        private final Class<?> holderClass = type("net.minecraft.core.Holder");
        private final Class<?> holderSetClass = type("net.minecraft.core.HolderSet");
        private final Class<?> enchantmentClass =
                type("net.minecraft.world.item.enchantment.Enchantment");
        private final Class<?> costClass = nested(enchantmentClass, "Cost");
        private final Class<?> componentClass = type("net.minecraft.network.chat.Component");
        private final Class<?> dataComponentMapClass =
                type("net.minecraft.core.component.DataComponentMap");
        private final Class<?> slotGroupClass =
                type("net.minecraft.world.entity.EquipmentSlotGroup");

        private final Object enchantmentRegistryKey = staticRequired(registriesClass, "ENCHANTMENT");
        private final Object itemRegistryKey = staticRequired(registriesClass, "ITEM");

        private Registrar(ConfigurationSection supportedItems, Logger logger) {
            this.supportedItems = supportedItems;
            this.logger = logger;
        }

        private int register(List<ObjectCustomEnchantment> definitions)
                throws ReflectiveOperationException {
            Object minecraftServer = invoke(Bukkit.getServer(), "getServer");
            Object registryAccess = invoke(minecraftServer, "registryAccess");
            Object enchantmentRegistry = lookupRegistry(registryAccess, enchantmentRegistryKey);
            Object itemRegistry = lookupRegistry(registryAccess, itemRegistryKey);

            RegistryMutation mutation = RegistryMutation.open(enchantmentRegistry);
            try {
                ItemTagResolver itemTags = new ItemTagResolver(itemRegistry, supportedItems);
                Map<String, Object> holders = new LinkedHashMap<>();
                Set<String> pendingRegistration = new LinkedHashSet<>();
                Object registrationLookup = invokeCompatible(
                        enchantmentRegistry,
                        "createRegistrationLookup");

                // Create all standalone holders before constructing enchantments so custom
                // exclusivity can reference enchantments declared later in the config order.
                for (ObjectCustomEnchantment definition : definitions) {
                    String id = definition.getKey().asString();
                    Object key = createResourceKey(enchantmentRegistryKey, identifier(id));
                    Object existingHolder = getOptional(enchantmentRegistry, key);
                    if (existingHolder != null) {
                        holders.put(id, existingHolder);
                        logger.info("Spigot enchantment already exists, keeping registry entry: " + id);
                        continue;
                    }
                    Object holder = invokeCompatible(registrationLookup, "getOrThrow", key);
                    holders.put(id, holder);
                    pendingRegistration.add(id);
                }

                int registered = 0;
                Object emptyComponents = staticField(dataComponentMapClass, "EMPTY");
                for (ObjectCustomEnchantment definition : definitions) {
                    String id = definition.getKey().asString();
                    if (!pendingRegistration.contains(id)) {
                        continue;
                    }

                    Object key = createResourceKey(enchantmentRegistryKey, identifier(id));
                    Object exclusiveSet = resolveExclusiveSet(
                            definition,
                            enchantmentRegistry,
                            holders);
                    Object enchantment = createEnchantment(
                            definition,
                            itemTags,
                            registryAccess,
                            exclusiveSet);

                    invokeStaticCompatible(registryClass, "register", enchantmentRegistry, key, enchantment);
                    Object holder = holders.get(id);
                    invokeCompatible(holder, "bindValue", enchantment);
                    invokeCompatible(holder, "bindComponents", emptyComponents);
                    exposeBukkitMirror(enchantment);
                    registered++;
                }

                for (ObjectCustomEnchantment definition : definitions) {
                    Object holder = holders.get(definition.getKey().asString());
                    if (holder == null) {
                        continue;
                    }
                    applyObtainingTags(enchantmentRegistry, definition, holder);
                    applyRarityTags(enchantmentRegistry, definition, holder);
                    addToTag(enchantmentRegistry, "minecraft:tooltip_order", holder, false);
                }

                // 26.2 HolderSet.Named#contains checks the holder's bound tag set rather
                // than the named set contents directly, so refresh after rebinding tags.
                invokeCompatible(enchantmentRegistry, "refreshTagsInHolders");
                refreshComponentLookup(enchantmentRegistry);
                return registered;
            } finally {
                mutation.close();
            }
        }

        private Object createEnchantment(ObjectCustomEnchantment definition,
                                         ItemTagResolver itemTags,
                                         Object registryAccess,
                                         Object exclusiveSet)
                throws ReflectiveOperationException {
            List<Object> supportedHolders = itemTags.resolve(definition.getSupportedItems());
            if (supportedHolders.isEmpty()) {
                throw new IllegalArgumentException(definition.getFileName()
                        + ".yml: supported-items resolved to an empty item set");
            }

            List<Object> primaryHolders = definition.getPrimaryItems() == null
                    ? supportedHolders
                    : itemTags.resolve(definition.getPrimaryItems());
            if (primaryHolders.isEmpty()) {
                primaryHolders = supportedHolders;
            }

            Object supportedSet = directHolderSet(supportedHolders);
            Object primarySet = directHolderSet(primaryHolders);
            Object minCost = construct(
                    costClass,
                    definition.getMinimumCost().base(),
                    definition.getMinimumCost().perLevel());
            Object maxCost = construct(
                    costClass,
                    definition.getMaximumCost().base(),
                    definition.getMaximumCost().perLevel());
            Object slots = slotArray(definition.getActiveSlots());

            Object nativeDefinition = invokeStaticCompatible(
                    enchantmentClass,
                    "definition",
                    supportedSet,
                    primarySet,
                    definition.getWeight(),
                    definition.getMaxLevel(),
                    minCost,
                    maxCost,
                    definition.getAnvilCost(),
                    slots);

            Object description = createDescription(definition);
            Object effects = decodeEffects(definition, registryAccess);
            return construct(enchantmentClass, description, nativeDefinition, exclusiveSet, effects);
        }

        private Object createDescription(ObjectCustomEnchantment definition)
                throws ReflectiveOperationException {
            Object description = invokeStaticCompatible(
                    componentClass,
                    "translatableWithFallback",
                    definition.getTranslationKey(),
                    definition.getRegistryDescription());
            Integer color = parseColorPrefix(definition.getColorPrefix());
            if (color == null) {
                return description;
            }
            Object style = invokeCompatible(description, "getStyle");
            Object coloredStyle = invokeCompatible(style, "withColor", color);
            return invokeCompatible(description, "setStyle", coloredStyle);
        }

        private Object decodeEffects(ObjectCustomEnchantment definition,
                                     Object registryAccess)
                throws ReflectiveOperationException {
            if (!definition.hasNativeEffects()) {
                return staticField(dataComponentMapClass, "EMPTY");
            }

            Object javaOps = staticField(type("com.mojang.serialization.JavaOps"), "INSTANCE");
            Class<?> registryOpsClass = type("net.minecraft.resources.RegistryOps");
            Object registryOps = invokeStaticCompatible(
                    registryOpsClass,
                    "create",
                    javaOps,
                    registryAccess);

            Class<?> effectComponents = type(
                    "net.minecraft.world.item.enchantment.EnchantmentEffectComponents");
            Object codec = staticField(effectComponents, "CODEC");
            Object input = namespacedComponentKeys(
                    definition.getNativeEffects(),
                    registryOps);
            Object dataResult = invokeCompatible(codec, "parse", registryOps, input);

            Function<String, RuntimeException> errorFactory = message ->
                    new IllegalArgumentException(definition.getFileName()
                            + ".yml: invalid native enchantment effects: " + message);
            return invokeCompatible(dataResult, "getOrThrow", errorFactory);
        }

        private Map<String, Object> namespacedComponentKeys(Map<String, Object> effects,
                                                             Object operations)
                throws ReflectiveOperationException {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : effects.entrySet()) {
                String key = entry.getKey();
                result.put(
                        key.indexOf(':') >= 0 ? key : "minecraft:" + key,
                        normalizeNativeValue(key, entry.getValue(), operations));
            }
            return result;
        }

        private Object normalizeNativeValue(String key,
                                            Object value,
                                            Object operations)
                throws ReflectiveOperationException {
            if ("offset".equals(key)
                    && value instanceof List<?> values
                    && values.size() == 3
                    && values.stream().allMatch(Number.class::isInstance)) {
                return invokeCompatible(
                        operations,
                        "createIntList",
                        IntStream.of(
                                ((Number) values.get(0)).intValue(),
                                ((Number) values.get(1)).intValue(),
                                ((Number) values.get(2)).intValue()));
            }
            if (value instanceof Map<?, ?> values) {
                Map<String, Object> normalized = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : values.entrySet()) {
                    String nestedKey = String.valueOf(entry.getKey());
                    normalized.put(
                            nestedKey,
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

        private Object resolveExclusiveSet(ObjectCustomEnchantment definition,
                                           Object enchantmentRegistry,
                                           Map<String, Object> holders)
                throws ReflectiveOperationException {
            List<String> configured = definition.getExclusiveWith();
            if (configured.isEmpty()) {
                return directHolderSet(List.of());
            }

            if (configured.size() == 1 && configured.getFirst().startsWith("#")) {
                Object tagKey = createTagKey(
                        enchantmentRegistryKey,
                        identifier(normalizeKey(configured.getFirst().substring(1), "minecraft")));
                Object set = getOptional(enchantmentRegistry, tagKey);
                if (set == null) {
                    throw new IllegalArgumentException(definition.getFileName()
                            + ".yml: unknown exclusive enchantment tag '"
                            + configured.getFirst() + "'");
                }
                return set;
            }

            if (configured.stream().anyMatch(value -> value.startsWith("#"))) {
                throw new IllegalArgumentException(definition.getFileName()
                        + ".yml: exclusive-with cannot mix tags and enchantment keys");
            }

            List<Object> exclusiveHolders = new ArrayList<>();
            for (String id : configured) {
                String normalized = normalizeKey(id, "minecraft");
                Object holder = holders.get(normalized);
                if (holder == null) {
                    Object key = createResourceKey(enchantmentRegistryKey, identifier(normalized));
                    holder = getOptional(enchantmentRegistry, key);
                }
                if (holder == null) {
                    throw new IllegalArgumentException(definition.getFileName()
                            + ".yml: unknown exclusive enchantment '" + id + "'");
                }
                exclusiveHolders.add(holder);
            }
            return directHolderSet(exclusiveHolders);
        }

        private void applyObtainingTags(Object registry,
                                        ObjectCustomEnchantment definition,
                                        Object holder)
                throws ReflectiveOperationException {
            for (String source : definition.getObtainingSources()) {
                String tag = switch (source) {
                    case "VILLAGER_TRADE" -> "minecraft:tradeable";
                    case "ENCHANTING_TABLE" -> "minecraft:in_enchanting_table";
                    case "RANDOM_LOOT" -> "minecraft:on_random_loot";
                    case "MOB_EQUIPMENT" -> "minecraft:on_mob_spawn_equipment";
                    case "TRADED_EQUIPMENT" -> "minecraft:on_traded_equipment";
                    default -> null;
                };
                if (tag != null) {
                    addToTag(registry, tag, holder, true);
                }
            }
        }

        private void applyRarityTags(Object registry,
                                     ObjectCustomEnchantment definition,
                                     Object holder)
                throws ReflectiveOperationException {
            ConfigurationSection root = supportedItems == null ? null : supportedItems.getRoot();
            ConfigurationSection rarities = root == null
                    ? null
                    : root.getConfigurationSection("rarity");
            ConfigurationSection rarity = findSectionIgnoreCase(
                    rarities,
                    definition.getRarity());
            if (rarity == null) {
                return;
            }
            if (rarity.getBoolean("curse", false)) {
                addToTag(registry, "minecraft:curse", holder, true);
            }
            if (rarity.getBoolean("double-trade-multiplier", false)) {
                addToTag(registry, "minecraft:double_trade_price", holder, false);
            }
        }

        private void addToTag(Object registry,
                              String tagId,
                              Object holder,
                              boolean warnWhenMissing)
                throws ReflectiveOperationException {
            Object tagKey = createTagKey(enchantmentRegistryKey, identifier(tagId));
            Object named = getOptional(registry, tagKey);
            if (named == null) {
                if (warnWhenMissing) {
                    logger.warning("Missing Spigot enchantment tag " + tagId
                            + "; the related obtaining source was skipped.");
                }
                return;
            }

            List<Object> contents = streamHolderSet(named);
            if (!contents.contains(holder)) {
                contents.add(holder);
                invokeCompatible(registry, "bindTags", Map.of(tagKey, contents));
            }
        }

        private Object directHolderSet(Collection<Object> holders)
                throws ReflectiveOperationException {
            List<Object> copy = List.copyOf(holders);
            try {
                return invokeStaticCompatible(holderSetClass, "direct", copy);
            } catch (ReflectiveOperationException ignored) {
                Object array = Array.newInstance(holderClass, copy.size());
                for (int index = 0; index < copy.size(); index++) {
                    Array.set(array, index, copy.get(index));
                }
                return invokeStaticCompatible(holderSetClass, "direct", array);
            }
        }

        private Object slotArray(List<String> configured)
                throws ReflectiveOperationException {
            List<Object> slots = new ArrayList<>();
            for (String raw : configured) {
                String name = switch (raw.toUpperCase(Locale.ROOT)) {
                    case "OFF_HAND" -> "OFFHAND";
                    case "MAIN_HAND" -> "MAINHAND";
                    default -> raw.toUpperCase(Locale.ROOT);
                };
                try {
                    slots.add(staticField(slotGroupClass, name));
                } catch (ReflectiveOperationException ignored) {
                    logger.warning("Unknown active slot group '" + raw
                            + "'; it was skipped for native Spigot registration.");
                }
            }
            if (slots.isEmpty()) {
                slots.add(staticField(slotGroupClass, "ANY"));
            }

            Object result = Array.newInstance(slotGroupClass, slots.size());
            for (int index = 0; index < slots.size(); index++) {
                Array.set(result, index, slots.get(index));
            }
            return result;
        }

        private Object lookupRegistry(Object access, Object key)
                throws ReflectiveOperationException {
            Object result = invokeCompatible(access, "lookup", key);
            Object registry = optionalValue(result);
            if (registry == null) {
                throw new IllegalStateException("Cannot obtain NMS registry " + key);
            }
            return registry;
        }

        private Object identifier(String value)
                throws ReflectiveOperationException {
            return invokeStaticCompatible(
                    identifierClass,
                    "parse",
                    normalizeKey(value, "minecraft"));
        }

        private Object createResourceKey(Object registry, Object id)
                throws ReflectiveOperationException {
            return invokeStaticCompatible(resourceKeyClass, "create", registry, id);
        }

        private Object createTagKey(Object registry, Object id)
                throws ReflectiveOperationException {
            return invokeStaticCompatible(tagKeyClass, "create", registry, id);
        }

        private Object getOptional(Object registry, Object key)
                throws ReflectiveOperationException {
            try {
                return optionalValue(invokeCompatible(registry, "get", key));
            } catch (ReflectiveOperationException ignored) {
                return optionalValue(invokeCompatible(registry, "getValue", key));
            }
        }

        private void refreshComponentLookup(Object registry) {
            try {
                Class<?> lookupType = type("net.minecraft.core.component.DataComponentLookup");
                Class<?> objectListType = type("it.unimi.dsi.fastutil.objects.ObjectList");
                Field lookupField = findNamedField(
                        registry.getClass(),
                        lookupType,
                        "componentLookup");
                Field byIdField = findNamedField(
                        registry.getClass(),
                        objectListType,
                        "byId");
                lookupField.setAccessible(true);
                byIdField.setAccessible(true);
                Object lookup = construct(lookupType, byIdField.get(registry));
                lookupField.set(registry, lookup);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                logger.fine("Could not refresh Spigot 26.2 registry component lookup: "
                        + exception.getMessage());
            }
        }

        private void exposeBukkitMirror(Object enchantment) {
            try {
                String craftPackage = Bukkit.getServer().getClass().getPackageName();
                Class<?> craftEnchantment = type(
                        craftPackage + ".enchantments.CraftEnchantment");
                invokeStaticCompatible(
                        craftEnchantment,
                        "minecraftToBukkit",
                        enchantment);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                logger.fine("Could not eagerly create Bukkit enchantment mirror: "
                        + exception.getMessage());
            }
        }

        private final class ItemTagResolver {

            private final Object itemRegistry;
            private final Map<String, ConfigurationSection> customTags = new LinkedHashMap<>();
            private final Map<String, List<Object>> cache = new LinkedHashMap<>();
            private final Set<String> resolving = new LinkedHashSet<>();

            private ItemTagResolver(Object itemRegistry,
                                    ConfigurationSection supportedItems) {
                this.itemRegistry = itemRegistry;
                ConfigurationSection tags = supportedItems == null
                        ? null
                        : supportedItems.getConfigurationSection("tags");
                if (tags == null) {
                    return;
                }
                for (String logicalName : tags.getKeys(false)) {
                    ConfigurationSection section = tags.getConfigurationSection(logicalName);
                    if (section == null) {
                        continue;
                    }
                    String configuredKey = section.getString("key", logicalName);
                    customTags.put(
                            normalizeKey(configuredKey, "enchantmentreform"),
                            section);
                    customTags.putIfAbsent(
                            normalizeKey(logicalName, "enchantmentreform"),
                            section);
                }
            }

            private List<Object> resolve(String configured)
                    throws ReflectiveOperationException {
                if (configured == null || configured.isBlank()) {
                    return List.of();
                }
                return resolveTag(normalizeKey(
                        stripTagPrefix(configured),
                        "enchantmentreform"));
            }

            private List<Object> resolveTag(String tag)
                    throws ReflectiveOperationException {
                List<Object> cached = cache.get(tag);
                if (cached != null) {
                    return cached;
                }
                if (!resolving.add(tag)) {
                    throw new IllegalArgumentException(
                            "Circular supported-items tag reference: " + tag);
                }

                try {
                    LinkedHashSet<Object> holders = new LinkedHashSet<>();
                    ConfigurationSection custom = customTags.get(tag);
                    if (custom == null) {
                        Object tagKey = createTagKey(itemRegistryKey, identifier(tag));
                        Object set = getOptional(itemRegistry, tagKey);
                        if (set == null) {
                            throw new IllegalArgumentException("Unknown item tag '#" + tag + "'");
                        }
                        holders.addAll(streamHolderSet(set));
                    } else {
                        for (String value : custom.getStringList("values")) {
                            if (value.startsWith("#")) {
                                holders.addAll(resolveTag(normalizeKey(
                                        value.substring(1),
                                        "enchantmentreform")));
                            } else {
                                Object itemId = identifier(value);
                                Object holder = getOptional(itemRegistry, itemId);
                                if (holder == null) {
                                    Object key = createResourceKey(itemRegistryKey, itemId);
                                    holder = getOptional(itemRegistry, key);
                                }
                                if (holder == null) {
                                    throw new IllegalArgumentException(
                                            "Unknown item key '" + value
                                                    + "' in supported-items tag " + tag);
                                }
                                holders.add(holder);
                            }
                        }
                    }

                    List<Object> result = List.copyOf(holders);
                    cache.put(tag, result);
                    return result;
                } finally {
                    resolving.remove(tag);
                }
            }
        }
    }

    private static final class RegistryMutation implements AutoCloseable {

        private final Object registry;
        private final Field frozenField;
        private final Field intrusiveField;
        private final boolean oldFrozen;
        private final Object oldIntrusive;

        private RegistryMutation(Object registry,
                                 Field frozenField,
                                 Field intrusiveField,
                                 boolean oldFrozen,
                                 Object oldIntrusive) {
            this.registry = registry;
            this.frozenField = frozenField;
            this.intrusiveField = intrusiveField;
            this.oldFrozen = oldFrozen;
            this.oldIntrusive = oldIntrusive;
        }

        private static RegistryMutation open(Object registry)
                throws ReflectiveOperationException {
            Field frozen = findNamedField(registry.getClass(), boolean.class, "frozen");
            Field intrusive = findNamedField(
                    registry.getClass(),
                    Map.class,
                    "unregisteredIntrusiveHolders");
            frozen.setAccessible(true);
            intrusive.setAccessible(true);

            boolean oldFrozen = frozen.getBoolean(registry);
            Object oldIntrusive = intrusive.get(registry);
            frozen.setBoolean(registry, false);
            // Standalone holders are pre-created through createRegistrationLookup().
            // Keeping this null makes MappedRegistry#register use those holders.
            intrusive.set(registry, null);
            return new RegistryMutation(registry, frozen, intrusive, oldFrozen, oldIntrusive);
        }

        @Override
        public void close() {
            try {
                intrusiveField.set(registry, oldIntrusive);
                frozenField.setBoolean(registry, oldFrozen);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(
                        "Could not restore Spigot 26.2 enchantment registry state",
                        exception);
            }
        }
    }

    private static List<Object> streamHolderSet(Object holderSet)
            throws ReflectiveOperationException {
        Object streamObject = invokeCompatible(holderSet, "stream");
        if (!(streamObject instanceof Stream<?> stream)) {
            throw new IllegalStateException("NMS holder set did not expose a stream");
        }
        return new ArrayList<>(stream.toList());
    }

    private static Object optionalValue(Object value) {
        return value instanceof Optional<?> optional ? optional.orElse(null) : value;
    }

    private static ConfigurationSection findSectionIgnoreCase(
            ConfigurationSection parent,
            String name) {
        if (parent == null || name == null) {
            return null;
        }
        ConfigurationSection direct = parent.getConfigurationSection(name);
        if (direct != null) {
            return direct;
        }
        for (String key : parent.getKeys(false)) {
            if (key.equalsIgnoreCase(name)) {
                return parent.getConfigurationSection(key);
            }
        }
        return null;
    }

    private static String normalizeKey(String value, String defaultNamespace) {
        String normalized = value == null
                ? ""
                : value.strip().toLowerCase(Locale.ROOT);
        return normalized.indexOf(':') >= 0
                ? normalized
                : defaultNamespace + ':' + normalized;
    }

    private static String stripTagPrefix(String value) {
        return value != null && value.startsWith("#")
                ? value.substring(1)
                : value;
    }

    private static Object construct(Class<?> type, Object... args)
            throws ReflectiveOperationException {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (compatible(constructor.getParameterTypes(), args)) {
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        }
        throw new NoSuchMethodException("No compatible constructor for " + type.getName());
    }

    private static Object invoke(Object target, String method)
            throws ReflectiveOperationException {
        Method found = compatibleMethod(target.getClass(), method, new Object[0]);
        found.setAccessible(true);
        return found.invoke(target);
    }

    private static Object invokeCompatible(Object target, String name, Object... args)
            throws ReflectiveOperationException {
        Method method = compatibleMethod(target.getClass(), name, args);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Object invokeStaticCompatible(Class<?> type, String name, Object... args)
            throws ReflectiveOperationException {
        Method method = compatibleMethod(type, name, args);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException(type.getName() + '#' + name + " is not static");
        }
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    private static Method compatibleMethod(Class<?> type, String name, Object[] args)
            throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name)
                    && compatible(method.getParameterTypes(), args)) {
                return method;
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name)
                        && compatible(method.getParameterTypes(), args)) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException(type.getName() + '#' + name);
    }

    private static boolean compatible(Class<?>[] parameters, Object[] args) {
        if (parameters.length != args.length) {
            return false;
        }
        for (int index = 0; index < parameters.length; index++) {
            if (args[index] != null
                    && !wrap(parameters[index]).isInstance(args[index])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == long.class) return Long.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static Field findNamedField(Class<?> type,
                                        Class<?> fieldType,
                                        String... names)
            throws NoSuchFieldException {
        for (String name : names) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                try {
                    Field field = current.getDeclaredField(name);
                    if (fieldType == field.getType()
                            || fieldType.isAssignableFrom(field.getType())) {
                        return field;
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        throw new NoSuchFieldException(String.join(",", names));
    }

    private static Object staticField(Class<?> type, String name)
            throws ReflectiveOperationException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(null);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(type.getName() + '#' + name);
    }

    private static Object staticRequired(Class<?> type, String name) {
        try {
            return staticField(type, name);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Missing NMS field " + type.getName() + '#' + name,
                    exception);
        }
    }

    private static Class<?> nested(Class<?> owner, String simpleName) {
        return type(owner.getName() + '$' + simpleName);
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

    private static Class<?> type(String name) {
        try {
            return Class.forName(
                    name,
                    true,
                    Bukkit.getServer().getClass().getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Missing NMS type: " + name, exception);
        }
    }
}
