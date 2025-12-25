package template.rip.gui.clickgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.CharPressEvent;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.util.Arrays;

import static template.rip.Template.mc;

public class SearchBar implements Renderable {

    private static final SearchBar instance = new SearchBar();
    private final SearchHolder holder = new SearchHolder();
    private final ImString string = new ImString("", 0);
    private boolean isSelected;
    private int lastHash;
    public ImVec2 pos;

    private SearchBar() {}

    public void prepSearch() {
        String[] str = holder.get();
        if (str.length == 0) {
            Template.moduleManager.getModules().forEach(m -> m.setSearched(true));
            lastHash = -1;
        } else {
            int hash = Arrays.hashCode(str);
            if (lastHash != hash) {
                lastHash = hash;
                Template.moduleManager.getModules().forEach(m -> m.prepSearch(str));
            }
        }
    }

    public static SearchBar getInstance() {
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
        return "ASB5";
    }

    /*public enum SearchMode {Module, Setting, All};
    public SearchMode searchMode = SearchMode.All;
    boolean bl = switch (sb.searchMode) {
            case Module -> !module.getName().toLowerCase().contains(search);
            case Setting ->
                    module.settings.stream().noneMatch(s -> s.getName().toLowerCase().contains(search));
            case All ->
                    !module.getName().toLowerCase().contains(search) || module.settings.stream().noneMatch(s -> s.getName().toLowerCase().contains(search));
        };*/

    @Override
    public void render() {
        if (doNotRender()) {
            return;
        }

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDecoration;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 4, 6);
        ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0.5f, 0.5f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 16f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.setNextWindowPos(mc.getWindow().getWidth() / 2f - 200f, mc.getWindow().getHeight() - 70f);
        ImGui.setNextWindowSize(400f, 60f, 0);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.13f, 0.15f, 0.19f, 1.00f);
        ImGui.begin(getName(), imGuiWindowFlags);
        ImGui.popStyleColor(1);

        ImGui.setNextItemWidth(400f);
        ImGui.pushFont(ImguiLoader.poppins32);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 8f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 12f, 12f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.15f, 0.17f, 0.22f, 0f);
        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, .5f);
        ImGui.pushStyleColor(ImGuiCol.TextDisabled, 0.83f, 0.86f, 0.94f, 0.5f);

        ImVec2 preSearch = ImGui.getCursorPos();

        boolean empty = holder.isEmpty();
        String[] get;
        if (empty) {
            get = new String[]{"Sea", "rch", " mo", "dul", "es.", ".."};
        } else {
            get = holder.get();
        }
        ImGui.setCursorPos(12.5f, 15f);
        RenderUtils.drawTexts(get);
        ImGui.sameLine(0, 0);
        if (!empty) {
            ImGui.setCursorPosX(preSearch.x + RenderUtils.calcTextSize(get).x);
        } else {
            ImGui.setCursorPosX(preSearch.x);
        }

        ImGui.setCursorPosY(0);
        ImGui.inputText("##", string);
        isSelected = ImGui.isItemFocused();

        ImGui.setCursorPos(354f, 16f);
        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.75f);
        ImGui.pushFont(ImguiLoader.fontAwesome28);
        ImGui.text("\uF002");
        ImGui.popStyleColor(1);
        ImGui.popFont();
        ImGui.popStyleVar(2);
        ImGui.popStyleColor(3);
        ImGui.popFont();

        pos = ImGui.getWindowPos();
        GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.4f),
                ImGui.getColorU32(0f, 0f, 0f, 0f),
                5f);
        ImGui.popStyleVar(5);
        ImGui.end();
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        if (!ImguiLoader.isRendered(this)) {
            return;
        }

        if (doNotRender()) {
            return;
        }

        if (event.window != mc.getWindow().getHandle()) {
            return;
        }

        if (event.action != GLFW.GLFW_PRESS) {
            return;
        }

        if (event.key != GLFW.GLFW_KEY_BACKSPACE && event.key != GLFW.GLFW_KEY_DELETE) {
            return;
        }

        if (!isSelected) {
            return;
        }

        holder.pop();
    }

    @EventHandler
    private void onCharPress(CharPressEvent event) {
        if (!ImguiLoader.isRendered(this)) {
            return;
        }

        if (doNotRender()) {
            return;
        }

        if (event.window != mc.getWindow().getHandle()) {
            return;
        }

        if (!isSelected) {
            return;
        }

        holder.push(event.keys);
    }

    private boolean doNotRender() {
        return ConfigParent.getInstance().isOn ||
                LegitMenu.getInstance().isOn ||
                !Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class) ||
                !Template.moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI);
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
            colors[ImGuiCol.WindowBg] = new float[]{0.13f, 0.14f, 0.18f, 1f};
            colors[ImGuiCol.ChildBg] = new float[]{0.08f, 0.09f, 0.14f, 1.00f};
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