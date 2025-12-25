package template.rip.gui.windowgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.AnimationUtil;
import template.rip.api.util.EasingUtil;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;

import java.util.ArrayList;

public class LegitModulesMenu implements Renderable {

    private static LegitModulesMenu instance;
    public static Module editingModule = null;
    public float scrollY = 0;
    public float scrollUntil = 0;

    public static LegitModulesMenu getInstance() {
        if (instance == null) {
            instance = new LegitModulesMenu();
        }
        return instance;
    }

    public static void toggleVisibility() {
        if (ImguiLoader.isRendered(getInstance())) {
            ImguiLoader.queueRemove(getInstance());
        } else {
            ImguiLoader.addRenderable(getInstance());
        }
    }

    @Override
    public String getName() {
        return Template.name;
    }

    @Override
    public void render() {
        if (!MainMenu.getInstance().selectedSection.equals("\uF07C Mod Menu")) return;
        float[] c = JColor.getGuiColor().getFloatColor();

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoCollapse;
        //imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        ImGui.getStyle().setFramePadding(4, 6);
        ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
        ImGui.getStyle().setWindowPadding(16,16);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.setNextWindowSize(630f, 430f, 0);
        ImGui.begin(getName(), imGuiWindowFlags);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.getStyle().setWindowPadding(6,6);
        ImGui.getStyle().setItemSpacing(8, 4);
        ImGui.getStyle().setItemInnerSpacing(4, 4);
        ImGui.getStyle().setWindowBorderSize(1f);
        ImGui.getStyle().setFrameBorderSize(0f);

        //float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 330);
        //float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);
        float posX = MainMenu.getInstance().getPos().x+800-630;
        float posY = MainMenu.getInstance().getPos().y+500-430;
        ImGui.setWindowPos(posX, posY);

        if (scrollUntil > ImGui.getScrollMaxY()) {
            scrollUntil = ImGui.getScrollMaxY();
        } else if (scrollUntil < 0) {
            scrollUntil = 0;
        }

        scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
        if (editingModule == null) ImGui.setScrollY(scrollY);

        ArrayList<Module> all = new ArrayList<>(Template.moduleManager.getModulesByCategory(Module.Category.LEGIT));
        ImGui.setCursorPos(20, 20);

        int row = 0;
        int column = 0;
        for (Module module : all) {
            ImGui.setCursorPosX(20+203*column);
            ImGui.setCursorPosY(20+160*row);
            ImGui.pushID(module.getName());

            if (module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.8f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.9f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 1.0f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.3f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.4f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 0.5f);
            }

            ImGui.pushFont(ImguiLoader.poppins24);
            ImVec2 prevPos = ImGui.getCursorPos();
            boolean isToggled = ImGui.button("##", 193f, 150f);
            ImVec2 postPos = ImGui.getCursorPos();
            ImGui.popStyleColor(3);
            ImGui.popFont();

            if (isToggled) {
                module.toggle();
            }

            if (ImGui.isItemHovered()) {
                ToolTipHolder.setToolTip(module.getDescription().getContent());

                if (ImGui.isMouseClicked(1)) {
                    editingModule = module;
                }
            }

            if (module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
            }

            ImGui.pushFont(ImguiLoader.poppins24);
            ImGui.setCursorPos(prevPos.x + 20f, prevPos.y + 150f - 20f - 24f);
            RenderUtils.drawTexts(module.getFullName());
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();
            ImGui.popStyleColor(1);

            if (module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], 1.00f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
            }

            ImGui.pushFont(ImguiLoader.fontAwesome28);
            ImGui.setCursorPos(prevPos.x + 20f, prevPos.y + 20f);
            ImGui.text(module.isEnabled() ? "\uF205" : "\uF204");
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();
            ImGui.popStyleColor(1);

            if (module.isEnabled()) {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
            }

            ImGui.pushFont(ImguiLoader.fontAwesome20);
            ImGui.setCursorPos(prevPos.x + 170f, prevPos.y + 20f);
            ImGui.text("\uF142");
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();
            ImGui.popStyleColor(1);

            //if (module.isEnabled()) {
            //    float[] color = JColor.getGuiColor().getFloatColor();
            //    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 0.5f);
            //    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.5f);
            //    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.5f);
            //} else {
            //    ImGui.pushStyleColor(ImGuiCol.Button, 0.14f, 0.16f, 0.21f, 1.0f);
            //    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.14f, 0.16f, 0.21f, 1.0f);
            //    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.14f, 0.16f, 0.21f, 1.0f);
            //}
            //ImGui.setCursorPos(prevPos.x + 10, prevPos.y + 10);
            //ImGui.button("##", 40f, 40f);
            //ImGui.setCursorPos(postPos.x, postPos.y);

