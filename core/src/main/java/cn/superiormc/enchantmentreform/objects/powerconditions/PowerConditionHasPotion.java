package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PotionEffectSelector;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionHasPotion extends AbstractPowerCondition {

    public PowerConditionHasPotion() {
        super("has_potion");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        LivingEntity entity = context == null ? null : context.livingEntity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        if (entity == null) {
            return false;
        }

        int requiredEffects = Math.max(1, condition.getInt(
                "require-min-effects", 1, condition.getContext()));
        return PotionEffectSelector.parse(condition.getStringList(
                        "potions", condition.player(), condition.getContext()))
                .selectEffects(entity.getActivePotionEffects(), requiredEffects)
                .size() >= requiredEffects;
    }
}
