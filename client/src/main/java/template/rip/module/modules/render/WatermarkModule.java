package template.rip.module.modules.render;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.ColorUtil;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.RenderUtils;
import template.rip.api.util.UI;
import template.rip.deprecated.StatusHUDModule;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.*;

import java.awt.*;

public class WatermarkModule extends Module implements Renderable {

    public enum modeEnum{Text, Line}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Text, "Mode");

    // Text Mode
    public final DividerSetting textDividerText = new DividerSetting(this, false, "Text");
    //public final ColorSetting textColor1 = new ColorSetting("Text Color #1 ", this, new JColor(0.79f, 0.24f, 0.32f, 1f), true);
    //public final ColorSetting textColor2 = new ColorSetting("Text Color #2 ", this, new JColor(0.79f, 0.24f, 0.32f, 1f).jDarker(), true);
    public final BooleanSetting lowercaseText = new BooleanSetting(this, false, "Text Lowercase");
    public final BooleanSetting textShadowText = new BooleanSetting(this, true, "Text Shadow");
    public final BooleanSetting minecraftFontText = new BooleanSetting(this, false, "Minecraft Font");
    public final DividerSetting waveDividerText = new DividerSetting(this, false, "Text Wave");
    public final BooleanSetting waveEnabledText = new BooleanSetting(this, true, "Text Wave Enabled");
    public final NumberSetting waveIndexText = new NumberSetting(this, 50, 1, 100, 1, "Text Wave Index");
    public final NumberSetting waveSpeedText = new NumberSetting(this, 1, 1, 10, 1, "Text Wave Speed");
    public final BooleanSetting rainbowEnabledText = new BooleanSetting(this, false, "Text Rainbow Enabled");
    public final NumberSetting rainbowSaturationText = new NumberSetting(this, 0.8, 0, 1, 0.01, "Text Rainbow Saturation");
    public final NumberSetting rainbowBrightnessText = new NumberSetting(this, 1, 0, 1, 0.01, "Text Rainbow Brightness");
    public final DividerSetting glowDividerText = new DividerSetting(this, false, "Text Glow");
    public final BooleanSetting glowEnabledText = new BooleanSetting(this, false, "Text Glow Enabled ");
    public final BooleanSetting glowInheritColorText = new BooleanSetting(this, false, "Text Glow Inherit Color ");
    public final ColorSetting glowColorText = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Text Glow Color");
    public final NumberSetting glowSizeText = new NumberSetting(this, 30, 0, 60, 1, "Text Glow Size");

    // Line Mode
    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");
    public enum seperatorEnum{Dash, Divider, None}
    public final ModeSetting<StatusHUDModule.seperatorEnum> seperator = new ModeSetting<>(this, StatusHUDModule.seperatorEnum.Dash, "Seperator");
    public final ColorSetting bgColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.19f, 1f), true, "Background Color");
    public final NumberSetting rounding = new NumberSetting(this, 10, 0, 30, 1, "Rounding");
    public final NumberSetting height = new NumberSetting(this, 30, 24, 50, 1, "Height");
    public final NumberSetting gap = new NumberSetting(this, 8, 0, 30, 1, "Gap");
    public final NumberSetting padding = new NumberSetting(this, 8, 0, 30, 1, "Padding");

    public final DividerSetting textDivider = new DividerSetting(this, false, "Text Divider");
    //public final ColorSetting clientColor1 = new ColorSetting("Client Color #1", this, new JColor(0.79f, 0.24f, 0.32f, 1f), true);
    //public final ColorSetting clientColor2 = new ColorSetting("Client Color #2", this, new JColor(0.79f, 0.24f, 0.32f, 1f).jDarker(), true);
    public final ColorSetting primaryColor = new ColorSetting(this, new JColor(0.83f, 0.86f, 0.94f, 1.00f), true, "Primary Color");
    public final ColorSetting secondaryColor = new ColorSetting(this, new JColor(0.83f, 0.86f, 0.94f, 1.00f).jDarker(), true, "Secondary Color");
    public final BooleanSetting textShadow = new BooleanSetting(this, true, "Has Text Shadow");
    public final BooleanSetting lowercase = new BooleanSetting(this, true, "Lowercase");
    public final BooleanSetting bold = new BooleanSetting(this, false, "Bold");

    public final DividerSetting modulesDivider = new DividerSetting(this, false, "Modules");
    public final BooleanSetting showName = new BooleanSetting(this, true, "Show Name");
    public final BooleanSetting showIP = new BooleanSetting(this, true, "Show Server IP");
    public final BooleanSetting showPing = new BooleanSetting(this, true, "Show Ping");
    public final BooleanSetting showFps = new BooleanSetting(this, true, "Show FPS");

    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave");
    public final BooleanSetting waveEnabled = new BooleanSetting(this, true, "Wave Enabled");
    public final NumberSetting waveIndex = new NumberSetting(this, 50, 1, 100, 1, "Wave Index");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 1, 10, 1, "Wave Speed");
    public final BooleanSetting rainbowEnabled = new BooleanSetting(this, false, "Rainbow Enabled");
    public final NumberSetting rainbowSaturation = new NumberSetting(this, 0.8, 0, 1, 0.01, "Rainbow Saturation");
    public final NumberSetting rainbowBrightness = new NumberSetting(this, 1, 0, 1, 0.01, "Rainbow Brightness");

    public final DividerSetting glowDivider = new DividerSetting(this, false, "Glow Divider");
    public final BooleanSetting glowEnabled = new BooleanSetting(this, false, "Glow Enabled");
    public final BooleanSetting glowInherit = new BooleanSetting(this, false, "Glow Wave");
    public final ColorSetting glowColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Glow Color");
    public final NumberSetting glowIndex = new NumberSetting(this, 30, 0, 100, 1, "Glow Index");
    public final NumberSetting glowSize = new NumberSetting(this, 30, 0, 60, 1, "Glow's Size");
    public final BooleanSetting glowFill = new BooleanSetting(this, false, "Glow's Fill");

    public final DividerSetting lineDivider = new DividerSetting(this, false, "Line");
    public final BooleanSetting lineEnabled = new BooleanSetting(this, false, "Line Enabled");
    public final BooleanSetting lineInherit = new BooleanSetting(this, false, "Line Inherit Color");
    public final ColorSetting lineColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Line Color");
    public final NumberSetting lineSize = new NumberSetting(this, 3, 0, 10, 1, "Line Size");

    public final DividerSetting outlineDivider = new DividerSetting(this, false, "Outline");
    public final BooleanSetting outlineEnabled = new BooleanSetting(this, false, "Outline Enabled");
    public final BooleanSetting outlineInherit = new BooleanSetting(this, false, "Outline Inherit Color");
    public final BooleanSetting outlineWave = new BooleanSetting(this, false, "Outline Wave");
    public final ColorSetting outlineColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Outline Color");
    public final NumberSetting outlineSize = new NumberSetting(this, 3, 0, 10, 1, "Outline Size");

    private boolean firstFrame;

    public WatermarkModule(Category category, Description description, String name) {
        super(category, description, name);
        firstFrame = true;
        mergeDividers();
        glowDividerText.addConditionMode(mode, modeEnum.Text);
        textDividerText.addConditionMode(mode, modeEnum.Text);
        waveDividerText.addConditionMode(mode, modeEnum.Text);

        generalDivider.addConditionMode(mode, modeEnum.Line);
        textDivider.addConditionMode(mode, modeEnum.Line);
        modulesDivider.addConditionMode(mode, modeEnum.Line);
        waveDivider.addConditionMode(mode, modeEnum.Line);
        glowDivider.addConditionMode(mode, modeEnum.Line);
        lineDivider.addConditionMode(mode, modeEnum.Line);
        outlineDivider.addConditionMode(mode, modeEnum.Line);
    }

    @Override
    public void onEnable() {
        ImguiLoader.addRenderable(this);
    }

    @Override
    public void onDisable() {
        ImguiLoader.queueRemove(this);
    }

    @Override
    public void render() {
        if (!Template.displayRender()) return;
        if (mode.is(modeEnum.Text)) {
            if (!this.isEnabled()) {
                firstFrame = true;
                return;
            }

            int imGuiWindowFlags = 0;
            imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
            imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
            imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
            imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
            imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
            imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
            imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
            if (Template.moduleManager.isModuleDisabled(AchillesSettingsModule.class)) imGuiWindowFlags |= ImGuiWindowFlags.NoMove;

            float windowWidth = 0;
            float windowHeight = ImGui.getTextLineHeightWithSpacing();

            ImGui.pushFont(UI.getFont(32, true));
            String[] strs = new String[]{(lowercaseText.isEnabled() ? "a" : "A"), "ch", "ill", "es"};
            windowWidth = Math.max(RenderUtils.calcTextSize(strs).x, windowWidth);
            ImGui.popFont();

            ImGui.getStyle().setWindowMinSize(windowWidth + 10, windowHeight + 10);

            if (this.updatedPos.x != 0) {
                super.position.x = super.position.x + this.updatedPos.x;
                this.updatedPos.x = 0;
                ImGui.setNextWindowPos(super.position.x, super.position.y);
            }
            if (this.updatedPos.y != 0) {
                super.position.y = super.position.y + this.updatedPos.y;
                this.updatedPos.y = 0;
                ImGui.setNextWindowPos(super.position.x, super.position.y);
            }
            if (firstFrame || reloadPosition || !Template.shouldMove()) {
                ImGui.setNextWindowPos(super.position.x, super.position.y);
                reloadPosition = false;
            }

            ImGui.begin(this.getName(), imGuiWindowFlags);
            if (minecraftFontText.isEnabled()) {
                ImGui.pushFont(UI.getFont(48, true, true));
            } else {
                ImGui.pushFont(UI.getFont(48, true));
            }

            int i = -1;
            for (String s : strs) {
                for (char c : s.toCharArray()) {
                    String text = String.valueOf(c);
                    i++;

                    float cursorX = ImGui.getCursorPosX();
                    float cursorY = ImGui.getCursorPosY();

                    Color initColor = UI.getColorOne();
                    if (waveEnabledText.isEnabled()) {
                        initColor = ColorUtil.interpolateColorsBackAndForth((int) (waveSpeedText.getMaximum() + 1) - waveSpeedText.getIValue(), i * waveIndexText.getIValue(), UI.getColorOne(), UI.getColorTwo(), false);
                    }

                    if (waveEnabledText.isEnabled() && rainbowEnabledText.isEnabled()) {
                        initColor = ColorUtil.rainbow((int) (waveSpeedText.getMaximum() + 1) - waveSpeedText.getIValue(), i * waveIndexText.getIValue(), rainbowSaturationText.getFValue(), rainbowBrightnessText.getFValue(), 1f);
                    }

                    float[] color = new JColor(initColor).getFloatColor();
                    float[] color2 = new JColor(initColor).jDarker().jDarker().getFloatColor();
                    if (minecraftFontText.isEnabled()) {
                        color2 = new JColor(color[0] / 4, color[1] / 4, color[2] / 4, 1f).getFloatColor();
                    }
                    if (glowEnabledText.isEnabled()) {
                        //float shift = 14;

                        float[] glowColorF = glowColorText.getColor().getFloatColorWAlpha();
                        float alpha = glowColorF[3];
                        if (glowInheritColorText.isEnabled()) glowColorF = new JColor(initColor).getFloatColor();

                        float centerX = ImGui.calcTextSize(text).x / 2;
                        float centerY = ImGui.calcTextSize(text).y / 2 + 4;

                        GuiUtils.drawShadow(
                                new ImVec2(ImGui.getWindowPosX() + cursorX + centerX, ImGui.getWindowPosY() + cursorY + centerY),
                                new ImVec2(ImGui.getWindowPosX() + cursorX + centerX, ImGui.getWindowPosY() + cursorY + centerY),
                                0,
                                ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], alpha),
                                ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], 0f),
                                glowSizeText.getFValue()
                        );
                    }

                    float offset = 1;
                    if (minecraftFontText.isEnabled()) offset = 3;
                    if (textShadowText.isEnabled()) {
                        ImGui.setCursorPos(cursorX + offset, cursorY + offset);
                        ImGui.pushStyleColor(ImGuiCol.Text, color2[0], color2[1], color2[2], 1f);
                        ImGui.text(text);
                        ImGui.popStyleColor();
                    }

                    ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                    ImGui.setCursorPos(cursorX, cursorY);
                    ImGui.text(text);
                    ImGui.popStyleColor();

                    ImGui.sameLine(0, 0);
                }
            }
            ImGui.popFont();
            super.position = ImGui.getWindowPos();
            isFocused = ImGui.isWindowFocused();

            super.position = ImGui.getWindowPos();
            isFocused = ImGui.isWindowFocused();

            ImGui.end();
            if (firstFrame) firstFrame = false;
        } else {
            if (!Template.moduleManager.getModule(this.getClass()).isEnabled()) {
                firstFrame = true;
                return;
            }

            // Flags
            int imGuiWindowFlags = 0;
            imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
            imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
            imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
            imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
            imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
            imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
            if (Template.moduleManager.isModuleDisabled(AchillesSettingsModule.class)) imGuiWindowFlags |= ImGuiWindowFlags.NoMove;

            // Colors
            float[] floatBgColor = bgColor.getColor().getFloatColorWAlpha();

            Color initColor = UI.getColorOne();

            if (waveEnabled.isEnabled()) {
                initColor = ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), waveIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false);
            }

            if (waveEnabled.isEnabled() && rainbowEnabled.isEnabled()) {
                initColor = ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), waveIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f);
            }

            // Values
            int fps = mc.getCurrentFps();

            int ping = 0;
            if (mc.getCurrentServerEntry() != null) ping = (int) mc.getCurrentServerEntry().ping;

            String ip = "localhost";
            if (mc.getCurrentServerEntry() != null) ip = mc.getCurrentServerEntry().address;

            String name = "???";
            if (mc.player != null) name = mc.player.getName().getString();

            // Prepare Style
            ImGui.pushStyleColor(ImGuiCol.WindowBg, floatBgColor[0], floatBgColor[1], floatBgColor[2], floatBgColor[3]);
            ImGui.pushStyleColor(ImGuiCol.Border, 0f,0f,0f,0f);
            ImGui.getStyle().setWindowRounding(rounding.getFValue());
            ImGui.getStyle().setWindowBorderSize(0);
            ImGui.getStyle().setWindowMinSize(1, height.getFValue());

            if (this.updatedPos.x != 0) {
                super.position.x = super.position.x + this.updatedPos.x;
                this.updatedPos.x = 0;
                ImGui.setNextWindowPos(super.position.x, super.position.y);
            }
            if (this.updatedPos.y != 0) {
                super.position.y = super.position.y + this.updatedPos.y;
                this.updatedPos.y = 0;
                ImGui.setNextWindowPos(super.position.x, super.position.y);
            }
            if (firstFrame) {
                ImGui.setNextWindowPos(super.position.x, super.position.y);
            }

            // Begin
            ImGui.begin(this.getName(), imGuiWindowFlags);
            ImGui.setCursorPos(padding.getFValue(), (height.getFValue()-24)/2);
            ImGui.pushFont(UI.getFont(24, bold.isEnabled()));

            // Client Text
            String[] strs = new String[]{"ach", "ill", "es.", "ac"};
            for (String s : strs) {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    String text = String.valueOf(c);

                    float cursorX = ImGui.getCursorPosX();
                    float cursorY = ImGui.getCursorPosY();

                    Color textColor = UI.getColorOne();
                    if (waveEnabled.isEnabled()) {
                        textColor = ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), i * waveIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false);
                    }

                    if (waveEnabled.isEnabled() && rainbowEnabled.isEnabled()) {
                        textColor = ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), i * waveIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f);
                    }

                    float[] color = new JColor(textColor).getFloatColor();
                    float[] color2 = new JColor(textColor).jDarker().jDarker().getFloatColor();

                    if (textShadow.isEnabled()) {
                        ImGui.setCursorPos(cursorX + 1, cursorY + 1);
                        ImGui.pushStyleColor(ImGuiCol.Text, color2[0], color2[1], color2[2], 1f);
                        ImGui.text(text);
                        ImGui.popStyleColor();
                    }

                    ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                    ImGui.setCursorPos(cursorX, cursorY);
                    ImGui.text(text);
                    ImGui.popStyleColor();
                    ImGui.sameLine(0, 0);
                    ImGui.setCursorPosX(ImGui.getCursorPosX() + gap.getFValue());

                    ImGui.sameLine(0, 0);
                }
            }
            ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());

            // FPS
            if (showFps.isEnabled()) {
                String sepText = "";
                if (seperator.is(StatusHUDModule.seperatorEnum.Dash)) sepText = "-";
                if (seperator.is(StatusHUDModule.seperatorEnum.Divider)) sepText = "|";
                float[] floatSecondaryColor1 = secondaryColor.getColor().getFloatColorWAlpha();
                float[] floatSecondaryColorShadow1 = secondaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursor = ImGui.getCursorPos();
                if (lowercase.isEnabled()) sepText = sepText.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursor.x+1, cursor.y+1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColorShadow1[0], floatSecondaryColorShadow1[1], floatSecondaryColorShadow1[2], floatSecondaryColorShadow1[3]);
                    ImGui.text(sepText);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursor.x, cursor.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColor1[0], floatSecondaryColor1[1], floatSecondaryColor1[2], floatSecondaryColor1[3]);
                ImGui.text(sepText);

                ImGui.sameLine(0,0);
                ImGui.popStyleColor(1);
                ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());

                String text = fps + " FPS ";
                if (lowercase.isEnabled()) text = text.toLowerCase();

                String theString = text;

                float[] floatPrimaryColor2 = primaryColor.getColor().getFloatColorWAlpha();
                float[] floatPrimaryColorShadow2 = primaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursorTS = ImGui.getCursorPos();
                if (lowercase.isEnabled()) theString = theString.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursorTS.x + 1, cursorTS.y + 1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColorShadow2[0], floatPrimaryColorShadow2[1], floatPrimaryColorShadow2[2], floatPrimaryColorShadow2[3]);
                    ImGui.text(theString);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursorTS.x, cursorTS.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColor2[0], floatPrimaryColor2[1], floatPrimaryColor2[2], floatPrimaryColor2[3]);
                ImGui.text(theString);

                ImGui.popStyleColor(1);
                ImGui.sameLine(0,0);

                if (showName.isEnabled())
                    ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());
            }

            // Name
            if (showName.isEnabled()) {
                String sepText = "";
                if (seperator.is(StatusHUDModule.seperatorEnum.Dash)) sepText = "-";
                if (seperator.is(StatusHUDModule.seperatorEnum.Divider)) sepText = "|";
                float[] floatSecondaryColor1 = secondaryColor.getColor().getFloatColorWAlpha();
                float[] floatSecondaryColorShadow1 = secondaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursor = ImGui.getCursorPos();
                if (lowercase.isEnabled()) sepText = sepText.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursor.x+1, cursor.y+1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColorShadow1[0], floatSecondaryColorShadow1[1], floatSecondaryColorShadow1[2], floatSecondaryColorShadow1[3]);
                    ImGui.text(sepText);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursor.x, cursor.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColor1[0], floatSecondaryColor1[1], floatSecondaryColor1[2], floatSecondaryColor1[3]);
                ImGui.text(sepText);

                ImGui.sameLine(0,0);
                ImGui.popStyleColor(1);
                ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());

                String theString = name;

                float[] floatPrimaryColor2 = primaryColor.getColor().getFloatColorWAlpha();
                float[] floatPrimaryColorShadow2 = primaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursorTS = ImGui.getCursorPos();
                if (lowercase.isEnabled()) theString = theString.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursorTS.x + 1, cursorTS.y + 1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColorShadow2[0], floatPrimaryColorShadow2[1], floatPrimaryColorShadow2[2], floatPrimaryColorShadow2[3]);
                    ImGui.text(theString);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursorTS.x, cursorTS.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColor2[0], floatPrimaryColor2[1], floatPrimaryColor2[2], floatPrimaryColor2[3]);
                ImGui.text(theString);

                ImGui.popStyleColor(1);
                ImGui.sameLine(0,0);


                if (showIP.isEnabled())
                    ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());
            }

            // IP
            if (showIP.isEnabled()) {

                String sepText = "";
                if (seperator.is(StatusHUDModule.seperatorEnum.Dash)) sepText = "-";
                if (seperator.is(StatusHUDModule.seperatorEnum.Divider)) sepText = "|";
                float[] floatSecondaryColor1 = secondaryColor.getColor().getFloatColorWAlpha();
                float[] floatSecondaryColorShadow1 = secondaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursor = ImGui.getCursorPos();
                if (lowercase.isEnabled()) sepText = sepText.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursor.x+1, cursor.y+1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColorShadow1[0], floatSecondaryColorShadow1[1], floatSecondaryColorShadow1[2], floatSecondaryColorShadow1[3]);
                    ImGui.text(sepText);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursor.x, cursor.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColor1[0], floatSecondaryColor1[1], floatSecondaryColor1[2], floatSecondaryColor1[3]);
                ImGui.text(sepText);

                ImGui.sameLine(0,0);
                ImGui.popStyleColor(1);
                ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());

                String theString = ip;

                float[] floatPrimaryColor2 = primaryColor.getColor().getFloatColorWAlpha();
                float[] floatPrimaryColorShadow2 = primaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursorTS = ImGui.getCursorPos();
                if (lowercase.isEnabled()) theString = theString.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursorTS.x + 1, cursorTS.y + 1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColorShadow2[0], floatPrimaryColorShadow2[1], floatPrimaryColorShadow2[2], floatPrimaryColorShadow2[3]);
                    ImGui.text(theString);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursorTS.x, cursorTS.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColor2[0], floatPrimaryColor2[1], floatPrimaryColor2[2], floatPrimaryColor2[3]);
                ImGui.text(theString);

                ImGui.popStyleColor(1);
                ImGui.sameLine(0,0);

                if (showPing.isEnabled())
                    ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());
            }

            // Ping
            if (showPing.isEnabled()) {

                String sepText = "";
                if (seperator.is(StatusHUDModule.seperatorEnum.Dash)) sepText = "-";
                if (seperator.is(StatusHUDModule.seperatorEnum.Divider)) sepText = "|";
                float[] floatSecondaryColor1 = secondaryColor.getColor().getFloatColorWAlpha();
                float[] floatSecondaryColorShadow1 = secondaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursor = ImGui.getCursorPos();
                if (lowercase.isEnabled()) sepText = sepText.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursor.x+1, cursor.y+1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColorShadow1[0], floatSecondaryColorShadow1[1], floatSecondaryColorShadow1[2], floatSecondaryColorShadow1[3]);
                    ImGui.text(sepText);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursor.x, cursor.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatSecondaryColor1[0], floatSecondaryColor1[1], floatSecondaryColor1[2], floatSecondaryColor1[3]);
                ImGui.text(sepText);

                ImGui.sameLine(0,0);
                ImGui.popStyleColor(1);
                ImGui.setCursorPosX(ImGui.getCursorPosX()+gap.getFValue());

                String theString = ping + " Ping";

                float[] floatPrimaryColor2 = primaryColor.getColor().getFloatColorWAlpha();
                float[] floatPrimaryColorShadow2 = primaryColor.getColor().jDarker().jDarker().getFloatColorWAlpha();
                ImVec2 cursorTS = ImGui.getCursorPos();
                if (lowercase.isEnabled()) theString = theString.toLowerCase();

                if (textShadow.isEnabled()) {
                    ImGui.setCursorPos(cursorTS.x + 1, cursorTS.y + 1);
                    ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColorShadow2[0], floatPrimaryColorShadow2[1], floatPrimaryColorShadow2[2], floatPrimaryColorShadow2[3]);
                    ImGui.text(theString);
                    ImGui.popStyleColor(1);
                }

                ImGui.setCursorPos(cursorTS.x, cursorTS.y);
                ImGui.pushStyleColor(ImGuiCol.Text, floatPrimaryColor2[0], floatPrimaryColor2[1], floatPrimaryColor2[2], floatPrimaryColor2[3]);
                ImGui.text(theString);

                ImGui.popStyleColor(1);
                ImGui.sameLine(0,0);
            }
            ImGui.setCursorPosX(ImGui.getCursorPosX()+padding.getFValue());
            ImGui.popFont();
            ImGui.setCursorPos(0, height.getFValue());

            // Glow
            if (glowEnabled.isEnabled()) {
                float[] glowColorF = glowColor.getColor().getFloatColorWAlpha();
                float alpha = glowColorF[3];

                if (!glowInherit.isEnabled()) {
                    glowColorF = new JColor(initColor).getFloatColor();
                    GuiUtils.drawWindowShadow(ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], alpha), ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], 0f), glowSize.getFValue(), glowFill.isEnabled());
                } else {
                    if (rainbowEnabled.isEnabled()) {
                        GuiUtils.drawHRainbowWindowShadow(rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), waveSpeed.getFValue(), glowIndex.getFValue(), glowSize.getFValue(), alpha, 0, glowFill.isEnabled());
                    } else {
                        GuiUtils.drawHWaveWindowShadow(waveSpeed.getFValue(), glowIndex.getFValue(), glowSize.getFValue(), glowFill.isEnabled(), alpha, 0);
                    }
                }
            }

            // Outline
            if (outlineEnabled.isEnabled() && false) {
                float size = outlineSize.getFValue();
                float[] outlineColorF = outlineColor.getColor().getFloatColorWAlpha();
                float alpha = outlineColorF[3];

                if (outlineInherit.isEnabled()) outlineColorF = new JColor(initColor).getFloatColor();
                ImGui.getBackgroundDrawList().addRectFilled(
                        ImGui.getWindowPosX() - size,
                        ImGui.getWindowPosY() - size,
                        ImGui.getWindowPosX() + ImGui.getWindowSizeX() + size,
                        ImGui.getWindowPosY() + ImGui.getWindowSizeY() + size,
                        ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                        ImGui.getStyle().getWindowRounding()+size
                );
            }

            // Outline
            if (outlineEnabled.isEnabled()) {
                float size = outlineSize.getFValue();
                float[] outlineColorF = outlineColor.getColor().getFloatColorWAlpha();
                if (outlineInherit.isEnabled()) outlineColorF = new JColor(initColor).getFloatColor();
                float alpha = outlineColorF[3];

                if (!outlineWave.isEnabled()) {
                    ImGui.getBackgroundDrawList().addRectFilled(
                            ImGui.getWindowPosX() - size,
                            ImGui.getWindowPosY() - size,
                            ImGui.getWindowPosX() + ImGui.getWindowSizeX() + size,
                            ImGui.getWindowPosY() + ImGui.getWindowSizeY() + size,
                            ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                            ImGui.getStyle().getWindowRounding()+size
                    );
                } else {
                    if (rainbowEnabled.isEnabled()) {
                        GuiUtils.drawHRainbowWindowShadow(rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), waveSpeed.getFValue(), glowIndex.getFValue(), size, alpha, alpha, false);
                    } else {
                        GuiUtils.drawHWaveWindowShadow(waveSpeed.getFValue(), glowIndex.getFValue(), size, false, alpha, alpha);
                    }
                }
            }

            // Line
            if (lineEnabled.isEnabled()) {
                float size = lineSize.getFValue();
                float[] lineColorF = lineColor.getColor().getFloatColorWAlpha();
                float alpha = lineColorF[3];

                if (lineInherit.isEnabled()) lineColorF = new JColor(initColor).getFloatColor();
                ImGui.getForegroundDrawList().addRectFilled(
                        ImGui.getWindowPosX(),
                        ImGui.getWindowPosY() + ImGui.getWindowSizeY() - size,
                        ImGui.getWindowPosX() + ImGui.getWindowSizeX(),
                        ImGui.getWindowPosY() + ImGui.getWindowSizeY(),
                        ImGui.getColorU32(lineColorF[0], lineColorF[1], lineColorF[2], alpha),
                        ImGui.getStyle().getWindowRounding()
                );
            }

            super.position = ImGui.getWindowPos();
            isFocused = ImGui.isWindowFocused();

            ImGui.end();
            if (firstFrame) firstFrame = false;
            ImGui.popStyleColor(2);
            ImGui.getStyle().setWindowRounding(8);
            ImGui.getStyle().setWindowBorderSize(1);
        }
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    private final Theme theme = new Theme() {

        @Override
        public void preRender() {
            float[][] colors = ImGui.getStyle().getColors();
            colors[ImGuiCol.Text] = new float[]{0.80f, 0.84f, 0.96f, 1.00f};
            colors[ImGuiCol.TextDisabled] = new float[]{0.58f, 0.60f, 0.70f, 1.00f};
            colors[ImGuiCol.WindowBg] = new float[]{0.09f, 0.09f, 0.15f, 1.00f};
            colors[ImGuiCol.ChildBg] = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.PopupBg] = new float[]{0.08f, 0.08f, 0.08f, 0.94f};
            colors[ImGuiCol.Border] = new float[]{0.43f, 0.43f, 0.50f, 0.50f};
            colors[ImGuiCol.BorderShadow] = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.FrameBg] = new float[]{0.90f, 0.27f, 0.33f, 0.59f};
            colors[ImGuiCol.FrameBgHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.85f};
            colors[ImGuiCol.FrameBgActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.TitleBg] = new float[]{0.04f, 0.04f, 0.04f, 1.00f};
            colors[ImGuiCol.TitleBgActive] = new float[]{0.46f, 0.15f, 0.18f, 1.00f};
            colors[ImGuiCol.TitleBgCollapsed] = new float[]{0.00f, 0.00f, 0.00f, 0.51f};
            colors[ImGuiCol.MenuBarBg] = new float[]{0.14f, 0.14f, 0.14f, 1.00f};
            colors[ImGuiCol.ScrollbarBg] = new float[]{0.02f, 0.02f, 0.02f, 0.53f};
            colors[ImGuiCol.ScrollbarGrab] = new float[]{0.31f, 0.31f, 0.31f, 1.00f};
            colors[ImGuiCol.ScrollbarGrabHovered] = new float[]{0.41f, 0.41f, 0.41f, 1.00f};
            colors[ImGuiCol.ScrollbarGrabActive] = new float[]{0.51f, 0.51f, 0.51f, 1.00f};
            colors[ImGuiCol.CheckMark] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.SliderGrab] = new float[]{0.77f, 0.23f, 0.27f, 1.00f};
            colors[ImGuiCol.SliderGrabActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.Button] = new float[]{0.90f, 0.27f, 0.33f, 0.45f};
            colors[ImGuiCol.ButtonHovered] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.ButtonActive] = new float[]{0.75f, 0.21f, 0.25f, 1.00f};
            colors[ImGuiCol.Header] = new float[]{0.90f, 0.27f, 0.33f, 0.32f};
            colors[ImGuiCol.HeaderHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.73f};
            colors[ImGuiCol.HeaderActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.Separator] = new float[]{0.42f, 0.44f, 0.52f, 1.00f};
            colors[ImGuiCol.SeparatorHovered] = new float[]{0.81f, 0.25f, 0.30f, 0.78f};
            colors[ImGuiCol.SeparatorActive] = new float[]{0.76f, 0.22f, 0.26f, 1.00f};
            colors[ImGuiCol.ResizeGrip] = new float[]{0.90f, 0.27f, 0.33f, 0.21f};
            colors[ImGuiCol.ResizeGripHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.79f};
            colors[ImGuiCol.ResizeGripActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.Tab] = new float[]{0.56f, 0.17f, 0.21f, 0.85f};
            colors[ImGuiCol.TabHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.85f};
            colors[ImGuiCol.TabActive] = new float[]{0.70f, 0.22f, 0.26f, 1.00f};
            colors[ImGuiCol.TabUnfocused] = new float[]{0.15f, 0.07f, 0.07f, 0.97f};
            colors[ImGuiCol.TabUnfocusedActive] = new float[]{0.42f, 0.14f, 0.14f, 1.00f};
            colors[ImGuiCol.DockingPreview] = new float[]{0.90f, 0.27f, 0.33f, 0.70f};
            colors[ImGuiCol.DockingEmptyBg] = new float[]{0.20f, 0.20f, 0.20f, 1.00f};
            colors[ImGuiCol.PlotLines] = new float[]{0.61f, 0.61f, 0.61f, 1.00f};
            colors[ImGuiCol.PlotLinesHovered] = new float[]{1.00f, 0.43f, 0.35f, 1.00f};
            colors[ImGuiCol.PlotHistogram] = new float[]{0.90f, 0.70f, 0.00f, 1.00f};
            colors[ImGuiCol.PlotHistogramHovered] = new float[]{1.00f, 0.60f, 0.00f, 1.00f};
            colors[ImGuiCol.TableHeaderBg] = new float[]{0.19f, 0.19f, 0.20f, 1.00f};
            colors[ImGuiCol.TableBorderStrong] = new float[]{0.31f, 0.31f, 0.35f, 1.00f};
            colors[ImGuiCol.TableBorderLight] = new float[]{0.23f, 0.23f, 0.25f, 1.00f};
            colors[ImGuiCol.TableRowBg] = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.TableRowBgAlt] = new float[]{1.00f, 1.00f, 1.00f, 0.06f};
            colors[ImGuiCol.TextSelectedBg] = new float[]{0.90f, 0.27f, 0.33f, 0.35f};
            colors[ImGuiCol.DragDropTarget] = new float[]{1.00f, 1.00f, 0.00f, 0.90f};
            colors[ImGuiCol.NavHighlight] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.NavWindowingHighlight] = new float[]{1.00f, 1.00f, 1.00f, 0.70f};
            colors[ImGuiCol.NavWindowingDimBg] = new float[]{0.80f, 0.80f, 0.80f, 0.20f};
            colors[ImGuiCol.ModalWindowDimBg] = new float[]{0.80f, 0.80f, 0.80f, 0.35f};
            ImGui.getStyle().setColors(colors);

            ImGui.getStyle().setWindowRounding(8);
            ImGui.getStyle().setFrameRounding(4);
            ImGui.getStyle().setGrabRounding(4);
            ImGui.getStyle().setPopupRounding(4);
            ImGui.getStyle().setScrollbarRounding(4);
            ImGui.getStyle().setTabRounding(4);
            ImGui.getStyle().setWindowTitleAlign(0.5f, 0.5f);
            ImGui.getStyle().setScrollbarSize(1);

            ImGui.getStyle().setButtonTextAlign(0, 0.5f);
            ImGui.getStyle().setChildBorderSize(0f);
            ImGui.getStyle().setWindowPadding(0f, 0f);
            ImGui.getStyle().setFramePadding(0f, 0f);
            ImGui.getStyle().setCellPadding(0f, 0f);

            ImGui.getStyle().setItemSpacing(8f, 4f);
            ImGui.getStyle().setItemInnerSpacing(4f, 4f);

            if (ImguiLoader.poppins24 != null) {
                ImGui.pushFont(ImguiLoader.poppins24);
            }
        }

        @Override
        public void postRender() {
            if (ImguiLoader.poppins24 != null) {
                ImGui.popFont();
            }
        }
    };
}
