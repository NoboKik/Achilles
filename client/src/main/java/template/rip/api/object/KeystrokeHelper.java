package template.rip.api.object;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.util.UI;
import template.rip.module.modules.legit.KeystrokesModule;

import java.util.ArrayList;
import java.util.List;

public class KeystrokeHelper {

    public int key;
    public String display;
    public long pressTime;
    public boolean pressed;
    public float index;
    public static List<KeystrokeHelper> list = new ArrayList<>();

    public KeystrokeHelper(int key, String display) {
        new KeystrokeHelper(key, display, 1);
    }

    public KeystrokeHelper(int key, String display, float index) {
        this.key = key;
        this.display = display;
        this.index = index;
        list.add(this);
    }

    public static KeystrokeHelper getHelper(int key) {
        for (KeystrokeHelper k : list) {
            if (k.key == key) return k;
        }
        return null;
    }

    public void drawButton() {
        KeystrokesModule module = Template.moduleManager.getModule(KeystrokesModule.class);
        ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
        double percent = Math.sin((((double) Math.min(System.currentTimeMillis() - this.pressTime, module.fadeTime.getFValue()) / module.fadeTime.getFValue()) * Math.PI) / 2);
        //double percent = 1;
        if (this.pressTime == 0) percent = 1;

        JColor bgColor = module.background.getColor();
        float waveIndex = module.waveIndex.getFValue();
        if (module.reverseWave.isEnabled()) waveIndex = -waveIndex;
        if (module.waveEnabled.isEnabled() && module.backgroundW.isEnabled())
            bgColor = new JColor(UI.interfaceColor((int) (module.waveSpeed.getMaximum() + 1) - module.waveSpeed.getIValue(), (int) (index * waveIndex)));

        JColor pressedBgColor = module.pressedBackground.getColor();
        if (module.waveEnabled.isEnabled() && module.pressedBackgroundW.isEnabled())
            pressedBgColor = new JColor(UI.interfaceColor((int) (module.waveSpeed.getMaximum() + 1) - module.waveSpeed.getIValue(),  (int) (index * waveIndex)));

        JColor textColor = module.text.getColor();
        if (module.waveEnabled.isEnabled() && module.textW.isEnabled())
            textColor = new JColor(UI.interfaceColor((int) (module.waveSpeed.getMaximum() + 1) - module.waveSpeed.getIValue(), (int) (index * waveIndex)));

        JColor pressedTextColor = module.pressedText.getColor();
        if (module.waveEnabled.isEnabled() && module.pressedTextW.isEnabled())
            pressedTextColor = new JColor(UI.interfaceColor((int) (module.waveSpeed.getMaximum() + 1) - module.waveSpeed.getIValue(), (int) (index * waveIndex)));

        float[] bgF;
        float[] pressedF = pressedBgColor.getFloatColor();
        float[] textF;
        if (pressed) {
            JColor bg = UI.blendColors(bgColor, pressedBgColor, (float) percent);
            bgF = bg.getFloatColor();
            JColor text = UI.blendColors(textColor, pressedTextColor, (float) percent);
            textF = text.getFloatColor();
        } else {
            JColor bg = UI.blendColors(pressedBgColor, bgColor, (float) percent);
            bgF = bg.getFloatColor();
            JColor text = UI.blendColors(pressedTextColor, textColor, (float) percent);
            textF = text.getFloatColor();
        }
        if (module.scaleIn.isEnabled()) {
            bgF = bgColor.getFloatColorWAlpha();
        }

        ImVec2 pos = ImGui.getCursorPos();
        float oldScale = ImGui.getFont().getScale();
        ImFont newFont = ImGui.getFont();

        /*float scaleChange;

        if (!pressed) scaleChange = (float) (0.8f + percent * 0.2f);
        else scaleChange = (float) (1f - percent * 0.2f);

        if (false) {
            if (!pressed)
                newFont.setScale(ImGui.getFont().getScale() * scaleChange);
            else
                newFont.setScale(ImGui.getFont().getScale() * scaleChange);
        }*/
        ImGui.pushFont(newFont);
        ImGui.setCursorPos(pos.x, pos.y);
        ImGui.pushStyleColor(ImGuiCol.Text, textF[0], textF[1], textF[2], textF[3]);
        ImGui.pushStyleColor(ImGuiCol.Button, bgF[0], bgF[1], bgF[2], bgF[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, bgF[0], bgF[1], bgF[2], bgF[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, bgF[0], bgF[1], bgF[2], bgF[3]);

        float x = ImGui.calcTextSize(this.display).x;
        float y = ImGui.calcTextSize(this.display).y;

        if (key == GLFW.GLFW_MOUSE_BUTTON_LEFT || key == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            //ImGui.button(this.display,
            //        77f * module.scale.getFValue(),
            //        50f * module.scale.getFValue());
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX() + (77f * module.scale.getFValue()),
                    ImGui.getCursorScreenPosY() + (50f * module.scale.getFValue()),
                    ImGui.getColorU32(bgF[0], bgF[1], bgF[2], bgF[3]),
                    module.roundedCorners.getFValue() * module.scale.getFValue()
            );

            if (module.scaleIn.isEnabled()) {
                float perc = 1f - (float) percent / 2;
                if (pressed) perc = (float) percent / 2 + 0.5f;

                float w = (77f * module.scale.getFValue());
                float h = (50f * module.scale.getFValue());
                float paddingH;
                float paddingV;

                w = perc * w;
                if (w < module.roundedCorners.getFValue())
                    w = module.roundedCorners.getFValue();
                paddingH = (77f * module.scale.getFValue()) - w;

                h = perc * h;
                if (h < module.roundedCorners.getFValue())
                    h = module.roundedCorners.getFValue();
                paddingV = (50f * module.scale.getFValue()) - h;

                ImGui.getWindowDrawList().addRectFilled(
                        ImGui.getCursorScreenPosX() + paddingH,
                        ImGui.getCursorScreenPosY() + paddingV,
                        ImGui.getCursorScreenPosX() + w,
                        ImGui.getCursorScreenPosY() + h,
                        ImGui.getColorU32(pressedF[0], pressedF[1], pressedF[2], pressedF[3]),
                        module.roundedCorners.getFValue() * module.scale.getFValue()
                );
            }
            ImGui.getWindowDrawList().addText(
                    ImGui.getCursorScreenPosX()+(77f * module.scale.getFValue()) / 2 - x / 2,
                    ImGui.getCursorScreenPosY()+(50f * module.scale.getFValue()) / 2 - y / 2,
                    ImGui.getColorU32(textF[0], textF[1], textF[2], textF[3]),
                    this.display
            );
            ImGui.setCursorPosX(ImGui.getCursorPosX() + 50f + 4f);
        } else if (key == GLFW.GLFW_KEY_SPACE) {
            //ImGui.button(this.display, 158f * module.scale.getFValue(), 50f * module.scale.getFValue());
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX() + (158f * module.scale.getFValue()),
                    ImGui.getCursorScreenPosY() + (50f * module.scale.getFValue()),
                    ImGui.getColorU32(bgF[0], bgF[1], bgF[2], bgF[3]),
                    module.roundedCorners.getFValue() * module.scale.getFValue()
            );
            if (module.scaleIn.isEnabled()) {
                float perc = 1f - (float) percent / 2;
                if (pressed) perc = (float) percent / 2 + 0.5f;

                float w = (158f * module.scale.getFValue());
                float h = (50f * module.scale.getFValue());
                float paddingH;
                float paddingV;

                w = perc * w;
                if (w < module.roundedCorners.getFValue())
                    w = module.roundedCorners.getFValue();
                paddingH = (158f * module.scale.getFValue()) - w;

                h = perc * h;
                if (h < module.roundedCorners.getFValue())
                    h = module.roundedCorners.getFValue();
                paddingV = (50f * module.scale.getFValue()) - h;

                ImGui.getWindowDrawList().addRectFilled(
                        ImGui.getCursorScreenPosX() + paddingH,
                        ImGui.getCursorScreenPosY() + paddingV,
                        ImGui.getCursorScreenPosX() + w,
                        ImGui.getCursorScreenPosY() + h,
                        ImGui.getColorU32(pressedF[0], pressedF[1], pressedF[2], pressedF[3]),
                        module.roundedCorners.getFValue() * module.scale.getFValue()
                );
            }
            ImGui.getWindowDrawList().addText(
                    ImGui.getCursorScreenPosX()+(158f * module.scale.getFValue()) / 2 - x / 2,
                    ImGui.getCursorScreenPosY()+(50f * module.scale.getFValue()) / 2 - y / 2,
                    ImGui.getColorU32(textF[0], textF[1], textF[2], textF[3]),
                    this.display
            );
            ImGui.setCursorPosX(ImGui.getCursorPosX() + 50f + 4f);
        } else {
            //ImGui.button(this.display, 50f * module.scale.getFValue(), 50f * module.scale.getFValue());
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX() + (50f * module.scale.getFValue()),
                    ImGui.getCursorScreenPosY() + (50f * module.scale.getFValue()),
                    ImGui.getColorU32(bgF[0], bgF[1], bgF[2], bgF[3]),
                    module.roundedCorners.getFValue() * module.scale.getFValue()
            );
            if (module.scaleIn.isEnabled()) {
                float perc = 1f - (float) percent / 2;
                if (pressed) perc = (float) percent / 2 + 0.5f;

                float w = (50f * module.scale.getFValue());
                float h = (50f * module.scale.getFValue());
                float paddingH;
                float paddingV;

                w = perc * w;
                if (w < module.roundedCorners.getFValue())
                    w = module.roundedCorners.getFValue();
                paddingH = (50f * module.scale.getFValue()) - w;

                h = perc * h;
                if (h < module.roundedCorners.getFValue())
                    h = module.roundedCorners.getFValue();
                paddingV = (50f * module.scale.getFValue()) - h;

                ImGui.getWindowDrawList().addRectFilled(
                        ImGui.getCursorScreenPosX() + paddingH,
                        ImGui.getCursorScreenPosY() + paddingV,
                        ImGui.getCursorScreenPosX() + w,
                        ImGui.getCursorScreenPosY() + h,
                        ImGui.getColorU32(pressedF[0], pressedF[1], pressedF[2], pressedF[3]),
                        module.roundedCorners.getFValue() * module.scale.getFValue()
                );
            }
            ImGui.getWindowDrawList().addText(
                    ImGui.getCursorScreenPosX() + (50f * module.scale.getFValue()) / 2 - x / 2,
                    ImGui.getCursorScreenPosY() + (50f * module.scale.getFValue()) / 2 - y / 2,
                    ImGui.getColorU32(textF[0], textF[1], textF[2], textF[3]),
                    this.display
            );
            ImGui.setCursorPosX(ImGui.getCursorPosX() + 50f + 4f);
        }
        ImGui.popFont();
        ImGui.popStyleColor(4);
    }
}
