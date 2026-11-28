package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.methods.EnchantsUtil;
import cn.superiormc.enchantmentreform.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;

public final class SubRandomEnchant extends AbstractRandomEnchantCommand {

    public SubRandomEnchant() {
        super("randomenchant");
    }

    @Override
    protected void applyRolls(CommandSender sender, Player target, int power, int amount,
                             String rarity, int minimumLevel, int maximumLevel, Set<Material> targetItems) {
        ItemStack held = target.getInventory().getItemInMainHand();
        if (!ItemUtil.isValid(held)) {
            LanguageManager.languageManager.sendStringText(sender, "random-enchant.empty-hand", "player", target.getName());
            return;
        }
        ItemStack result = EnchantsUtil.enchantHeldItem(held, rarity, power, amount, minimumLevel, maximumLevel);
        if (result == null) {
            LanguageManager.languageManager.sendStringText(sender, "random-enchant.no-result");
            return;
        }
        Player viewer = sender instanceof Player player ? player : null;
        Set<Enchantment> existing = EnchantsUtil.getEnchantments(held, false).keySet();
        String enchantments = EnchantsUtil.getEnchantments(result, false).keySet().stream()
                .filter(enchantment -> !existing.contains(enchantment))
                .map(enchantment -> EnchantsUtil.getEnchantmentName(enchantment, viewer))
                .collect(Collectors.joining(", "));
        target.getInventory().setItemInMainHand(result);
        LanguageManager.languageManager.sendStringText(sender, "random-enchant.applied-with-enchantments",
                "player", target.getName(), "enchantments", enchantments);
    }
}
