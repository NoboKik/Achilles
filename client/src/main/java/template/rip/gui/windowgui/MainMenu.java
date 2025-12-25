package template.rip.gui.windowgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.MinecraftClient;
import template.rip.Template;
import template.rip.api.config.ConfigManager;
import template.rip.api.config.ConfigProfile;
import template.rip.api.font.JColor;
import template.rip.api.util.AnimationUtil;
import template.rip.api.util.EasingUtil;
import template.rip.api.util.GuiUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.LegitMenu;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ConfigModule;

import java.util.Arrays;
import java.util.List;

public class MainMenu implements Renderable {

    public boolean firstFrame = true;
    public boolean firstFrameEver = true;
    private static MainMenu instance;
    public Module.Category selectedCategory = Module.Category.COMBAT;
    public ImVec2 pos;
    long openTime = System.currentTimeMillis();
    public ImString config = new ImString();
    public List<String> sections = Arrays.asList("\uF0CA Modules", "\uF07C Mod Menu", "\uF013 Config");
    public String selectedSection = "\uF0CA Modules";

    public static MainMenu getInstance() {
        if (instance == null) {
            instance = new MainMenu();
        }
        return instance;
    }

    public ImVec2 getPos() {
        return pos;
    }

    @Override
    public String getName() {
        return "Categories";
    }

    @Override
    public void render() {
        if (LegitMenu.getInstance().isOn) return;
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
        float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 400);
        float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);
        if (firstFrame) {
            if (Template.moduleManager.getModule(AchillesSettingsModule.class).openAnimation.isEnabled()) {
                ImGui.setNextWindowPos(posX, posY + 1000);
            } else {
                ImGui.setNextWindowPos(posX, posY);
            }
        }
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.18f, 1f);
        ImGui.begin(getName(), imGuiWindowFlags);
        ImGui.popStyleColor(1);
        if (firstFrame) {
            //pos = new ImVec2((float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 400),
            //        (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250));
            openTime = System.currentTimeMillis();
            firstFrame = false;
        }
        if (Template.moduleManager.getModule(AchillesSettingsModule.class).openAnimation.isEnabled()) {
            float percent = (float) (System.currentTimeMillis() - openTime) / 500;
            if (percent <= 1f) {
                ImGui.setWindowPos(posX, posY + 1000 - 1000 * EasingUtil.easeOutBack(percent));
            }
        }

        float[] color = JColor.getGuiColor().getFloatColor();

        ImGui.pushFont(ImguiLoader.poppins48);
        ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1.00f);
        ImGui.setCursorPos(16, 16);
        for (String str : new String[]{"Ach", "ill", "es"}) {
            ImGui.text(str);
            ImGui.sameLine(0, 0);
        }
        ImGui.popFont();
        ImGui.popStyleColor();

        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 8f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 7f, 7f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.15f, 0.17f, 0.22f, 0.5f);
        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TextDisabled, 0.83f, 0.86f, 0.94f, 0.5f);
        ImGui.setCursorPos(584, 16);
        ImGui.pushFont(ImguiLoader.poppins22);
        ImGui.setNextItemWidth(200f);
        ImVec2 pre = ImGui.getCursorPos().clone();
        if (selectedSection.contains("Config")) {
            ImGui.setNextItemWidth(125f);
            boolean configInput = ImGui.inputTextWithHint("##", "Config... ", config);
            ImGui.setCursorPos(584f + 200f - ImGui.calcTextSize("Create").x - 10f, 16);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 8f);
            ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 0.9f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.8f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 0.7f);
            ImGui.pushStyleColor(ImGuiCol.Text,       0.83f, 0.86f, 0.94f,1f);
            ImGui.pushFont(ImguiLoader.poppins24);
            float x = ImGui.calcTextSize("Create").x + 10;
            float y = 36;

            if (ImGui.button("Create", x, y)) {
                Template.configManager().addProfile(new ConfigProfile(config.get(), ConfigManager.pathProfilesFolder.resolve(config.get().toLowerCase().trim().replaceAll("[^A-Za-z0-9()\\[\\]]", "") + ".ac")));
                config.clear();
            }
            ImGui.popStyleColor(4);
            ImGui.popStyleVar();
            ImGui.popFont();
        }

        ImGui.popStyleColor(3);
        ImGui.popFont();
        ImGui.popStyleVar(2);

        ImGui.setCursorPosY(85);
        ImGui.setCursorPosX(0);
        for (String section : sections) {
            ImGui.pushID(section);

            if (selectedSection.equals(section)) {
                ImGui.pushStyleColor(ImGuiCol.Button,        0.11f, 0.12f, 0.16f, 0.5f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.11f, 0.12f, 0.16f, 0.4f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  0.11f, 0.12f, 0.16f, 0.3f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button,        0.11f, 0.12f, 0.16f, 0.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.11f, 0.12f, 0.16f, 0.4f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  0.11f, 0.12f, 0.16f, 0.3f);
            }
            ImVec2 prevPos = ImGui.getCursorPos().clone();
            ImGui.pushFont(ImguiLoader.poppins24);
            ImGui.getStyle().setFrameRounding(0f);
            ImGui.button("##", 169f, 50f);
            ImVec2 postPos = ImGui.getCursorPos().clone();
            if (selectedSection.equals(section)) {
                if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                else
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1f);
            } else {
                if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.2f);
                else
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.3f);
            }
            if (ImGui.isItemHovered() && (ImGui.isMouseClicked(1) || ImGui.isMouseClicked(0))) {
                if (!selectedSection.equals(section)) AnimationUtil.hookPress("CategorySwitch", true);
                selectedSection = section;
                ModulesMenu.getInstance().scrollY = 0;
                ModulesMenu.getInstance().scrollUntil = 0;
            }
            ImGui.setCursorPos(prevPos.x + 25, prevPos.y + 15);
            ImGui.text(section);
            ImGui.setCursorPos(postPos.x, postPos.y);

            if (selectedSection.equals(section)) {
                ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 1f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 1f);
                ImGui.setCursorPos(prevPos.x, prevPos.y);
                ImGui.button("##", 3f, 50f);
                ImGui.setCursorPos(postPos.x, postPos.y);
                ImGui.popStyleColor(3);
            }
            ImGui.getStyle().setFrameRounding(10f);
            ImGui.popFont();
            ImGui.popStyleColor(4);

            ImGui.popID();
        }

        // Config Manager
        ConfigModule module = Template.moduleManager.getModule(ConfigModule.class);
        if (module != null && selectedSection.contains("Config")) {
            ImGui.pushFont(ImguiLoader.poppins18);
            ImGui.setCursorPos(180, 8);
            module.visuals.render();
            module.modules.render();
            ImGui.popFont();
        }

        //ImGui.pushFont(ImguiLoader.getPoppins18());
        //ConfigModule module = Template.moduleManager().getModule(ConfigModule.class);
//
        //ImGui.setCursorPos(10, 300);
        //if (module != null) {
        //    module.visuals.render();
        //    module.modules.render();
        //}
        //ImGui.popFont();

        pos = ImGui.getWindowPos();
        GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.4f), ImGui.getColorU32(0f, 0f, 0f, 0f), 5f);

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