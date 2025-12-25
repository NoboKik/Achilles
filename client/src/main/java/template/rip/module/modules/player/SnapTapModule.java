package template.rip.module.modules.player;

import net.minecraft.client.option.GameOptions;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;

public class SnapTapModule extends Module {

    public static long lastLeft = 0;
    public static long lastRight = 0;
    public static long lastForward = 0;
    public static long lastBackwards = 0;

    public SnapTapModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    public static Float getSideways() {
        GameOptions settings = mc.options;
        if (settings.leftKey.isPressed() && settings.rightKey.isPressed()) {
            return lastLeft >= lastRight ? 1f : -1f;
        } else {
            return null;
        }
    }
    public static Float getForward() {
        GameOptions settings = mc.options;
        if (settings.forwardKey.isPressed() && settings.backKey.isPressed()) {
            return lastForward >= lastBackwards ? 1f : -1f;
        } else {
            return null;
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        Float forward = getForward();
        Float sideways = getSideways();
        if (forward != null) {
            event.input.movementForward = forward;
        }
        if (sideways != null) {
            event.input.movementSideways = sideways;
        }
    }
}
