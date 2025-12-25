package template.rip.gui.clickgui.mode;

import imgui.ImGui;
import imgui.ImVec2;
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
import template.rip.gui.clickgui.CategoryTab;
import template.rip.module.Module;

import java.util.ArrayList;

public class MoonTab {
    public static void render(CategoryTab tab) {
        Module.Category category = tab.category;
        float scrollY = tab.scrollY;
        float scrollUntil = tab.scrollUntil;
        float maxY = tab.maxY;
        boolean isCollapsed = tab.isCollapsed;
        long lastOpen = tab.lastOpen;
        String name = tab.getName();
//
        // Moon ClickGUI
        //

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;

        float percentage = 1f;
        float percentage1 = 1f;

        if (isCollapsed) {
            percentage = 0f;
            percentage1 = 0f;
        }
        ImGui.getStyle().setButtonTextAlign(0, 0.5f);

        ImGui.getStyle().setWindowPadding(0f, 0f);
        ImGui.getStyle().setFramePadding(0f, 0f);
        ImGui.getStyle().setCellPadding(0f, 0f);
        ImGui.getStyle().setItemSpacing(0, 1);
        ImGui.getStyle().setItemInnerSpacing(0, 0);
        ImGui.getStyle().setWindowBorderSize(1f);
        ImGui.getStyle().setFrameBorderSize(0f);

        float windowY = maxY * percentage;
        if (windowY < 1.0f) windowY = 30f;
        if (percentage1 == 1f && isCollapsed) windowY = 30;
        if (percentage == 1f && !isCollapsed) windowY = 30;
        ImGui.getStyle().setWindowMinSize(200, windowY);

        ImGui.pushStyleColor(ImGuiCol.Border, 0.21f, 0.24f, 0.31f, 1.00f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.15f, 0.17f, 0.22f, 1f);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.begin(name, imGuiWindowFlags);
        ImGui.getStyle().setWindowBorderSize(1f);
        if (scrollUntil > ImGui.getScrollMaxY()) {
            scrollUntil = ImGui.getScrollMaxY();
        } else if (scrollUntil < 0) {
            scrollUntil = 0;
        }

        scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
        ImGui.setScrollY(scrollY);

        ImGui.pushFont(ImguiLoader.product20);
        ImGui.getStyle().setFramePadding(4, 6);
        ImGui.getStyle().setCellPadding(4, 4);
        ImGui.getStyle().setWindowPadding(4, 4);

        ImGui.setCursorPos(0, 40);

        if (ImGui.isMouseClicked(1)) {
            float mouseX = ImGui.getMousePosX();
            float mouseY = ImGui.getMousePosY();
            if (mouseX >= ImGui.getCursorScreenPos().x && mouseX <= ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x &&
                    mouseY >= ImGui.getCursorScreenPos().y - 40 && mouseY <= ImGui.getCursorScreenPos().y) {
                lastOpen = System.currentTimeMillis();
                isCollapsed = !isCollapsed;
            }
        }
        if (tab.firstFrame) {
            ImGui.setWindowPos(tab.posX, tab.posY);
            tab.firstFrame = false;
        }

        tab.isWindowFocused = ImGui.isWindowFocused();
        tab.isWindowHovered = ImGui.isWindowHovered();

        float[] color = JColor.getGuiColor().getFloatColor();
        ImVec2 pos = ImGui.getCursorPos();
        //0.14f, 0.14f, 0.18f, 1.00f

        ImGui.popStyleColor(2);

        if (isCollapsed || percentage != 1f) {
            GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.2f),
                    ImGui.getColorU32(0f, 0f, 0f, 0f),
                    15f);


            //UI.roundedClipRect(ImGui.getBackgroundDrawList(), () -> ImGui.getBackgroundDrawList().addRectFilled(0, 0, 1000, 1000,
            //        ImGui.getColorU32(1,1,1,0.5f)), 200, 200, 200, 200, 10);

