package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.SmeltUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerModifierModifyDrops extends AbstractPowerModifier {

    public PowerModifierModifyDrops(ConfigurationSection section) {
        super("modify_drops", section);
    }

    @Override
    public void onUnload() {
        // Recipes may change across a reload; drop the cached lookups so they are rebuilt lazily.
        SmeltUtil.clearCache();
    }

    @Override
    protected void onApply(PowerContext context) {
        if (context.event() instanceof BlockDropItemEvent event) {
            modifyBlockDrops(context, event);
        } else if (context.event() instanceof EntityDeathEvent event) {
            modifyEntityDrops(context, event);
        } else if (context.event() instanceof PlayerFishEvent event) {
            collectFishingCatch(context, event);
        }
    }

    private void collectFishingCatch(PowerContext context, PlayerFishEvent event) {
        String destination = section.getString("destination", "EVENT").toUpperCase(Locale.ROOT);
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH
                && (destination.equals("INVENTORY") || destination.equals("SOURCE"))) {
            // TriggerResult applies this after every enchantment has run, so catch replacements
            // and smelting are reflected in the item that is finally moved into the inventory.
            context.result().collectDrops(true);
        }
    }

    private void modifyBlockDrops(PowerContext context, BlockDropItemEvent event) {
        List<ItemStack> drops = new ArrayList<>();
        for (Item item : event.getItems()) {
            drops.add(item.getItemStack().clone());
        }
        List<ItemStack> transformed = transform(context, drops);
        replaceBlockDrops(event, transformed);
        String destination = section.getString("destination", "WORLD").toUpperCase(Locale.ROOT);
        if (destination.equals("INVENTORY") || destination.equals("SOURCE")) {
            context.result().collectDrops(true);
            return;
        }
        if (destination.equals("WORLD") || destination.equals("EVENT")) {
            return;
        }
        event.getItems().forEach(Item::remove);
        event.getItems().clear();
        distribute(context, event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D), transformed);
    }

    private void modifyEntityDrops(PowerContext context, EntityDeathEvent event) {
        List<ItemStack> transformed = transform(context, new ArrayList<>(event.getDrops()));
        event.getDrops().clear();
        event.getDrops().addAll(transformed);
        String destination = section.getString("destination", "EVENT").toUpperCase(Locale.ROOT);
        if (destination.equals("INVENTORY") || destination.equals("SOURCE")) {
            context.result().collectDrops(true);
            return;
        }
        if (destination.equals("EVENT") || destination.equals("WORLD")) {
            return;
        }
        event.getDrops().clear();
        distribute(context, event.getEntity().getLocation(), transformed);
    }

    private void replaceBlockDrops(BlockDropItemEvent event, List<ItemStack> drops) {
        List<Item> existing = new ArrayList<>(event.getItems());
        List<Item> replacements = new ArrayList<>();
        Location location = event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D);
        for (int index = 0; index < drops.size(); index++) {
            Item item;
            if (index < existing.size()) {
                item = existing.get(index);
                item.setItemStack(drops.get(index));
            } else {
                item = location.getWorld().dropItemNaturally(location, drops.get(index));
            }
            replacements.add(item);
        }
        for (int index = drops.size(); index < existing.size(); index++) {
            existing.get(index).remove();
        }
        event.getItems().clear();
        event.getItems().addAll(replacements);
    }

    private List<ItemStack> transform(PowerContext context, List<ItemStack> drops) {
        if (section.getBoolean("clear", false)) {
            return List.of();
        }
        double multiplier = Math.max(0.0D, getDouble("multiplier", 1.0D, context));
        boolean smelt = section.getBoolean("smelt", false);
        List<ItemStack> result = new ArrayList<>();
        String replacement = section.getString("replacement", "").toUpperCase(Locale.ROOT);
        if (!replacement.isBlank()) {
            result.addAll(replacements(context, replacement));
            if (!section.getBoolean("append", false)) {
                return result;
            }
        }
        for (ItemStack original : drops) {
            if (original == null || original.getType().isAir()) {
                continue;
            }
            ItemStack transformed = smelt ? SmeltUtil.smelt(original) : original.clone();
            int totalAmount = Math.max(0, (int) Math.round(transformed.getAmount() * multiplier));
            split(result, transformed, totalAmount);
        }
        result.addAll(buildConfiguredItems(context, "bonus-items",
                Math.max(1, getInt("bonus-amount", 1, context))));
        return result;
    }

    private List<ItemStack> replacements(PowerContext context, String replacement) {
        int amount = Math.max(1, getInt("replacement-amount", 1, context));
        if (replacement.equals("RANDOM_CONFIGURED")) {
            return buildConfiguredItems(context, "replacement-items", amount);
        }
        Material material;
        if (replacement.equals("EVENT_BLOCK") && context.block() != null) {
            material = context.block().getType();
        } else if (replacement.equals("MATCHING_PLANKS") && context.block() != null) {
            String name = context.block().getType().name().replace("_WOOD", "_PLANKS")
                    .replace("_LOG", "_PLANKS").replace("_STEM", "_PLANKS");
            material = Material.matchMaterial(name);
        } else {
            material = Material.matchMaterial(replacement);
        }
        if (material == null || !material.isItem()) {
            return List.of();
        }
        List<ItemStack> result = new ArrayList<>();
        split(result, new ItemStack(material), amount);
        return result;
    }

    private List<ItemStack> buildConfiguredItems(PowerContext context, String path, int amount) {
        List<WeightedItem> items = getItems(context, path);
        if (items.isEmpty() || amount <= 0) {
            return List.of();
        }
        double weightScale = items.stream().mapToDouble(WeightedItem::weight).max().orElse(1.0D);
        double totalWeight = items.stream().mapToDouble(item -> item.weight() / weightScale).sum();
        List<ItemStack> result = new ArrayList<>();
        for (int index = 0; index < amount; index++) {
            WeightedItem selected = selectItem(items, weightScale, totalWeight);
            ItemStack item = BuildItem.buildItemStack(getBuildPlayer(context), selected.section());
            if (item != null && !item.getType().isAir()) {
                split(result, item, item.getAmount());
            }
        }
        return result;
    }

    private List<WeightedItem> getItems(PowerContext context, String path) {
        ConfigurationSection items = section.getConfigurationSection(path);
        if (items == null) {
            return List.of();
        }
        return items.getKeys(false).stream()
                .map(key -> {
                    ConfigurationSection item = items.getConfigurationSection(key);
                    double weight = getDouble(path + "." + key + ".rate", 1.0D, context);
                    return new WeightedItem(item, weight);
                })
                .filter(item -> item.section() != null
                        && Double.isFinite(item.weight())
                        && item.weight() > 0.0D)
                .toList();
    }

    private WeightedItem selectItem(List<WeightedItem> items, double weightScale, double totalWeight) {
        double value = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (WeightedItem item : items) {
            value -= item.weight() / weightScale;
            if (value < 0.0D) {
                return item;
            }
        }
        return items.getLast();
    }

    private Player getBuildPlayer(PowerContext context) {
        if (context.player() != null) {
            return context.player();
        }
        if (context.source() instanceof Player player) {
            return player;
        }
        return context.target() instanceof Player player ? player : null;
    }

    private void split(List<ItemStack> output, ItemStack template, int totalAmount) {
        int remaining = totalAmount;
        while (remaining > 0) {
            ItemStack stack = template.clone();
            int amount = Math.min(stack.getMaxStackSize(), remaining);
            stack.setAmount(amount);
            output.add(stack);
            remaining -= amount;
        }
    }

    private void distribute(PowerContext context, Location location, List<ItemStack> drops) {
        if (location.getWorld() == null || drops.isEmpty()) {
            return;
        }
        String destination = section.getString("destination", "WORLD").toUpperCase(Locale.ROOT);
        Player player = selectPlayer(context, destination);
        if (destination.equals("ENDER_CHEST") && context.source() instanceof Player owner) {
            for (ItemStack drop : drops) {
                HashMap<Integer, ItemStack> leftovers = owner.getEnderChest().addItem(drop);
                for (ItemStack leftover : leftovers.values()) {
                    location.getWorld().dropItemNaturally(location, leftover);
                }
            }
            return;
        }
        if (player == null) {
            for (ItemStack drop : drops) {
                location.getWorld().dropItemNaturally(location, drop);
            }
            return;
        }
        for (ItemStack drop : drops) {
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(drop);
            for (Map.Entry<Integer, ItemStack> entry : leftovers.entrySet()) {
                location.getWorld().dropItemNaturally(location, entry.getValue());
            }
        }
    }

    private Player selectPlayer(PowerContext context, String destination) {
        Entity entity;
        if (destination.equals("TARGET")) {
            entity = context.target();
        } else if (destination.equals("SOURCE") || destination.equals("INVENTORY")) {
            entity = context.source();
        } else {
            return null;
        }
        return entity instanceof Player player ? player : null;
    }

    private record WeightedItem(ConfigurationSection section, double weight) {
    }
}
