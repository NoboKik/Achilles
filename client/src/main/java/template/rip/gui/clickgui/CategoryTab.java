package template.rip.gui.clickgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.RenderUtils;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.mode.FlatTab;
import template.rip.gui.clickgui.mode.GlassTab;
import template.rip.gui.clickgui.mode.MoonTab;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.util.ArrayList;

public class CategoryTab implements Renderable {

    public Module.Category category;
    public boolean firstFrame, isWindowFocused, isWindowHovered;
    public final float posX, posY;
    public float scrollY;
    public float scrollUntil = 0;
    public float maxY = 40;

    public boolean isCollapsed = false;
    public long lastOpen = 0;

    public CategoryTab(Module.Category category, float posX, float posY) {
        this.category = category;
        this.posX = posX;
        this.posY = posY;
        this.scrollY = 0;
        this.firstFrame = true;
        this.isWindowFocused = false;
        this.isWindowHovered = false;
    }

    public boolean isWindowFocused() {
        return isWindowFocused;
    }

    public boolean isWindowHovered() {
        return isWindowHovered;
    }

    @Override
    public String getName() {
        return category.name;
    }

    @Override
    public void render() {
        String name = getName();
        if (name == null) return;
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if(asm == null) return;
        switch (asm.clickGuiStyle.getMode()) {
            case Glass -> GlassTab.render(this);
            case Flat -> FlatTab.render(this);
            case Moon -> MoonTab.render(this);
        }
    }

    @Override
    public Theme getTheme() {
        return AchillesMenu.getInstance().getTheme();
    }
}