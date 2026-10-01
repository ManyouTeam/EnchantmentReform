package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public final class PowerConditionFirstAttackFromMonster extends AbstractPowerCondition {

    public PowerConditionFirstAttackFromMonster() {
        this("first_attack_from_monster");
    }

    public PowerConditionFirstAttackFromMonster(String type) {
        super(type);
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null
                || context.result() != null && context.result().cancelled()
                || !(context.source() instanceof Monster monster)) {
            return false;
        }

        Player defender = context.player();
        if (defender == null && context.target() instanceof Player target) {
            defender = target;
        }
        if (defender == null) {
            return false;
        }

        String powerId = context.power() == null ? "none" : context.power().getId();
        PowerStateStore.Key key = PowerStateStore.key(
                defender,
                powerId,
                "first-attack-from-monster",
                condition.getSection().getCurrentPath(),
                monster);
        PowerStateStore.Update update = PowerStateStore.update(
                key,
                ignored -> 1.0D,
                1.0D,
                0.0D);
        return update.previous() <= 0.0D;
    }
}
