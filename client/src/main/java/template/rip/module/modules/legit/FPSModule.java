package template.rip.module.modules.legit;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.DividerSetting;
import template.rip.module.setting.settings.NumberSetting;

public class FPSModule extends Module implements Renderable {

    //public final PosSetting position = new PosSetting(this, 20, 20);
    public final ColorSetting background = new ColorSetting(this, new JColor(0f, 0f, 0f, 0.75f), true, "Background Color");
    public final ColorSetting text = new ColorSetting(this, new JColor(1f, 1f, 1f), false, "Text Color");
    public final NumberSetting scale = new NumberSetting(this, 1, 0.5, 2, 0.1, "Scale");
    public final NumberSetting width = new NumberSetting(this, 150, 100, 250, 1, "Width");
    public final NumberSetting height = new NumberSetting(this, 50, 32, 100, 1, "Height");
    public final BooleanSetting invert = new BooleanSetting(this, false, "Invert");
    public final BooleanSetting backgroundEnabled = new BooleanSetting(this, true, "Background");
    public final NumberSetting roundedCorners = new NumberSetting(this, 0, 0, 16, 1, "Rounded Corners");
    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave Settings");
    public final BooleanSetting waveEnabled = new BooleanSetting(this, false, "Wave Enabled");
    public final NumberSetting waveIndex = new NumberSetting(this, 50, 0, 150, 1, "Wave Index");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 1, 10, 1, "Wave Speed");
    public final BooleanSetting splitColors = new BooleanSetting(this, false, "Split Colors");
    public final BooleanSetting rainbowEnabled = new BooleanSetting(this, false, "Rainbow Enabled");
    public final NumberSetting rainbowSaturation = new NumberSetting(this, 0.8, 0, 1, 0.01, "Rainbow Saturation");
    public final NumberSetting rainbowBrightness = new NumberSetting(this, 1, 0, 1, 0.01, "Rainbow Brightness");

    private boolean firstFrame = true;

    public FPSModule(Category category, Description description, String name) {
        super(category, description, name);
        toggleVisibility();

        waveDivider.addSetting(waveEnabled, waveIndex, waveSpeed, rainbowEnabled, rainbowSaturation, rainbowBrightness, splitColors);
        //titleColor.addConditionBoolean(changeTitle, true);
    }


    public void toggleVisibility() {
        ImguiLoader.addRenderable(this);
    }

    private void drawText() {
        String text;
        String title;
        String value;
        if (invert.isEnabled()) {
            if (backgroundEnabled.isEnabled()) text = String.format("FPS: %s", mc.getCurrentFps());
            else text = String.format("[FPS: %s]", mc.getCurrentFps());
            title = "FPS ";
        } else {
            if (backgroundEnabled.isEnabled()) text = String.format("%s FPS", mc.getCurrentFps());
            else text = String.format("[%s FPS]", mc.getCurrentFps());
            title = " FPS";
        }
        value = mc.getCurrentFps() + "";

        float windowWidth = ImGui.getWindowSize().x;
        float windowHeight = ImGui.getWindowSize().y;
        float textWidth = ImGui.calcTextSize(text).x;
        float textHeight = ImGui.calcTextSize(text).y;

        if (invert.isEnabled() && splitColors.isEnabled() && waveEnabled.isEnabled())
            textWidth = textWidth - ImGui.calcTextSize(":").x;
        if (!backgroundEnabled.isEnabled())
            textWidth = textWidth - ImGui.calcTextSize("[]").x;

        ImGui.setCursorPos((windowWidth - textWidth) * 0.5f, (windowHeight - textHeight) * 0.5f);
        if (waveEnabled.isEnabled()) {
            if (splitColors.isEnabled()) {
                if (invert.isEnabled()) {
                    // Wave Invert Title
                    UI.drawWaveText(title, waveIndex.getIValue(), (int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue());
                    ImGui.sameLine(0,0);
                    ImGui.text(value);
                } else {
                    // Wave Title
                    ImGui.text(value);
                    ImGui.sameLine(0,0);
                    UI.drawWaveText(title, waveIndex.getIValue(), (int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue());
                }
            } else {
                // Wave
                UI.drawWaveText(text, waveIndex.getIValue(), (int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue());
            }
        } else {
            // Normal
            ImGui.text(text);
        }
    }

    @Override
    public void render() {
        if (!Template.displayRender() || !AchillesMenu.isClientEnabled())
            return;

        if (!this.isEnabled()) {
            firstFrame = true;
            return;
        }

        ImFont font = UI.getFont(32);
        font.setScale(scale.getFValue());

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoScrollbar;
        if (!backgroundEnabled.isEnabled()) imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        float[] c;
        if (!Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
            ImGui.getStyle().setWindowBorderSize(0);
            ImGui.pushStyleColor(ImGuiCol.Border, 0f,0f,0f,0f);
            c = background.getColor().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, c[0], c[1], c[2], c[3]);
        } else {
            ImGui.getStyle().setWindowBorderSize(1);
            ImGui.pushStyleColor(ImGuiCol.Border, 1f, 1f, 1f, 1f);
            c = background.getColor().jBrighter().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, c[0], c[1], c[2], c[3]);
        }
        c = text.getColor().getFloatColor();
        ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]);

        if (this.updatedPos.x != 0) {
            this.position.x = this.position.x + this.updatedPos.x;
            this.updatedPos.x = 0;
            ImGui.setNextWindowPos(this.position.x, this.position.y);
        }
        if (this.updatedPos.y != 0) {
            this.position.y = this.position.y + this.updatedPos.y;
            this.updatedPos.y = 0;
            ImGui.setNextWindowPos(this.position.x, this.position.y);
        }
        if (firstFrame || reloadPosition || !Template.shouldMove()) {
            ImGui.setNextWindowPos(super.position.x, super.position.y);
            reloadPosition = false;
        }
        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.setNextWindowSize(width.getFValue() * scale.getFValue(), height.getFValue() * scale.getFValue());
        ImGui.pushFont(font);
        ImGui.getStyle().setWindowRounding(0);
        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.getStyle().setWindowRounding(roundedCorners.getFValue() * scale.getFValue());
        ImGui.begin(String.format("%s/Legit", this.getName()), imGuiWindowFlags);

        // Main logic
        drawText();

        ImGui.popStyleColor(3);
        ImGui.popFont();
        font.setScale(1f);
        ImGui.getStyle().setWindowRounding(8);
        this.position.x = ImGui.getWindowPosX();
        this.position.y = ImGui.getWindowPosY();
        isFocused = ImGui.isWindowFocused();

        ImGui.end();
        if (firstFrame) firstFrame = false;
    }
}
