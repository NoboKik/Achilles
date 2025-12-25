package template.rip.module.modules.render;

import net.minecraft.entity.Entity;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;

public class TargetCircleModule extends Module {

    public final NumberSetting speed = new NumberSetting(this, 25, 0, 100, 1, "Speed");
    public final NumberSetting width = new NumberSetting(this, 0.1, 0, 0.5, 0.01, "Width");
    public final NumberSetting segments = new NumberSetting(this, 20, 4, 360, 1, "Segments");
    public final BooleanSetting invertColors = new BooleanSetting(this, true, "Invert Colors on lower half");
    public final ColorSetting oneColor = new ColorSetting(this, new JColor(0.8f, 0.0f, 0.0f, 0.5f), true, "First Color");
    public final ColorSetting twoColor = new ColorSetting(this, new JColor(0.1f, 0.1f, 0.1f, 0.5f), true, "Second Color");

    private boolean pos = true;
    private float y = 0f;

    public TargetCircleModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onWorldRender(WorldRenderEvent event) {
        Entity e = PlayerUtils.findFirstTarget();
        if (e == null)
            return;

        double h = e.getHeight() * 1.1;
        if (y > h ) {
            pos = false;
        } else if (y < 0) {
            pos = true;
        }
        float v = speed.getFValue() / 1000;
        y = y - (pos ? -v : v);
        Color one = oneColor.getColor();
        Color two = twoColor.getColor();
        if (y < h / 2 && invertColors.isEnabled()) {
            one = twoColor.getColor();
            two = oneColor.getColor();
        }
        RenderUtils.Render3D.renderCircle(one, two, e.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)).add(0.0, y, 0), e.getWidth(), segments.getIValue(), width.getFValue(), 0.0025f, event.context);
    }
}
