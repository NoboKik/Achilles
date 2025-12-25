package template.rip.api.util;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.PixelRegion;
import template.rip.gui.ImguiLoader;
import template.rip.module.modules.client.InterfaceModule;

import java.awt.*;
import java.util.ArrayList;

public class UI {

    public static JColor color1 = new JColor(0.79f, 0.24f, 0.32f, 1f);
    public static JColor color2 = new JColor(0.79f, 0.24f, 0.32f, 1f).jDarker();

    public static JColor getColorOne() {
        return color1;
    }

    public static JColor getColorTwo() {
        return color2;
    }

    public static Color interfaceColor(int speed, int index) {
        JColor color1 = new JColor(0.79f, 0.24f, 0.32f, 1f);
        JColor color2 = new JColor(0.79f, 0.24f, 0.32f, 1f).jDarker();

        InterfaceModule im = Template.moduleManager.getModule(InterfaceModule.class);
        if (im != null) {
            color1 = im.color1.getColor();
            color2 = im.color2.getColor();
            if (im.rainbowEnabled.isEnabled()) return ColorUtil.rainbow(speed, index, im.rainbowSaturation.getFValue(), im.rainbowBrightness.getFValue(), 1);
        }
        return ColorUtil.interpolateColorsBackAndForth(speed, index, color1, color2, false);
    }

