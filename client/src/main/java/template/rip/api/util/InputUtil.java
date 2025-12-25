package template.rip.api.util;

import net.minecraft.util.PlayerInput;

public class InputUtil {

    public static PlayerInput setJumping(PlayerInput input, boolean jumping) {
        return setValues(input, jumping, input.sneak(), input.sprint());
    }

    public static PlayerInput setSneaking(PlayerInput input, boolean sneaking) {
        return setValues(input, input.jump(), sneaking, input.sprint());
    }

    public static PlayerInput setSprinting(PlayerInput input, boolean sprinting) {
        return setValues(input, input.jump(), input.sneak(), sprinting);
    }

    public static PlayerInput setValues(PlayerInput input, boolean jumping, boolean sneaking) {
        return new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), jumping, sneaking, input.sprint());
    }

    public static PlayerInput setValues(PlayerInput input, boolean jumping, boolean sneaking, boolean sprinting) {
        return new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), jumping, sneaking, sprinting);
    }

    public static PlayerInput newInput(boolean forward, boolean backward, boolean  left, boolean right, boolean jumping, boolean sneaking, boolean sprinting) {
        return new PlayerInput(forward, backward, left, right, jumping, sneaking, sprinting);
    }
}
