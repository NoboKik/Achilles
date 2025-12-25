package template.rip.deprecated;

import template.rip.Template;
import template.rip.api.event.events.UpdateCrosshairEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;

public class ThroughWallsModule extends Module {
    public ThroughWallsModule() {
        super(Category.MISC, Description.of("Allows entity interactions through walls"), "ThroughWalls");
    }
    @EventHandler
    private void onPlayerTick(UpdateCrosshairEvent event) {
        mc.crosshairTarget = PlayerUtils.getHitResult(mc.player, e -> true, Template.rotationManager().yaw(), Template.rotationManager().pitch(), 3.0, true, 0);
    }
}
