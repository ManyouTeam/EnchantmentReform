package cn.superiormc.enchantmentreform.nms.advice.v1_21_11;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * EnchantRandomlyFunction advice for the Minecraft 1.21.11 named NMS ABI.
 *
 * <p>The advice body is inlined into a server-loaded class, so it intentionally
 * uses only JDK types and reflective NMS access.</p>
 */
public final class RandomToLevelsAdvice12111 {

    private RandomToLevelsAdvice12111() {
    }

    public static void probe(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> target = Class.forName(
                "net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction",
                false,
                loader
        );
        Class<?> itemStack = Class.forName(
                "net.minecraft.world.item.ItemStack",
                false,
                loader
        );
        Class<?> lootContext = Class.forName(
                "net.minecraft.world.level.storage.loot.LootContext",
                false,
                loader
        );
        Class<?> randomSource = Class.forName(
                "net.minecraft.util.RandomSource",
                false,
                loader
        );
        Class<?> registryAccess = Class.forName(
                "net.minecraft.core.RegistryAccess",
                false,
                loader
        );
        Class<?> resourceKey = Class.forName(
                "net.minecraft.resources.ResourceKey",
                false,
                loader
        );
        Class<?> registry = Class.forName(
                "net.minecraft.core.Registry",
                false,
                loader
        );
        Class<?> holder = Class.forName(
                "net.minecraft.core.Holder",
                false,
                loader
        );
        Class<?> holderSet = Class.forName(
                "net.minecraft.core.HolderSet",
                false,
                loader
        );
        Class<?> componentType = Class.forName(
                "net.minecraft.core.component.DataComponentType",
                false,
                loader
        );
        Class<?> enchantmentInstance = Class.forName(
                "net.minecraft.world.item.enchantment.EnchantmentInstance",
                false,
                loader
        );
        Class<?> helper = Class.forName(
                "net.minecraft.world.item.enchantment.EnchantmentHelper",
                false,
                loader
        );

        Method run = target.getMethod("run", itemStack, lootContext);
        if (run.getReturnType() != itemStack) {
            throw new NoSuchMethodException(
                    "Unexpected EnchantRandomlyFunction#run return type"
            );
        }

        Field options = target.getDeclaredField("options");
        if (!Optional.class.isAssignableFrom(options.getType())) {
            throw new NoSuchFieldException(
                    "Unexpected EnchantRandomlyFunction.options type"
            );
        }
        boolean hasAdditionalCostComponent;
        try {
            target.getDeclaredField("includeAdditionalCostComponent");
            hasAdditionalCostComponent = true;
        } catch (NoSuchFieldException ignored) {
            hasAdditionalCostComponent = false;
        }
        if (hasAdditionalCostComponent) {
            throw new NoSuchFieldException(
                    "Minecraft 1.21.11 advice cannot bind "
                            + "includeAdditionalCostComponent"
            );
        }

        lootContext.getMethod("getRandom");
        lootContext.getMethod("getLevel").getReturnType()
                .getMethod("registryAccess");
        randomSource.getMethod("nextInt", int.class);
        registryAccess.getMethod("lookupOrThrow", resourceKey);
        registry.getMethod("listElements");
        holderSet.getMethod("stream");
        helper.getMethod(
                "selectEnchantment",
                randomSource,
                itemStack,
                int.class,
                Stream.class
        );
        enchantmentInstance.getMethod("enchantment");
        enchantmentInstance.getMethod("level");
        itemStack.getMethod("getItem");
        itemStack.getMethod("get", componentType);
        findCompatibleUnaryMethod(
                itemStack,
                "transmuteCopy",
                Class.forName("net.minecraft.world.item.Items", false, loader)
                        .getField("ENCHANTED_BOOK")
                        .get(null)
        );
        itemStack.getMethod("enchant", holder, int.class);

        Class.forName(
                "net.minecraft.core.registries.Registries",
                false,
                loader
        ).getField("ENCHANTMENT");
        Class<?> items = Class.forName(
                "net.minecraft.world.item.Items",
                false,
                loader
        );
        Object book = items.getField("BOOK").get(null);
        book.getClass().getMethod("getDefaultInstance");
        items.getField("ENCHANTED_BOOK");
        Class.forName(
                "net.minecraft.core.component.DataComponents",
                false,
                loader
        ).getField("ENCHANTABLE");
    }

