package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.hooks.ItemPriceUtil;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.ItemStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CostPriceAbility extends AbstractAbility {

    public CostPriceAbility(ConfigurationSection section) {
        super("CostPrice", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection successAbilities = section.getConfigurationSection("abilities");
        if (successAbilities == null) {
            return false;
        }

        ConfigurationSection price = section.getConfigurationSection("price");
        Player payer = getPayer(context);
        if (payer == null) {
            return AbilityManager.abilityManager.execute(getFailureAbilities(), context);
        }
        if (!shouldCharge(context, payer)) {
            return AbilityManager.abilityManager.execute(successAbilities, context);
        }
        double cost = getCost(price, context);
        boolean success = playerHasEnough(price, payer, shouldTake(price), cost, context);
        ConfigurationSection branch = success ? successAbilities : getFailureAbilities();
        return AbilityManager.abilityManager.execute(branch, context);
    }

    private boolean shouldCharge(PowerContext context, Player payer) {
        int costEvery = Math.max(1, getInt("cost-every", 1, context));
        String powerId = context.power() == null ? "none" : context.power().getId();
        PowerStateStore.Key key = PowerStateStore.key(
                payer, powerId, "cost-price-trigger", section.getCurrentPath(), null);
        if (costEvery <= 1) {
            PowerStateStore.remove(key);
            return true;
        }
        PowerStateStore.Update update = PowerStateStore.update(
                key,
                current -> current + 1.0D >= costEvery ? 0.0D : current + 1.0D,
                costEvery,
                0.0D);
        return update.previous() + 1.0D >= costEvery;
    }

    private Player getPayer(PowerContext context) {
        EntitySelector selector = EntitySelector.parse(
                getString("payer", "PLAYER", context), EntitySelector.PLAYER);
        return context.player(selector);
    }

    private double getCost(ConfigurationSection price, PowerContext context) {
        if (section.contains("cost")) {
            return getDouble("cost", 0.0D, context);
        }
        if (section.contains("amount")) {
            return getDouble("amount", 0.0D, context);
        }
        if (price != null && price.contains("cost")) {
            return getDouble("price.cost", 0.0D, context);
        }
        return 0.0D;
    }

    private boolean shouldTake(ConfigurationSection price) {
        return price != null && price.contains("take")
                ? price.getBoolean("take", true)
                : section.getBoolean("take", true);
    }

    private ConfigurationSection getFailureAbilities() {
        ConfigurationSection abilities = section.getConfigurationSection("else-abilities");
        if (abilities == null) {
            abilities = section.getConfigurationSection("failure-abilities");
        }
        if (abilities == null) {
            abilities = section.getConfigurationSection("fail-abilities");
        }
        return abilities;
    }

    private boolean playerHasEnough(ConfigurationSection price,
                                    Player player,
                                    boolean take,
                                    double cost,
                                    PowerContext context) {
        if (cost < 0.0D) {
            return false;
        }
        PriceType type = PriceType.detect(price);

        ItemStorage storage = ItemStorage.of(player.getInventory());
        return switch (type) {
            case HOOK_ITEM -> hasHookItem(storage, price, take, cost);
            case VANILLA_ITEM -> hasVanillaItem(storage, price, player, take, cost);
            case MATCH_ITEM -> ItemPriceUtil.getPrice(storage, player, price, (int) cost, take);
            case HOOK_ECONOMY -> HookManager.hookManager.getPrice(
                    player,
                    price.getString("economy-plugin", ""),
                    price.getString("economy-type", "default"),
                    cost,
                    take);
            case VANILLA_ECONOMY -> HookManager.hookManager.getPrice(
                    player,
                    price.getString("economy-type", ""),
                    (int) cost,
                    take);
            case CUSTOM -> getDouble("price.match-placeholder", player, context) >= cost;
            case FREE, RESERVE -> true;
            case UNKNOWN -> {
                ErrorManager.errorManager.sendErrorMessage("§cInvalid cost_price price configuration.");
                yield false;
            }
        };
    }

    private boolean hasHookItem(ItemStorage storage,
                                ConfigurationSection price,
                                boolean take,
                                double cost) {
        String pluginName = price.getString("hook-plugin", "");
        String itemId = price.getString("hook-item", "");
        if (pluginName.equals("MMOItems") && !itemId.contains(";;")) {
            itemId = price.getString("hook-item-type", "") + ";;" + itemId;
        } else if (pluginName.equals("EcoArmor") && !itemId.contains(";;")) {
            itemId = itemId + ";;" + price.getString("hook-item-type", "");
        }
        String resolvedItemId = itemId;
        return ItemPriceUtil.getPrice(storage, pluginName, resolvedItemId, (int) cost, take);
    }

    private boolean hasVanillaItem(ItemStorage storage,
                                   ConfigurationSection price,
                                   Player player,
                                   boolean take,
                                   double cost) {
        ItemStack item = BuildItem.buildItemStack(player, price);
        if (item == null || item.getType().isAir()) {
            return false;
        }
        return ItemPriceUtil.getPrice(storage, item, (int) cost, take);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private enum PriceType {
        HOOK_ITEM,
        VANILLA_ITEM,
        MATCH_ITEM,
        HOOK_ECONOMY,
        VANILLA_ECONOMY,
        CUSTOM,
        FREE,
        RESERVE,
        UNKNOWN;

        private static PriceType detect(ConfigurationSection section) {
            if (section == null) {
                return UNKNOWN;
            }
            if (section.contains("hook-plugin") && section.contains("hook-item")) {
                return HOOK_ITEM;
            }
            if (section.contains("match-item")) {
                return MATCH_ITEM;
            }
            if (section.contains("match-placeholder")) {
                return CUSTOM;
            }
            if (section.contains("economy-plugin")) {
                return HOOK_ECONOMY;
            }
            if (section.contains("economy-type")) {
                return VANILLA_ECONOMY;
            }
            if (section.contains("material") || section.contains("item")) {
                return VANILLA_ITEM;
            }
            if (section.contains("amount")) {
                return RESERVE;
            }
            return FREE;
        }
    }
}
