package template.rip.module.modules.legit;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.MousePressEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.KeystrokeHelper;
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

public class KeystrokesModule extends Module implements Renderable {

    public final ColorSetting background = new ColorSetting(this, new JColor(0f, 0f, 0f, 0.75f), true, "Background Color");
    public final ColorSetting text = new ColorSetting(this, new JColor(1f, 1f, 1f), false, "Text Color");
    public final ColorSetting pressedBackground = new ColorSetting(this, new JColor(1f, 1f, 1f, 0.75f), true, "Pressed Background Color");
    public final ColorSetting pressedText = new ColorSetting(this, new JColor(0f, 0f, 0f), false, "Pressed Text Color");
    public final NumberSetting roundedCorners = new NumberSetting(this, 0, 0, 16, 1, "Rounded Corners");

    public final BooleanSetting mouseButtons = new BooleanSetting(this, true, "Mouse Buttons");
    public final BooleanSetting spaceBar = new BooleanSetting(this, true, "Space Bar");
    public final NumberSetting scale = new NumberSetting(this, 1, 0.5, 2, 0.1, "Scale");
    public final NumberSetting fadeTime = new NumberSetting(this, 1, 0, 1000, 1, "Fade Time");
    public final BooleanSetting scaleIn = new BooleanSetting(this, false, "Scale In");

    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave Settings");
    public final BooleanSetting waveEnabled = new BooleanSetting(this, false, "Wave Enabled");
    public final BooleanSetting reverseWave = new BooleanSetting(this, false, "Reverse Wave");
    public final NumberSetting waveIndex = new NumberSetting(this, 50, 0, 150, 1, "Wave Index");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 1, 10, 1, "Wave Speed");
    public final DividerSetting waveColorsDivider = new DividerSetting(this, false, "Wave Colors");
    public final BooleanSetting backgroundW = new BooleanSetting(this, false, "Has Background Color ");
    public final BooleanSetting textW = new BooleanSetting(this, false, "Has Text Color");
    public final BooleanSetting pressedBackgroundW = new BooleanSetting(this, false, "Press Background Color");
    public final BooleanSetting pressedTextW = new BooleanSetting(this, false, "Press Text Color");

    private boolean firstFrame = true;

    public KeystrokesModule(Category category, Description description, String name) {
        super(category, description, name);
        toggleVisibility();

        new KeystrokeHelper(GLFW.GLFW_KEY_W, "W", 0.752941f*5);
        new KeystrokeHelper(GLFW.GLFW_KEY_A, "A", 0.705882f*5);
        new KeystrokeHelper(GLFW.GLFW_KEY_S, "S", 0.588235f*5);
        new KeystrokeHelper(GLFW.GLFW_KEY_D, "D" ,0.462745f*5);
        new KeystrokeHelper(GLFW.GLFW_MOUSE_BUTTON_LEFT, "LMB", 0.513725f*5);
        new KeystrokeHelper(GLFW.GLFW_MOUSE_BUTTON_RIGHT, "RMB", 0.309804f*5);
        new KeystrokeHelper(GLFW.GLFW_KEY_SPACE, "Space", 0.239216f*5);

        waveDivider.addSetting(waveEnabled, waveIndex, waveSpeed);
        waveColorsDivider.addSetting(backgroundW,textW,pressedBackgroundW,pressedTextW);
    }

    public void toggleVisibility() {
        ImguiLoader.addRenderable(this);
    }

    @EventHandler
    private void onPressEvent(KeyPressEvent event) {
        for (KeystrokeHelper k : KeystrokeHelper.list) {
            if (event.key == k.key) {
                k.pressTime = System.currentTimeMillis();
                k.pressed = event.action == 1;
            }
        }
    }

    @EventHandler
    private void onButtonEvent(MousePressEvent event) {
        for (KeystrokeHelper k : KeystrokeHelper.list) {
            if (event.button == k.key) {
                k.pressTime = System.currentTimeMillis();
                k.pressed = event.action == 1;
            }
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
        //(!isRenderable()) return;

        ImFont font = UI.getFont(32);
        ImGui.getIO().setFontGlobalScale(scale.getFValue());

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoScrollbar;
        if (!Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
            imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
            ImGui.getStyle().setWindowBorderSize(0f);
        } else {
            ImGui.getStyle().setWindowBorderSize(1f);
        }

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
        ImGui.setNextWindowSize((163f-4f) * scale.getFValue(), (220f-4f) * scale.getFValue());
        ImGui.pushFont(font);
        ImGui.getStyle().setWindowRounding(0);
        ImGui.getStyle().setFrameRounding(0);

        ImGui.pushStyleColor(ImGuiCol.Border, 1f,1f,1f,1f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 1f,1f,1f,0.3f);
        ImGui.getStyle().setFrameRounding(roundedCorners.getFValue() * scale.getFValue());
        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.getStyle().setItemSpacing(4, 4);
        ImGui.getStyle().setWindowPadding(0, 0);
        ImGui.begin(this.getName(), imGuiWindowFlags);
        ImGui.popStyleColor(2);

        //ImGui.pushStyleColor(ImGuiCol.Text, 1f,1f,1f, 1f);
        //ImGui.pushStyleColor(ImGuiCol.Button, 0f,0f,0f, 0f);
        //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f,0f,0f, 0f);
        //ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0f,0f,0f, 0f);
        //ImGui.button("##", 50f * scale.getFValue(), 50f * scale.getFValue());
        //ImGui.popStyleColor(4);

        //ImGui.sameLine();
        ImGui.setCursorPos((50f+4f)*scale.getFValue(), 0);
        KeystrokeHelper.getHelper(GLFW.GLFW_KEY_W).drawButton();
        //ImGui.sameLine();

        //ImGui.pushStyleColor(ImGuiCol.Text, 1f,1f,1f, 1f);
        //ImGui.pushStyleColor(ImGuiCol.Button, 0f,0f,0f, 0f);
        //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f,0f,0f, 0f);
        //ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0f,0f,0f, 0f);
        //ImGui.button("##", 50f * scale.getFValue(), 50f * scale.getFValue());
        //ImGui.popStyleColor(4);
        ImGui.setCursorPos(0, (50 + 4f) * scale.getFValue());
        KeystrokeHelper.getHelper(GLFW.GLFW_KEY_A).drawButton();
        ImGui.setCursorPos((50 + 4f) * scale.getFValue(), (50 + 4f) * scale.getFValue());
        //ImGui.sameLine();
        KeystrokeHelper.getHelper(GLFW.GLFW_KEY_S).drawButton();
        ImGui.setCursorPos((50 + 4f + 50f + 4f) * scale.getFValue(), (50 + 4f) * scale.getFValue());
        //ImGui.sameLine();
        KeystrokeHelper.getHelper(GLFW.GLFW_KEY_D).drawButton();
        if (mouseButtons.isEnabled()) {
            ImGui.setCursorPos(0f, (50 + 4f + 50f + 4f) * scale.getFValue());
            KeystrokeHelper.getHelper(GLFW.GLFW_MOUSE_BUTTON_LEFT).drawButton();
            //ImGui.sameLine();
            ImGui.setCursorPos((77f + 4f) * scale.getFValue(), (50 + 4f + 50f + 4f) * scale.getFValue());
            KeystrokeHelper.getHelper(GLFW.GLFW_MOUSE_BUTTON_RIGHT).drawButton();
        }
        if (spaceBar.isEnabled()) {
            ImGui.setCursorPos(0, (50 + 4f + 50f + 4f + 50f + 4f) * scale.getFValue());
            if (!mouseButtons.isEnabled())
                ImGui.setCursorPos(0, (50f + 4f + 50f + 4f) * scale.getFValue());
            KeystrokeHelper.getHelper(GLFW.GLFW_KEY_SPACE).drawButton();
        }
        ImGui.popFont();
        this.position = ImGui.getWindowPos();
        isFocused = ImGui.isWindowFocused();
        ImGui.getStyle().setFrameRounding(4f);
        ImGui.end();
        ImGui.getIO().setFontGlobalScale(1f);
        if (firstFrame) firstFrame = false;
    }
}
