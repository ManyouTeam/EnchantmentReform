package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Input;
import org.bukkit.event.player.PlayerInputEvent;

import java.util.Locale;
import java.util.Set;

public final class PowerConditionInputType extends AbstractPowerCondition {

    public PowerConditionInputType() {
        super("input_type");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Set<String> inputs = CommonUtil.values(condition.getSection(), "inputs", "input");
        if (inputs.isEmpty() || condition.getContext() == null
                || !(condition.getContext().event() instanceof PlayerInputEvent event)) {
            return false;
        }
        Input input = event.getInput();
        Input previousInput = event.getPlayer().getCurrentInput();
        InputState state = InputState.parse(condition.getSection().getString("state", "ACTIVE"));
        for (String type : inputs) {
            if (matchesState(input, previousInput, type, state)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesState(Input input, Input previousInput, String type, InputState state) {
        boolean active = isActive(input, type);
        boolean previouslyActive = isActive(previousInput, type);
        return switch (state) {
            case ACTIVE -> active;
            case PRESSED -> active && !previouslyActive;
            case RELEASED -> !active && previouslyActive;
        };
    }

    private boolean isActive(Input input, String type) {
        if (input == null || type == null) {
            return false;
        }
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "FORWARD" -> input.isForward();
            case "BACKWARD" -> input.isBackward();
            case "LEFT" -> input.isLeft();
            case "RIGHT" -> input.isRight();
            case "JUMP" -> input.isJump();
            case "SNEAK" -> input.isSneak();
            case "SPRINT" -> input.isSprint();
            default -> false;
        };
    }

    private enum InputState {
        ACTIVE,
        PRESSED,
        RELEASED;

        private static InputState parse(String value) {
            if (value == null || value.isBlank()) {
                return ACTIVE;
            }
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return ACTIVE;
            }
        }
    }
}
