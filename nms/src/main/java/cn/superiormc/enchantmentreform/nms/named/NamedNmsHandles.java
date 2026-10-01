package cn.superiormc.enchantmentreform.nms.named;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

final class NamedNmsHandles {

    record Common(MethodHandle craftPlayerGetHandle, MethodHandle serverPlayerLevel) {
    }

    record Food(MethodHandle asNmsCopy, MethodHandle asBukkitCopy, MethodHandle finishUsingItem) {
    }

    record Fishing(MethodHandle craftEntityGetHandle, MethodHandle getItemInHand,
                   MethodHandle useItem, VarHandle fishingHook,
                   Object mainHand, Object offHand) {
    }

    record UseOn(MethodHandle getItemInHand,
                 MethodHandle blockPosConstructor,
                 MethodHandle vec3Constructor,
                 MethodHandle blockHitResultConstructor,
                 MethodHandle useOnContextConstructor,
                 boolean extendedContextConstructor,
                 MethodHandle useOnItem,
                 Object mainHand,
                 Object offHand,
                 Object down,
                 Object up,
                 Object north,
                 Object south,
                 Object west,
                 Object east) {
    }

    private NamedNmsHandles() {
    }
}
