package cn.superiormc.enchantmentreform.objects.effects;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class ObjectAuraSkillsEffect extends AbstractEffect {

    private final AuraSkillsApi auraSkills = AuraSkillsApi.get();

    private final Stat stat;

    private SkillsUser user;

    public ObjectAuraSkillsEffect(String id, Player player, ConfigurationSection section, double value) {
        super(id, player, section, value);
        String name = section.getString("stat", "");
        stat = auraSkills.getGlobalRegistry().getStat(NamespacedId.fromDefault(name));
        if (stat == null) {
            throw new IllegalArgumentException("Unknown AuraSkills stat: " + name);
        }
    }

    @Override
    protected boolean addPlayerStat() {
        user = auraSkills.getUser(player.getUniqueId());
        if (user == null || !user.isLoaded()) {
            return false;
        }
        user.addStatModifier(new StatModifier(id, stat, getValue()));
        return true;
    }

    @Override
    protected void removePlayerStat() {
        user.removeStatModifier(id);
    }
}
