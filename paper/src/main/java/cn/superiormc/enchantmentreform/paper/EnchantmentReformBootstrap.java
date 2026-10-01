package cn.superiormc.enchantmentreform.paper;

import cn.superiormc.enchantmentreform.commands.PaperCommandRegistrar;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.paper.registry.PaperCurseTagRegistrar;
import cn.superiormc.enchantmentreform.paper.registry.PaperDoubleTradePriceTagRegistrar;
import cn.superiormc.enchantmentreform.paper.registry.PaperEnchantmentRegistrar;
import cn.superiormc.enchantmentreform.paper.registry.PaperTooltipOrderTagRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.bukkit.configuration.ConfigurationSection;

public final class EnchantmentReformBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        EnchantmentConfigManager manager = new EnchantmentConfigManager(
                context.getDataDirectory(),
                message -> context.getLogger().info(message),
                (message, throwable) -> context.getLogger().error(message, throwable));
        PaperCommandRegistrar.bind(context);
        ConfigurationSection supportedItems = manager.getSupportedItemsConfiguration();
        ConfigurationSection root = supportedItems == null ? null : supportedItems.getRoot();
        ConfigurationSection raritySettings = root == null
                ? null
                : root.getConfigurationSection("rarity");
        ConfigurationSection tooltipOrderSettings = root == null
                ? null
                : root.getConfigurationSection("tooltip-order");
        PaperEnchantmentRegistrar.bind(
                context, manager.getEnchantments(), manager.getVanillaEnchantments(),
                supportedItems);
        PaperCurseTagRegistrar.bind(
                context, manager.getEnchantments(), raritySettings);
        PaperDoubleTradePriceTagRegistrar.bind(
                context, manager.getEnchantments(), raritySettings);
        PaperTooltipOrderTagRegistrar.bind(
                context,
                manager.getEnchantments(),
                manager.getVanillaEnchantments(),
                raritySettings,
                tooltipOrderSettings);
    }
}
