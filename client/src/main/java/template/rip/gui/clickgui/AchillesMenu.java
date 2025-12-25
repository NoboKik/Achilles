package template.rip.gui.clickgui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import template.rip.MixinMethods;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ClientDestructModule;
import template.rip.module.modules.render.ArrayListModule;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AchillesMenu implements Renderable {

    private static final AchillesMenu instance;

    private static final AtomicBoolean clientEnabled = new AtomicBoolean(true);
    public final List<CategoryTab> tabs = new ArrayList<>();

    static {
        instance = new AchillesMenu();

        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        float posX = 10f;
        for (Module.Category category : Module.Category.values()) {
            if (category.equals(Module.Category.LEGIT) || category.equals(Module.Category.ALL)) continue;
            instance.tabs.add(new CategoryTab(category, posX, 10f));
            posX += asm != null && asm.clickGuiStyle.is(AchillesSettingsModule.styleEnum.Moon) ? 255f : 205f;
        }
    }

    public static AchillesMenu getInstance() {
        return instance;
    }

    public static void toggleVisibility() {
        if (ImguiLoader.isRendered(getInstance())) {
            ImguiLoader.queueRemove(getInstance());
        } else {
            ImguiLoader.addRenderable(getInstance());
        }
    }

    public static boolean isClientEnabled() {
        return clientEnabled.get();
    }

    public static void stopClient() {
        MixinMethods.mc14();

        AchillesSettingsModule sm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (sm != null && sm.isEnabled()) sm.setEnabled(false);

        ArrayListModule arrayListModule = Template.moduleManager.getModule(ArrayListModule.class);
        if (arrayListModule != null && arrayListModule.isEnabled()) arrayListModule.setEnabled(false);

        clientEnabled.set(false);

        try {
            String sdf = Template.moduleManager.getModule(ClientDestructModule.class).downloadURL.getContent();
            ClientDestructModule.modeEnum mode = Template.moduleManager.getModule(ClientDestructModule.class).mode.getMode();
            for (Module module : Template.moduleManager.getModules()) {
                if (module.isEnabled())
                    module.setEnabled(false);
            }

            File clientFile = new File(/*URLDecoder.decode(*/Template.class.getProtectionDomain().getCodeSource().getLocation().toURI()/*, UTF_8)*/);
            if (mode == ClientDestructModule.modeEnum.Replace) {
                long time = clientFile.lastModified();
                try (BufferedInputStream inputStream = new BufferedInputStream(new URL(sdf).openStream());
                     FileOutputStream fileOS = new FileOutputStream(clientFile)) {
                    byte[] data = new byte[102400];
                    int byteContent;
                    while ((byteContent = inputStream.read(data, 0, 102400)) != -1) {
                        fileOS.write(data, 0, byteContent);
                    }
                } catch (Exception ignored) {
                }
                File newClientFile = new File(URLDecoder.decode(Template.class.getProtectionDomain().getCodeSource().getLocation().getPath(), UTF_8));
                newClientFile.setLastModified(time);
            } else if (mode == ClientDestructModule.modeEnum.Delete) {
                clientFile.delete();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public String getName() {
        return Template.name;
    }

    public static int searchRGB = Color.WHITE.getRGB();

    @Override
    public void render() {
        if (LegitMenu.getInstance().isOn || ConfigParent.getInstance().isOn) return;
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (asm != null) {
            JColor temp = asm.searchColor.getColor();
            searchRGB = new JColor(temp.getRed(), temp.getGreen(), temp.getBlue(), 255).getU32();
        }
        SearchBar.getInstance().prepSearch();
        tabs.forEach(CategoryTab::render);
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
            colors[ImGuiCol.WindowBg]               = new float[]{0.13f, 0.14f, 0.19f, 0.8f};
            colors[ImGuiCol.ChildBg]                = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.PopupBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
            colors[ImGuiCol.Border]                 = new float[]{0.21f, 0.24f, 0.31f, 1.00f};
            colors[ImGuiCol.BorderShadow]           = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.FrameBg]                = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.FrameBgHovered]         = new float[]{color[0], color[1], color[2], 0.8f};
            colors[ImGuiCol.FrameBgActive]          = new float[]{color[0], color[1], color[2], 0.7f};
            colors[ImGuiCol.TitleBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgActive]          = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgCollapsed]       = new float[]{0.13f, 0.14f, 0.19f, 0.5f};
            colors[ImGuiCol.MenuBarBg]              = new float[]{0.13f, 0.14f, 0.19f, 1.00f};
            colors[ImGuiCol.ScrollbarBg]            = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.ScrollbarGrab]          = new float[]{0.18f, 0.21f, 0.27f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabHovered]   = new float[]{0.22f, 0.25f, 0.33f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabActive]    = new float[]{0.25f, 0.29f, 0.37f, 0.00f};
            colors[ImGuiCol.CheckMark]              = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            //colors[ImGuiCol.SliderGrab]             = new float[]{color[0], color[1], color[2], 0.9f};
            //colors[ImGuiCol.SliderGrabActive]       = new float[]{color[0], color[1], color[2], 0.95f};
            colors[ImGuiCol.SliderGrab]             = new float[]{1f, 1f, 1f, 1f};
            colors[ImGuiCol.SliderGrabActive]       = new float[]{0.9f, 0.9f, 0.9f, 1f};
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

            if (Template.moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.is(AchillesSettingsModule.styleEnum.Flat)) {
                colors[ImGuiCol.WindowBg]           = new float[]{0.12f, 0.14f, 0.18f, 1.00f};
                //colors[ImGuiCol.Border]             = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
                colors[ImGuiCol.Text]               = new float[]{0.83f, 0.86f, 0.94f, 1.00f};
            }
            ImGui.getStyle().setColors(colors);

            ImGui.getStyle().setWindowRounding(8);
            ImGui.getStyle().setFrameRounding(4);
            ImGui.getStyle().setGrabRounding(4);
            ImGui.getStyle().setPopupRounding(4);
            ImGui.getStyle().setScrollbarRounding(4);
            ImGui.getStyle().setTabRounding(4);
            ImGui.getStyle().setWindowTitleAlign(0.5f, 0.5f);
            ImGui.getStyle().setScrollbarSize(1);
            ImGui.getStyle().setWindowBorderSize(1f);
            //ImGui.getStyle().setWindowPadding(8f, 8f);
            //ImGui.getStyle().setFramePadding(4f, 3f);
            //ImGui.getStyle().setCellPadding(4f, 2f);
            ImGui.getStyle().setWindowPadding(0f, 0f);
            ImGui.getStyle().setFramePadding(0f, 0f);
            ImGui.getStyle().setCellPadding(0f, 0f);
            ImGui.getStyle().setItemSpacing(8, 4);
            ImGui.getStyle().setItemInnerSpacing(4, 4);
            ImGui.getStyle().setWindowBorderSize(1f);
            ImGui.getStyle().setFrameBorderSize(0f);

            if (Template.moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.is(AchillesSettingsModule.styleEnum.Flat)) {
                ImGui.getStyle().setWindowRounding(8);
            }
            if (Template.moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.is(AchillesSettingsModule.styleEnum.Moon)) {
                ImGui.getStyle().setWindowRounding(16);
            }

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