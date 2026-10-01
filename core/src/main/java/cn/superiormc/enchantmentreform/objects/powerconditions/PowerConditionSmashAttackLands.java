package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.event.Event;

import java.lang.reflect.InvocationTargetException;

public final class PowerConditionSmashAttackLands extends AbstractPowerCondition {

    private static final String EVENT_CLASS =
            "io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent";

    public PowerConditionSmashAttackLands() {
        super("smash_attack_lands");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Event event = context == null ? null : context.event();
        if (event == null || !EVENT_CLASS.equals(event.getClass().getName())) {
            return false;
        }

        boolean expected = condition.getSection().getBoolean("value", true);
        try {
            Event.Result result = (Event.Result) event.getClass()
                    .getMethod("getResult").invoke(event);
            boolean originalResult = (boolean) event.getClass()
                    .getMethod("getOriginalResult").invoke(event);
            boolean actual = switch (result) {
                case ALLOW -> true;
                case DENY -> false;
                case DEFAULT -> originalResult;
            };
            return actual == expected;
        } catch (NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | ClassCastException exception) {
            return false;
        }
    }
}
