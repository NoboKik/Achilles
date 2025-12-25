package template.rip.module.modules.render;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.Rectangle;
import template.rip.api.util.ColorUtil;
import template.rip.api.util.CrystalUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.Map;

public class ESP2DModule extends Module implements Renderable {

    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");

    public enum healthBarEnum {Left, Right, Off}
    public final ModeSetting<healthBarEnum> healthBar = new ModeSetting<>(this, healthBarEnum.Left, "Health ESP");

    public enum espStyleEnum {Box, Corners, Sides}
    public final ModeSetting<espStyleEnum> espStyle = new ModeSetting<>(this, espStyleEnum.Box, "ESP Style");
    public final NumberSetting cornerSize = new NumberSetting(this, 20, 1, 50, 1, "Corner Size");
    public final NumberSetting width = new NumberSetting(this, 0, 0, 30, 1, "Width");
    public final NumberSetting height = new NumberSetting(this, 0, 0, 30, 1, "Height");
    public final NumberSetting thickness = new NumberSetting(this, 1, 0, 10, 1, "Thickness");
    public final NumberSetting healthThickness = new NumberSetting(this, 3, 0, 20, 1, "Health Thickness");
    public final NumberSetting healthOffset = new NumberSetting(this, 5, 1, 20, 1, "Health Offset");
//    public final BooleanSetting armor = new BooleanSetting("Armor", this, false);
//    public final NumberSetting armorScale = new NumberSetting("Armor Scale", this, 2, 1, 3, 0.01);
    public final DividerSetting colorDivider = new DividerSetting(this, false, "Colors Divider");
    public final BooleanSetting inheritColor = new BooleanSetting(this, false, "Inherit Color");
    public final BooleanSetting teamColor = new BooleanSetting(this, false, "Team Color");
    public final BooleanSetting hurtColor = new BooleanSetting(this, false, "Hurt Color");
    public final BooleanSetting friendHasColor = new BooleanSetting(this, false, "Enable Friend Color");
    private final ColorSetting friendColor = new ColorSetting(this, new JColor(0.90f, 0.27f, 0.33f, 1f), true, "Friend Color");
    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave");
    public final BooleanSetting waveEnabled = new BooleanSetting(this, false, "Wave Enabled");
    public final NumberSetting waveIndex = new NumberSetting(this, 50, 1, 500, 1, "Wave Index");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 1, 10, 1, "Wave Speed");
    public final BooleanSetting rainbowEnabled = new BooleanSetting(this, false, "Rainbow Enabled");
    public final NumberSetting rainbowSaturation = new NumberSetting(this, 0.8, 0, 1, 0.01, "Rainbow Saturation");
    public final NumberSetting rainbowBrightness = new NumberSetting(this, 1, 0, 1, 0.01, "Rainbow Brightness");
    private final ColorSetting color = new ColorSetting(this, new JColor(0.90f, 0.27f, 0.33f, 1f), true, "Color");
    private final ColorSetting borderColor = new ColorSetting(this, new JColor(0f, 0f, 0f, 1f), true, "Border Color");

    public ESP2DModule(Category category, Description description, String name) {
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

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        ImGui.setNextWindowSize(1, 1);
        ImGui.setNextWindowPos(10000, 10000);
        ImGui.begin(this.getName(), imGuiWindowFlags);

        JColor colorOne = UI.getColorOne();
        JColor colorTwo = UI.getColorTwo();
        JColor colorThree = UI.getColorOne();
        JColor colorFour = UI.getColorTwo();
        JColor colorHurt = new JColor(1f,0f,0f,1f);

        if (waveEnabled.isEnabled()) {
            colorOne = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), waveIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false));
            colorTwo = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 2 * waveIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false));
            colorThree = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 3 * waveIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false));
            colorFour = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 4 * waveIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false));
        }

        if (waveEnabled.isEnabled() && rainbowEnabled.isEnabled()) {
            colorOne = new JColor(ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), waveIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f));
            colorTwo = new JColor(ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 2 * waveIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f));
            colorThree = new JColor(ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 3 * waveIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f));
            colorFour = new JColor(ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 4 * waveIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f));
        }

        float scale = (float) (mc.getWindow().getScaleFactor());
        float[] c = borderColor.getColor().getFloatColorWAlpha();
        float[] c2 = color.getColor().getFloatColorWAlpha();

        for (Map.Entry<Entity, Pair<Rectangle, Boolean>> e : CrystalUtils.getEntrySet()) {
            if (mc.world == null || mc.world.getEntityById(e.getKey().getId()) == null || !PlayerUtils.findTargets(true).contains(e.getKey()) || !e.getValue().getLeft().safe()) {
                continue;
            }
            float[] color1 = colorOne.getFloatColorWAlpha();
            float[] color2 = colorTwo.getFloatColorWAlpha();
            float[] color3 = colorThree.getFloatColorWAlpha();
            float[] color4 = colorFour.getFloatColorWAlpha();
            if (inheritColor.isEnabled()) c2 = color1;

            if (teamColor.isEnabled()) {
                int col = e.getKey().getTeamColorValue();
                int red = (col >> 16) & 0xFF;
                int green = (col >> 8) & 0xFF;
                int blue = col & 0xFF;
                color1 = color2 = color3 = color4 = new JColor(red,green,blue).getFloatColorWAlpha();
            }
            Entity ent = e.getKey();
            if (ent instanceof PlayerEntity pe && friendHasColor.isEnabled() && PlayerUtils.isFriend(pe)) {
                color1 = color2 = color3 = color4 = new JColor(friendColor.getColor()).getFloatColorWAlpha();
            }
            if (ent instanceof LivingEntity && ((LivingEntity) e.getKey()).hurtTime != 0 && hurtColor.isEnabled())
                color1 = color2 = color3 = color4 = colorHurt.getFloatColorWAlpha();

            if (e.getValue().getRight() && e.getValue().getLeft() != null) {
                int offset = 0;
                Rectangle rect = e.getValue().getLeft();
                float rx = (float) rect.x;
                float ry = (float) rect.y;
                float rz = (float) rect.z;
                float rw = (float) rect.w;

                float corner = ((rz * scale + width.getFValue())-(rx * scale - width.getFValue()))*(cornerSize.getFValue()/100);

                if (espStyle.is(espStyleEnum.Box)) {
                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                    if (waveEnabled.isEnabled()) {
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset);
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset);
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                } else if (espStyle.is(espStyleEnum.Sides)) {
                    // Middle

                    // Top
                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() + corner,
                            ry * scale - height.getFValue(),
                            rz * scale + width.getFValue() - corner,
                            ry * scale - height.getFValue() + thickness.getFValue()
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + corner,
                            ry * scale - height.getFValue() - 2,
                            rz * scale + width.getFValue() - corner - 1,
                            rw * scale + height.getFValue() + 2,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );


                    ImGui.getBackgroundDrawList().popClipRect();

                    // Bottom
                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() + corner,
                            rw * scale + height.getFValue() - thickness.getFValue(),
                            rz * scale + width.getFValue() - corner,
                            rw * scale + height.getFValue()
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + corner,
                            ry * scale - height.getFValue() - 2,
                            rz * scale + width.getFValue() - corner - 1,
                            rw * scale + height.getFValue() + 2,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();


                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() - 1,
                            ry * scale - height.getFValue() - 1,
                            rx * scale - width.getFValue() + corner + 1,
                            rw * scale + height.getFValue() + 1
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    if (waveEnabled.isEnabled()) {
                        // Inside Start
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // Inside end
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();
                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();

                    // Right
                    offset = 0;

                    ImGui.getBackgroundDrawList().pushClipRect(
                            rz * scale + width.getFValue() - corner - 1,
                            ry * scale - height.getFValue() - 1,
                            rz * scale + width.getFValue() + 1,
                            rw * scale + height.getFValue() + 1
                    );


                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                    if (waveEnabled.isEnabled()) {
                        // Inside Start
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // Inside end
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();
                } else if (espStyle.is(espStyleEnum.Corners)) {
                    // Middle

                    // Top-Vertical
                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() + corner,
                            ry * scale - height.getFValue(),
                            rz * scale + width.getFValue() - corner,
                            ry * scale - height.getFValue() + thickness.getFValue()
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + corner,
                            ry * scale - height.getFValue() - 2,
                            rz * scale + width.getFValue() - corner - 1,
                            rw * scale + height.getFValue() + 2,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );


                    ImGui.getBackgroundDrawList().popClipRect();

                    // Bottom-Vertical
                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() + corner,
                            rw * scale + height.getFValue() - thickness.getFValue(),
                            rz * scale + width.getFValue() - corner,
                            rw * scale + height.getFValue()
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + corner,
                            ry * scale - height.getFValue() - 2,
                            rz * scale + width.getFValue() - corner - 1,
                            rw * scale + height.getFValue() + 2,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();

                    // Left-Horizontal
                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue(),
                            ry * scale - height.getFValue() + corner,
                            rx * scale - width.getFValue()+ thickness.getFValue(),
                            rw * scale + height.getFValue() - corner
                    );
                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue()-2,
                            ry * scale - height.getFValue() + corner+1,
                            rz * scale + width.getFValue()+2,
                            rw * scale + height.getFValue() - corner,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                    ImGui.getBackgroundDrawList().popClipRect();

                    // Right-Horizontal
                    ImGui.getBackgroundDrawList().pushClipRect(
                            rz * scale + width.getFValue()- thickness.getFValue(),
                            ry * scale - height.getFValue() + corner,
                            rz * scale + width.getFValue(),
                            rw * scale + height.getFValue() - corner
                    );
                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue()-2,
                            ry * scale - height.getFValue() + corner+1,
                            rz * scale + width.getFValue()+2,
                            rw * scale + height.getFValue() - corner,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                    ImGui.getBackgroundDrawList().popClipRect();


                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() - 1,
                            ry * scale - height.getFValue() - 1,
                            rx * scale - width.getFValue() + corner + 1,
                            ry * scale - height.getFValue() + corner + 1
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    if (waveEnabled.isEnabled()) {
                        // Inside Start
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // Inside end
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();
                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();

                    // Right-Top
                    offset = 0;

                    ImGui.getBackgroundDrawList().pushClipRect(
                            rz * scale + width.getFValue() - corner - 1,
                            ry * scale - height.getFValue() - 1,
                            rz * scale + width.getFValue() + 1,
                            ry * scale - height.getFValue() + corner + 1
                    );


                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                    if (waveEnabled.isEnabled()) {
                        // Inside Start
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // Inside end
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();

                    // Bottom-Left
                    offset = 0;


                    ImGui.getBackgroundDrawList().pushClipRect(
                            rx * scale - width.getFValue() - 1,
                            rw * scale + height.getFValue() - corner - 1,
                            rx * scale - width.getFValue() + corner + 1,
                            rw * scale + height.getFValue() + 1
                    );

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    if (waveEnabled.isEnabled()) {
                        // Inside Start
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // Inside end
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();
                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();

                    // Right-Bottom
                    offset = 0;

                    ImGui.getBackgroundDrawList().pushClipRect(
                            rz * scale + width.getFValue() - corner - 1,
                            rw * scale + height.getFValue() - corner - 1,
                            rz * scale + width.getFValue() + 1,
                            rw * scale + height.getFValue() + 1
                    );


                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );
                    if (waveEnabled.isEnabled()) {
                        // Inside Start
                        offset = 1;
                        // *....
                        // *   .
                        // *....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rx * scale - width.getFValue() + offset + thickness.getIValue(),
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color3[0], color3[1], color3[2], color3[3]),
                                ImGui.getColorU32(color4[0], color4[1], color4[2], color4[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // ....*
                        // .   *
                        // ....*
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rz * scale + width.getFValue() - offset - thickness.getFValue() + 1,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset + 1,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();

                        // *****
                        // .   .
                        // .....
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset - 1,
                                rz * scale + width.getFValue() - offset,
                                ry * scale - height.getFValue() + offset + thickness.getFValue() - 1, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // .....
                        // .   .
                        // *****
                        ImGui.getBackgroundDrawList().pushClipRect(
                                rx * scale - width.getFValue() + offset,
                                rw * scale + height.getFValue() - offset - thickness.getFValue(),
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset, true
                        );
                        ImGui.getBackgroundDrawList().addRectFilledMultiColor(
                                rx * scale - width.getFValue() + offset,
                                ry * scale - height.getFValue() + offset,
                                rz * scale + width.getFValue() - offset,
                                rw * scale + height.getFValue() - offset,
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3]),
                                ImGui.getColorU32(color1[0], color1[1], color1[2], color1[3]),
                                ImGui.getColorU32(color2[0], color2[1], color2[2], color2[3])
                        );
                        ImGui.getBackgroundDrawList().popClipRect();
                        // Inside end
                    } else {
                        for (int i = 0; i < thickness.getIValue(); i++) {
                            offset++;
                            ImGui.getBackgroundDrawList().addRect(
                                    rx * scale - width.getFValue() + offset,
                                    ry * scale - height.getFValue() + offset,
                                    rz * scale + width.getFValue() - offset,
                                    rw * scale + height.getFValue() - offset,
                                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                                    0
                            );
                        }
                    }
                    offset = thickness.getIValue();

                    ImGui.getBackgroundDrawList().addRect(
                            rx * scale - width.getFValue() + offset,
                            ry * scale - height.getFValue() + offset,
                            rz * scale + width.getFValue() - offset,
                            rw * scale + height.getFValue() - offset,
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().popClipRect();
                }

                if (!healthBar.is(healthBarEnum.Off) && e.getKey() instanceof LivingEntity entity) {
                    float h = (float) (((e.getValue().getLeft().w * scale) + height.getFValue() - 1) - ((e.getValue().getLeft().y * scale) - height.getFValue() + 1));
                    float w = ((float) (e.getValue().getLeft().x * scale - width.getFValue() + 1) - ((float) (e.getValue().getLeft().z * scale - width.getFValue() - 1)));
                    float hp = Math.min((entity.getHealth() + entity.getAbsorptionAmount()) / entity.getMaxHealth(), 1f);

                    float divide = entity.getHealth() / entity.getMaxHealth();
                    int g = Math.round(255 * divide);
                    int r = 255 - Math.round(255 * divide);
                    if (entity.getAbsorptionAmount() > 0f) {
                        g = 255;
                        r = 255;
                    }
                    float offsetWidth = -healthOffset.getFValue();
                    if (healthBar.is(healthBarEnum.Right)) offsetWidth = -w + healthOffset.getFValue();

                    ImGui.getBackgroundDrawList().addRect(
                            (rx * scale) - width.getFValue() + offsetWidth - healthThickness.getFValue(),
                            (ry * scale) - height.getFValue(),
                            (rx * scale) - width.getFValue() + offsetWidth,
                            (rw * scale) + height.getFValue(),
                            ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                            0
                    );

                    ImGui.getBackgroundDrawList().addRectFilled(
                            (rx * scale) - width.getFValue() + offsetWidth - healthThickness.getFValue() + 1,
                            (ry * scale) - height.getFValue() + 1 - h * hp + h,
                            (rx * scale) - width.getFValue() + offsetWidth - 1,
                            (rw * scale) + height.getFValue() - 1,
                            ImGui.getColorU32((float) r / 255, (float) g / 255, 0, 1f),
                            0
                    );

                }
            }
        }
        ImGui.end();
    }

    /*@EventHandler
    private void onRender(HudRenderEvent event) {
        if (!armor.isEnabled()) return;
        for (Map.Entry<LivingEntity, Pair<Rectangle, Boolean>> e : positions.entrySet()) {
            LivingEntity target = e.getKey();
            Rectangle rect = e.getValue().getLeft();
            if (mc.world == null || mc.world.getEntityById(target.getId()) == null || !PlayerUtils.findTargets(true).contains(e.getKey()) || !rect.safe() || !e.getValue().getRight()) {
                continue;
            }
            float scale = (float) (mc.getWindow().getScaleFactor());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            double i = 1;
            double distance = distance(target.getPos());
            ArrayList<ItemStack> armors = new ArrayList<>();
            armors.add(target.getEquippedStack(EquipmentSlot.HEAD));
            armors.add(target.getEquippedStack(EquipmentSlot.CHEST));
            armors.add(target.getEquippedStack(EquipmentSlot.LEGS));
            armors.add(target.getEquippedStack(EquipmentSlot.FEET));
            float armorScal = armorScale.getFValue() * 5f;
            for (ItemStack item : armors) {
                RenderUtils.renderHotbarItem(event.context, (float) (rect.z + ((rect.z - rect.x) / 10.0)), (float) MathHelper.lerp(i, rect.w, rect.y), event.tickDelta, target, item, 1, (float) ((1/(distance))*armorScal)/scale);
                i -= 0.25;
            }
            RenderSystem.disableBlend();
        }
    }*/

    private double distance(Vec3d vec) {
        Camera cam = mc.gameRenderer.getCamera();
        if (cam != null) return cam.getPos().distanceTo(vec);
        else return vec.length();
    }
}
