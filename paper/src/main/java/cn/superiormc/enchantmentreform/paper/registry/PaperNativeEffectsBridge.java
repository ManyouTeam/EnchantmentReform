package cn.superiormc.enchantmentreform.paper.registry;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Injects the native enchantment effect component map that Paper's public
 * {@link EnchantmentRegistryEntry.Builder} API does not currently expose.
 *
 * <p>The bridge deliberately runs inside the registry compose callback. It
 * only fills the internal builder before Paper builds and freezes the entry;
 * it never thaws or mutates an already-frozen registry.</p>
 */
final class PaperNativeEffectsBridge {

    private static final String PAPER_ENTRY_CLASS =
            "io.papermc.paper.registry.data.PaperEnchantmentRegistryEntry";
    private static final String EFFECT_COMPONENTS_CLASS =
            "net.minecraft.world.item.enchantment.EnchantmentEffectComponents";
    private static final String REGISTRY_OPS_CLASS = "net.minecraft.resources.RegistryOps";
    private static final String JAVA_OPS_CLASS = "com.mojang.serialization.JavaOps";

    private PaperNativeEffectsBridge() {}

    static void apply(EnchantmentRegistryEntry.Builder builder, ObjectCustomEnchantment definition) {
        if (!definition.hasNativeEffects()) {
            return;
        }
        apply(builder, definition.getFileName(), definition.getNativeEffects());
    }

    static void apply(EnchantmentRegistryEntry.Builder builder, String source,
                      Map<String, Object> nativeEffects) {
        try {
            ClassLoader loader = builder.getClass().getClassLoader();
            validateBuilder(builder, loader);
            Object conversions = read(findField(builder.getClass(), "conversions"), builder);
            Object lookup = invokeCompatible(conversions, "lookup");
            Object registryOps = createRegistryOps(loader, lookup);
            Object effects = decodeEffects(loader, registryOps, source, nativeEffects);
            setEffects(builder, effects);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(source
                    + ".yml: failed to decode native enchantment effects", cause);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(source
                    + ".yml: Paper does not expose native enchantment effects for this server build", exception);
        }
    }

    static void clear(EnchantmentRegistryEntry.Builder builder, String source) {
        try {
            ClassLoader loader = builder.getClass().getClassLoader();
            validateBuilder(builder, loader);
            Class<?> dataComponentMap = Class.forName(
                    "net.minecraft.core.component.DataComponentMap", false, loader);
            setEffects(builder, dataComponentMap.getField("EMPTY").get(null));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(source
                    + ".yml: failed to disable the vanilla enchantment", exception);
        }
    }

    private static void validateBuilder(EnchantmentRegistryEntry.Builder builder, ClassLoader loader)
            throws ReflectiveOperationException {
        Class<?> paperEntry = Class.forName(PAPER_ENTRY_CLASS, false, loader);
        if (!paperEntry.isInstance(builder)) {
            throw new IllegalStateException("Unexpected Paper enchantment builder: "
                    + builder.getClass().getName());
        }
    }

    private static void setEffects(EnchantmentRegistryEntry.Builder builder, Object effects)
            throws ReflectiveOperationException {
        Field effectsField = findField(builder.getClass(), "effects");
        effectsField.setAccessible(true);
        effectsField.set(builder, effects);
    }

    private static Object createRegistryOps(ClassLoader loader, Object lookup)
            throws ReflectiveOperationException {
        Class<?> javaOpsClass = Class.forName(JAVA_OPS_CLASS, false, loader);
        Object javaOps = javaOpsClass.getField("INSTANCE").get(null);
        Class<?> registryOpsClass = Class.forName(REGISTRY_OPS_CLASS, false, loader);
        return invokeStaticCompatible(registryOpsClass, "create", javaOps, lookup);
    }

    private static Object decodeEffects(ClassLoader loader, Object registryOps, String source,
                                        Map<String, Object> nativeEffects)
            throws ReflectiveOperationException {
        Class<?> componentsClass = Class.forName(EFFECT_COMPONENTS_CLASS, false, loader);
        Object codec = componentsClass.getField("CODEC").get(null);
        Object dataResult = invokeCompatible(codec, "parse", registryOps,
                namespacedComponentKeys(nativeEffects, registryOps));
        Function<String, RuntimeException> errorFactory = message ->
                new IllegalArgumentException(source
                        + ".yml: invalid native enchantment effects: " + message);
        return invokeCompatible(dataResult, "getOrThrow", errorFactory);
    }

    private static Map<String, Object> namespacedComponentKeys(
            Map<String, Object> effects, Object operations) throws ReflectiveOperationException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : effects.entrySet()) {
            String key = entry.getKey();
            result.put(key.indexOf(':') >= 0 ? key : "minecraft:" + key,
                    normalizeNativeValue(key, entry.getValue(), operations));
        }
        return result;
    }

    private static Object normalizeNativeValue(String key, Object value, Object operations)
            throws ReflectiveOperationException {
        if ("offset".equals(key) && value instanceof List<?> values
                && values.size() == 3 && values.stream().allMatch(Number.class::isInstance)) {
            return invokeCompatible(operations, "createIntList", IntStream.of(
                    ((Number) values.get(0)).intValue(),
                    ((Number) values.get(1)).intValue(),
                    ((Number) values.get(2)).intValue()
            ));
        }
        if (value instanceof Map<?, ?> values) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                String nestedKey = String.valueOf(entry.getKey());
                result.put(nestedKey, normalizeNativeValue(
                        nestedKey, entry.getValue(), operations));
            }
            return result;
        }
        if (value instanceof List<?> values) {
            List<Object> result = new java.util.ArrayList<>(values.size());
            for (Object nested : values) {
                result.add(normalizeNativeValue(null, nested, operations));
            }
            return List.copyOf(result);
        }
        return value;
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(type.getName() + '#' + name);
    }

    private static Object read(Field field, Object target) throws ReflectiveOperationException {
        field.setAccessible(true);
        return field.get(target);
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
            if (method.getName().equals(name) && compatible(method.getParameterTypes(), args)) {
                return method;
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && compatible(method.getParameterTypes(), args)) {
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
        for (int i = 0; i < parameters.length; i++) {
            if (args[i] != null && !wrap(parameters[i]).isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
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
}
