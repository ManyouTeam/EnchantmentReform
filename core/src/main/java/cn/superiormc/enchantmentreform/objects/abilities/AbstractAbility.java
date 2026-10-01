package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.ItemSelector;
import cn.superiormc.enchantmentreform.api.trigger.LocationSelector;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public abstract class AbstractAbility extends AbstractConfiguredSection<PowerContext> {

    public AbstractAbility(String type, ConfigurationSection section) {
        super(type, section);
    }

    public abstract boolean execute(PowerContext context);

    public abstract TargetEntityType getDefaultTargetEntityType();

    public boolean breakOtherBlock() {
        return false;
    }

    public void onUnload() {
    }

    public void onEntityUnload(UUID entityId) {
    }

    public void onPowerSourceDeactivate(Player player, ActivePowerSource source) {
    }

    protected Entity getTargetEntity(PowerContext context) {
        EntitySelector fallback = EntitySelector.valueOf(getDefaultTargetEntityType().name());
        EntitySelector selector = EntitySelector.parse(section.getString("target"), fallback);
        return context.entity(selector);
    }

    protected Entity getSourceEntity(PowerContext context) {
        EntitySelector selector = EntitySelector.parse(section.getString("source"), EntitySelector.SOURCE);
        return context.entity(selector);
    }

    public boolean shouldExecute(PowerContext context) {
        if (!matchesContext(context)) {
            return false;
        }
        Entity source = context.source();
        if (source == null) {
            return true;
        }

        PowerStateStore.Key timesKey = PowerStateStore.key(
                context, "ability-times", section.getCurrentPath(), false);
        PowerStateStore.Key cooldownKey = PowerStateStore.key(
                context, "ability-cooldown", section.getCurrentPath(), false);

        int maxTimes = Math.max(0, getInt("times", getInt("limit.times", 0, context), context));
        if (maxTimes > 0 && PowerStateStore.get(timesKey) >= maxTimes) {
            return false;
        }

        double remainingCooldown = PowerStateStore.getRemainingSeconds(cooldownKey);
        if (remainingCooldown > 0.0D) {
            sendCooldownMessage(context, remainingCooldown);
            return false;
        }

        double randomChance = getDouble("random", getDouble("limit.random", 1.0, context), context);
        if (randomChance < 1.0 && java.util.concurrent.ThreadLocalRandom.current().nextDouble() > randomChance) {
            return false;
        }

        double cooldownSeconds = getDouble("cooldown", getDouble("limit.cooldown", 0.0, context), context);
        if (cooldownSeconds > 0.0D
                && !PowerStateStore.tryAcquireCooldown(cooldownKey, cooldownSeconds)) {
            sendCooldownMessage(context, PowerStateStore.getRemainingSeconds(cooldownKey));
            return false;
        }

        if (maxTimes > 0) {
            PowerStateStore.update(timesKey, value -> value + 1.0D, maxTimes, 0.0D);
        }

        return true;
    }

    private void sendCooldownMessage(PowerContext context, double remainingSeconds) {
        if (!section.contains("cooldown-message") || remainingSeconds <= 0.0D) {
            return;
        }
        Player player = context.player();
        if (player == null && context.source() instanceof Player sourcePlayer) {
            player = sourcePlayer;
        }
        if (player == null) {
            return;
        }

        String powerId = context.power() == null ? "none" : context.power().getId();
        PowerStateStore.Key messageKey = PowerStateStore.key(
                player, powerId, "ability-cooldown-message", section.getCurrentPath(), null);
        if (!PowerStateStore.tryAcquireCooldown(messageKey, 1.0D)) {
            return;
        }

        double rounded = Math.ceil(remainingSeconds * 10.0D) / 10.0D;
        String remaining = rounded == Math.rint(rounded)
                ? Long.toString((long) rounded)
                : String.format(Locale.ROOT, "%.1f", rounded);
        String message = getString("cooldown-message", "", context,
                "remaining", remaining,
                "power_id", powerId);
        TextUtil.sendMessage(player, message);
    }

    private boolean matchesContext(PowerContext context) {
        return PowerConditionsManager.powerConditions.matches(
                section.getConfigurationSection("conditions"), context);
    }

    protected Location getLocation(PowerContext context) {
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        String rawSelector = locationSection == null
                ? section.getString("location", "CONTEXT")
                : locationSection.getString("target", "CONTEXT");
        LocationSelector selector = LocationSelector.parse(rawSelector, LocationSelector.CONTEXT);
        Location location = resolveLocation(context, selector,
                locationSection == null ? getDouble("distance", 1.0D, context)
                        : getDouble("location.distance", 1.0D, context));
        if (location == null) {
            return null;
        }
        String offset = locationSection == null ? "offset" : "location.offset";
        return location.clone().add(
                getLocationOffset(context, offset, "x"),
                getLocationOffset(context, offset, "y"),
                getLocationOffset(context, offset, "z"));
    }

    private double getLocationOffset(PowerContext context, String offset, String axis) {
        String path = offset + "." + axis;
        return getDouble(section.contains(path) ? path : "location.offset-" + axis, 0.0D, context);
    }

    protected List<ItemStack> getItems(PowerContext context) {
        ConfigurationSection itemSection = section.getConfigurationSection("item");
        String rawSelector = itemSection == null
                ? section.getString("item", "CONTEXT")
                : itemSection.getString("selector", "CONTEXT");
        String rawHolder = itemSection == null
                ? section.getString("item-holder", getDefaultTargetEntityType().name())
                : itemSection.getString("holder", getDefaultTargetEntityType().name());
        return resolveItems(context,
                ItemSelector.parse(rawSelector, ItemSelector.CONTEXT),
                EntitySelector.parse(rawHolder, EntitySelector.valueOf(getDefaultTargetEntityType().name())));
    }

    protected ItemStack getItem(PowerContext context) {
        return getItems(context).stream().findFirst().orElse(null);
    }

    public static List<ItemStack> resolveItems(PowerContext context,
                                               ItemSelector selector,
                                               EntitySelector holderSelector) {
        if (selector == ItemSelector.CONTEXT) return valid(context.item());
        if (selector == ItemSelector.TRIGGER_ITEM) return valid(context.triggerItem());
        Entity holder = context.entity(holderSelector);
        if (!(holder instanceof LivingEntity living)) return List.of();
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) return List.of();
        return switch (selector) {
            case MAIN_HAND -> valid(equipment.getItemInMainHand());
            case OFF_HAND -> valid(equipment.getItemInOffHand());
            case HELMET -> valid(equipment.getHelmet());
            case CHESTPLATE -> valid(equipment.getChestplate());
            case LEGGINGS -> valid(equipment.getLeggings());
            case BOOTS -> valid(equipment.getBoots());
            case ARMOR -> valid(Arrays.asList(equipment.getArmorContents()));
            case ALL_EQUIPMENT -> {
                List<ItemStack> result = new ArrayList<>();
                result.addAll(valid(equipment.getItemInMainHand()));
                result.addAll(valid(equipment.getItemInOffHand()));
                result.addAll(valid(Arrays.asList(equipment.getArmorContents())));
                yield List.copyOf(result);
            }
            default -> List.of();
        };
    }

    private Location resolveLocation(PowerContext context, LocationSelector selector, double distance) {
        return switch (selector) {
            case CONTEXT -> context.location();
            case FROM -> context.triggerData() == null ? null : context.triggerData().from();
            case TO -> context.triggerData() == null ? null : context.triggerData().to();
            case PLAYER -> locationOf(context.player());
            case SOURCE -> locationOf(context.source());
            case SKILL -> locationOf(context.skill());
            case TARGET -> locationOf(getTargetEntity(context));
            case BLOCK -> context.block() == null ? null : context.block().getLocation().add(0.5D, 0.5D, 0.5D);
            case LOOK -> lookLocation(context.source(), distance);
        };
    }

    private static List<ItemStack> valid(ItemStack item) {
        return item == null || item.getType().isAir() ? List.of() : List.of(item);
    }

    private static List<ItemStack> valid(List<ItemStack> items) {
        return items.stream().filter(item -> item != null && !item.getType().isAir()).toList();
    }

    private Location locationOf(Entity entity) {
        return entity == null ? null : entity.getLocation();
    }

    private Location lookLocation(Entity entity, double distance) {
        Location location = locationOf(entity);
        if (location == null) return null;
        Vector direction = location.getDirection();
        return direction.lengthSquared() < 1.0E-4D
                ? location
                : location.add(direction.normalize().multiply(distance));
    }
}
