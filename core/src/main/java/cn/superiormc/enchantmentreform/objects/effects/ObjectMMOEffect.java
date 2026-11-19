package cn.superiormc.enchantmentreform.objects.effects;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class ObjectMMOEffect extends AbstractEffect {

    private final StatModifier modifier;

    private MMOPlayerData playerData;

    public ObjectMMOEffect(String id, Player player, ConfigurationSection section, double value) {
        super(id, player, section, value);
        modifier = new StatModifier(id, section.getString("stat", ""), value, ModifierType.FLAT);
    }

    @Override
    protected boolean addPlayerStat() {
        if (!MMOPlayerData.has(player)) {
            return false;
        }
        playerData = MMOPlayerData.get(player);
        modifier.register(playerData);
        return true;
    }

    @Override
    protected void removePlayerStat() {
        modifier.unregister(playerData);
    }
}
