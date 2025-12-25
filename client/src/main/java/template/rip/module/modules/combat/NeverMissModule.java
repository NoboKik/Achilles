package template.rip.module.modules.combat;

import net.minecraft.util.hit.HitResult;
import template.rip.api.event.events.AttackEvent;
import template.rip.api.event.events.BlockBreakEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class NeverMissModule extends Module {

    private final BooleanSetting blockBreak = new BooleanSetting(this, true, "Cancel block breaking");

    public NeverMissModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    private boolean cross() {
        return mc.crosshairTarget == null || mc.crosshairTarget.getType() == HitResult.Type.MISS || mc.crosshairTarget.getType() == HitResult.Type.BLOCK;
    }

    @EventHandler
    private void onAttack(AttackEvent.Pre event) {
        if (cross())
            event.cancel();
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent.Pre event) {
        if (blockBreak.isEnabled() && cross())
            event.cancel();
    }
}
