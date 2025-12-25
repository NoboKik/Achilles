package template.rip.gui.clickgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector2f;
import template.rip.Template;
import template.rip.api.config.ConfigManager;
import template.rip.api.config.ConfigProfile;
import template.rip.api.font.JColor;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.GuiUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.modules.client.ConfigModule;

public class ConfigParent implements Renderable {

    private boolean firstFrame = true;
    public boolean isOn = false;
    private static ConfigParent instance;
    public ImString config = new ImString();
    public ImVec2 pos;

    public static ConfigParent getInstance() {
        if (instance == null) {
            instance = new ConfigParent();
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

    public ImVec2 getPos() {
        return pos;
    }

    @Override
    public String getName() {
        return "ConfigParent";
    }

    @Override
    public void render() {
        if (!ConfigParent.getInstance().isOn) return;
        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDecoration;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        ImGui.getStyle().setFramePadding(4, 6);
        ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
        ImGui.getStyle().setWindowPadding(0,0);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.setNextWindowSize(800f, 500f, 0);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.18f, 1f);
        ImGui.begin(getName(), imGuiWindowFlags);
        ImGui.popStyleColor(1);
        float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 490);
        float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);

        if (firstFrame) {
            ImGui.setWindowPos(posX, posY);
            firstFrame = false;
        }
        pos = ImGui.getWindowPos();

        float[] color = JColor.getGuiColor().getFloatColor();

        ImGui.pushFont(ImguiLoader.poppins48);
        ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1.00f);
        ImGui.setCursorPos(16, 16);
        ImGui.text("Config Hub");
        ImGui.sameLine(0,0);
        ImGui.popFont();
        ImGui.popStyleColor(1);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 8f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 12f, 12f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.15f, 0.17f, 0.22f, 0.5f);
        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TextDisabled, 0.83f, 0.86f, 0.94f, 0.5f);
        ImGui.setCursorPos(498, 16);
        ImGui.pushFont(ImguiLoader.poppins24);
        ImGui.setNextItemWidth(200f);
        ImVec2 pre = ImGui.getCursorPos().clone();
        ImGui.setNextItemWidth(150f);

        // Config Manager
        boolean configInput = ImGui.inputTextWithHint("##", "Config... ", config);
        ImGui.setCursorPos(800f - 16f - 120f, 16);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 8f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 0f);
        ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 0.9f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.8f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 0.7f);
        ImGui.pushStyleColor(ImGuiCol.Text,       0.83f, 0.86f, 0.94f,1f);
        ImGui.pushFont(ImguiLoader.poppins32);
        float x = 120f;
        float y = 48f;

        if (ImGui.button("Create", x, y)) {
            ConfigProfile configProfile = new ConfigProfile(config.get(), ConfigManager.pathProfilesFolder.resolve(config.get().toLowerCase().trim().replaceAll("[^A-Za-z0-9()\\[\\]]", "") + ".ac"));
            Template.configManager().addProfile(configProfile);
            configProfile.saveProfile();
            config.clear();
        }

        ImGui.popStyleColor(4);
        ImGui.popStyleVar();
        ImGui.popFont();

        ImGui.popStyleColor(3);
        ImGui.popFont();
        ImGui.popStyleVar(3);

        // Toggles
        ConfigModule module = Template.moduleManager.getModule(ConfigModule.class);
        if (module != null) {
            ImGui.pushFont(ImguiLoader.poppins18);
            ImGui.setCursorPos(220, 12);
            module.visuals.render();
            module.modules.render();
            ImGui.popFont();
        }

        // Folder
        ImGui.setCursorPos(405, 26);
        ImGui.pushFont(ImguiLoader.poppins32);
        if (ImGui.invisibleButton("Folder", ImGui.calcTextSize("\uF07C").x, ImGui.calcTextSize("\uF07C").y)) {
            Template.configManager().openFolder();
        }
        if (ImGui.isItemHovered()) {
            ToolTipHolder.setToolTip("Open folder");
        }
        ImGui.setCursorPos(405, 26);
        ImGui.textColored(0.83f, 0.86f, 0.94f, ImGui.isItemHovered() ? 0.4f : 0.5f, "\uF07C");
        ImGui.popFont();

        // Refresh
        ImGui.setCursorPos(455, 26);
        ImGui.pushFont(ImguiLoader.poppins32);
        if (ImGui.invisibleButton("Refresh", ImGui.calcTextSize("\uF2F1").x, ImGui.calcTextSize("\uF2F1").y)) {
            Template.configManager().refreshProfiles();
        }
        if (ImGui.isItemHovered()) {
            ToolTipHolder.setToolTip("Refresh configs");
        }
        ImGui.setCursorPos(455, 26);
        ImGui.textColored(0.83f, 0.86f, 0.94f, ImGui.isItemHovered() ? 0.4f : 0.5f, "\uF2F1");
        ImGui.popFont();

        pos = ImGui.getWindowPos();
        GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.4f),
                ImGui.getColorU32(0f, 0f, 0f, 0f),
                5f);

        //float[] c = JColor.getGuiColor().getFloatColor();
        //GuiUtils.drawWindowShadow(
        //        ImGui.getColorU32(c[0], c[1], c[2], 1f),
        //        ImGui.getColorU32(c[0], c[1], c[2], 0f),
        //        16f);
        ImGui.end();
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
            colors[ImGuiCol.WindowBg]               = new float[]{0.13f, 0.14f, 0.18f, 1f};
            colors[ImGuiCol.ChildBg]                = new float[]{0.08f, 0.09f, 0.14f, 1.00f};
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