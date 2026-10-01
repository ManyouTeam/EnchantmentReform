package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ParticleAbility extends AbstractAbility {

    public ParticleAbility(ConfigurationSection section) {
        super("Particle", section);
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

        String typeStr = getString("particle", "FLAME").toUpperCase();
        Particle particle;

        try {
            particle = Particle.valueOf(typeStr);
        } catch (Exception e) {
            return false;
        }

        int count = getInt("count", 1, context);
        double offsetX = getDouble("offset-x", 0, context);
        double offsetY = getDouble("offset-y", 0, context);
        double offsetZ = getDouble("offset-z", 0, context);
        double extra = getDouble("extra", 0, context);

        Object data = null;

        switch (particle) {
            case BLOCK:
            case FALLING_DUST:
            case DUST_PILLAR:
            case BLOCK_CRUMBLE:
            case BLOCK_MARKER:
                String blockStr = getString("block", "STONE").toUpperCase();
                Material mat = Material.matchMaterial(blockStr);
                if (mat == null || !mat.isBlock()) {
                    return false;
                }
                data = Bukkit.createBlockData(mat);
                break;
            case ITEM:
                String itemStr = getString("item", "STONE").toUpperCase();
                Material itemMat = Material.matchMaterial(itemStr);
                if (itemMat == null) {
                    return false;
                }
                data = new ItemStack(itemMat);
                break;
            case DUST:
                String colorStr = getString("color", "255,0,0");
                float size = (float) getDouble("size", 1, context);

                String[] rgb = colorStr.split(",");
                if (rgb.length != 3) {
                    return false;
                }

                Color color = Color.fromRGB(
                        Integer.parseInt(rgb[0]),
                        Integer.parseInt(rgb[1]),
                        Integer.parseInt(rgb[2])
                );

                data = new Particle.DustOptions(color, size);
                break;

            case DUST_COLOR_TRANSITION:
                String fromStr = getString("from", "255,0,0");
                String toStr = getString("to", "0,0,255");
                float transitionSize = (float) getDouble("size", 1, context);

                Color from = CommonUtil.parseColor(fromStr);
                Color to = CommonUtil.parseColor(toStr);

                data = new Particle.DustTransition(from, to, transitionSize);
                break;

            case ENTITY_EFFECT:
            case FLASH:
                data = CommonUtil.parseColor(getString("color", "255,255,255"));
                break;

            case DRAGON_BREATH:
            case SCULK_CHARGE:
                data = (float) getDouble("value", 1.0, context);
                break;

            case SHRIEK:
                data = getInt("delay", 20, context);
                break;

            case VIBRATION:
                String[] split = getString("to", "0,0,0").split(",");
                Location target = new Location(
                        loc.getWorld(),
                        Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2])
                );
                int duration = getInt("duration", 20, context);
                data = new Vibration(
                        loc,
                        new Vibration.Destination.BlockDestination(target),
                        duration
                );
                break;

            default:
                break;
        }

        if (data != null) {
            world.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra, data);
        } else {
            world.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
