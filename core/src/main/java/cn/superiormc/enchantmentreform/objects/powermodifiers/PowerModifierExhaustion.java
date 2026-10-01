package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityExhaustionEvent;

public final class PowerModifierExhaustion extends AbstractPowerModifier {

    public PowerModifierExhaustion(ConfigurationSection section) {
        super("exhaustion", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof EntityExhaustionEvent event)) {
            return;
        }
        double modified = modifyValue(context, event.getExhaustion());
        event.setExhaustion((float) Math.min(40.0D, Math.max(0.0D, modified)));
    }
}
