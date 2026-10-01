package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;


public final class ScanBlocksAbility extends AbstractAbility {

    public ScanBlocksAbility(ConfigurationSection section) {
        super("ScanBlocks", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.source() instanceof Player player)) {
            return false;
        }
        Block center = context.block() == null ? player.getLocation().getBlock() : context.block();
        int radius = Math.max(1, getInt("radius", 5, context));
        boolean sameType = section.getBoolean("same-type", false);
        Material origin = center.getType();
        int count = 0, limit = Math.max(1, getInt("max-results", 256, context));
        for (int x=-radius; x<=radius && count<limit; x++) for (int y=-radius; y<=radius && count<limit; y++) for (int z=-radius; z<=radius && count<limit; z++) {
            Block block = center.getRelative(x,y,z); Material type = block.getType();
            boolean match = sameType ? type == origin : type.name().endsWith("_ORE") || type == Material.ANCIENT_DEBRIS;
            if (!match) {
                continue;
            }
            count++;
            if (section.getBoolean("highlight", true)) {
                player.spawnParticle(Particle.END_ROD, block.getLocation().add(.5,.5,.5), 1, 0, 0, 0, 0);
            }
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
