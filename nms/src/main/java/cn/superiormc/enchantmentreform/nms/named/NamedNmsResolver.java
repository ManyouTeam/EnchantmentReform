package cn.superiormc.enchantmentreform.nms.named;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class NamedNmsResolver {

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final String craftPackage = Bukkit.getServer().getClass().getPackageName();

    NamedNmsHandles.Common resolveCommon() throws ReflectiveOperationException {
        Class<?> craftPlayer = Class.forName(craftPackage + ".entity.CraftPlayer");
        Class<?> serverPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");

        return new NamedNmsHandles.Common(
                method(craftPlayer, "getHandle"),
                method(serverPlayer, "level"));
    }

    NamedNmsHandles.Food resolveFood() throws ReflectiveOperationException {
        Class<?> craftItemStack = Class.forName(craftPackage + ".inventory.CraftItemStack");
        Class<?> nmsItemStack = Class.forName("net.minecraft.world.item.ItemStack");
        Class<?> level = Class.forName("net.minecraft.world.level.Level");
        Class<?> livingEntity = Class.forName("net.minecraft.world.entity.LivingEntity");

        return new NamedNmsHandles.Food(
                method(craftItemStack, "asNMSCopy", ItemStack.class),
                bukkitItemCopy(craftItemStack, nmsItemStack),
                method(nmsItemStack, "finishUsingItem", level, livingEntity));
    }

    NamedNmsHandles.Fishing resolveFishing() throws ReflectiveOperationException {
        Class<?> craftEntity = Class.forName(craftPackage + ".entity.CraftEntity");
        Class<?> interactionHand = Class.forName("net.minecraft.world.InteractionHand");
        Class<?> level = Class.forName("net.minecraft.world.level.Level");
        Class<?> livingEntity = Class.forName("net.minecraft.world.entity.LivingEntity");
        Class<?> player = Class.forName("net.minecraft.world.entity.player.Player");
        Class<?> nmsItemStack = Class.forName("net.minecraft.world.item.ItemStack");
        Class<?> fishingHook = Class.forName("net.minecraft.world.entity.projectile.FishingHook");

        Field fishingField = player.getField("fishing");
        if (fishingField.getType() != fishingHook) {
            throw new NoSuchFieldException("Player.fishing has an unexpected type");
        }

        return new NamedNmsHandles.Fishing(
                method(craftEntity, "getHandle"),
                method(livingEntity, "getItemInHand", interactionHand),
                method(nmsItemStack, "use", level, player, interactionHand),
                variable(fishingField),
                interactionHand.getField("MAIN_HAND").get(null),
                interactionHand.getField("OFF_HAND").get(null));
    }

    NamedNmsHandles.UseOn resolveUseOn() throws ReflectiveOperationException {
        Class<?> interactionHand = Class.forName("net.minecraft.world.InteractionHand");
        Class<?> direction = Class.forName("net.minecraft.core.Direction");
        Class<?> blockPos = Class.forName("net.minecraft.core.BlockPos");
        Class<?> vec3 = Class.forName("net.minecraft.world.phys.Vec3");
        Class<?> blockHitResult = Class.forName("net.minecraft.world.phys.BlockHitResult");
        Class<?> useOnContext = Class.forName("net.minecraft.world.item.context.UseOnContext");
        Class<?> level = Class.forName("net.minecraft.world.level.Level");
        Class<?> livingEntity = Class.forName("net.minecraft.world.entity.LivingEntity");
        Class<?> player = Class.forName("net.minecraft.world.entity.player.Player");
        Class<?> nmsItemStack = Class.forName("net.minecraft.world.item.ItemStack");

        MethodHandle contextConstructor;
        boolean extendedContextConstructor;
        try {
            contextConstructor = constructor(useOnContext, player, interactionHand, blockHitResult);
            extendedContextConstructor = false;
        } catch (NoSuchMethodException ignored) {
            contextConstructor = constructor(
                    useOnContext, level, player, interactionHand, nmsItemStack, blockHitResult);
            extendedContextConstructor = true;
        }

        return new NamedNmsHandles.UseOn(
                method(livingEntity, "getItemInHand", interactionHand),
                constructor(blockPos, int.class, int.class, int.class),
                constructor(vec3, double.class, double.class, double.class),
                constructor(blockHitResult, vec3, direction, blockPos, boolean.class),
                contextConstructor,
                extendedContextConstructor,
                method(nmsItemStack, "useOn", useOnContext),
                interactionHand.getField("MAIN_HAND").get(null),
                interactionHand.getField("OFF_HAND").get(null),
                direction.getField("DOWN").get(null),
                direction.getField("UP").get(null),
                direction.getField("NORTH").get(null),
                direction.getField("SOUTH").get(null),
                direction.getField("WEST").get(null),
                direction.getField("EAST").get(null));
    }

    private MethodHandle method(Class<?> owner, String name, Class<?>... parameters)
            throws ReflectiveOperationException {
        Method method = owner.getMethod(name, parameters);
        method.trySetAccessible();
        return lookup.unreflect(method);
    }

    private MethodHandle constructor(Class<?> owner, Class<?>... parameters)
            throws ReflectiveOperationException {
        Constructor<?> constructor = owner.getDeclaredConstructor(parameters);
        constructor.trySetAccessible();
        return lookup.unreflectConstructor(constructor);
    }

    private MethodHandle bukkitItemCopy(Class<?> craftItemStack, Class<?> nmsItemStack)
            throws ReflectiveOperationException {
        try {
            return method(craftItemStack, "asBukkitCopy", nmsItemStack);
        } catch (NoSuchMethodException ignored) {
            Class<?> itemInstance = Class.forName("net.minecraft.world.item.ItemInstance");
            if (!itemInstance.isAssignableFrom(nmsItemStack)) {
                throw new NoSuchMethodException("NMS ItemStack does not implement ItemInstance");
            }
            return method(craftItemStack, "asBukkitCopy", itemInstance);
        }
    }

    private VarHandle variable(Field field) throws IllegalAccessException {
        field.trySetAccessible();
        return lookup.unreflectVarHandle(field);
    }
}