            //ImGui.popStyleColor(3);

            //if (module.isEnabled()) {
            //    float[] color = JColor.getGuiColor().getFloatColor();
            //    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
            //    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
            //    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 1f);
            //    ImGui.getStyle().setFrameRounding(10f - 3);

            //    ImGui.setCursorPos(prevPos.x + 10 + 3, prevPos.y + 10 + 3);
            //    ImGui.button("##", 40f - 3 * 2, 40f - 3 * 2);
            //    ImGui.setCursorPos(postPos.x, postPos.y);

            //    ImGui.getStyle().setFrameRounding(10f);
            //    ImGui.popStyleColor(3);
            //}

            //if (module.showOptions()) {
            //    ImGui.indent(4f + 9f);
            //    ImGui.pushFont(ImguiLoader.getPoppins18());
            //    ImGui.getStyle().setFrameRounding(4f);
            //    ImGui.getStyle().setFramePadding(4, 4);
            //    ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
            //    //ImGui.beginChild(module.getName()+"/SettingsCalc", 1, 1, false, ImGuiWindowFlags.AlwaysAutoResize);
            //    //float Y = ImGui.getCursorPosY();
            //    //module.renderSettings();
            //    //float nextY = ImGui.getCursorPosY()-Y;
            //    //ImGui.endChild();

            //    float settingsHeight = module.getSettingsHeight();

            //    ImGui.setCursorPosY(ImGui.getCursorPosY() + 7f);
            //    ImGui.getWindowDrawList().addRectFilled(
            //            ImGui.getWindowPosX() + prevPos.x + 4f,
            //            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 4f,
            //            ImGui.getWindowPosX() + prevPos.x + 296f + 304f - 4f,
            //            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 14f + 15 + settingsHeight,
            //            ImGui.getColorU32(0.16f, 0.18f, 0.24f, 0.3f), 10f
            //    );
            //    module.renderSettings();
            //    ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
            //    ImGui.getStyle().setFramePadding(4, 6);
            //    ImGui.getStyle().setFrameRounding(10f);
            //    ImGui.popFont();
            //    ImGui.unindent(4f + 9f);
            //    ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
            //}
            //ImGui.setCursorPosY(ImGui.getCursorPosY() + 3);
            ImGui.popID();

