package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import org.bukkit.Material;

import java.util.Locale;
import java.util.Set;

public final class PowerConditionCooldownMaterial extends AbstractPowerCondition {

    public PowerConditionCooldownMaterial() {
        super("cooldown_material");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null || !(context.event() instanceof PlayerItemCooldownEvent event)) {
            return false;
        }

        Set<String> materials = CommonUtil.values(condition.getSection(), "materials", "material");
        if (materials.isEmpty()) {
            return false;
        }

        Material material = event.getType();
        String materialName = material.name();
        String materialKey = material.getKey().toString().toUpperCase(Locale.ROOT);
        return materials.contains(materialName) || materials.contains(materialKey);
    }
}
