package template.rip.deprecated;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;

import java.awt.*;

public class EdgeModule extends Module {

    boolean pressed = false;

    public EdgeModule() {
        super(Category.RENDER, Description.of("Renders the edge of a block"), "Edge");
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!nullCheck())
            return;

        Pair<Box, Vec3d> pr = PlayerUtils.blockEdgePair();
        if (pr.getLeft() != null) {
            RenderUtils.Render3D.renderBox(pr.getLeft(), Color.WHITE, 100, event.context);
        }
        RenderUtils.Render3D.renderBox(new Box(pr.getRight().subtract(0.05, 0.05, 0.05), pr.getRight().add(0.05, 0.05, 0.05)), Color.BLACK, 200, event.context);
        mc.inGameHud.getChatHud().addMessage(Text.of(String.valueOf(mc.player.getPos().distanceTo(pr.getRight()))));
    }

}
