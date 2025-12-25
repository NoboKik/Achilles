package template.rip.module.modules.legit;


import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.Text;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;


public class PotionStatusModule extends Module implements Renderable {

    public final ColorSetting text = new ColorSetting(this, new JColor(1f, 1f, 1f), false, "Text Color");
    public final ColorSetting durationText = new ColorSetting(this, new JColor(0.7f, 0.7f, 0.7f), false, "Duration Text Color");
    public final NumberSetting scale = new NumberSetting(this, 1, 0.5, 2, 0.1, "Scale");

    private boolean firstFrame = true;

    public PotionStatusModule() {
        super(Category.LEGIT, Description.of("Shows your active potions."), "Potion Status");
        toggleVisibility();
    }
//    public final BooleanSetting hideVanilla = new BooleanSetting("Hide Vanilla Potions", this, true);

    public void toggleVisibility() {
        ImguiLoader.addRenderable(this);
    }

    @Override
    public void render() {
        if (!Template.displayRender() || !AchillesMenu.isClientEnabled())
            return;

        if (!this.isEnabled()) {
            firstFrame = true;
            return;
        }
        //if (!isRenderable()) return;

        ImFont font = ImguiLoader.poppins32;
        font.setScale(scale.getFValue());

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoScrollbar;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        float[] c;
        if (!Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
            imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
            ImGui.pushStyleColor(ImGuiCol.Border, 0f,0f,0f,0f);
            ImGui.pushStyleColor(ImGuiCol.WindowBg, 1f, 1f, 1f, 1f);
        } else {
            ImGui.pushStyleColor(ImGuiCol.Border, 1f, 1f, 1f, 1f);
            ImGui.pushStyleColor(ImGuiCol.WindowBg, 1f, 1f, 1f, 0.3f);
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
        ImGui.pushFont(font);
        ImGui.getStyle().setWindowRounding(0);
        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.begin(this.getName(), imGuiWindowFlags);

        ImGui.indent(5f);
        if (mc.player != null) {
            List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
            for (StatusEffectInstance effect : effects) {
                String name = Text.translatable(effect.getEffectType().value().getTranslationKey()).getString();
                c = text.getColor().getFloatColor();
                ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]);
                ImGui.text(name + " " + (effect.getAmplifier() + 1));
                ImGui.popStyleColor(1);
                c = durationText.getColor().getFloatColor();
                ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]);
                ImGui.text(StatusEffectUtil.getDurationText(effect, 1, 0).getString());
                ImGui.popStyleColor(1);
            }
        } else {
            c = text.getColor().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]);
            ImGui.text("Speed 2");
            ImGui.popStyleColor(1);
            c = durationText.getColor().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]);
            ImGui.text("00:00");
            ImGui.popStyleColor(1);
        }
        ImGui.unindent();
        ImGui.popStyleColor(2);
        ImGui.popFont();
        font.setScale(1f);
        ImGui.getStyle().setWindowRounding(8);
        this.position = ImGui.getWindowPos();
        isFocused = ImGui.isWindowFocused();

        ImGui.end();
        if (firstFrame) firstFrame = false;
    }
}
