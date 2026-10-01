package cn.superiormc.enchantmentreform.hooks.economy;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyVaultHook extends AbstractEconomyHook {

    private RegisteredServiceProvider<Economy> rsp;

    public EconomyVaultHook() {
        super("Vault");
        rsp = EnchantmentReform.instance.getServer().getServicesManager().getRegistration(Economy.class);
    }

    @Override
    public boolean hasEnoughEconomy(Player player, double value, String currencyID) {
        Economy eco = rsp.getProvider();
        return eco.has(player, value);
    }

    @Override
    public double getEconomy(Player player, String currencyID) {
        Economy eco = rsp.getProvider();
        return eco.getBalance(player);
    }

    @Override
    public void takeEconomy(Player player, double value, String currencyID) {
        Economy eco = rsp.getProvider();
        eco.withdrawPlayer(player, value);
    }

    @Override
    public void giveEconomy(Player player, double value, String currencyID) {
        Economy eco = rsp.getProvider();
        eco.depositPlayer(player, value);
    }

    @Override
    public boolean isEnabled() {
        if (rsp == null) {
            rsp = EnchantmentReform.instance.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                ErrorManager.errorManager.sendErrorMessage("§cCan not hook into Vault plugin, " +
                        "Vault is a API plugin, maybe you didn't install a Vault-based economy plugin in your server!");
                return false;
            }
        }
        return true;
    }
}
