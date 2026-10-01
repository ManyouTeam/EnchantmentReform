package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.nms.FoodUseResult;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.NmsCapability;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AutoFeedAbility extends AbstractAbility {

    private final NmsBridge nmsBridge;

    public AutoFeedAbility(ConfigurationSection section) {
        this(section, EnchantmentReform.instance == null
                ? new UnsupportedNmsBridge("Plugin is not enabled")
                : EnchantmentReform.instance.getNmsBridge());
    }

    public AutoFeedAbility(ConfigurationSection section, NmsBridge nmsBridge) {
        super("AutoFeed", section);
        this.nmsBridge = nmsBridge;
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof Player player) || player.isDead() || player.getFoodLevel() >= 20) {
            return false;
        }
        int threshold = Math.max(0, getInt("threshold", 10, context));
        if (player.getHealth() < CommonUtil.getMaxHealth(player)) {
            threshold = Math.max(0, getInt("health-not-full-threshold", threshold, context));
        }
        if (player.getFoodLevel() > threshold) {
            return false;
        }
        if (!executeNms(player)) {
            return false;
        }
        return AbilityManager.abilityManager.execute(
                section.getConfigurationSection("success-abilities"), context);
    }

    private boolean executeNms(Player player) {
        if (!nmsBridge.supports(NmsCapability.FINISH_USING_ITEM)) {
            return false;
        }
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!isFood(item)) {
                continue;
            }

            ItemStack consumed = item.clone();
            consumed.setAmount(1);
            ItemStack remainderInSlot = item.clone();
            remainderInSlot.setAmount(item.getAmount() - 1);
            player.getInventory().setItem(slot,
                    remainderInSlot.getAmount() <= 0 ? null : remainderInSlot);

            FoodUseResult result = nmsBridge.finishUsingItem(player, consumed);
            if (result.successful()) {
                CommonUtil.giveOrDrop(player, result.remainder());
                return true;
            } else {
                CommonUtil.giveOrDrop(player, consumed);
            }
            return false;
        }
        return false;
    }

    private boolean isFood(ItemStack item) {
        return item != null && item.getType() != Material.AIR
                && (item.getType().isEdible() || item.getItemMeta().hasFood());
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
