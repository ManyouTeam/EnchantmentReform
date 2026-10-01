package cn.superiormc.enchantmentreform.power;

public record PowerExecutionResult(boolean executed, boolean cancelled) {

    public static final PowerExecutionResult SKIPPED = new PowerExecutionResult(false, false);
}
