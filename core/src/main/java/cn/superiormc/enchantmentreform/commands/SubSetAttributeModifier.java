package cn.superiormc.enchantmentreform.commands;

public final class SubSetAttributeModifier extends AbstractAttributeModifierCommand {

    public SubSetAttributeModifier() {
        super("setattributemodifier");
    }

    @Override
    protected boolean replacesExistingModifier() {
        return true;
    }

    @Override
    protected String successMessage() {
        return "attribute.modifier-set";
    }
}
