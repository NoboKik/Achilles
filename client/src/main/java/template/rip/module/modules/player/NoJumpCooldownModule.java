package template.rip.module.modules.player;

import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;

public class NoJumpCooldownModule extends Module {

    public NoJumpCooldownModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player == null) return;
        mc.player.jumpingCooldown = 0;
    }
}