    private static Method findCompatibleUnaryMethod(
            Class<?> owner,
            String name,
            Object argument
    ) throws NoSuchMethodException {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isInstance(argument)) {
                return method;
            }
        }
        throw new NoSuchMethodException(
                owner.getName() + "." + name + "(compatible argument)"
        );
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static Object enter(
            @Advice.Argument(0) Object itemStack,
            @Advice.Argument(1) Object context,
            @Advice.FieldValue("options") Object options
    ) {
        if (!Boolean.getBoolean(
                "enchantmentreform.loot-enchant-patch.enabled")) {
            return null;
        }

        try {
            int configuredMinimum = Integer.getInteger(
                    "enchantmentreform.loot-enchant-patch.min-cost",
                    10
            );
            int configuredMaximum = Integer.getInteger(
                    "enchantmentreform.loot-enchant-patch.max-cost",
                    30
            );
            int minimum = Math.max(
                    0,
                    Math.min(configuredMinimum, configuredMaximum)
            );
            int maximum = Math.max(
                    minimum,
                    Math.max(configuredMinimum, configuredMaximum)
            );

            ClassLoader loader = context.getClass().getClassLoader();
            Class<?> lootContextClass = Class.forName(
                    "net.minecraft.world.level.storage.loot.LootContext",
                    false,
                    loader
            );
            Class<?> itemStackClass = Class.forName(
                    "net.minecraft.world.item.ItemStack",
                    false,
                    loader
            );
            Class<?> randomSourceClass = Class.forName(
                    "net.minecraft.util.RandomSource",
                    false,
                    loader
            );
            Class<?> registryAccessClass = Class.forName(
                    "net.minecraft.core.RegistryAccess",
                    false,
                    loader
            );

            Object random = lootContextClass
                    .getMethod("getRandom")
                    .invoke(context);
            int enchantmentCost = minimum;
            if (maximum > minimum) {
                int offset = (int) randomSourceClass
                        .getMethod("nextInt", int.class)
                        .invoke(random, maximum - minimum + 1);
                enchantmentCost += offset;
            }

            Object level = lootContextClass
                    .getMethod("getLevel")
                    .invoke(context);
            Object registryAccess = level.getClass()
                    .getMethod("registryAccess")
                    .invoke(level);

            Stream<?> possibleEnchantments;
            Optional<?> configuredOptions = (Optional<?>) options;
            if (configuredOptions.isPresent()) {
                Object holderSet = configuredOptions.get();
                Class<?> holderSetClass = Class.forName(
                        "net.minecraft.core.HolderSet",
                        false,
                        loader
                );
                possibleEnchantments = (Stream<?>) holderSetClass
                        .getMethod("stream")
                        .invoke(holderSet);
            } else {
                Class<?> resourceKeyClass = Class.forName(
                        "net.minecraft.resources.ResourceKey",
                        false,
                        loader
                );
                Object enchantmentRegistryKey = Class.forName(
                                "net.minecraft.core.registries.Registries",
                                false,
                                loader
                        )
                        .getField("ENCHANTMENT")
                        .get(null);
                Object enchantmentRegistry = registryAccessClass
                        .getMethod("lookupOrThrow", resourceKeyClass)
                        .invoke(registryAccess, enchantmentRegistryKey);
                Class<?> registryClass = Class.forName(
                        "net.minecraft.core.Registry",
                        false,
                        loader
                );
                possibleEnchantments = (Stream<?>) registryClass
                        .getMethod("listElements")
                        .invoke(enchantmentRegistry);
            }

            Class<?> helper = Class.forName(
                    "net.minecraft.world.item.enchantment.EnchantmentHelper",
                    false,
                    loader
            );
            Class<?> componentTypeClass = Class.forName(
                    "net.minecraft.core.component.DataComponentType",
                    false,
                    loader
            );
            Object enchantableComponent = Class.forName(
                            "net.minecraft.core.component.DataComponents",
                            false,
                            loader
                    )
                    .getField("ENCHANTABLE")
                    .get(null);
            Class<?> itemsClass = Class.forName(
                    "net.minecraft.world.item.Items",
                    false,
                    loader
            );
            Object book = itemsClass.getField("BOOK").get(null);
            Object selectionStack = itemStack;
            Object enchantable = itemStackClass
                    .getMethod("get", componentTypeClass)
                    .invoke(itemStack, enchantableComponent);
            if (enchantable == null) {
                selectionStack = book.getClass()
                        .getMethod("getDefaultInstance")
                        .invoke(book);
            }

            // selectEnchantment performs the vanilla weight-based selection.
            List<?> selected = (List<?>) helper.getMethod(
                            "selectEnchantment",
                            randomSourceClass,
                            itemStackClass,
                            int.class,
                            Stream.class
                    )
                    .invoke(
                            null,
                            random,
                            selectionStack,
                            enchantmentCost,
                            possibleEnchantments
                    );

            if (selected.isEmpty()) {
                return itemStack;
            }

            Object chosen = selected.get(0);
            Class<?> instanceClass = Class.forName(
                    "net.minecraft.world.item.enchantment.EnchantmentInstance",
                    false,
                    loader
            );
            Object enchantment = instanceClass
                    .getMethod("enchantment")
                    .invoke(chosen);
            int enchantmentLevel = (int) instanceClass
                    .getMethod("level")
                    .invoke(chosen);

            Object enchantedBook = itemsClass
                    .getField("ENCHANTED_BOOK")
                    .get(null);
            Object result = itemStack;

            Object currentItem = itemStackClass
                    .getMethod("getItem")
                    .invoke(itemStack);
            if (currentItem == book) {
                Method transmuteCopy = null;
                for (Method method : itemStackClass.getMethods()) {
                    if (method.getName().equals("transmuteCopy")
                            && method.getParameterCount() == 1
                            && method.getParameterTypes()[0]
                            .isInstance(enchantedBook)) {
                        transmuteCopy = method;
                        break;
                    }
                }
                if (transmuteCopy == null) {
                    throw new NoSuchMethodException(
                            "ItemStack.transmuteCopy(compatible argument)"
                    );
                }
                result = transmuteCopy.invoke(itemStack, enchantedBook);
            }

            Class<?> holderClass = Class.forName(
                    "net.minecraft.core.Holder",
                    false,
                    loader
            );
            itemStackClass
                    .getMethod("enchant", holderClass, int.class)
                    .invoke(result, enchantment, enchantmentLevel);

            if (Boolean.getBoolean(
                    "enchantmentreform.loot-enchant-patch.debug")) {
                Object resultItem = itemStackClass
                        .getMethod("getItem")
                        .invoke(result);
                String enchantmentName = String.valueOf(enchantment);
                try {
                    Object optionalKey = holderClass
                            .getMethod("unwrapKey")
                            .invoke(enchantment);
                    if (optionalKey instanceof Optional<?> key
                            && key.isPresent()) {
                        Object resourceKey = key.get();
                        enchantmentName = String.valueOf(
                                resourceKey.getClass()
                                        .getMethod("location")
                                        .invoke(resourceKey)
                        );
                    }
                } catch (Throwable ignored) {
                }
                Logger.getLogger("EnchantmentReform").info(
                        "[Debug] Applied enchant_randomly replacement: item="
                                + resultItem
                                + ", cost=" + enchantmentCost
                                + ", enchantment=" + enchantmentName
                                + ", level=" + enchantmentLevel
                );
            }

            return result;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Advice.OnMethodExit
    public static void exit(
            @Advice.Enter Object replacement,
            @Advice.Return(
                    readOnly = false,
                    typing = Assigner.Typing.DYNAMIC
            )
            Object returned
    ) {
        if (replacement != null) {
            returned = replacement;
        }
    }
}
