package template.rip.api.notification;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import template.rip.api.font.JColor;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Theme;

import java.util.UUID;

public class Notification {

    private final String title, uuid, msg;
    private final String[] moduleNames;
    private final long duration;
    private final long timeAdded;
    private float windowY = 0;
    private boolean firstFrame = true;

    public Notification(String title, long duration, String msg, String... moduleNames) {
        this.title = title;
        this.msg = msg;
        this.moduleNames = moduleNames;
        this.duration = duration;
        this.uuid = UUID.randomUUID().toString();

        this.timeAdded = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public float getTimeProgress() {
        return Math.max(1f - (float) (System.currentTimeMillis() - timeAdded) / (float) duration, 0f);
    }

    public float getWindowY() {
        return windowY;
    }

    public void setWindowY(float windowY) {
        this.windowY = windowY;
    }

    public void render(float windowX) {

        getTheme().preRender();

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDecoration;
        if (firstFrame) imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;

        ImGui.pushStyleColor(ImGuiCol.Text, 0.90f, 0.91f, 0.94f,1f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.19f, 1f);
        ImGui.setNextWindowSize(350f, 100f);
        ImGui.begin(uuid, imGuiWindowFlags);
        ImGui.popFont();
        ImGui.pushFont(ImguiLoader.mediumPoppins32);

        if (getTimeProgress() >= 0.8f) {
            float x = (getTimeProgress() - 0.8f) / 0.2f;
            float percent = x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
            float animationXOffset = 350 * (1f - percent);
            ImGui.setWindowPos(windowX + 300 - animationXOffset, getWindowY());
        } else if (getTimeProgress() <= 0.2f) {
            float x = getTimeProgress() / 0.2f;
            float percent = x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
            float animationXOffset = 350 * (1f - percent);
            ImGui.setWindowPos(windowX + animationXOffset, getWindowY());
        }

        ImGui.setCursorPos(20, 25);
        ImGui.text(getTitle());

        ImGui.popFont();
        ImGui.pushFont(ImguiLoader.poppins24);
        ImGui.getStyle().setFramePadding(4, 6);
        ImGui.getStyle().setCellPadding(4, 4);
        ImGui.getStyle().setWindowPadding(4, 4);

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX(),
                ImGui.getWindowPosY()+100f-8f,
                ImGui.getWindowPosX()+350f,
                ImGui.getWindowPosY()+100f,
                ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f)
        );
        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX()+1+1f,
                ImGui.getWindowPosY()+100f-8f,
                ImGui.getWindowPosX()+350f-1f,
                ImGui.getWindowPosY()+100f-1f,
                ImGui.getColorU32(0.13f, 0.14f, 0.19f, 1.00f)
        );

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX(),
                ImGui.getWindowPosY()+100f-8f,
                ImGui.getWindowPosX()+(350f * getTimeProgress()),
                ImGui.getWindowPosY()+100f,
                ImGui.getColorU32(0.90f, 0.91f, 0.94f, 1f)
                );

        // Main stuff
        ImGui.setCursorPos(20, 55);
        if (getTitle().equalsIgnoreCase("Module Disabled") ||
                getTitle().equalsIgnoreCase("Module Enabled") ||
                getTitle().equalsIgnoreCase("Friend Added") ||
                getTitle().equalsIgnoreCase("Friend Removed") ||
                getTitle().equalsIgnoreCase("Focus Target Removed") ||
                getTitle().equalsIgnoreCase("Focus Target Added")) {
            String[] array = msg.split(" ");
            String between1 = array[0];
            String between2 = array[1];
            String toggle = array[2];
            ImGui.getStyle().setItemInnerSpacing(0,0);
            ImGui.pushStyleColor(ImGuiCol.Text, 0.90f, 0.91f, 0.94f,0.8f);
            RenderUtils.drawTexts(moduleNames);
            ImGui.popStyleColor(1);
            ImGui.sameLine(0,0);
            ImGui.pushStyleColor(ImGuiCol.Text, 0.90f, 0.91f, 0.94f,1f);
            ImGui.text(" "+between1+" ");
            ImGui.sameLine(0,0);
            ImGui.text(between2+" ");
            ImGui.popStyleColor(1);
            ImGui.sameLine(0,0);
            if (getTitle().equalsIgnoreCase("Module Disabled")) ImGui.pushStyleColor(ImGuiCol.Text, 0.82f, 0.06f, 0.22f,1f);
            if (getTitle().equalsIgnoreCase("Module Enabled"))  ImGui.pushStyleColor(ImGuiCol.Text, 0.25f, 0.63f, 0.17f,1f);
            if (getTitle().equalsIgnoreCase("Friend Removed") || getTitle().equalsIgnoreCase("Focus Target Removed")) ImGui.pushStyleColor(ImGuiCol.Text, 0.82f, 0.06f, 0.22f,1f);
            if (getTitle().equalsIgnoreCase("Friend Added") || getTitle().equalsIgnoreCase("Focus Target Added"))  ImGui.pushStyleColor(ImGuiCol.Text, 0.25f, 0.63f, 0.17f,1f);
            ImGui.text(toggle);
            ImGui.popStyleColor(1);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String str : moduleNames) {
                sb.append(str);
            }
            sb.append(msg);
            ImGui.text(sb.toString());
        }

        ImGui.setCursorPos(10, 6);
        ImGui.pushFont(ImguiLoader.fontAwesome20);
        ImGui.text("\uF129");
        ImGui.popFont();
        ImGui.popStyleColor(2);

        ImGui.end();
        getTheme().postRender();
        firstFrame = false;
    }

    private Theme getTheme() {
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
            colors[ImGuiCol.Border] = new float[]{0.21f, 0.24f, 0.31f, 1.00f};
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

            ImGui.getStyle().setWindowMinSize(350, 60);

            if (ImguiLoader.poppins18 != null) {
                ImGui.pushFont(ImguiLoader.poppins18);
            }
        }

        @Override
        public void postRender() {
            if (ImguiLoader.poppins18 != null) {
                ImGui.popFont();
            }
        }
    };
}
