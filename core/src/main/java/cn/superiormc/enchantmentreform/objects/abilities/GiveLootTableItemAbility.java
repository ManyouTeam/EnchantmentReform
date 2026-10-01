package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Draws random items from a loot table and either gives them to a player or drops them.
 * <p>
 * Unlike {@link GiveItemAbility}, this ability does not build a concrete item from the
 * configuration. Instead it rolls the specified {@code loot-table} and hands the resulting
 * item(s) to the target player. This makes it reusable for any trigger (e.g. piglin bartering,
 * block breaking, entity kills) where "a random item from a loot table" is the desired reward.
 */
public final class GiveLootTableItemAbility extends AbstractAbility {

    public GiveLootTableItemAbility(ConfigurationSection section) {
        super("GiveLootTableItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        NamespacedKey tableKey = resolveLootTableKey(context);
        LootTable table = tableKey == null ? null : Bukkit.getLootTable(tableKey);
        if (table == null) {
            return false;
        }

        String delivery = getString("delivery", "INVENTORY", context).trim().toUpperCase(java.util.Locale.ROOT);
        Location location = getLocation(context);
        List<ItemStack> loot = rollLoot(context, location, table);
        if (loot.isEmpty()) {
            return false;
        }

        if (delivery.equals("DROP")) {
            return dropLoot(context, location, loot);
        }

        Entity target = getTargetEntity(context);
        if (!(target instanceof Player player)) {
            return false;
        }
        var leftovers = player.getInventory().addItem(loot.toArray(new ItemStack[0]));
        if (section.getBoolean("drop-overflow", true)) {
            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
        return false;
    }

    private boolean dropLoot(PowerContext context, Location location, List<ItemStack> loot) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        boolean naturally = section.getBoolean("naturally", true);
        for (ItemStack stack : loot) {
            Item dropped = naturally
                    ? location.getWorld().dropItemNaturally(location, stack)
                    : location.getWorld().dropItem(location, stack);
            if (section.contains("pickup-delay")) {
                dropped.setPickupDelay(Math.max(0, getInt("pickup-delay", 0, context)));
            }
        }
        return false;
    }

    private NamespacedKey resolveLootTableKey(PowerContext context) {
        String raw = getString("loot-table", "", context);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return CommonUtil.parseNamespacedKey(raw.trim());
    }

    private List<ItemStack> rollLoot(PowerContext context, Location location, LootTable table) {
        if (location == null || location.getWorld() == null) {
            Player player = context.player();
            if (player == null) {
                return List.of();
            }
            location = player.getLocation();
        }

        LootContext.Builder builder = new LootContext.Builder(location);
        Entity lootedEntity = context.target();
        if (lootedEntity != null) {
            builder.lootedEntity(lootedEntity);
        }
        double luck = getDouble("luck", 0.0D, context);
        if (luck != 0.0D) {
            builder.luck((float) luck);
        }

        Collection<ItemStack> rolled = table.populateLoot(ThreadLocalRandom.current(), builder.build());
        if (rolled.isEmpty()) {
            return List.of();
        }

        List<ItemStack> result = new java.util.ArrayList<>(rolled);
        int amount = getInt("amount", 1, context);
        if (amount > 1) {
            for (int i = 1; i < amount; i++) {
                result.addAll(table.populateLoot(ThreadLocalRandom.current(), builder.build()));
            }
        }
        return result;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.PLAYER;
    }
}
