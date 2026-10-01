package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.VirtualGuardianBeam;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class GuardianBeamAbility extends AbstractAbility {

    public GuardianBeamAbility(ConfigurationSection section) {
        super("GuardianBeam", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!CommonUtil.checkPluginLoad("packetevents")) {
            return false;
        }
        Entity source = context.source();
        Entity target = getTargetEntity(context);
        if (!(source instanceof LivingEntity caster) || !(target instanceof LivingEntity victim)) {
            return false;
        }

        double range = getDouble("range", 18.0, context);
        if (!caster.getWorld().equals(victim.getWorld())
                || caster.getLocation().distanceSquared(victim.getLocation()) > range * range) {
            return false;
        }

        int chargeTicks = Math.max(1, getInt("charge-ticks", 30, context));
        double damage = Math.max(0.0, getDouble("damage", 6.0, context));
        Entity damageSource = getSourceEntity(context);
        VirtualGuardianBeam.start(caster, victim, chargeTicks, range,
                () -> SchedulerUtil.runSync(caster, () -> {
                    if (!caster.isValid() || !victim.isValid() || victim.isDead()) {
                        return;
                    }
                    if (!caster.getWorld().equals(victim.getWorld())) {
                        return;
                    }
                    if (caster.getLocation().distanceSquared(victim.getLocation()) > range * range) {
                        return;
                    }

                    AbilityDamageUtil.runDirectDamage(() -> victim.damage(damage, damageSource));
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1f, 1f);
                }));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
