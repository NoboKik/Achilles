package template.rip.deprecated;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.scoreboard.ScoreboardObjective;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.DividerSetting;
import template.rip.module.setting.settings.NumberSetting;

public class SidebarModule extends Module implements Renderable {
    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");
    public final DividerSetting windowDivider = new DividerSetting(this, false, "Window");
    public final NumberSetting rounding = new NumberSetting(this, 8, 0, 20, 1, "Rounding");
    public final NumberSetting paddingH = new NumberSetting(this, 0, 0, 20, 1, "Padding H");
    public final NumberSetting paddingV = new NumberSetting(this, 0, 0, 20, 1, "Padding V");
    public final DividerSetting colorsDivider = new DividerSetting(this, false, "Colors");
    public final ColorSetting titleLine = new ColorSetting(this, new JColor(0.79f, 0.24f, 0.32f, 1f), true, "Title Line");
    public final ColorSetting titleBg = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.19f, 1f), true, "Title BG");
    public final ColorSetting windowBg = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.19f, 0.8f), true, "Background");
    public final ColorSetting textColor = new ColorSetting(this, new JColor(0.83f, 0.86f, 0.94f, 1.00f), true, "Text");
    public final DividerSetting glowDivider = new DividerSetting(this, false, "Glow");
    public final BooleanSetting glowEnabled = new BooleanSetting(this, false, "Glow Enabled");
    public final ColorSetting glowColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Glow Color");
    public final NumberSetting glowSize = new NumberSetting(this, 30, 0, 60, 1, "Glow Size");
    private boolean firstFrame = true;

    public SidebarModule() {
        super(Category.RENDER, Description.of("Custom implementation of scoreboard"), "Sidebar");
        mergeDividers();
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
        if (!AchillesMenu.isClientEnabled())
            return;

        if (!Template.moduleManager.getModule(this.getClass()).isEnabled()) {
            firstFrame = true;
            return;
        }

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        if (Template.moduleManager.isModuleDisabled(AchillesSettingsModule.class)) imGuiWindowFlags |= ImGuiWindowFlags.NoMove;

        float[] bg = windowBg.getColor().getFloatColorWAlpha();
        float[] text = textColor.getColor().getFloatColorWAlpha();
        ImGui.pushStyleColor(ImGuiCol.WindowBg, bg[0], bg[1], bg[2], bg[3]);
        ImGui.pushStyleColor(ImGuiCol.Text, text[0], text[1], text[2], text[3]);
        ImGui.getStyle().setItemSpacing(0f, 0f);
        ImGui.getStyle().setItemInnerSpacing(0f, 0f);
        ImGui.getStyle().setWindowMinSize(10, 10);
        ImGui.getStyle().setWindowRounding(rounding.getFValue());


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


        ImGui.begin(getName(), imGuiWindowFlags);


        float[] color = JColor.getGuiColor().getFloatColor();
        ImVec2 pos = ImGui.getCursorPos();

        ImGui.pushFont(ImguiLoader.poppins24);

        ImGui.setCursorPos(paddingH.getFValue(), paddingV.getFValue());
        if (mc.player != null) {
            ScoreboardObjective o = null;
            for (ScoreboardObjective obj : mc.player.getScoreboard().getObjectives()) {
                if (obj.getScoreboard() != null) {
                    o = obj;
                    break;
                }
            }
            if (o != null) {
                int i = 0;
                float highestX = 0;
//                for (Text t : PlayerUtils.scoreboardLines(o)) {
//                    ImGui.setCursorPosX(paddingH.getFValue());
//                    PlayerUtils.drawMcText(t);
//                    if (ImGui.getCursorPosX()>highestX) highestX=ImGui.getCursorPosX();
//                    ImGui.text("");
//                    i++;
//                }
//                ImGui.setCursorPosY(ImGui.getCursorPosY()+paddingV.getFValue());
//                ImGui.setCursorPosX(highestX+paddingH.getFValue());
            }

        }




        // Glow
        if (glowEnabled.isEnabled()) {
            float[] glowColorF = glowColor.getColor().getFloatColorWAlpha();
            float alpha = glowColorF[3];

            GuiUtils.drawWindowShadow(
                    ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], alpha),
                    ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], 0f),
                    glowSize.getFValue()
            );
        }

        ImGui.popFont();
        super.position = ImGui.getWindowPos();
        isFocused = ImGui.isWindowFocused();

        ImGui.end();
        if (firstFrame) firstFrame = false;
        ImGui.popStyleColor(2);
        ImGui.getStyle().setItemSpacing(8f, 4f);
        ImGui.getStyle().setItemInnerSpacing(4f, 4f);
        ImGui.getStyle().setWindowRounding(8);
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

            colors[ImGuiCol.Text] = new float[]{0.83f, 0.86f, 0.94f, 1.00f};
            colors[ImGuiCol.TextDisabled] = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.WindowBg] = new float[]{0.13f, 0.14f, 0.19f, 0.8f};
            colors[ImGuiCol.ChildBg] = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.PopupBg] = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
            colors[ImGuiCol.Border] = new float[]{0.21f, 0.24f, 0.31f, 0.00f};
            colors[ImGuiCol.BorderShadow] = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.FrameBg] = new float[]{color[0], color[1], color[2], 0.54f};
            colors[ImGuiCol.FrameBgHovered] = new float[]{color[0], color[1], color[2], 0.40f};
            colors[ImGuiCol.FrameBgActive] = new float[]{color[0], color[1], color[2], 0.67f};
            colors[ImGuiCol.TitleBg] = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgActive] = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgCollapsed] = new float[]{0.13f, 0.14f, 0.19f, 0.5f};
            colors[ImGuiCol.MenuBarBg] = new float[]{0.13f, 0.14f, 0.19f, 1.00f};
            colors[ImGuiCol.ScrollbarBg] = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.ScrollbarGrab] = new float[]{0.18f, 0.21f, 0.27f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabHovered] = new float[]{0.22f, 0.25f, 0.33f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabActive] = new float[]{0.25f, 0.29f, 0.37f, 0.00f};
            colors[ImGuiCol.CheckMark] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            colors[ImGuiCol.SliderGrab] = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.SliderGrabActive] = new float[]{color[0], color[1], color[2], 0.95f};
            colors[ImGuiCol.Button] = new float[]{color[0], color[1], color[2], 0.59f};
            colors[ImGuiCol.ButtonHovered] = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.ButtonActive] = new float[]{color[0], color[1], color[2], 1.00f};
            colors[ImGuiCol.Header] = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.HeaderHovered] = new float[]{color[0], color[1], color[2], 0.95f};

            colors[ImGuiCol.HeaderActive] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
            colors[ImGuiCol.Separator] = new float[]{0.18f, 0.21f, 0.27f, 1.00f};
            colors[ImGuiCol.SeparatorHovered] = new float[]{0.81f, 0.25f, 0.33f, 1.00f};
            colors[ImGuiCol.SeparatorActive] = new float[]{0.74f, 0.22f, 0.30f, 1.00f};

            colors[ImGuiCol.ResizeGrip] = new float[]{color[0], color[1], color[2], 0.59f};
            colors[ImGuiCol.ResizeGripHovered] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
            colors[ImGuiCol.ResizeGripActive] = new float[]{color[0], color[1], color[2], 1.00f};
            colors[ImGuiCol.Tab] = new float[]{dColor[0], dColor[1], dColor[2], 0.86f};
            colors[ImGuiCol.TabHovered] = new float[]{color[0], color[1], color[2], 0.80f};
            colors[ImGuiCol.TabActive] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            colors[ImGuiCol.TabUnfocused] = new float[]{0.15f, 0.18f, 0.25f, 1.00f};
            colors[ImGuiCol.TabUnfocusedActive] = new float[]{0.56f, 0.21f, 0.26f, 0.67f};
            colors[ImGuiCol.DockingPreview] = new float[]{0.91f, 0.26f, 0.36f, 0.67f};
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
            colors[ImGuiCol.TextSelectedBg] = new float[]{0.26f, 0.59f, 0.98f, 0.35f};
            colors[ImGuiCol.DragDropTarget] = new float[]{1.00f, 1.00f, 0.00f, 0.90f};
            colors[ImGuiCol.NavHighlight] = new float[]{0.26f, 0.59f, 0.98f, 1.00f};
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
