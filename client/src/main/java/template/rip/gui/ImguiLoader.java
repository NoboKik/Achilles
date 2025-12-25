package template.rip.gui;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profilers;
import template.rip.Template;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class ImguiLoader {

    private static boolean isInit = false;
    private static final Set<Renderable> renderstack = new HashSet<>();

    private static final Set<Renderable> toRemove = new HashSet<>();
    private static final Set<Renderable> toAdd = new HashSet<>();

    public static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    // Breul
    public static ImFont breul20;
    public static ImFont breul18;
    public static ImFont breul24;
    public static ImFont breul26;
    public static ImFont breul32;
    public static ImFont breul48;

    // Highbrow
    public static ImFont highbrow20;
    public static ImFont highbrow18;
    public static ImFont highbrow24;
    public static ImFont highbrow26;
    public static ImFont highbrow32;
    public static ImFont highbrow48;

    // Product Sans
    public static ImFont product20;
    public static ImFont product18;
    public static ImFont product24;
    public static ImFont product26;
    public static ImFont product32;
    public static ImFont product48;

    public static ImFont productBold20;
    public static ImFont productBold18;
    public static ImFont productBold24;
    public static ImFont productBold26;
    public static ImFont productBold32;
    public static ImFont productBold48;

    // Iosevka
    public static ImFont iosevka20;
    public static ImFont iosevka18;
    public static ImFont iosevka24;
    public static ImFont iosevka26;
    public static ImFont iosevka32;
    public static ImFont iosevka48;

    public static ImFont iosevkaBold20;
    public static ImFont iosevkaBold18;
    public static ImFont iosevkaBold24;
    public static ImFont iosevkaBold26;
    public static ImFont iosevkaBold32;
    public static ImFont iosevkaBold48;

    // Poppins
    public static ImFont poppins12;
    public static ImFont poppins20;
    public static ImFont poppins22;
    public static ImFont poppins18;
    public static ImFont poppins24;
    public static ImFont poppins32;
    public static ImFont poppins48;

    public static ImFont mediumPoppins18;
    public static ImFont mediumPoppins20;
    public static ImFont mediumPoppins24;
    public static ImFont mediumPoppins32;
    public static ImFont mediumPoppins48;

    public static ImFont semiBoldPoppins18;
    public static ImFont semiBoldPoppins24;
    public static ImFont semiBoldPoppins32;
    public static ImFont semiBoldPoppins48;

    // Montserrat
    public static ImFont montserrat20;
    public static ImFont montserrat22;
    public static ImFont montserrat24;
    public static ImFont montserrat32;
    public static ImFont montserrat48;

    public static ImFont mediumMontserrat18;
    public static ImFont mediumMontserrat20;
    public static ImFont mediumMontserrat24;
    public static ImFont mediumMontserrat32;
    public static ImFont mediumMontserrat48;

    public static ImFont semiBoldMontserrat18;
    public static ImFont semiBoldMontserrat22;
    public static ImFont semiBoldMontserrat24;
    public static ImFont semiBoldMontserrat32;
    public static ImFont semiBoldMontserrat48;

    // Outfit
    public static ImFont outfit20;
    public static ImFont outfit18;
    public static ImFont outfit24;
    public static ImFont outfit26;
    public static ImFont outfit32;
    public static ImFont outfit48;

    public static ImFont semiBoldOutfit18;
    public static ImFont semiBoldOutfit24;
    public static ImFont semiBoldOutfit26;
    public static ImFont semiBoldOutfit30;
    public static ImFont semiBoldOutfit32;
    public static ImFont semiBoldOutfit48;

    public static ImFont mediumOutfit18;
    public static ImFont mediumOutfit20;
    public static ImFont mediumOutfit24;
    public static ImFont mediumOutfit26;
    public static ImFont mediumOutfit30;
    public static ImFont mediumOutfit32;
    public static ImFont mediumOutfit48;

    // Roboto
    public static ImFont roboto20;
    public static ImFont roboto18;
    public static ImFont roboto24;
    public static ImFont roboto32;
    public static ImFont roboto48;

    public static ImFont mediumRoboto18;
    public static ImFont mediumRoboto24;
    public static ImFont mediumRoboto32;
    public static ImFont mediumRoboto48;

    // Font Awesome
    public static ImFont fontAwesome12;
    public static ImFont fontAwesome16;
    public static ImFont fontAwesome20;
    public static ImFont fontAwesome28;
    public static ImFont fontAwesome60;

    // MC
    public static ImFont mc20;
    public static ImFont mc18;
    public static ImFont mc24;
    public static ImFont mc32;
    public static ImFont mc48;

    private ImguiLoader() {}

    public static void onGlfwInit(long handle) {
        initializeImGui();
        imGuiGlfw.init(handle,true);
        imGuiGl3.init();
    }

    public static void onFrameRender() {
        if (isInit && ImGui.getIO().getFonts().isBuilt()) {
            imGuiGlfw.newFrame();
            ImGui.newFrame();
        }

        ToolTipHolder.clearToolTip();

//      ImGui.getIO().setKeyCtrl(false);
//      ImGui.getIO().setKeyAlt(false);

        if (Template.INSTANCE != null && AchillesMenu.isClientEnabled() && Template.moduleManager != null) {
            AchillesSettingsModule sm = Template.moduleManager.getModule(AchillesSettingsModule.class);
            if (sm != null) sm.updateMode();
        }

        // User render code
        for (Renderable renderable : renderstack) {
            if (!AchillesMenu.isClientEnabled()) {
                break;
            }
            Profilers.get().push("ImGui Render " + renderable.getName());
            renderable.getTheme().preRender();
            ImGui.pushAllowKeyboardFocus(false);
            try {
                renderable.render();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            ImGui.popAllowKeyboardFocus();
            renderable.getTheme().postRender();
            Profilers.get().pop();
        }
        // End of user code

        String[] toolTip = ToolTipHolder.getToolTip();
        if (toolTip.length != 0) {
            ImGui.pushFont(poppins18);
            float x = ImGui.getMousePosX() + 10;
            float y = ImGui.getMousePosY() + 10;
            float startX = x + 5;
            float yOffset = 0;
            ImVec2 w = RenderUtils.calcTextSizeWithNewLines(toolTip);

            float width = w.x + 5 * 2;
            float height = w.y + 5 * 2;

            ImGui.getForegroundDrawList().addRectFilled(
                    x-1, y-1, x + width + 1, y + height + 1,
                    ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f),
                    5f
            );
            ImGui.getForegroundDrawList().addRectFilled(
                    x, y, x + width, y + height,
                    ImGui.getColorU32(0.15f, 0.17f, 0.22f, 1f),
                    4f
            );

            float space = 0;
            for (String s : toolTip) {
                int newLineIndex = s.indexOf("\n");
                String[] c = s.split("\n");
                if (c.length == 1) {
                    if (newLineIndex == 0) {
                        space = 0;
                        yOffset += ImGui.calcTextSize(c[0]).y;
                    }
                    ImGui.getForegroundDrawList().addText(
                            startX + space,
                            y + 5 + yOffset,
                            ImGui.getColorU32(0.83f, 0.86f, 0.94f, 1f),
                            c[0]
                    );
                    space += ImGui.calcTextSize(c[0]).x;
                    if (newLineIndex != 0 && newLineIndex != -1) {
                        space = 0;
                        yOffset += ImGui.calcTextSize(c[0]).y;
                    }
                } else {
                    for (int i = 0; i < c.length; i++) {
                        ImGui.getForegroundDrawList().addText(
                                startX + space,
                                y + 5 + yOffset,
                                ImGui.getColorU32(0.83f, 0.86f, 0.94f, 1f),
                                c[i]
                        );
                        if (i != c.length - 1) {
                            space = ImGui.calcTextSize(c[i]).x;
                            yOffset += ImGui.calcTextSize(c[i]).y;
                        } else {
                            space += ImGui.calcTextSize(c[i]).x;
                        }
                    }

                }
            }
            ImGui.popFont();
        }

        ImGui.render();
        endFrame();
    }

    private static void initializeImGui() {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);                               // We don't want to save .ini file
        //io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        //io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts); // Otherwise fonts are huge on large screens
        //io.addConfigFlags(ImGuiConfigFlags.DockingEnable);     // Enable Docking
        //io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);   // Enable Multi-Viewport / Platform Windows
        //io.setConfigViewportsNoTaskBarIcon(true);

        ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide

        short iconRangeMin = (short) 0xe005;
        short iconRangeMax = (short) 0xf8ff;
        short[] iconRange = new short[]{iconRangeMin, iconRangeMax, 0};

        rangesBuilder.addRanges(iconRange);

        ImFontConfig iconsConfig = new ImFontConfig();

        iconsConfig.setMergeMode(true);
        iconsConfig.setPixelSnapH(true);
        iconsConfig.setOversampleH(3);
        iconsConfig.setOversampleV(3);

        ImFontAtlas fontAtlas = io.getFonts();
        ImFontConfig fontConfig = new ImFontConfig(); // Natively allocated object, should be explicitly destroyed

        fontAtlas.addFontDefault();
        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesCyrillic());
        byte[] fontAwesomeData = null;

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/fa-solid-900.ttf")) {
            if (is != null) {
                fontAwesomeData = is.readAllBytes();
            }
        } catch (IOException ignored) {}

        byte[] poppinsFontData = null;
        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Poppins-Regular.ttf")) {
            if (is != null) {
                poppinsFontData = is.readAllBytes();

                // Poppins
                poppins12 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 12);
                poppins18 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 18);
                poppins20 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 20);
                poppins22 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 22);
                poppins24 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 24);
                poppins32 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 32);
                poppins48 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 48);
            }
        } catch (IOException ignored) {}

        byte[] poppinsFontData2;
        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Poppins-Medium.ttf")) {
            if (is != null) {
                poppinsFontData2 = is.readAllBytes();

                // Medium Poppins
                mediumPoppins18 = fontAtlas.addFontFromMemoryTTF(poppinsFontData2, 18);
                mediumPoppins20 = fontAtlas.addFontFromMemoryTTF(poppinsFontData2, 20);
                mediumPoppins24 = fontAtlas.addFontFromMemoryTTF(poppinsFontData2, 24);
                mediumPoppins32 = fontAtlas.addFontFromMemoryTTF(poppinsFontData2, 32);
                mediumPoppins48 = fontAtlas.addFontFromMemoryTTF(poppinsFontData2, 48);
            }
        } catch (IOException ignored) {}

        byte[] poppinsFontData3;
        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Poppins-SemiBold.ttf")) {
            if (is != null) {
                poppinsFontData3 = is.readAllBytes();

                // Medium Poppins
                semiBoldPoppins18 = fontAtlas.addFontFromMemoryTTF(poppinsFontData3, 18);
                semiBoldPoppins24 = fontAtlas.addFontFromMemoryTTF(poppinsFontData3, 24);
                semiBoldPoppins32 = fontAtlas.addFontFromMemoryTTF(poppinsFontData3, 32);
                semiBoldPoppins48 = fontAtlas.addFontFromMemoryTTF(poppinsFontData3, 48);
            }
        } catch (IOException ignored) {}

        byte[] mcFontData;
        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Minecraft.ttf")) {
            if (is != null) {
                mcFontData = is.readAllBytes();

                // Minecraft
                mc18 = fontAtlas.addFontFromMemoryTTF(mcFontData, 18);
                mc20 = fontAtlas.addFontFromMemoryTTF(mcFontData, 20);
                mc24 = fontAtlas.addFontFromMemoryTTF(mcFontData, 24);
                mc32 = fontAtlas.addFontFromMemoryTTF(mcFontData, 32);
                mc48 = fontAtlas.addFontFromMemoryTTF(mcFontData, 48);
            }
        } catch (IOException ignored) {}

        // Other fonts
        byte[] otherFontData;
        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Highbrow-Regular.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                highbrow18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                highbrow20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                highbrow24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                highbrow26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                highbrow32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                highbrow48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Breul-Bold.otf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                breul18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                breul20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                breul24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                breul26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                breul32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                breul48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Product-Regular.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                product18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                product20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                product24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                product26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                product32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                product48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Product-Bold.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                productBold18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                productBold20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                productBold24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                productBold26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                productBold32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                productBold48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Iosevka-Bold.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                iosevkaBold18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                iosevkaBold20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                iosevkaBold24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                iosevkaBold26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                iosevkaBold32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                iosevkaBold48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Iosevka-Regular.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                iosevka18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                iosevka20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                iosevka24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                iosevka26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                iosevka32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                iosevka48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Outfit-Regular.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                outfit18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                outfit20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                outfit24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                outfit26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                outfit32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                outfit48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Outfit-SemiBold.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                semiBoldOutfit18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                semiBoldOutfit24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                semiBoldOutfit26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                semiBoldOutfit30 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                semiBoldOutfit32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 32);
                semiBoldOutfit48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Outfit-Medium.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                mediumOutfit18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                mediumOutfit20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                mediumOutfit24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                mediumOutfit26 = fontAtlas.addFontFromMemoryTTF(otherFontData, 26);
                mediumOutfit30 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                mediumOutfit32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 32);
                mediumOutfit48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Roboto-Regular.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                roboto18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                roboto20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                roboto24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                roboto32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 32);
                roboto48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/Roboto-SemiBold.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                mediumRoboto18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                mediumRoboto24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 24);
                mediumRoboto32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 32);
                mediumRoboto48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Montserrat-Regular.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                montserrat20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                montserrat22 = fontAtlas.addFontFromMemoryTTF(otherFontData, 22);
                montserrat24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 22);
                montserrat32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                montserrat48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Montserrat-Medium.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                mediumMontserrat18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                mediumMontserrat20 = fontAtlas.addFontFromMemoryTTF(otherFontData, 20);
                mediumMontserrat24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 22);
                mediumMontserrat32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                mediumMontserrat48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        try (InputStream is = ImguiLoader.class.getClassLoader().getResourceAsStream("assets/fonts/Montserrat-SemiBold.ttf")) {
            if (is != null) {
                otherFontData = is.readAllBytes();
                semiBoldMontserrat18 = fontAtlas.addFontFromMemoryTTF(otherFontData, 18);
                semiBoldMontserrat22 = fontAtlas.addFontFromMemoryTTF(otherFontData, 22);
                semiBoldMontserrat24 = fontAtlas.addFontFromMemoryTTF(otherFontData, 22);
                semiBoldMontserrat32 = fontAtlas.addFontFromMemoryTTF(otherFontData, 30);
                semiBoldMontserrat48 = fontAtlas.addFontFromMemoryTTF(otherFontData, 48);
            }
        } catch (IOException ignored) {}

        // Initialize fonts

        // // When enabled, all fonts added with this config would be merged with the previously added font
        // fontConfig.setMergeMode(true);
        //
        // // Font Awesome
        // fontAwesome12 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 12, iconsConfig, iconRange);
        // fontAwesome16 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 16, iconsConfig, iconRange);
        // fontAwesome18 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 18, iconsConfig, iconRange);
        // fontAwesome20 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 20, iconsConfig, iconRange);
        // fontAwesome28 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 28, iconsConfig, iconRange);
        // fontAwesome60 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 60, iconsConfig, iconRange);

        fontConfig.setMergeMode(true); // When enabled, all fonts added with this config would be merged with the previously added font
        poppins18 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 18);
        fontAwesome20 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 20, iconsConfig, iconRange);
        poppins24 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 24);
        fontAwesome20 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 20, iconsConfig, iconRange);
        poppins32 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 32);
        fontAwesome28 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 28, iconsConfig, iconRange);
        poppins20 = fontAtlas.addFontFromMemoryTTF(poppinsFontData, 20);
        fontAwesome16 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 16, iconsConfig, iconRange);
        fontAwesome12 = fontAtlas.addFontFromMemoryTTF(fontAwesomeData, 12, iconsConfig, iconRange);
        fontConfig.destroy();
        fontAtlas.build();

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
        isInit = true;
    }

    private static void endFrame() {
        // After Dear ImGui prepared a draw data, we use it in the LWJGL3 renderer.
        // At that moment ImGui will be rendered to the current OpenGL context.
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }

        synchronized (renderstack) {
            synchronized (toRemove) {
                renderstack.removeAll(toRemove);
                toRemove.clear();
            }
        }

        synchronized (renderstack) {
            synchronized (toAdd) {
                renderstack.addAll(toAdd);
                toAdd.clear();
            }
        }
    }

    public static void addRenderable(Renderable renderable) {
        synchronized (toAdd) {
            toAdd.add(renderable);
        }
    }

    public static void queueRemove(Renderable renderable) {
        synchronized (toRemove) {
            toRemove.add(renderable);
        }
    }

    public static boolean isRendered(Renderable renderable) {
        synchronized (renderstack) {
            return renderstack.contains(renderable);
        }
    }
}