package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.abilities.ModifyRepairCostAbility;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class AnvilListener implements Listener {

    private final Map<PowerEnchantmentDefinition, List<ModifyRepairCostAbility>> abilityCache =
            Collections.synchronizedMap(new WeakHashMap<>());

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView() instanceof AnvilView view) {
            configureView(view);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilView view = event.getView();
        configureView(view);

        ItemStack result = event.getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }

        Player player = view.getPlayer() instanceof Player current ? current : null;
        int repairCost = Math.max(0, view.getRepairCost());
        for (RuleSource source : findRuleSources(
                event.getInventory().getItem(0),
                event.getInventory().getItem(1))) {
            PowerContext context = new PowerContext(
                    source.enchantment().getPower(),
                    source.level(),
                    null,
                    null,
                    source.item(),
                    null,
                    null,
                    player);
            if (!source.enchantment().meetsConditions(player, context)) {
                continue;
            }
            for (ModifyRepairCostAbility ability : abilities(source.enchantment())) {
                repairCost = ability.modify(repairCost, context);
            }
        }
        view.setRepairCost(repairCost);
    }

    private void configureView(AnvilView view) {
        boolean bypass = ConfigManager.configManager != null &&
                ConfigManager.configManager.getBoolean("anvil.bypass-enchantment-level-limit", false);
        view.bypassEnchantmentLevelRestriction(bypass);
        boolean noMaxRepairCost = ConfigManager.configManager != null &&
                ConfigManager.configManager.getBoolean("anvil.no-max-repair-cost", false);
        if (noMaxRepairCost) {
            view.setMaximumRepairCost(Integer.MAX_VALUE);
        }
    }

    private List<RuleSource> findRuleSources(ItemStack first, ItemStack second) {
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        if (manager == null) {
            return List.of();
        }

        Map<PowerEnchantmentDefinition, RuleSource> result = new LinkedHashMap<>();
        collectSources(manager, result, first);
        collectSources(manager, result, second);
        return result.values().stream()
                .sorted(Comparator
                        .comparingInt((RuleSource source) ->
                                source.enchantment().getExecutionPriority())
                        .reversed()
                        .thenComparing(source -> source.enchantment().getKey().asString(),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void collectSources(EnchantmentConfigManager manager,
                                Map<PowerEnchantmentDefinition, RuleSource> result,
                                ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments(item).entrySet()) {
            PowerEnchantmentDefinition definition = manager.getPowerEnchantment(
                    entry.getKey().getKey().toString());
            if (definition == null || definition.getPower() == null
                    || abilities(definition).isEmpty()) {
                continue;
            }

            int level = entry.getValue();
            RuleSource previous = result.get(definition);
            if (previous == null || level > previous.level()) {
                result.put(definition, new RuleSource(definition, level, item));
            }
        }
    }

    private Map<Enchantment, Integer> enchantments(ItemStack item) {
        Map<Enchantment, Integer> result = new LinkedHashMap<>(item.getEnchantments());
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.getStoredEnchants().forEach((enchantment, level) ->
                    result.merge(enchantment, level, Math::max));
        }
        return result;
    }

    private List<ModifyRepairCostAbility> abilities(PowerEnchantmentDefinition definition) {
        synchronized (abilityCache) {
            List<ModifyRepairCostAbility> cached = abilityCache.get(definition);
            if (cached != null) {
                return cached;
            }
            List<ModifyRepairCostAbility> parsed = new ArrayList<>();
            if (AbilityManager.abilityManager != null && definition.getPower() != null) {
                AbilityManager.abilityManager.parseActions(
                                definition.getPower().getSection("activation-abilities"))
                        .stream()
                        .filter(ModifyRepairCostAbility.class::isInstance)
                        .map(ModifyRepairCostAbility.class::cast)
                        .forEach(parsed::add);
            }
            List<ModifyRepairCostAbility> immutable = List.copyOf(parsed);
            abilityCache.put(definition, immutable);
            return immutable;
        }
    }

    private record RuleSource(PowerEnchantmentDefinition enchantment, int level, ItemStack item) {
    }
}
