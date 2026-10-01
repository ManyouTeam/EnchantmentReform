package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierWardenAnger extends AbstractPowerModifier {

    public PowerModifierWardenAnger(ConfigurationSection section) {
        super("warden_anger", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof WardenAngerChangeEvent event)) {
            return;
        }
        int increase = event.getNewAnger() - event.getOldAnger();
        if (increase <= 0) {
            return;
        }
        double modified = modifyValue(context, increase);
        if (!Double.isFinite(modified)) {
            return;
        }
        event.setNewAnger(Math.max(0, Math.min(150, (int) Math.round(Math.max(0.0D, modified)))));
    }
}