package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

/** Checks whether a living entity is actively blocking with a shield. */
public final class PowerConditionBlocking extends AbstractPowerCondition {

    public PowerConditionBlocking() {
        super("blocking");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        LivingEntity living = context == null ? null : context.livingEntity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        if (living == null) {
            return false;
        }
        boolean blocking = living instanceof Player player
                ? player.isBlocking()
                : living.isHandRaised() && hasShield(living.getEquipment());
        return blocking == condition.getSection().getBoolean("value", true);
    }

    private boolean hasShield(EntityEquipment equipment) {
        return equipment != null
                && (equipment.getItemInMainHand().getType() == Material.SHIELD
                || equipment.getItemInOffHand().getType() == Material.SHIELD);
    }
}
