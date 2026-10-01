package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Player;

public final class PowerConditionMinAttackCooldown extends AbstractPowerCondition {

    public PowerConditionMinAttackCooldown() {
        super("min_attack_cooldown");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Player player = context == null ? null : context.player(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        if (player == null) {
            return false;
        }
        double cooldown = context.triggerData() == null ? player.getAttackCooldown()
                : context.triggerData().extra(BuiltinContextKeys.ATTACK_COOLDOWN)
                .orElse((double) player.getAttackCooldown());
        return cooldown >= condition.getSection().getDouble("value");
    }
}
