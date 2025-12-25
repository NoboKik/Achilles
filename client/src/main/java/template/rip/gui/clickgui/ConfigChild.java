package template.rip.gui.clickgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.config.ConfigProfile;
import template.rip.api.font.JColor;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ConfigModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigChild implements Renderable {

    private static ConfigChild instance;
    public static Module editingModule = null;
    public float scrollY = 0;
    public float scrollUntil = 0;

    public static ConfigChild getInstance() {
        if (instance == null) {
            instance = new ConfigChild();
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
        if (!ConfigParent.getInstance().isOn) return;
        ConfigParent.getInstance().render();

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
        ImGui.setNextWindowSize(800f, 420f, 0);
        ImGui.begin(getName(), imGuiWindowFlags);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.getStyle().setWindowPadding(6,6);
        ImGui.getStyle().setItemSpacing(8, 4);
        ImGui.getStyle().setItemInnerSpacing(4, 4);
        ImGui.getStyle().setWindowBorderSize(1f);
        ImGui.getStyle().setFrameBorderSize(0f);

        //float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 330);
        //float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);
        float posX = ConfigParent.getInstance().getPos().x;
        float posY = ConfigParent.getInstance().getPos().y+80;
        ImGui.setWindowPos(posX, posY);

        if (scrollUntil > ImGui.getScrollMaxY()) {
            scrollUntil = ImGui.getScrollMaxY();
        } else if (scrollUntil < 0) {
            scrollUntil = 0;
        }

        scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
        if (editingModule == null) ImGui.setScrollY(scrollY);

        Set<Map.Entry<String, ConfigProfile>> all = Template.configManager().profilesByName.entrySet();
        all.removeIf(al -> al.getKey() == null);
        //if (!MainMenu.getInstance().config.isEmpty())
        //    all.removeIf(s -> !s.toLowerCase().contains(MainMenu.getInstance().config.get().toLowerCase()));
        AchillesSettingsModule m = Template.moduleManager.getModule(AchillesSettingsModule.class);
        ImGui.setCursorPos(20, 20);

        List<ConfigProfile> toRemove = new ArrayList<>();

        int row = 0;
        int column = 0;
        for (Map.Entry<String, ConfigProfile> fullCfg : all) {
            String config = fullCfg.getKey();
            ConfigProfile profile = fullCfg.getValue();
            ImGui.setCursorPosX(20 + 192 * column);
            ImGui.setCursorPosY(20 + 160 * row);
            ImGui.pushID(String.format("Config/%s", config));

            ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.3f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.5f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 0.5f);

            ImGui.pushFont(ImguiLoader.poppins24);
            ImVec2 prevPos = ImGui.getCursorPos();
            int color;
//            if (profile == Template.configManager().currentProfile) {
//                Color mid = ColorUtil.interpolateColorC(new Color(0.16f, 0.18f, 0.24f), new Color(JColor.getGuiColor().getRGB()), 0.5f);
//                color = ImGui.getColorU32(mid.getRed() / 255f, mid.getGreen() / 255f, mid.getBlue() / 255f, 0.3f);
//            } else {
                color = ImGui.getColorU32(0.16f, 0.18f, 0.24f, 0.3f);
//            }
            ImGui.getWindowDrawList().addRectFilled(ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX()+182f,
                    ImGui.getCursorScreenPosY()+150f,
                    color,
                    8f
            );
            ImGui.setCursorPos(prevPos.x, prevPos.y+203);
            ImVec2 postPos = ImGui.getCursorPos();
            ImGui.popStyleColor(3);
            ImGui.popFont();

            // Config name
            ImGui.pushStyleColor(ImGuiCol.Text, 0.52f, 0.54f, 0.63f, 1.00f);
            ImGui.pushFont(ImguiLoader.poppins24);
            ImGui.setCursorPos(prevPos.x + 20f, prevPos.y + 150f - 20f - 24f);
            ImGui.text(config);
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();
            ImGui.popStyleColor(1);


            // Save button
            ImGui.pushFont(ImguiLoader.fontAwesome20);
            ImGui.setCursorPos(prevPos.x + 20f, prevPos.y + 20f);
            if (ImGui.invisibleButton("Save", ImGui.calcTextSize("\uF0C7").x, ImGui.calcTextSize("\uF0C7").y)) {
                profile.saveProfile();
            }
            ImGui.setCursorPos(prevPos.x + 20f, prevPos.y + 20f);
            ImGui.textColored(0.65f, 0.81f, 0.53f, ImGui.isItemHovered() ? 0.8f : 1f, "\uF0C7");
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();

            // Load button
            ImGui.pushFont(ImguiLoader.fontAwesome20);
            ImGui.setCursorPos(prevPos.x + 50f, prevPos.y + 20f);
            if (ImGui.invisibleButton("Load", ImGui.calcTextSize("\uF019").x, ImGui.calcTextSize("\uF019").y)) {
                ConfigModule configModule = Template.moduleManager.getModule(ConfigModule.class);
                if (configModule != null) {
                    if (configModule.modules.isEnabled()) {
                        profile.loadProfile(false);
                    }
                    if (configModule.visuals.isEnabled()) {
                        profile.loadProfile(true);
                    }
                }
            }
            ImGui.setCursorPos(prevPos.x + 50f, prevPos.y + 20f);
            ImGui.textColored(0.54f, 0.67f, 0.93f, ImGui.isItemHovered() ? 0.8f : 1f, "\uF019");
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();



            // Delete button
            ImGui.pushFont(ImguiLoader.fontAwesome20);
            ImGui.setCursorPos(prevPos.x + 150f, prevPos.y + 20f);
            if (ImGui.invisibleButton("Delete", ImGui.calcTextSize("\uF1F8").x, ImGui.calcTextSize("\uF1F8").y)) {
//                System.out.println(config + " delete");
                toRemove.add(profile);
            }
            ImGui.setCursorPos(prevPos.x + 150f, prevPos.y + 20f);
            ImGui.textColored(0.79f, 0.24f, 0.32f, ImGui.isItemHovered() ? 0.8f : 1f, "\uF1F8");
            ImGui.setCursorPos(postPos.x, postPos.y);
            ImGui.popFont();

            ImGui.popID();

            column++;
            if (column > 3) {
                column = 0;
                row++;
            }
        }

        toRemove.forEach(prof -> Template.configManager().removeProfile(prof));

        //ImGui.setCursorPos(0, 414);
        //ImGui.button("##", 16, 16);
        //ImGui.setCursorPos(614, 0);
        //ImGui.button("##", 16, 16);
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