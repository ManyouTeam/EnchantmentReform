package cn.superiormc.enchantmentreform.paper.registry;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.VanillaEnchantmentOverride;
import cn.superiormc.enchantmentreform.utils.EnchantmentOrderUtil;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class PaperTooltipOrderTagRegistrar {

    private PaperTooltipOrderTagRegistrar() {
    }

    public static void bind(BootstrapContext context,
                            Collection<ObjectCustomEnchantment> customDefinitions,
                            Collection<VanillaEnchantmentOverride> vanillaDefinitions,
                            ConfigurationSection raritySettings,
                            ConfigurationSection tooltipSettings) {
        if (!EnchantmentOrderUtil.isEnabled(tooltipSettings)) {
            return;
        }

        Set<TypedKey<Enchantment>> customKeys = customDefinitions.stream()
                .filter(ObjectCustomEnchantment::isEnabled)
                .map(PaperTooltipOrderTagRegistrar::key)
                .collect(java.util.stream.Collectors.toSet());
        List<TypedKey<Enchantment>> configuredOrder = Stream.concat(
                        customDefinitions.stream(),
                        vanillaDefinitions.stream())
                .filter(PowerEnchantmentDefinition::isEnabled)
                .sorted(EnchantmentOrderUtil.comparator(raritySettings, tooltipSettings))
                .map(PaperTooltipOrderTagRegistrar::key)
                .toList();

        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler(event ->
                        applyOrder(event.registrar(), configuredOrder, customKeys)));
    }

    private static void applyOrder(PostFlattenTagRegistrar<Enchantment> registrar,
                                   List<TypedKey<Enchantment>> configuredOrder,
                                   Set<TypedKey<Enchantment>> customKeys) {
        List<TypedKey<Enchantment>> existingOrder = registrar.hasTag(EnchantmentTagKeys.TOOLTIP_ORDER)
                ? new ArrayList<>(registrar.getTag(EnchantmentTagKeys.TOOLTIP_ORDER))
                : List.of();
        Set<TypedKey<Enchantment>> existingKeys = new HashSet<>(existingOrder);
        List<TypedKey<Enchantment>> completeOrder = new ArrayList<>();

        for (TypedKey<Enchantment> key : configuredOrder) {
            if (customKeys.contains(key) || existingKeys.contains(key)) {
                completeOrder.add(key);
            }
        }

        Set<TypedKey<Enchantment>> configuredKeys = new HashSet<>(completeOrder);
        for (TypedKey<Enchantment> key : existingOrder) {
            if (!configuredKeys.contains(key)) {
                completeOrder.add(key);
            }
        }

        registrar.setTag(EnchantmentTagKeys.TOOLTIP_ORDER, completeOrder);
    }

    private static TypedKey<Enchantment> key(PowerEnchantmentDefinition definition) {
        return EnchantmentKeys.create(Key.key(definition.getKey().asString()));
    }
}
