package cn.superiormc.enchantmentreform.hooks.enchantmentdescription;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.hooks.fakechange.FakeChange;
import cn.superiormc.enchantmentreform.hooks.fakechange.FakeChangeContext;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class EnchantmentDescriptionItemDisplay implements FakeChange {

    private volatile EnchantmentDescriptionRenderer.DisplaySettings settings =
            EnchantmentDescriptionRenderer.DisplaySettings.defaults();

    private volatile ConfigurationSection blackItem;

    private boolean enchantmentSlotsDetected;

    private boolean conflictWarningShown;

    @Override
    public void reload() {
        ConfigurationSection section = ConfigManager.configManager.getSection(
                "enchantment-description.item-display");
        settings = EnchantmentDescriptionRenderer.DisplaySettings.from(section);
        blackItem = section == null ? null : section.getConfigurationSection("black-item");
        enchantmentSlotsDetected = CommonUtil.getClass(
                "cn.superiormc.enchantmentslots.EnchantmentSlots");
        if (enchantmentSlotsDetected && settings.enabled() && !settings.forceEnabled()
                && !conflictWarningShown) {
            conflictWarningShown = true;
            EnchantmentReform.instance.getLogger().warning(
                    "EnchantmentSlots was detected, so Item Display enchantment lore is disabled "
                            + "to avoid duplicate lore. Set enchantment-description.item-display.force-enabled "
                            + "to true to force it.");
        }
    }

    @Override
    public boolean isEnabled() {
        return settings.enabled() && (!enchantmentSlotsDetected || settings.forceEnabled());
    }

    @Override
    public ItemStack apply(ItemStack item, FakeChangeContext context) {
        Player player = context.player();
        if (!isEnabled()
                || item == null
                || item.getType().isAir()
                || (settings.blackCreative() && player.getGameMode() == GameMode.CREATIVE)
                || (blackItem != null && MatchItemManager.matchItemManager.getMatch(
                        blackItem, player, item))) {
            return item;
        }
        return EnchantmentDescriptionRenderer.applyItemDisplay(
                item,
                player,
                settings,
                text -> TextUtil.withPAPI(text, player));
    }
}
