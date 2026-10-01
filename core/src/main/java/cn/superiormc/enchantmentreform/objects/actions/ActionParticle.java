package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ActionParticle<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionParticle() {
        super("particle");
        setRequiredArgs("particle", "count", "offset-x", "offset-y", "offset-z", "speed");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        Location loc = player.getLocation().add(0, 1, 0);
        String particleName = singleAction.getString("particle", player, context);
        int count = singleAction.getInt("count");
        double offsetX = singleAction.getDouble("offset-x", player, context);
        double offsetY = singleAction.getDouble("offset-y", player, context);
        double offsetZ = singleAction.getDouble("offset-z", player, context);
        double speed = singleAction.getDouble("speed", player, context);
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            SchedulerUtil.runSync(loc, () -> loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed));
        } catch (IllegalArgumentException e) {
            ErrorManager.errorManager.sendErrorMessage("§cInvalid particle name: " + particleName);
        }
    }
}
