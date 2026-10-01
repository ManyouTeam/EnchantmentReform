package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class SoundAbility extends AbstractAbility {
    
    public SoundAbility(ConfigurationSection section) {
        super("Sound", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location loc = getLocation(context);
        if (loc == null) {
            return false;
        }

        World world = loc.getWorld();
        if (world == null) {
            return false;
        }

        String typeStr = getString("sound", "ENTITY_PLAYER_LEVELUP").toUpperCase();
        Sound sound;

        try {
            sound = Sound.valueOf(typeStr);
        } catch (Exception e) {
            return false;
        }

        float volume = (float) getDouble("volume", 1, context);
        float pitch = (float) getDouble("pitch", 1, context);

        world.playSound(loc, sound, volume, pitch);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
