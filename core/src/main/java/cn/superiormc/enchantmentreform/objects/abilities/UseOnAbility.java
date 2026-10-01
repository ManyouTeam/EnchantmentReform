package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.nms.UseOnNmsBridge;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Locale;

public final class UseOnAbility extends AbstractAbility {

    private final NmsBridge nmsBridge;

    public UseOnAbility(ConfigurationSection section) {
        this(section, EnchantmentReform.instance == null
                ? new UnsupportedNmsBridge("Plugin is not enabled")
                : EnchantmentReform.instance.getNmsBridge());
    }

    public UseOnAbility(ConfigurationSection section, NmsBridge nmsBridge) {
        super("UseOn", section);
        this.nmsBridge = nmsBridge;
    }

    @Override
    public boolean execute(PowerContext context) {
        Player player = context.player(EntitySelector.parse(
                section.getString("player"), EntitySelector.SOURCE));
        EquipmentSlot hand = resolveHand(context);
        Block block = resolveBlock(context);
        BlockFace face = resolveFace();

        boolean successful = false;
        if (player != null && hand != null && block != null && face != null
                && nmsBridge instanceof UseOnNmsBridge useOnBridge) {
            Vector hitPosition = resolveHitPosition(context, block);
            ConfigurationSection inventoryItems = inventoryItems();
            successful = inventoryItems == null
                    ? useOnBridge.useOn(player, hand, block, face, hitPosition,
                    section.getBoolean("inside", false))
                    : useMatchedInventoryItem(context, player, hand, block, face,
                    hitPosition, inventoryItems, useOnBridge);
        }

        ConfigurationSection abilities = section.getConfigurationSection(
                successful ? "success-abilities" : "failure-abilities");
        return AbilityManager.abilityManager.execute(
                abilities, block == null ? context : context.withBlock(block));
    }

    private boolean useMatchedInventoryItem(PowerContext context,
                                            Player player,
                                            EquipmentSlot hand,
                                            Block block,
                                            BlockFace face,
                                            Vector hitPosition,
                                            ConfigurationSection rules,
                                            UseOnNmsBridge useOnBridge) {
        MatchItemManager manager = MatchItemManager.matchItemManager;
        if (manager == null) {
            return false;
        }

        ItemStack currentHand = handItem(player, hand);
        if (manager.getMatch(rules, player, currentHand, context)) {
            return useOnBridge.useOn(player, hand, block, face, hitPosition,
                    section.getBoolean("inside", false));
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack[] storage = inventory.getStorageContents();
        int heldSlot = inventory.getHeldItemSlot();
        for (int slot = 0; slot < storage.length; slot++) {
            if (hand == EquipmentSlot.HAND && slot == heldSlot) {
                continue;
            }
            ItemStack candidate = storage[slot];
            if (!manager.getMatch(rules, player, candidate, context)) {
                continue;
            }
            return useInventorySlot(player, hand, slot, candidate, block, face,
                    hitPosition, useOnBridge);
        }
        return false;
    }

    private boolean useInventorySlot(Player player,
                                     EquipmentSlot hand,
                                     int inventorySlot,
                                     ItemStack candidate,
                                     Block block,
                                     BlockFace face,
                                     Vector hitPosition,
                                     UseOnNmsBridge useOnBridge) {
        PlayerInventory inventory = player.getInventory();
        ItemStack originalHand = copy(handItem(player, hand));
        inventory.setItem(inventorySlot, null);
        setHandItem(player, hand, candidate.clone());

        try {
            return useOnBridge.useOn(player, hand, block, face, hitPosition,
                    section.getBoolean("inside", false));
        } finally {
            ItemStack remaining = copy(handItem(player, hand));
            setHandItem(player, hand, originalHand);
            inventory.setItem(inventorySlot, remaining);
            player.updateInventory();
        }
    }

    private ConfigurationSection inventoryItems() {
        ConfigurationSection rules = section.getConfigurationSection("inventory-items");
        return rules == null ? section.getConfigurationSection("inventory-item") : rules;
    }

    private ItemStack handItem(Player player, EquipmentSlot hand) {
        return hand == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
    }

    private void setHandItem(Player player, EquipmentSlot hand, ItemStack item) {
        ItemStack value = item == null ? new ItemStack(Material.AIR) : item;
        if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(value);
        } else {
            player.getInventory().setItemInMainHand(value);
        }
    }

    private ItemStack copy(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() <= 0
                ? null : item.clone();
    }

    private EquipmentSlot resolveHand(PowerContext context) {
        String value = section.getString("hand", "CONTEXT")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_');
        EquipmentSlot slot = switch (value) {
            case "CONTEXT" -> context.contextSlot();
            case "TRIGGER", "TRIGGER_ITEM" -> context.triggerData() == null
                    ? null : context.triggerData().triggerItemSlot();
            case "HAND", "MAINHAND", "MAIN_HAND" -> EquipmentSlot.HAND;
            case "OFFHAND", "OFF_HAND" -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
        return slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND ? slot : null;
    }

    private Block resolveBlock(PowerContext context) {
        Block block = context.block();
        if (block == null) {
            Location location = getLocation(context);
            if (location == null || location.getWorld() == null) {
                return null;
            }
            block = location.getBlock();
        }
        int offsetX = getInt("block-offset.x", 0, context);
        int offsetY = getInt("block-offset.y", 0, context);
        int offsetZ = getInt("block-offset.z", 0, context);
        return block.getRelative(offsetX, offsetY, offsetZ);
    }

    private BlockFace resolveFace() {
        String value = section.getString("face", "UP")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_');
        value = switch (value) {
            case "TOP" -> "UP";
            case "BOTTOM" -> "DOWN";
            default -> value;
        };
        try {
            BlockFace face = BlockFace.valueOf(value);
            return switch (face) {
                case UP, DOWN, NORTH, SOUTH, EAST, WEST -> face;
                default -> null;
            };
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Vector resolveHitPosition(PowerContext context, Block block) {
        double x = clamp(getDouble("hit.x", 0.5D, context));
        double y = clamp(getDouble("hit.y", 0.5D, context));
        double z = clamp(getDouble("hit.z", 0.5D, context));
        return new Vector(block.getX() + x, block.getY() + y, block.getZ() + z);
    }

    private double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
