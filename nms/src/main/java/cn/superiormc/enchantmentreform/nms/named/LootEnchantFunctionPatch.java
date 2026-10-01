package cn.superiormc.enchantmentreform.nms.named;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * Installs the version-specific EnchantRandomlyFunction advice selected from
 * the named NMS ABI available on the running server.
 */
public final class LootEnchantFunctionPatch {

    private static final String TARGET =
            "net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction";
    private static final String ITEM_STACK = "net.minecraft.world.item.ItemStack";
    private static final String LOOT_CONTEXT =
            "net.minecraft.world.level.storage.loot.LootContext";

    private static final String ADVICE_1_21_11 =
            "cn.superiormc.enchantmentreform.nms.advice.v1_21_11."
                    + "RandomToLevelsAdvice12111";
    private static final String ADVICE_26_1 =
            "cn.superiormc.enchantmentreform.nms.advice.v26_1."
                    + "RandomToLevelsAdvice261";

    private static final String ENABLED =
            "enchantmentreform.loot-enchant-patch.enabled";
    private static final String MIN_COST =
            "enchantmentreform.loot-enchant-patch.min-cost";
    private static final String MAX_COST =
            "enchantmentreform.loot-enchant-patch.max-cost";
    private static final String DEBUG =
            "enchantmentreform.loot-enchant-patch.debug";

    private static Instrumentation instrumentation;
    private static ResettableClassFileTransformer transformer;
    private static String activeAbi;

    private LootEnchantFunctionPatch() {
    }

    public static synchronized boolean install(
            Logger logger,
            int minimumCost,
            int maximumCost
    ) {
        int minimum = Math.max(0, Math.min(minimumCost, maximumCost));
        int maximum = Math.max(minimum, Math.max(minimumCost, maximumCost));
        boolean debug = readDebugOption();
        setProperties(minimum, maximum, debug);

        if (transformer != null) {
            logger.info("Updated EnchantRandomly replacement cost to "
                    + minimum + "-" + maximum + ". Debug: " + debug
                    + ". ABI: " + activeAbi + '.');
            return true;
        }

        try {
            AdviceSelection selection = selectAdvice();
            instrumentation = ByteBuddyAgent.install();
            transformer = new AgentBuilder.Default()
                    .disableClassFormatChanges()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(named(TARGET))
                    .transform((builder, type, loader, module, protectionDomain) ->
                            builder.visit(Advice.to(selection.adviceClass()).on(
                                    named("run")
                                            .and(takesArguments(2))
                                            .and(takesArgument(0, named(ITEM_STACK)))
                                            .and(takesArgument(1, named(LOOT_CONTEXT)))
                                            .and(returns(named(ITEM_STACK))))))
                    .installOn(instrumentation);
            activeAbi = selection.id();

            logger.info("Replaced EnchantRandomlyFunction with a single-result "
                    + "EnchantWithLevels selection. Cost: "
                    + minimum + "-" + maximum + ". Debug: " + debug
                    + ". ABI: " + activeAbi + '.');
            return true;
        } catch (Throwable throwable) {
            clearProperties();
            instrumentation = null;
            transformer = null;
            activeAbi = null;
            logger.log(
                    Level.WARNING,
                    "Could not patch EnchantRandomlyFunction. No compatible named NMS "
                            + "advice module was found, the ABI probe failed, or dynamic "
                            + "agent loading is disabled.",
                    throwable
            );
            return false;
        }
    }

    public static synchronized void uninstall(Logger logger) {
        ResettableClassFileTransformer installedTransformer = transformer;
        Instrumentation installedInstrumentation = instrumentation;
        transformer = null;
        instrumentation = null;
        activeAbi = null;
        clearProperties();

        if (installedTransformer == null || installedInstrumentation == null) {
            return;
        }

        try {
            boolean restored = installedTransformer.reset(
                    installedInstrumentation,
                    AgentBuilder.RedefinitionStrategy.RETRANSFORMATION
            );
            if (restored) {
                logger.info("Restored the original EnchantRandomlyFunction.");
            } else {
                logger.warning("Could not restore the original EnchantRandomlyFunction.");
            }
        } catch (Throwable throwable) {
            logger.log(
                    Level.WARNING,
                    "An error occurred while restoring EnchantRandomlyFunction.",
                    throwable
            );
        }
    }

    private static AdviceSelection selectAdvice() throws ReflectiveOperationException {
        ClassLoader loader = LootEnchantFunctionPatch.class.getClassLoader();
        Class<?> target = Class.forName(TARGET, false, loader);
        AdviceDescriptor descriptor = hasAdditionalCostComponent(target)
                ? new AdviceDescriptor("26.1+", ADVICE_26_1)
                : new AdviceDescriptor("1.21.11", ADVICE_1_21_11);

        Class<?> adviceClass = Class.forName(descriptor.className(), true, loader);
        invokeProbe(adviceClass, loader);
        return new AdviceSelection(descriptor.id(), adviceClass);
    }

    private static boolean hasAdditionalCostComponent(Class<?> target) {
        try {
            Field field = target.getDeclaredField("includeAdditionalCostComponent");
            if (field.getType() != boolean.class) {
                throw new IllegalStateException(
                        "Unexpected EnchantRandomlyFunction.includeAdditionalCostComponent type: "
                                + field.getType().getName()
                );
            }
            return true;
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }

    private static void invokeProbe(
            Class<?> adviceClass,
            ClassLoader serverLoader
    ) throws ReflectiveOperationException {
        Method probe = adviceClass.getMethod("probe", ClassLoader.class);
        try {
            probe.invoke(null, serverLoader);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ReflectiveOperationException reflective) {
                throw reflective;
            }
            throw new ReflectiveOperationException(
                    "Advice ABI probe failed for " + adviceClass.getName(),
                    cause
            );
        }
    }

    private static boolean readDebugOption() {
        try {
            Class<?> managerClass = Class.forName(
                    "cn.superiormc.enchantmentreform.managers.ConfigManager",
                    false,
                    LootEnchantFunctionPatch.class.getClassLoader()
            );
            Object manager = managerClass.getField("configManager").get(null);
            return manager != null && (boolean) managerClass
                    .getMethod("getBoolean", String.class, boolean.class)
                    .invoke(manager, "debug", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void setProperties(int minimum, int maximum, boolean debug) {
        System.setProperty(ENABLED, Boolean.TRUE.toString());
        System.setProperty(MIN_COST, Integer.toString(minimum));
        System.setProperty(MAX_COST, Integer.toString(maximum));
        System.setProperty(DEBUG, Boolean.toString(debug));
    }

    private static void clearProperties() {
        System.clearProperty(ENABLED);
        System.clearProperty(MIN_COST);
        System.clearProperty(MAX_COST);
        System.clearProperty(DEBUG);
    }

    private record AdviceDescriptor(String id, String className) {
    }

    private record AdviceSelection(String id, Class<?> adviceClass) {
    }
}