            // Header
            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);

            ImGui.setCursorPos(0,0);
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getCursorScreenPos().x,
                    ImGui.getCursorScreenPos().y + ImGui.getScrollY(),
                    ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                    ImGui.getCursorScreenPos().y + ImGui.getScrollY() + 40,
                    ImGui.getColorU32(0.11f, 0.12f, 0.16f, 0.6f),
                    16f,
                    ImDrawFlags.RoundCornersTop
            );
            ImGui.setCursorPosY(ImGui.getScrollY() + 10);
            ImGui.setCursorPosX(15);
            ImGui.pushFont(ImguiLoader.fontAwesome16);
            ImGui.text(name.substring(0, 1));
            ImGui.popFont();
            ImGui.sameLine(0,0);
            ImGui.pushFont(ImguiLoader.product26);
            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);
            ImGui.setCursorPosY(7);
            ImGui.setCursorPosX(ImGui.getCursorPosX()+5);
            ImGui.text(name.substring(2));
            ImGui.popFont();
            ImGui.popStyleColor(2);
            ImGui.getStyle().setWindowPadding(0f, 0f);
            ImGui.getStyle().setFramePadding(0f, 0f);
            ImGui.getStyle().setCellPadding(0f, 0f);
            ImGui.popFont();
            ImGui.setCursorPos(pos.x, pos.y + 3);
            ImGui.end();
            return;
        }

        ImGui.pushStyleColor(ImGuiCol.Border, 0.16f, 0.17f, 0.21f, 1.00f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.09f, 0.10f, 0.14f, 1.00f);

        ArrayList<Module> toToggle = new ArrayList<>();
        for (Module module : Template.moduleManager.getModulesByCategory(category)) {
            if (module.isNotSearched()) {
                continue;
            }
            ImGui.pushID(module.getName());

            if (!module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1f, 1f, 1f, 0.02f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1f, 1f, 1f, 0.04f);
            } else {
                //ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 1f);
                //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.9f);
                //ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 0.8f);
                ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f, 0f, 0f, 0.02f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0f, 1f, 0f, 0.04f);
            }

            ImVec2 pos1 = ImGui.getCursorPos();
            ImGui.getStyle().setFrameRounding(0f);
            if(module.isEnabled()) {
                float posX = ImGui.getWindowPosX() + ImGui.getCursorPosX();
                float posY = ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY();
                ImGui.getWindowDrawList().addRectFilledMultiColor(
                        posX,
                        posY,
                        posX + 200,
                        posY + 34,
                        UI.getColorOne().getU32(),
                        UI.getColorTwo().getU32(),
                        UI.getColorTwo().getU32(),
                        UI.getColorOne().getU32()
                );
            }
            ImGui.setCursorPos(pos1.x, pos1.y);
            boolean isToggled = ImGui.button("", 200f, 35f);
            ImGui.getStyle().setFrameRounding(4f);
            ImGui.popStyleColor(3);
            ImVec2 pos2 = ImGui.getCursorPos();

            if (isToggled) {
                toToggle.add(module);
            }

            boolean isModuleHovered = ImGui.isItemHovered();
            if (isModuleHovered) {
                ToolTipHolder.setToolTip(module.getDescription().getContent());

                if (ImGui.isMouseClicked(1)) {
                    module.toggleShowOptions();
                    ImGui.setWindowFocus(name);
                }
            }

            if (module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1f);
                //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 1f);
            } else {
                if (ImGui.isItemHovered()) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);
                    //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 0.7f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                    //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 0.5f);
                }
            }

            ImVec2 textSize = RenderUtils.calcTextSize(module.getFullName());
            ImGui.setCursorPos(pos1.x + 15, pos1.y + 17 - textSize.y / 2);

            ImVec2 prePos = ImGui.getCursorPos();

            RenderUtils.drawTexts(module.getFullName());

            ImGui.popStyleColor(1);

            ImGui.setCursorPos(200, prePos.y);
            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1f);
            ImGui.pushFont(ImguiLoader.fontAwesome16);
            //if (module.showOptions()) {
            //    ImGui.text("\uF0D8");
            //} else {
            //    ImGui.text("\uF0D7");
            //}
            ImGui.popFont();
            ImGui.popStyleColor(1);


            ImGui.setCursorPos(pos2.x, pos2.y);

            if (module.showOptions()) {
                ImGui.getStyle().setItemSpacing(8, 4);
                ImGui.getStyle().setItemInnerSpacing(4, 4);
                imGuiWindowFlags = 0;
                imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                ImGui.pushStyleColor(ImGuiCol.ChildBg, 1f, 1f, 1f, 0.01f);
                //ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                //ImGui.beginChild("Module/"+module.getName()+"/Settings", 200f, module.getSettingsHeight2(), false, imGuiWindowFlags);
                ImGui.indent(10f);
                ImGui.setCursorPosY(ImGui.getCursorPosY()+5);
                ImGui.pushFont(ImguiLoader.outfit18);
                ImGui.getStyle().setFramePadding(4, 4);
                ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
                module.renderSettings();
                ImGui.getStyle().setButtonTextAlign(0f, 0f);
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.popFont();
                ImGui.unindent(10f);
                ImGui.setCursorPosY(ImGui.getCursorPosY()+10);
                //ImGui.endChild();
                ImGui.popStyleColor(1);
                ImGui.getStyle().setItemSpacing(0, 1);
                ImGui.getStyle().setItemInnerSpacing(0, 0);
            }
            ImGui.setCursorPosY(ImGui.getCursorPosY()-1);

            ImGui.popID();
        }
        ImGui.setCursorPosY(ImGui.getCursorPosY()+7f);
        ImGui.popFont();

        maxY = ImGui.getWindowSizeY();
        GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.3f), ImGui.getColorU32(0f, 0f, 0f, 0f), 5f);

        //UI.roundedClipRect(ImGui.getBackgroundDrawList(), () -> ImGui.getBackgroundDrawList().addRectFilled(0, 0, 1000, 1000,
        //        ImGui.getColorU32(1,1,1,0.5f)), 200, 200, 200, 200, 10);

        // Header
        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);

        ImGui.setCursorPos(0,0);
        ImGui.getWindowDrawList().addRectFilled(
                ImGui.getCursorScreenPos().x,
                ImGui.getCursorScreenPos().y + ImGui.getScrollY(),
                ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                ImGui.getCursorScreenPos().y + ImGui.getScrollY() + 40,
                ImGui.getColorU32(0.11f, 0.12f, 0.16f, 0.6f),
                16f,
                ImDrawFlags.RoundCornersTop);
        ImGui.setCursorPosY(ImGui.getScrollY() + 10);
        ImGui.setCursorPosX(15);
        ImGui.pushFont(ImguiLoader.fontAwesome16);
        ImGui.text(name.substring(0, 1));
        ImGui.popFont();
        ImGui.sameLine(0,0);
        ImGui.pushFont(ImguiLoader.outfit26);
        ImGui.setCursorPosY(7);
        ImGui.setCursorPosX(ImGui.getCursorPosX()+5);
        ImGui.text(name.substring(2));
        ImGui.popFont();
        ImGui.popStyleColor(3);
        ImGui.end();
        toToggle.forEach(Module::toggle);
        tab.scrollY = scrollY;
    }
}