            column++;
            if (column > 2) {
                column = 0;
                row++;
            }
        }

        ImGui.getWindowDrawList().addRectFilled(
                ImGui.getWindowPosX(),
                ImGui.getWindowPosY(),
                ImGui.getWindowPosX() + 800,
                ImGui.getWindowPosY() + 16,
                ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f), 0f
        );
        ImGui.getWindowDrawList().addRectFilled(
                ImGui.getWindowPosX() + 1,
                ImGui.getWindowPosY() + 1,
                ImGui.getWindowPosX() + 800 - 1,
                ImGui.getWindowPosY() + 16,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, 1.00f), 0f
        );

        //ImGui.setCursorPos(0, 414);
        //ImGui.button("##", 16, 16);
        //ImGui.setCursorPos(614, 0);
        //ImGui.button("##", 16, 16);

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX(),
                ImGui.getWindowPosY() + 414,
                ImGui.getWindowPosX() + 16,
                ImGui.getWindowPosY() + 414 + 16,
                ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f), 0f
        );
        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 1,
                ImGui.getWindowPosY() + 414 - 1,
                ImGui.getWindowPosX() + 16,
                ImGui.getWindowPosY() + 414 - 1 + 16,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, 1.00f), 0f
        );

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 614,
                ImGui.getWindowPosY(),
                ImGui.getWindowPosX() + 614 + 16,
                ImGui.getWindowPosY() + 16,
                ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f), 0f
        );
        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 614 - 1,
                ImGui.getWindowPosY() + 1,
                ImGui.getWindowPosX() + 614 - 1 + 16,
                ImGui.getWindowPosY() + 16,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, 1.00f), 0f
        );

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 15,
                ImGui.getWindowPosY() + 1,
                ImGui.getWindowPosX() + ImGui.getWindowSizeX() - 15,
                ImGui.getWindowPosY() + ImGui.getWindowSizeY() - 1,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, EasingUtil.easeInQuad(1f- AnimationUtil.getRawPressPercentage("CategorySwitch", 300))), 0f
        );

        ImGui.end();
        if (editingModule != null) {
            imGuiWindowFlags = 0;
            imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
            imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
            imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
            imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
            imGuiWindowFlags |= ImGuiWindowFlags.NoCollapse;
            posX = MainMenu.getInstance().getPos().x;
            posY = MainMenu.getInstance().getPos().y;
            ImGui.setNextWindowPos(posX, posY);
            ImGui.setNextWindowSize(800f, 500f, 0);
            ImGui.pushStyleColor(ImGuiCol.WindowBg, 0f, 0f, 0f, 0.5f);
            ImGui.pushStyleColor(ImGuiCol.Border, 0f, 0f, 0f, 0f);
            ImGui.getStyle().setWindowRounding(16f);
            ImGui.begin(getName()+"/BG", imGuiWindowFlags);
            ImGui.getStyle().setWindowRounding(16f);
            ImGui.setCursorPos(0,0);
            ImGui.invisibleButton(getName()+"/BG/Close", 800f, 500f);
            if (ImGui.isItemHovered() && ImGui.isMouseDown(0)) editingModule = null;
            ImGui.end();
            ImGui.popStyleColor(2);

            if (editingModule != null) {
                imGuiWindowFlags = 0;
                imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
                imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
                imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
                imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
                imGuiWindowFlags |= ImGuiWindowFlags.NoCollapse;
                //imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
                ImGui.getStyle().setWindowPadding(16, 16);
                ImGui.getStyle().setWindowRounding(16f);
                ImGui.getStyle().setFramePadding(0f, 0f);
                ImGui.getStyle().setCellPadding(0f, 0f);
                ImGui.getStyle().setItemSpacing(8, 4);
                ImGui.getStyle().setItemInnerSpacing(4, 4);
                ImGui.getStyle().setWindowBorderSize(1f);
                ImGui.getStyle().setFrameBorderSize(0f);
                posX = MainMenu.getInstance().getPos().x + 800 - 206;
                posY = MainMenu.getInstance().getPos().y;
                ImGui.setNextWindowPos(posX, posY);
                ImGui.setNextWindowSize(206f, 500f, 0);
                ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.18f, 1f);
                ImGui.begin(getName() + "/Settings", imGuiWindowFlags);
                ImGui.getStyle().setWindowRounding(4f);
                ImGui.getStyle().setWindowPadding(6, 6);
                ImGui.getStyle().setFramePadding(0f, 0f);
                ImGui.getStyle().setCellPadding(0f, 0f);
                ImGui.getStyle().setItemSpacing(8, 4);
                ImGui.getStyle().setItemInnerSpacing(4, 4);
                ImGui.getStyle().setWindowBorderSize(1f);
                ImGui.getStyle().setFrameBorderSize(0f);

                //float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 330);
                //float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);

                if (scrollUntil > ImGui.getScrollMaxY()) {
                    scrollUntil = ImGui.getScrollMaxY();
                } else if (scrollUntil < 0) {
                    scrollUntil = 0;
                }

                scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
                ImGui.setScrollY(scrollY);

                ImGui.pushFont(ImguiLoader.poppins18);

                ImGui.getStyle().setFrameRounding(4f);
                ImGui.getStyle().setFramePadding(4, 4);
                ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);

                editingModule.renderSettings();

                ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.getStyle().setFrameRounding(10f);

                ImGui.popFont();

                ImGui.popStyleColor(1);
                ImGui.end();
            }
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

            float[] color = JColor.getGuiColor().getFloatColor();
            float[] bColor = JColor.getGuiColor().jBrighter().getFloatColor();
            float[] dColor = JColor.getGuiColor().jDarker().getFloatColor();


            colors[ImGuiCol.Text]                   = new float[]{0.83f, 0.86f, 0.94f, 1.00f};
            colors[ImGuiCol.TextDisabled]           = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.WindowBg]               = new float[]{0.11f, 0.12f, 0.16f, 0.5f};
            colors[ImGuiCol.ChildBg]                = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.PopupBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
            colors[ImGuiCol.Border]                 = new float[]{0.21f, 0.24f, 0.31f, 1.00f};
            colors[ImGuiCol.BorderShadow]           = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.FrameBg]                = new float[]{color[0], color[1], color[2], 0.54f};
            colors[ImGuiCol.FrameBgHovered]         = new float[]{color[0], color[1], color[2], 0.40f};
            colors[ImGuiCol.FrameBgActive]          = new float[]{color[0], color[1], color[2], 0.67f};
            colors[ImGuiCol.TitleBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgActive]          = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgCollapsed]       = new float[]{0.13f, 0.14f, 0.19f, 0.5f};
            colors[ImGuiCol.MenuBarBg]              = new float[]{0.13f, 0.14f, 0.19f, 1.00f};
            colors[ImGuiCol.ScrollbarBg]            = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.ScrollbarGrab]          = new float[]{0.18f, 0.21f, 0.27f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabHovered]   = new float[]{0.22f, 0.25f, 0.33f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabActive]    = new float[]{0.25f, 0.29f, 0.37f, 0.00f};
            colors[ImGuiCol.CheckMark]              = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            colors[ImGuiCol.SliderGrab]             = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.SliderGrabActive]       = new float[]{color[0], color[1], color[2], 0.95f};
            colors[ImGuiCol.Button]                 = new float[]{color[0], color[1], color[2], 0.59f};
            colors[ImGuiCol.ButtonHovered]          = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.ButtonActive]           = new float[]{color[0], color[1], color[2], 1.00f};
            colors[ImGuiCol.Header]                 = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.HeaderHovered]          = new float[]{color[0], color[1], color[2], 0.95f};

            colors[ImGuiCol.HeaderActive]           = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
            colors[ImGuiCol.Separator]              = new float[]{0.18f, 0.21f, 0.27f, 1.00f};
            colors[ImGuiCol.SeparatorHovered]       = new float[]{0.81f, 0.25f, 0.33f, 1.00f};
            colors[ImGuiCol.SeparatorActive]        = new float[]{0.74f, 0.22f, 0.30f, 1.00f};

            colors[ImGuiCol.ResizeGrip]             = new float[]{color[0], color[1], color[2], 0.59f};
            colors[ImGuiCol.ResizeGripHovered]      = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
            colors[ImGuiCol.ResizeGripActive]       = new float[]{color[0], color[1], color[2], 1.00f};
            colors[ImGuiCol.Tab]                    = new float[]{dColor[0], dColor[1], dColor[2], 0.86f};
            colors[ImGuiCol.TabHovered]             = new float[]{color[0], color[1], color[2], 0.80f};
            colors[ImGuiCol.TabActive]              = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            colors[ImGuiCol.TabUnfocused]           = new float[]{0.15f, 0.18f, 0.25f, 1.00f};
            colors[ImGuiCol.TabUnfocusedActive]     = new float[]{0.56f, 0.21f, 0.26f, 0.67f};
            colors[ImGuiCol.DockingPreview]         = new float[]{0.91f, 0.26f, 0.36f, 0.67f};
            colors[ImGuiCol.DockingEmptyBg]         = new float[]{0.20f, 0.20f, 0.20f, 1.00f};
            colors[ImGuiCol.PlotLines]              = new float[]{0.61f, 0.61f, 0.61f, 1.00f};
            colors[ImGuiCol.PlotLinesHovered]       = new float[]{1.00f, 0.43f, 0.35f, 1.00f};
            colors[ImGuiCol.PlotHistogram]          = new float[]{0.90f, 0.70f, 0.00f, 1.00f};
            colors[ImGuiCol.PlotHistogramHovered]   = new float[]{1.00f, 0.60f, 0.00f, 1.00f};
            colors[ImGuiCol.TableHeaderBg]          = new float[]{0.19f, 0.19f, 0.20f, 1.00f};
            colors[ImGuiCol.TableBorderStrong]      = new float[]{0.31f, 0.31f, 0.35f, 1.00f};
            colors[ImGuiCol.TableBorderLight]       = new float[]{0.23f, 0.23f, 0.25f, 1.00f};
            colors[ImGuiCol.TableRowBg]             = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.TableRowBgAlt]          = new float[]{1.00f, 1.00f, 1.00f, 0.06f};
            colors[ImGuiCol.TextSelectedBg]         = new float[]{0.26f, 0.59f, 0.98f, 0.35f};
            colors[ImGuiCol.DragDropTarget]         = new float[]{1.00f, 1.00f, 0.00f, 0.90f};
            colors[ImGuiCol.NavHighlight]           = new float[]{0.26f, 0.59f, 0.98f, 1.00f};
            colors[ImGuiCol.NavWindowingHighlight]  = new float[]{1.00f, 1.00f, 1.00f, 0.70f};
            colors[ImGuiCol.NavWindowingDimBg]      = new float[]{0.80f, 0.80f, 0.80f, 0.20f};
            colors[ImGuiCol.ModalWindowDimBg]       = new float[]{0.80f, 0.80f, 0.80f, 0.35f};

            ImGui.getStyle().setColors(colors);

            ImGui.getStyle().setWindowRounding(8);
            ImGui.getStyle().setFrameRounding(4);
            ImGui.getStyle().setGrabRounding(4);
            ImGui.getStyle().setPopupRounding(4);
            ImGui.getStyle().setScrollbarRounding(4);
            ImGui.getStyle().setTabRounding(4);
            ImGui.getStyle().setWindowTitleAlign(0.5f, 0.5f);
            ImGui.getStyle().setScrollbarSize(1);

            if (ImguiLoader.fontAwesome16 != null) {
                ImGui.pushFont(ImguiLoader.fontAwesome16);
            }
        }

        @Override
        public void postRender() {
            if (ImguiLoader.fontAwesome16 != null) {
                ImGui.popFont();
            }
        }
    };
}