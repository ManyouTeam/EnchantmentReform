package cn.superiormc.enchantmentreform.commands;

public final class SubAddAttributeModifier extends AbstractAttributeModifierCommand {

    public SubAddAttributeModifier() {
        super("addattributemodifier");
    }

    @Override
    protected boolean replacesExistingModifier() {
        return false;
    }

    @Override
    protected String successMessage() {
        return "attribute.modifier-added";
    }
}
