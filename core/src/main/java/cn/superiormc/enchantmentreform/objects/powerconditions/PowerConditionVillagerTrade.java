package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;

public final class PowerConditionVillagerTrade extends AbstractPowerCondition {

    public PowerConditionVillagerTrade() {
        super("villager_trade");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null
                || !(condition.getContext().event() instanceof PlayerPurchaseEvent event)) {
            return false;
        }
        boolean expected = condition.getSection().getBoolean("value", true);
        return (event instanceof PlayerTradeEvent) == expected;
    }
}
