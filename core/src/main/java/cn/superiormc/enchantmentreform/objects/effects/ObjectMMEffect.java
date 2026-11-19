package cn.superiormc.enchantmentreform.objects.effects;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.players.PlayerData;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatRegistry;
import io.lumine.mythic.core.skills.stats.StatSource;
import io.lumine.mythic.core.skills.stats.StatType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class ObjectMMEffect extends AbstractEffect {

    private final StatType statType;

    private final StatModifierType modifier;

    private final ObjectMMEffectSource source;

    private StatRegistry stats;

    public ObjectMMEffect(String id, Player player, ConfigurationSection section, double value) {
        super(id, player, section, value);
        String name = section.getString("stat", "");
        statType = MythicBukkit.inst().getStatManager().getStat(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown MythicMobs stat: " + name));
        String type = section.getString("modifier-type", "ADD");
        modifier = StatModifierType.get(type.toUpperCase(Locale.ROOT));
        if (modifier == null) {
            throw new IllegalArgumentException("Unknown MythicMobs modifier-type: " + type);
        }
        source = new ObjectMMEffectSource(id);
    }

    @Override
    protected boolean addPlayerStat() {
        PlayerData profile = MythicBukkit.inst().getPlayerManager().getProfile(player);
        stats = profile == null ? null : profile.getStatRegistry();
        if (stats == null) {
            return false;
        }
        stats.putValue(statType, source, modifier, getValue());
        return true;
    }

    @Override
    protected void removePlayerStat() {
        stats.removeValue(statType, source);
    }

    private record ObjectMMEffectSource(String id) implements StatSource {}
}
