package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class PowerConditionFirstAttackAgainstEntity extends AbstractPowerCondition {

    public PowerConditionFirstAttackAgainstEntity() {
        super("first_attack_against_entity");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null
                || context.result() != null && context.result().cancelled()
                || !(context.source() instanceof Player attacker)
                || !(context.target() instanceof LivingEntity target)) {
            return false;
        }

        String powerId = context.power() == null ? "none" : context.power().getId();
        PowerStateStore.Key key = PowerStateStore.key(
                attacker,
                powerId,
                "first-attack-against-entity",
                condition.getSection().getCurrentPath(),
                target);
        PowerStateStore.Update update = PowerStateStore.update(
                key,
                ignored -> 1.0D,
                1.0D,
                0.0D);
        return update.previous() <= 0.0D;
    }
}