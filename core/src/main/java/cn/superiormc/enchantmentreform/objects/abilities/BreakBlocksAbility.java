package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.hooks.BlockPriceUtil;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public final class BreakBlocksAbility extends AbstractAbility {

    private static final int DEFAULT_MAX_RADIUS = 16;

    private static final Set<Material> DEFAULT_BLACKLIST = Set.of(
            Material.BEDROCK,
            Material.BARRIER,
            Material.END_PORTAL_FRAME,
            Material.REINFORCED_DEEPSLATE,
            Material.SPAWNER,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST);

    public BreakBlocksAbility(ConfigurationSection section) {
        super("BreakBlocks", section);
    }

    @Override
    public boolean breakOtherBlock() {
        return true;
    }

    @Override
    public boolean execute(PowerContext context) {
        if (context.result() != null) {
            context.result().recordChangedBlocks(0);
        }

        Player breaker = getBreaker(context);
        Block origin = getOrigin(context);
        if (breaker == null || origin == null) {
            return false;
        }
        if (section.getBoolean("require-sneaking", false) && !breaker.isSneaking()) {
            return false;
        }
        if (section.getBoolean("disable-when-sneaking", false) && breaker.isSneaking()) {
            return false;
        }

        List<Block> selected = selectBlocks(context, breaker, origin);
        if (selected.isEmpty()) {
            return false;
        }

        breakInBatches(context, breaker, selected);
        return false;
    }

    private Player getBreaker(PowerContext context) {
        String selector = section.getString("breaker", "SOURCE").toUpperCase(Locale.ROOT);
        Entity entity;
        if (selector.equals("TARGET")) {
            entity = context.target();
        } else if (selector.equals("SKILL")) {
            entity = context.skill();
        } else {
            entity = context.source();
        }
        return entity instanceof Player player ? player : null;
    }

    private Block getOrigin(PowerContext context) {
        String selector = section.getString("center", "EVENT_BLOCK").toUpperCase(Locale.ROOT);
        if (selector.equals("EVENT_BLOCK") && context.block() != null) {
            return context.block();
        }

        Entity entity;
        if (selector.equals("TARGET")) {
            entity = context.target();
        } else if (selector.equals("SKILL")) {
            entity = context.skill();
        } else {
            entity = context.source();
        }
        if (entity != null) {
            return entity.getLocation().getBlock();
        }

        Location location = getLocation(context);
        return location == null || location.getWorld() == null ? null : location.getBlock();
    }

    private List<Block> selectBlocks(PowerContext context, Player breaker, Block origin) {
        String shape = section.getString("shape", "CUBE")
                .toUpperCase(Locale.ROOT)
                .replace('-', '_');
        List<Block> candidates = switch (shape) {
            case "VEIN" -> selectVein(context, origin);
            case "PLANE" -> selectPlane(context, breaker, origin);
            case "LAYER" -> selectLayer(context, origin);
            case "TUNNEL", "LINE" -> selectTunnel(context, breaker, origin);
            case "SPHERE" -> selectSphere(context, origin);
            default -> selectCube(context, origin);
        };
        return filterCandidates(context, breaker, origin, candidates);
    }

    private List<Block> selectCube(PowerContext context, Block origin) {
        int radius = getRadius(context, "radius", 1);
        int radiusX = getBoundedInt(context, "radius-x", radius);
        int radiusY = getBoundedInt(context, "radius-y", radius);
        int radiusZ = getBoundedInt(context, "radius-z", radius);
        List<Block> result = new ArrayList<>();
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    result.add(origin.getRelative(x, y, z));
                }
            }
        }
        return result;
    }

    private List<Block> selectSphere(PowerContext context, Block origin) {
        int radius = getRadius(context, "radius", 1);
        double radiusSquared = radius * radius;
        List<Block> result = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radiusSquared) {
                        result.add(origin.getRelative(x, y, z));
                    }
                }
            }
        }
        return result;
    }

    private List<Block> selectLayer(PowerContext context, Block origin) {
        int radius = getRadius(context, "radius", 1);
        int verticalRadius = getBoundedInt(context, "vertical-radius", 0);
        List<Block> result = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -verticalRadius; y <= verticalRadius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    result.add(origin.getRelative(x, y, z));
                }
            }
        }
        return result;
    }

    private List<Block> selectPlane(PowerContext context, Player player, Block origin) {
        int radius = getRadius(context, "radius", 1);
        int depth = getBoundedInt(context, "depth", 0);
        Axis axis = dominantAxis(player);
        List<Block> result = new ArrayList<>();
        for (int first = -radius; first <= radius; first++) {
            for (int second = -radius; second <= radius; second++) {
                for (int along = -depth; along <= depth; along++) {
                    if (axis == Axis.X) {
                        result.add(origin.getRelative(along, first, second));
                    } else if (axis == Axis.Y) {
                        result.add(origin.getRelative(first, along, second));
                    } else {
                        result.add(origin.getRelative(first, second, along));
                    }
                }
            }
        }
        return result;
    }

    private List<Block> selectTunnel(PowerContext context, Player player, Block origin) {
        int length = Math.max(1, getInt("length", 3, context));
        length = Math.min(length, DEFAULT_MAX_RADIUS * 2);
        int radius = getRadius(context, "radius", 0);
        int stepX = Integer.signum((int) Math.round(player.getEyeLocation().getDirection().getX()));
        int stepY = Integer.signum((int) Math.round(player.getEyeLocation().getDirection().getY()));
        int stepZ = Integer.signum((int) Math.round(player.getEyeLocation().getDirection().getZ()));
        Axis axis = dominantAxis(player);
        if (axis == Axis.X) {
            stepY = 0;
            stepZ = 0;
        } else if (axis == Axis.Y) {
            stepX = 0;
            stepZ = 0;
        } else {
            stepX = 0;
            stepY = 0;
        }

        List<Block> result = new ArrayList<>();
        for (int distance = 0; distance < length; distance++) {
            Block center = origin.getRelative(stepX * distance, stepY * distance, stepZ * distance);
            addTunnelCrossSection(result, center, axis, radius);
        }
        return result;
    }

    private void addTunnelCrossSection(List<Block> result, Block center, Axis axis, int radius) {
        for (int first = -radius; first <= radius; first++) {
            for (int second = -radius; second <= radius; second++) {
                if (axis == Axis.X) {
                    result.add(center.getRelative(0, first, second));
                } else if (axis == Axis.Y) {
                    result.add(center.getRelative(first, 0, second));
                } else {
                    result.add(center.getRelative(first, second, 0));
                }
            }
        }
    }

    private List<Block> selectVein(PowerContext context, Block origin) {
        int maximum = Math.max(1, getInt("max-blocks", 64, context));
        maximum = Math.min(maximum, getGlobalBlockLimit());
        boolean diagonal = section.getBoolean("diagonal", true);
        Material originType = origin.getType();
        Queue<Block> pending = new ArrayDeque<>();
        Set<BlockKey> visited = new HashSet<>();
        List<Block> result = new ArrayList<>();
        pending.add(origin);
        visited.add(BlockKey.of(origin));

        while (!pending.isEmpty() && result.size() < maximum) {
            Block block = pending.remove();
            if (block.getType() != originType) {
                continue;
            }
            result.add(block);
            addVeinNeighbours(block, pending, visited, diagonal);
        }
        return result;
    }

    private void addVeinNeighbours(Block block,
                                   Queue<Block> pending,
                                   Set<BlockKey> visited,
                                   boolean diagonal) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
                    if (distance == 0 || !diagonal && distance != 1) {
                        continue;
                    }
                    Block next = block.getRelative(x, y, z);
                    if (visited.add(BlockKey.of(next))) {
                        pending.add(next);
                    }
                }
            }
        }
    }

    private List<Block> filterCandidates(PowerContext context,
                                         Player breaker,
                                         Block origin,
                                         List<Block> candidates) {
        boolean includeOrigin = section.getBoolean("include-origin", false);
        boolean sameType = section.getBoolean("same-type", false);
        boolean loadChunks = section.getBoolean("load-chunks", false);
        int maximum = Math.max(1, getInt("max-blocks", getGlobalBlockLimit(), context));
        maximum = Math.min(maximum, getGlobalBlockLimit());
        List<String> whitelist = section.getStringList("whitelist");
        List<String> blacklist = section.getStringList("blacklist");
        boolean useDefaultBlacklist = section.getBoolean("use-default-blacklist", true);
        LinkedHashSet<BlockKey> acceptedKeys = new LinkedHashSet<>();
        List<Block> accepted = new ArrayList<>();

        for (Block block : candidates) {
            if (accepted.size() >= maximum) {
                break;
            }
            if (!includeOrigin && sameBlock(block, origin)) {
                continue;
            }
            if (!loadChunks && !block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                continue;
            }
            Material material = block.getType();
            if (material.isAir() || !material.isBlock()) {
                continue;
            }
            if (sameType && material != origin.getType()) {
                continue;
            }
            if (useDefaultBlacklist && DEFAULT_BLACKLIST.contains(material)) {
                continue;
            }
            if (BlockPriceUtil.matchesAny(block, blacklist)) {
                continue;
            }
            if (!whitelist.isEmpty() && !BlockPriceUtil.matchesAny(block, whitelist)) {
                continue;
            }
            if (!HookManager.hookManager.getProtectionCanBreak(breaker, block.getLocation())) {
                continue;
            }
            BlockKey key = BlockKey.of(block);
            if (acceptedKeys.add(key)) {
                accepted.add(block);
            }
        }
        return accepted;
    }

    private int getRadius(PowerContext context, String path, int defaultValue) {
        if (section.contains("size") && path.equals("radius")) {
            int size = Math.max(1, getInt("size", defaultValue * 2 + 1, context));
            return Math.min(DEFAULT_MAX_RADIUS, Math.max(0, (size - 1) / 2));
        }
        return getBoundedInt(context, path, defaultValue);
    }

    private int getBoundedInt(PowerContext context, String path, int defaultValue) {
        int value = Math.max(0, getInt(path, defaultValue, context));
        return Math.min(DEFAULT_MAX_RADIUS, value);
    }

    private int getGlobalBlockLimit() {
        if (EnchantmentReform.instance == null) {
            return 512;
        }
        return Math.max(1, EnchantmentReform.instance.getConfig().getInt(
                "powers.break-block.max-blocks-per-activation", 512));
    }

    private void breakInBatches(PowerContext context, Player breaker, List<Block> blocks) {
        int blocksPerTick = Math.max(1, section.getInt("blocks-per-tick", 16));
        List<Block> snapshot = List.copyOf(blocks);
        ItemStack tool = context.contextItem().clone();
        BreakMode mode = getMode();

        if (EnchantmentReform.isFolia && mode == BreakMode.NATURAL) {
            breakNaturallyInFoliaRegions(context, breaker, snapshot, tool, blocksPerTick);
            return;
        }

        int[] index = {0};
        SchedulerUtil[] task = new SchedulerUtil[1];
        task[0] = SchedulerUtil.runTaskTimer(breaker, () -> {
            if (!breaker.isOnline()) {
                task[0].cancel();
                return;
            }
            int end = Math.min(index[0] + blocksPerTick, snapshot.size());
            while (index[0] < end) {
                Block block = snapshot.get(index[0]);
                if (breakBlock(context, mode, breaker, block, tool)) {
                    executeBlockAbilities(context, block);
                }
                index[0]++;
            }
            if (index[0] >= snapshot.size()) {
                task[0].cancel();
            }
        }, 1L, 1L);
    }

    private void breakNaturallyInFoliaRegions(PowerContext context,
                                              Player breaker,
                                              List<Block> blocks,
                                              ItemStack tool,
                                              int blocksPerTick) {
        for (int index = 0; index < blocks.size(); index++) {
            Block block = blocks.get(index);
            long delay = 1L + index / blocksPerTick;
            SchedulerUtil.runTaskLater(block.getLocation(), () -> {
                if (!breakNaturally(context, breaker, block, tool)) {
                    return;
                }
                PowerContext blockContext = context.withBlock(block);
                SchedulerUtil.runSync(breaker, () -> executeBlockAbilities(blockContext));
            }, delay);
        }
    }

    private boolean breakBlock(PowerContext context,
                               BreakMode mode,
                               Player breaker,
                               Block block,
                               ItemStack tool) {
        if (block.getType().isAir()) {
            return false;
        }
        if (!HookManager.hookManager.getProtectionCanBreak(breaker, block.getLocation())) {
            return false;
        }

        PowerContext blockContext = context.withBlock(block);
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection("before-abilities"), blockContext);

        if (mode == BreakMode.PLAYER) {
            return breakAsPlayer(breaker, block);
        }
        return block.breakNaturally(tool.clone());
    }

    private boolean breakNaturally(PowerContext context,
                                   Player breaker,
                                   Block block,
                                   ItemStack tool) {
        return breakBlock(context, BreakMode.NATURAL, breaker, block, tool);
    }

    private boolean breakAsPlayer(Player breaker, Block block) {
        if (PowerManager.powerManager != null) {
            PowerManager.powerManager.markInternalBlockBreak(block);
        }
        boolean broken = breaker.breakBlock(block);
        if (!broken && PowerManager.powerManager != null) {
            PowerManager.powerManager.clearInternalBlockBreak(block);
        }
        return broken;
    }

    private BreakMode getMode() {
        String value = section.getString("mode", "NATURAL")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
        return switch (value) {
            case "PLAYER", "PLAYER_BREAK", "PLAYER_BREAK_BLOCK", "BREAK_BLOCK" -> BreakMode.PLAYER;
            case "NATURAL", "BLOCK", "BREAK_NATURALLY", "BLOCK_BREAK_NATURALLY" -> BreakMode.NATURAL;
            default -> BreakMode.NATURAL;
        };
    }

    private void executeBlockAbilities(PowerContext context, Block block) {
        executeBlockAbilities(context.withBlock(block));
    }

    private void executeBlockAbilities(PowerContext context) {
        if (context.result() != null) {
            context.result().recordChangedBlocks(1);
        }
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), context);
    }

    private Axis dominantAxis(Player player) {
        double x = Math.abs(player.getEyeLocation().getDirection().getX());
        double y = Math.abs(player.getEyeLocation().getDirection().getY());
        double z = Math.abs(player.getEyeLocation().getDirection().getZ());
        if (x >= y && x >= z) {
            return Axis.X;
        }
        if (y >= x && y >= z) {
            return Axis.Y;
        }
        return Axis.Z;
    }

    private boolean sameBlock(Block first, Block second) {
        return first.getWorld().equals(second.getWorld())
                && first.getX() == second.getX()
                && first.getY() == second.getY()
                && first.getZ() == second.getZ();
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private enum BreakMode {
        PLAYER,
        NATURAL
    }

    private enum Axis {
        X,
        Y,
        Z
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {

        private static BlockKey of(Block block) {
            World world = block.getWorld();
            return new BlockKey(world.getUID(), block.getX(), block.getY(), block.getZ());
        }
    }
}
