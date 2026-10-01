package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierTradeUsesReturn extends AbstractPowerModifier {

    public PowerModifierTradeUsesReturn(ConfigurationSection section) {
        super("trade_uses_return", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (context.event() instanceof PlayerPurchaseEvent event) {
            event.setIncreaseTradeUses(false);
        }
    }
}
