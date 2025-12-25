package template.rip.module.modules.player;

import net.minecraft.util.hit.BlockHitResult;
import template.rip.Template;
import template.rip.api.event.events.UpdateCrosshairEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.combat.ReachModule;
import template.rip.module.setting.settings.BooleanSetting;

public class NoInteractModule extends Module {

    private final BooleanSetting interactThroughWalls = new BooleanSetting(this, Description.of("Allows attacking and interacting with entities through walls"), false, "Entity Through Walls");
    private final BooleanSetting noInteractWalls = new BooleanSetting(this, Description.of("Prevents you from interacting with blocks"), true, "No block interact");

    public NoInteractModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onRender(UpdateCrosshairEvent event) {
        if (!nullCheck()) {
            return;
        }
        if (interactThroughWalls.isEnabled()) {
            ReachModule reachModule = Template.moduleManager.getModule(ReachModule.class);
            double reach = reachModule != null && reachModule.isEnabled() ? reachModule.entityReach.getValue() : 3.0;
            mc.crosshairTarget = PlayerUtils.getHitResult(mc.player, e -> true, Template.rotationManager().yaw(), Template.rotationManager().pitch(), reach, true, 0);
        }
        if (noInteractWalls.isEnabled() && mc.crosshairTarget instanceof BlockHitResult bhr) {
            mc.crosshairTarget = BlockHitResult.createMissed(bhr.getPos(), bhr.getSide(), bhr.getBlockPos());
        }
    }
}
