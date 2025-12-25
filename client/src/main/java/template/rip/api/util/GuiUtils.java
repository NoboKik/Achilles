package template.rip.api.util;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.language.TranslationStorage;
import template.rip.api.font.JColor;

import java.util.Collections;
import java.util.HashMap;

import static template.rip.Template.mc;

public class GuiUtils {

    private static final HashMap<LanguageDefinition, TranslationStorage> languageCache = new HashMap<>();

    public static void drawWindowShadow(int colorStart,  int colorEnd, float size, boolean fill) {
        drawShadow(ImGui.getWindowPos(), new ImVec2(ImGui.getWindowPosX() + ImGui.getWindowSizeX(), ImGui.getWindowPosY() + ImGui.getWindowSizeY()),
                ImGui.getStyle().getWindowRounding(), colorStart, colorEnd, size, fill);
    }

    public static void drawWindowShadow(int colorStart, int colorEnd, float size) {
        drawWindowShadow(colorStart,colorEnd,size,false);
    }

    public static void drawGradientWindowShadow(JColor colorStart, JColor colorEnd, float size, boolean fill) {
        int i = (int) -size;
        int max = (int) (ImGui.getWindowSizeY()+size);
        while (i < max) {
            JColor color1 = UI.blendColors(colorStart, colorEnd, (float) i / max);
            JColor color2 = color1.setAlpha(0);
            drawGradientWindowShadow(size, fill, i, color1, color2);
            i++;
        }
    }

    public static void drawWaveWindowShadow(float speed, float index, float size, boolean fill, float alpha, float alpha2) {
        int i = (int) -size;
        int max = (int) (ImGui.getWindowSizeY()+size);
        while(i < max) {
            JColor color1 = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (11 - speed), (int) (index / 20 * i),
                    UI.getColorOne(), UI.getColorTwo(), false)).setAlpha((int) (alpha * 255));
            JColor color2 = color1.setAlpha((int) (alpha2 * 255));
            drawGradientWindowShadow(size, fill, i, color1, color2);
            i++;
        }
    }

    public static void drawHWaveWindowShadow(float speed, float index, float size, boolean fill, float alpha1, float alpha2) {
        int i = (int) -size;
        int max = (int) (ImGui.getWindowSizeX()+size);
        while (i < max) {
            JColor color1 = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (11 - speed), (int) (index/20 * i), UI.getColorOne(), UI.getColorTwo(), false));
            drawWindowShadow(size, alpha1, alpha2, fill, i, color1);
            i++;
        }
    }

    public static void drawHRainbowWindowShadow(float sat, float bright, float speed, float index, float size, float alpha1, float alpha2, boolean fill) {
        int i = (int) -size;
        int max = (int) (ImGui.getWindowSizeX()+size);
        while (i < max) {
            JColor color1 = new JColor(ColorUtil.rainbow((int) (11 - speed), (int) (index/20 * i), sat, bright, 1f));
            drawWindowShadow(size, alpha1, alpha2, fill, i, color1);
            i++;
        }
    }

    public static void drawRainbowWindowShadow(float sat, float bright, float speed, float index, float size, boolean fill) {
        int i = (int) -size;
        int max = (int) (ImGui.getWindowSizeY()+size);
        while (i < max) {
            JColor color1 = new JColor(ColorUtil.rainbow((int) (11 - speed), (int) (index/20 * i), sat, bright, 1f));
            JColor color2 = color1.setAlpha(0);
            drawGradientWindowShadow(size, fill, i, color1, color2);
            i++;
        }
    }


    //            if (waveEnabled.isEnabled()) {
    //                initColor = ColorUtil.interpolateColorsBackAndForth(
    //                        (int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(),
    //                        index * waveIndex.getIValue(),
    //                        UI.getColorOne(),
    //                        UI.getColorTwo(),
    //                        false
    //                );
    //            }

    private static void drawWindowShadow(float size, float alpha1, float alpha2, boolean fill, int i, JColor color1) {
        color1 = color1.setAlpha((int) (alpha1 * 255));
        JColor color2 = color1.setAlpha((int) (alpha2 * 255));
        float minY = ImGui.getWindowPos().y - size;
        if (minY < 0) minY = 0;
        float maxY = ImGui.getWindowPos().y + ImGui.getWindowSizeY() + size;
        if (maxY < 0) maxY = 0;
        ImGui.getBackgroundDrawList().pushClipRect(ImGui.getWindowPos().x + i, minY, ImGui.getWindowPos().x + i + 1, maxY, true);
        drawWindowShadow(color1.getU32(), color2.getU32(), size, fill);
        ImGui.getBackgroundDrawList().popClipRect();
    }

    private static void drawGradientWindowShadow(float size, boolean fill, int i, JColor color1, JColor color2) {
        ImGui.getBackgroundDrawList().pushClipRect(
                ImGui.getWindowPos().x - size,
                ImGui.getWindowPos().y + i,
                ImGui.getWindowPos().x + ImGui.getWindowSizeX() + size,
                ImGui.getWindowPos().y + i + 1,
                true
        );
        drawWindowShadow(color1.getU32(), color2.getU32(), size, fill);
        ImGui.getBackgroundDrawList().popClipRect();
    }


    public static void drawGradientWindowShadow(JColor colorStart, JColor colorEnd, float size) {
        drawGradientWindowShadow(colorStart, colorEnd, size,false);
    }

    public static void drawShadow(ImVec2 pos1, ImVec2 pos2, float rounding, int colorStart, int colorEnd, float size, boolean fill) {
        if (rounding < 0) rounding = 0;

        ImDrawList backgroundDrawList = ImGui.getBackgroundDrawList();

        if (fill) {
            backgroundDrawList.addRect(pos1.x - 1, pos1.y - 1, pos2.x + 1, pos2.y + 1,
                    evaluateColor(1 / size, colorStart, colorEnd), rounding + 1);
        }

        for (int i = fill ? 2 : 1; i < size + 1; i++) {
            backgroundDrawList.addRect(pos1.x - i, pos1.y - i, pos2.x + i, pos2.y + i,
                    evaluateColor(i / size, colorStart, colorEnd), rounding + i
            );
        }
    }

    public static void drawShadow(ImVec2 pos1, ImVec2 pos2, float rounding, int colorStart, int colorEnd, float size) {
        drawShadow(pos1, pos2, rounding, colorStart, colorEnd, size, false);
    }

    public static void drawFilledShadow(ImVec2 pos1, ImVec2 pos2, float rounding, int color, float size) {
        for (int i = 1; i < size + 1; i++) {
            ImGui.getBackgroundDrawList().addRectFilled(pos1.x - i, pos1.y - i, pos2.x + i, pos2.y + i, color, rounding + i);
        }
    }

    public static int evaluateColor(float fraction, int startValue, int endValue) {
        int result;

        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        result = ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                (startB + (int) (fraction * (endB - startB)));

        return result;
    }

    //from tarasande
    public static String uncoverTranslation(String key) {
        LanguageDefinition languageDefinition = LanguageManager.ENGLISH_US;
        if (languageCache.containsKey(languageDefinition)) {
            return languageCache.get(languageDefinition).get(key);
        }
        languageCache.put(languageDefinition, TranslationStorage.load(mc.getResourceManager(), Collections.singletonList("en_us"), languageDefinition.rightToLeft()));
        return languageCache.get(languageDefinition).get(key);
    }
}