    public static void drawWaveText(String s, int index, int speed) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            JColor jColor = new JColor(UI.interfaceColor(speed, index * i));
            float[] color = jColor.getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
            ImGui.text(c+"");
            ImGui.sameLine(0,0);
            ImGui.popStyleColor();
        }
    }

    public static ImFont getFont(int size) {
        return getFont(size, false, false);
    }

    public static ImFont getFont(int size, boolean isBold) {
        return getFont(size, isBold, false);
    }

    public static ImFont getFont(int size, boolean isBold, boolean isMc) {
        InterfaceModule module = Template.moduleManager.getModule(InterfaceModule.class);
        if (module == null) return ImguiLoader.poppins18;

        if (isMc) {
            switch (size) {
                case 18: return ImguiLoader.mc18;
                case 20: return ImguiLoader.mc20;
                case 24: return ImguiLoader.mc24;
                case 32: return ImguiLoader.mc32;
                case 48: return ImguiLoader.mc48;
            }
        }

        switch (module.font.getMode()) {
            case Poppins: {
                if (isBold) {
                    switch (size) {
                        case 18: return ImguiLoader.mediumPoppins18;
                        case 20: return ImguiLoader.mediumPoppins20;
                        case 24: return ImguiLoader.mediumPoppins24;
                        case 32: return ImguiLoader.mediumPoppins32;
                        case 48: return ImguiLoader.mediumPoppins48;
                    }
                } else {
                    switch (size) {
                        case 18: return ImguiLoader.poppins18;
                        case 20: return ImguiLoader.poppins20;
                        case 24: return ImguiLoader.poppins24;
                        case 32: return ImguiLoader.poppins32;
                        case 48: return ImguiLoader.poppins48;
                    }
                }
                break;
            }
            case Montserrat: {
                if (isBold) {
                    switch (size) {
                        case 18: return ImguiLoader.semiBoldMontserrat18;
                        case 22: return ImguiLoader.semiBoldMontserrat22;
                        case 24: return ImguiLoader.semiBoldMontserrat24;
                        case 32: return ImguiLoader.semiBoldMontserrat32;
                        case 48: return ImguiLoader.semiBoldMontserrat48;
                    }
                } else {
                    switch (size) {
                        case 18: return ImguiLoader.mediumMontserrat18;
                        case 20: return ImguiLoader.mediumMontserrat20;
                        case 24: return ImguiLoader.mediumMontserrat24;
                        case 32: return ImguiLoader.mediumMontserrat32;
                        case 48: return ImguiLoader.mediumMontserrat48;
                    }
                }
                break;
            }
            case Outfit: {
                if (isBold) {
                    switch (size) {
                        case 18: return ImguiLoader.semiBoldOutfit18;
                        case 24: return ImguiLoader.semiBoldOutfit24;
                        case 32: return ImguiLoader.semiBoldOutfit32;
                        case 48: return ImguiLoader.semiBoldOutfit48;
                    }
                } else {
                    switch (size) {
                        case 18: return ImguiLoader.outfit18;
                        case 20: return ImguiLoader.outfit20;
                        case 24: return ImguiLoader.outfit24;
                        case 32: return ImguiLoader.outfit32;
                        case 48: return ImguiLoader.outfit48;
                    }
                }
                break;
            }
            case Product: {
                if (isBold) {
                    switch (size) {
                        case 18: return ImguiLoader.productBold18;
                        case 24: return ImguiLoader.productBold24;
                        case 32: return ImguiLoader.productBold32;
                        case 48: return ImguiLoader.productBold48;
                    }
                } else {
                    switch (size) {
                        case 18: return ImguiLoader.product18;
                        case 20: return ImguiLoader.product20;
                        case 24: return ImguiLoader.product24;
                        case 32: return ImguiLoader.product32;
                        case 48: return ImguiLoader.product48;
                    }
                }
                break;
            }
            case Iosevka: {
                if (isBold) {
                    switch (size) {
                        case 18: return ImguiLoader.iosevkaBold18;
                        case 24: return ImguiLoader.iosevkaBold24;
                        case 32: return ImguiLoader.iosevkaBold32;
                        case 48: return ImguiLoader.iosevkaBold48;
                    }
                } else {
                    switch (size) {
                        case 18: return ImguiLoader.iosevka18;
                        case 20: return ImguiLoader.iosevka20;
                        case 24: return ImguiLoader.iosevka24;
                        case 32: return ImguiLoader.iosevka32;
                        case 48: return ImguiLoader.iosevka48;
                    }
                }
                break;
            }
        }
        return ImguiLoader.poppins18;
    }

    public static void shadowText(String text, int size, float r, float g, float b, float a) {
        float offset = size * 0.07f;

        ImVec2 pos = ImGui.getCursorPos();
        float r1 = r/4;
        float g1 = g/4;
        float b1 = b/4;
        ImGui.pushStyleColor(ImGuiCol.Text, r1, g1, b1, a);
        ImGui.text(text);
        ImGui.popStyleColor(1);
        ImGui.setCursorPos(pos.x - offset, pos.y - offset);
        ImGui.pushStyleColor(ImGuiCol.Text, r, g, b, a);
        ImGui.text(text);
        ImGui.popStyleColor(1);
    }

    public static JColor getFadeBetweenColors(JColor c1, JColor c2, double stage) {
        float[] cr = c1.getFloatColor();
        float[] cf1 = c1.getFloatColor();
        float[] cf2 = c2.getFloatColor();

        float r = cf1[0] - cf2[0];
        float g = cf1[1] - cf2[1];
        float b = cf1[2] - cf2[2];
        float a = cf1[3] - cf2[3];
        cr[0] = (float) (cr[0] + r * stage); if (cr[0] < 0) cr[0] = 0;
        cr[1] = (float) (cr[1] + g * stage); if (cr[1] < 0) cr[1] = 0;
        cr[2] = (float) (cr[2] + b * stage); if (cr[2] < 0) cr[2] = 0;
        cr[3] = (float) (cr[3] + a * stage); if (cr[3] < 0) cr[3] = 0;

//      System.out.println(cr[0] + ", " + cr[1] + ", " + cr[2] + ", " + cr[3]);
//      return new JColor(cr[0], cr[1], cr[2], cr[3]);

        return c2;
    }

    public static JColor blendColors(JColor color1, JColor color2, float ratio) {
        float inverseRatio = 1 - ratio;
        float a = color1.getFloatColor()[3] * inverseRatio + color2.getFloatColor()[3] * ratio;
        float r = color1.getFloatColor()[0] * inverseRatio + color2.getFloatColor()[0] * ratio;
        float g = color1.getFloatColor()[1] * inverseRatio + color2.getFloatColor()[1] * ratio;
        float b = color1.getFloatColor()[2] * inverseRatio + color2.getFloatColor()[2] * ratio;
        if (ratio == 1f) return color2;
        if (r > 1) r = 1; if (r < 0) r = 0f;
        if (g > 1) g = 1; if (g < 0) g = 0f;
        if (b > 1) b = 1; if (b < 0) b = 0f;
        if (a  >1) a = 1; if (a < 0) a = 0f;

        return new JColor(r, g, b, a);
    }

    public static void roundedClipRect(ImDrawList draw, Runnable runnable, float x, float y, float width, float height, float rounding) {
        ArrayList<PixelRegion> regions = new ArrayList<>();
        regions.add(new PixelRegion(x, y, x+rounding, y+rounding)); // top-left
        regions.add(new PixelRegion(x+width-rounding, y+height-rounding, x+width, y+height)); // bottom-right
        regions.add(new PixelRegion(x+width-rounding, y, x+width, y+rounding)); // top-right
        regions.add(new PixelRegion(x, y+height-rounding, x+rounding, y+height)); // bottom-left
        draw.pushClipRect(
                x+rounding,
                y,
                x+width-rounding,
                y+height,
                false
        );
        runnable.run();
        draw.popClipRect();

        draw.pushClipRect(
                x,
                y+rounding,
                x+rounding,
                y+height-rounding,
                false
        );
        runnable.run();
        draw.popClipRect();

        draw.pushClipRect(
                x+width-rounding,
                y+rounding,
                x+width,
                y+height-rounding,
                false
        );
        runnable.run();
        draw.popClipRect();

        drawSidedCircle(draw, runnable, regions, x+rounding, y+rounding, rounding, -1, 0, -1, 0);
        drawSidedCircle(draw, runnable, regions, x+width-rounding, y+rounding, rounding, 0, 1, -1, 0);
        drawSidedCircle(draw, runnable, regions, x+width-rounding, y+height-rounding, rounding, 0, 1, 0, 1);
        drawSidedCircle(draw, runnable, regions, x+rounding, y+height-rounding, rounding, -1, 0, 0, 1);
    }

    public static void drawSidedCircle(ImDrawList draw, Runnable runnable, ArrayList<PixelRegion> regions, float originX, float originY, float rounding, int minModX, int maxModX, int minModY, int maxModY) {
        for (int y1 = (int) -rounding; y1<rounding; y1++) {
            for (int x1 = (int) -rounding; x1<=rounding; x1++) {
                if (x1*x1+y1*y1 <= rounding*rounding) {
                    int i = 0;
                    for (PixelRegion r : regions) {
                        if (r.isInside(originX + x1, originY + y1)) i++;
                    }
                    if (i!=0 && y1 != -rounding && x1 != -rounding && y1 != rounding && x1 != rounding) {
                        draw.pushClipRect(
                                originX+x1+minModX,
                                originY+y1+minModY,
                                originX+x1+maxModX,
                                originY+y1+maxModY,
                                false
                        );
                        runnable.run();
                        draw.popClipRect();
                    }
                }
            }
        }
    }
}
