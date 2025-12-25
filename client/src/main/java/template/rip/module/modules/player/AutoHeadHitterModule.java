package template.rip.module.modules.player;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.InputUtil;
import template.rip.module.Module;

public class AutoHeadHitterModule extends Module {

    public AutoHeadHitterModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (event.check && head())
            event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player != null && head())
            mc.player.jumpingCooldown = 0;
    }

    private boolean head() {
        BlockPos bpos = mc.player.getBlockPos().up(2);

        if (!mc.player.isSprinting())
            return false;

        Box box = BlockUtils.blockBox(bpos);

        if (box == null)
            return false;

        return box.offset(bpos.multiply(-1)).minY == 0;
    }
}
