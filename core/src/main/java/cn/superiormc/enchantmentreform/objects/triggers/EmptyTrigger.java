package cn.superiormc.enchantmentreform.objects.triggers;

public final class EmptyTrigger extends EventTrigger<Object> {

    public EmptyTrigger() {
        super("empty", "on-empty", Object.class);
    }

    @Override
    protected void handle(Object event, TriggerRuntime runtime) {
        // Ignored
    }
}