package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import java.util.Set;

public final class PowerConditionTargetCategory extends AbstractPowerCondition {

    private static final Set<String> UNDEAD = Set.of("ZOMBIE", "HUSK", "DROWNED", "SKELETON", "STRAY",
            "WITHER_SKELETON", "WITHER", "ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN", "PHANTOM", "BOGGED");

    public PowerConditionTargetCategory() {
        super("target_category");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Entity target = condition.getContext() == null ? null : condition.getContext().target();
        return switch (condition.getSection().getString("category", "ANIMAL").toUpperCase()) {
            case "MONSTER" -> target instanceof Monster;
            case "ANIMAL" -> target instanceof Animals;
            case "UNDEAD" -> target != null && UNDEAD.contains(target.getType().name());
            default -> false;
        };
    }
}
