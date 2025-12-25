package template.rip.deprecated;

import net.minecraft.block.CobwebBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.BlockCollisionEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class NoWebModule extends Module {
    public final NumberSetting velocityH = new NumberSetting(this, 1, 0, 1, 0.01, "Horizontal slowdown");
    public final NumberSetting velocityV = new NumberSetting(this, 1, 0, 1, 0.01, "Vertical slowdown");
    public final BooleanSetting grim = new BooleanSetting(this, false, "Grim (200 pps)");
    public NoWebModule() {
        super(Category.BLATANT, Description.of("Reduces/removes web slowdown"), "NoWeb");
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @EventHandler
    private void onBlock(BlockCollisionEvent event) {
        if (!nullCheck())
            return;
        if (event.ent != mc.player)
            return;
        if (!(event.blockState.getBlock() instanceof CobwebBlock))
            return;

        event.setCancelled(true);
        mc.player.slowMovement(event.blockState, new Vec3d(velocityH.value, velocityV.value, velocityH.value));
        if (grim.isEnabled())
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, event.pos, Direction.DOWN));
    }
}
