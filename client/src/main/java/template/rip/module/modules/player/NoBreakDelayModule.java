package template.rip.module.modules.player;

import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

public class NoBreakDelayModule extends Module {

    private final NumberSetting breakDelay = new NumberSetting(this, 0, 0, 4, 1, "Break Delay");

    public NoBreakDelayModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onInput(HandleInputEvent.Pre event) {
        // When the cooldown goes to 5 it means it has been reset by minecraft code
        if (mc.interactionManager != null && mc.interactionManager.blockBreakingCooldown == 5)
            mc.interactionManager.blockBreakingCooldown = breakDelay.getIValue();
    }
}
