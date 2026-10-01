package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import io.papermc.paper.event.entity.EntityLungeEvent;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierLungePower extends AbstractPowerModifier {

    public PowerModifierLungePower(ConfigurationSection section) {
        super("lunge_power", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!CommonUtil.getYearVersion(26, 0, 0)) {
            return;
        }
        if (context.event() instanceof EntityLungeEvent event) {
            int power = (int) Math.round(modifyValue(context, event.getLungePower()));
            event.setLungePower(Math.max(0, power));
        }
    }
}
