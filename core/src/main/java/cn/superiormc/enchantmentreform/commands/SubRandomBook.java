package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.methods.EnchantsUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SubRandomBook extends AbstractRandomEnchantCommand {

    public SubRandomBook() {
        super("randombook");
    }

    @Override
    protected void applyRolls(CommandSender sender, Player target, int power, int amount,
                             String rarity, int minimumLevel, int maximumLevel, Set<Material> targetItems) {
        List<ItemStack> books = EnchantsUtil.randomEnchantedBooks(rarity, targetItems, power, amount, minimumLevel, maximumLevel);
        if (books.isEmpty()) {
            if (!targetItems.isEmpty()) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.no-target-result");
                return;
            }
            if (minimumLevel > 0) {
                String range = minimumLevel == maximumLevel ? String.valueOf(minimumLevel) : minimumLevel + "~" + maximumLevel;
                LanguageManager.languageManager.sendStringText(sender, "random-book.no-level-result", "value", range);
                return;
            }
            if (rarity != null) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.no-rarity-result", "value", rarity);
                return;
            }
            LanguageManager.languageManager.sendStringText(sender, power > 0 ? "random-book.no-result" : "random-book.no-enchantments",
                    "power", String.valueOf(power));
            return;
        }
        Player viewer = sender instanceof Player player ? player : null;
        String enchantments = books.stream()
                .flatMap(book -> EnchantsUtil.getEnchantments(book, false).keySet().stream())
                .distinct()
                .map(enchantment -> EnchantsUtil.getEnchantmentName(enchantment, viewer))
                .collect(Collectors.joining(", "));
        CommonUtil.giveOrDrop(target, books.toArray(ItemStack[]::new));
        LanguageManager.languageManager.sendStringText(sender, "random-book.given-with-enchantments",
                "player", target.getName(), "amount", String.valueOf(amount), "enchantments", enchantments);
    }
}
