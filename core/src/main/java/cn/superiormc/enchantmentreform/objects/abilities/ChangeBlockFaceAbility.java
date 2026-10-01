package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ChangeBlockFaceAbility extends AbstractAbility {

    private static final List<BlockFace> FACE_ORDER = List.of(
            BlockFace.NORTH,
            BlockFace.NORTH_NORTH_EAST,
            BlockFace.NORTH_EAST,
            BlockFace.EAST_NORTH_EAST,
            BlockFace.EAST,
            BlockFace.EAST_SOUTH_EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_SOUTH_WEST,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST_SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.WEST_NORTH_WEST,
            BlockFace.NORTH_WEST,
            BlockFace.NORTH_NORTH_WEST,
            BlockFace.UP,
            BlockFace.DOWN
    );

    private static final Set<BlockFace> ROTATABLE_FACES = Set.copyOf(FACE_ORDER.subList(0, 16));

    public ChangeBlockFaceAbility(ConfigurationSection section) {
        super("ChangeBlockFace", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Block block = context.block();
        if (block == null) {
            return executeFailure(context);
        }

        Player player = context.player();
        if (player == null && context.source() instanceof Player source) {
            player = source;
        }
        if (player != null && HookManager.hookManager != null
                && !HookManager.hookManager.getProtectionCanUse(player, block.getLocation())) {
            return executeFailure(context.withBlock(block));
        }

        BlockData changed = block.getBlockData().clone();
        boolean updated = changeFace(changed, context);
        if (!updated) {
            return executeFailure(context.withBlock(block));
        }

        block.setBlockData(changed, section.getBoolean("apply-physics", false));
        if (context.result() != null) {
            context.result().recordChangedBlocks(1);
        }
        return AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), context.withBlock(block));
    }

    private boolean changeFace(BlockData data, PowerContext context) {
        Mode mode = getMode();
        if (data instanceof Directional directional) {
            BlockFace current = directional.getFacing();
            BlockFace target = mode == Mode.NEXT
                    ? nextFace(current, directional.getFaces())
                    : getConfiguredFace(context);
            if (target == null || target == current || !directional.getFaces().contains(target)) {
                return false;
            }
            directional.setFacing(target);
            return true;
        }
        if (data instanceof Rotatable rotatable) {
            BlockFace current = rotatable.getRotation();
            BlockFace target = mode == Mode.NEXT
                    ? nextFace(current, ROTATABLE_FACES)
                    : getConfiguredFace(context);
            if (target == null || target == current || !ROTATABLE_FACES.contains(target)) {
                return false;
            }
            rotatable.setRotation(target);
            return true;
        }
        return false;
    }

    private BlockFace nextFace(BlockFace current, Set<BlockFace> supported) {
        List<BlockFace> available = FACE_ORDER.stream()
                .filter(supported::contains)
                .toList();
        if (available.isEmpty()) {
            return null;
        }
        int index = available.indexOf(current);
        return available.get(index < 0 ? 0 : (index + 1) % available.size());
    }

    private BlockFace getConfiguredFace(PowerContext context) {
        String value = getString("face", "NORTH", context)
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        try {
            return BlockFace.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Mode getMode() {
        String value = section.getString("mode", "FIXED")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        return switch (value) {
            case "NEXT", "CYCLE", "NEXT_AVAILABLE" -> Mode.NEXT;
            default -> Mode.FIXED;
        };
    }

    private boolean executeFailure(PowerContext context) {
        ConfigurationSection abilities = section.getConfigurationSection("failure-abilities");
        if (abilities == null) {
            abilities = section.getConfigurationSection("else-abilities");
        }
        return AbilityManager.abilityManager.execute(abilities, context);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private enum Mode {
        FIXED,
        NEXT
    }
}
