package template.rip.module.modules.render;

import imgui.ImGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.Rectangle;
import template.rip.api.util.CrystalUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.DividerSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.Map;

public class TracersModule extends Module implements Renderable  {

    public final BooleanSetting glowESP = new BooleanSetting(this, false, "Glow ESP");
    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");
    public final BooleanSetting teamColor = new BooleanSetting(this, false, "Team Color");
    public final BooleanSetting hurtColor = new BooleanSetting(this, false, "Hurt Color");
    private final ColorSetting color = new ColorSetting(this, new JColor(0.90f, 0.27f, 0.33f, 1f), true, "Color");
    public final BooleanSetting friendHasColor = new BooleanSetting(this, false, "Enable Friend Color");
    private final ColorSetting friendColor = new ColorSetting(this, new JColor(0.90f, 0.27f, 0.33f, 1f), true, "Friend Color");
    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave");
    public final BooleanSetting waveEnabled = new BooleanSetting(this, false, "Wave Enabled");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 1, 10, 1, "Wave Speed");

    public TracersModule(Category category, Description description, String name) {
        super(category, description, name);
        mergeDividers();
        friendColor.addConditionBoolean(friendHasColor, true);
    }

    @Override
    public void onDisable() {
        ImguiLoader.queueRemove(this);
    }

    @Override
    public void onEnable() {
        ImguiLoader.addRenderable(this);
    }

    @Override
    public void render() {
        if (!Template.displayRender() || !nullCheck())
            return;

        // Main Logic

        for (Map.Entry<Entity, Pair<Rectangle, Boolean>> e : CrystalUtils.getEntrySet()) {
            if (mc.world == null || mc.world.getEntityById(e.getKey().getId()) == null || !PlayerUtils.findTargets(true).contains(e.getKey())) {
                continue;
            }
            if (e.getValue().getLeft() != null) {
                JColor c = new JColor(color.getColor());
                if (waveEnabled.isEnabled()) {
                    c = new JColor(UI.interfaceColor((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 1));
                }

                if (teamColor.isEnabled()) {
                    int col = e.getKey().getTeamColorValue();
                    int red = (col >> 16) & 0xFF;
                    int green = (col >> 8) & 0xFF;
                    int blue = col & 0xFF;
                    c = new JColor(red,green,blue);
                }
                Entity ent = e.getKey();
                if (ent instanceof PlayerEntity pe && friendHasColor.isEnabled() && PlayerUtils.isFriend(pe)) {
                    c = new JColor(friendColor.getColor());
                }
                if (ent instanceof LivingEntity && ((LivingEntity)e.getKey()).hurtTime != 0 && hurtColor.isEnabled()) {
                    c = new JColor(255, 0, 0);
                }

                Rectangle r = e.getValue().getLeft();
                float scale = (float) (mc.getWindow().getScaleFactor());
                Vec2f center = new Vec2f(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f);
                Vec2f entity = new Vec2f((float) ((r.x + r.z) / 2f), (float) ((r.y + r.w) / 2f));
                if (!e.getValue().getRight()) {
                    entity = entity.add(center.multiply(-1)).multiply(Math.max(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
                }

                ImGui.getBackgroundDrawList().addLine(center.x * scale, center.y * scale, entity.x * scale, entity.y * scale, c.getU32());
            }
        }
    }
}
