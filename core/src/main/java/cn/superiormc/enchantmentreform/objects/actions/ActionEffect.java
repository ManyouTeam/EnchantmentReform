package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ActionEffect<S extends AbstractConfiguredSection<C>, C> extends AbstractRunAction<S, C> {

    public ActionEffect() {
        super("effect");
        setRequiredArgs("potion", "duration", "level");
    }

    @Override
    protected void onDoAction(S singleAction, Player player, C context) {
        PotionEffectType potionEffectType;
        if (CommonUtil.getMajorVersion(20)) {
            potionEffectType = Registry.EFFECT.get(CommonUtil.parseNamespacedKey(singleAction.getString("potion")));
        } else {
            potionEffectType = PotionEffectType.getByName(singleAction.getString("potion"));
        }
        if (potionEffectType != null) {
            PotionEffect effect = new PotionEffect(potionEffectType,
                    singleAction.getInt("duration"),
                    singleAction.getInt("level"),
                    singleAction.getBoolean("ambient", true),
                    singleAction.getBoolean("particles", true),
                    singleAction.getBoolean("icon", true));
            player.addPotionEffect(effect);
        }
    }
}
