package template.rip.module.modules.render;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.event.events.ArrayListTicker;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Blur;
import template.rip.api.object.Description;
import template.rip.api.util.ColorUtil;
import template.rip.api.util.EasingUtil;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArrayListModule extends Module implements Renderable {

    public static HashMap<Module, Long> moduleToggleOn = new HashMap<>();
    public static HashMap<Module, Long> moduleToggleOff = new HashMap<>();
    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");
    public final ModeSetting<positionEnum> position = new ModeSetting<>(this, positionEnum.Left, "Position");
    public final BooleanSetting lowerCase = new BooleanSetting(this, false, "Lowercase");
    public final BooleanSetting bold = new BooleanSetting(this, false, "Bold");
    public final BooleanSetting blur = new BooleanSetting(this, false, "Blur");
    public final NumberSetting blurRadius = new NumberSetting(this, 10, 0, 30, 1, "Blur Radius");
    public final NumberSetting rounding = new NumberSetting(this, 0, 0, 20, 1, "Rounding");
    public final NumberSetting height = new NumberSetting(this, 24, 20, 40, 1, "Height");
    public final NumberSetting toggleSpeed = new NumberSetting(this, 0.25, 0, 1, 0.01, "Speed");
    public final BooleanSetting pushOnAnimation = new BooleanSetting(this, true, "Push On Animation");
    public final BooleanSetting textShadow = new BooleanSetting(this, true, "Text Shadow");
    public final BooleanSetting excludeCertainModules = new BooleanSetting(this, false, "Exclude Some Modules");
    public final BooleanSetting suffix = new BooleanSetting(this, true, "Suffix");
    public final BooleanSetting backgroundMask = new BooleanSetting(this, true, "Background Mask");
    public final BooleanSetting minecraftFont = new BooleanSetting(this, false, "Minecraft Font");
    public final DividerSetting colorsDivider = new DividerSetting(this, false, "Colors");
    public final ColorSetting bgColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.5f), true, "Background Color"  /*new JColor(0.13f, 0.14f, 0.18f, 0.7f)*/);
    public final ColorSetting suffixColor = new ColorSetting(this, new JColor(1f, 1f, 1f, 1f), false, "Suffix Color");
    public final DividerSetting lineDivider = new DividerSetting(this, false, "Line");
    public final BooleanSetting lineEnabled = new BooleanSetting(this, false, "Line Enabled");
    public final NumberSetting lineRounding = new NumberSetting(this, 0, 0, 10, 1, "Line Rounding");
    public final NumberSetting lineVerticalOffset = new NumberSetting(this, 0, 0, 8, 1, "Line Vertical Offset");
    public final NumberSetting lineWidth = new NumberSetting(this, 4, 1, 10, 1, "Line Width");
    public final ModeSetting<linePositionEnum> linePosition = new ModeSetting<>(this, linePositionEnum.Left, "Line Position");

    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave");
    public final BooleanSetting waveEnabled = new BooleanSetting(this, true, "Wave Enabled");
    public final NumberSetting waveIndex = new NumberSetting(this, 50, 1, 100, 1, "Wave Index");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 1, 10, 1, "Wave Speed");
    public final BooleanSetting rainbowEnabled = new BooleanSetting(this, false, "Rainbow Enabled");
    public final NumberSetting rainbowSaturation = new NumberSetting(this, 0.8, 0, 1, 0.01, "Rainbow Saturation");
    public final NumberSetting rainbowBrightness = new NumberSetting(this, 1, 0, 1, 0.01, "Rainbow Brightness");

    public final DividerSetting glowDivider = new DividerSetting(this, false, "Glow");
    public final BooleanSetting glowEnabled = new BooleanSetting(this, true, "Glow Enabled");
    public final BooleanSetting glowInheritColor = new BooleanSetting(this, true, "Glow Inherit Color");
    public final ColorSetting glowColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Glow Color");
    public final ModeSetting<easeFunctions> easeFunction = new ModeSetting<>(this, easeFunctions.Cubic, "Ease Smoothing");
    public final NumberSetting glowThickness = new NumberSetting(this, 1.5, 0, 10, 0.1, "Glow Thickness");
    public final NumberSetting glowMultiplier = new NumberSetting(this, 3, 0, 10, 0.1, "Glow Multiplier");
    public final NumberSetting glowSize = new NumberSetting(this, 7, 0, 10, 1, "Glow Size");
    public final NumberSetting glowShift = new NumberSetting(this, 2, 0, 10, 0.1, "Glow Shift");
    public final NumberSetting glowRounding = new NumberSetting(this, 6, 0, 20, 1, "Glow Rounding");

    public final DividerSetting outlineDivider = new DividerSetting(this, false, "Outline");
    public final BooleanSetting outlineEnabled = new BooleanSetting(this, true, "Outline Enabled");
    public final BooleanSetting outlineInheritColor = new BooleanSetting(this, true, "Outline Inherit Color");
    public final ColorSetting outlineColor = new ColorSetting(this, new JColor(0.79f, 0.24f, 0.32f, 1f), true, "Outline Color");
    public final NumberSetting outlineSize = new NumberSetting(this, 2, 0, 10, 1, "Outline Size");
    private ArrayList<Module> cachedCopy = new ArrayList<>();
    private final HashMap<String, Float> cachedText = new HashMap<>();
    private boolean firstFrame;

    public ArrayListModule(Category category, Description description, String name) {
        super(category, description, name);
        firstFrame = true;

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

    @EventHandler
    private void onArrayTick(ArrayListTicker event) {
        ArrayList<Module> temp = get();
        cachedCopy = new ArrayList<>(temp);
        temp.forEach(module -> {
            String[] text;
            if (suffix.isEnabled()) {
                String[] fullName = module.getFullName();
                text = new String[fullName.length + 1];
                System.arraycopy(fullName, 0, text, 0, fullName.length);
                text[fullName.length] = module.getSuffix();
            } else {
                text = module.getFullName();
            }
            if (lowerCase.isEnabled()) toLowercase(text);
            module.displayName = text;
        });
    }

    private ArrayList<Module> get() {
        Comparator<Module> nameLengthComparator;

        ImFont font = UI.getFont(24, bold.isEnabled(), minecraftFont.isEnabled());
        boolean isLowerCase = lowerCase.isEnabled();
        if (suffix.isEnabled()) {
            nameLengthComparator = (o1, o2) -> Float.compare(CalcTextSize(o2.getNameSuffix(), font, isLowerCase), CalcTextSize(o1.getNameSuffix(), font, isLowerCase));
        } else {
            nameLengthComparator = (o1, o2) -> Float.compare(CalcTextSize(o2.getFullName(), font, isLowerCase), CalcTextSize(o1.getFullName(), font, isLowerCase));
        }

        ArrayList<Module> main = new ArrayList<>(Template.moduleManager.getModules());

        main.removeIf(m -> m.getCategory().equals(Category.LEGIT) || ((excludeCertainModules.isEnabled()) && (m.getCategory().equals(Category.RENDER) || m.getCategory().equals(Category.CLIENT)))
                        || (!m.isEnabled() && (!moduleToggleOff.containsKey(m) || System.currentTimeMillis() - moduleToggleOff.get(m) > toggleSpeed.getFValue() * 1000f)));
        main.sort(nameLengthComparator);
        return main;
    }

    public void clearTextCache() {
        cachedText.clear();
    }

    float CalcTextSize(String[] text, ImFont font) {
        return CalcTextSize(text, font, false);
    }

    float CalcTextSize(String[] text, ImFont font, boolean lowerCase) {
        float size = 0;
        float cursedGap = isMontserrat(font) ? 2 : 0;

        if (lowerCase) {
            for (String str : text) size += CalcTextSize(str.toLowerCase(), font) - cursedGap;
        } else {
            for (String str : text) size += CalcTextSize(str, font) - cursedGap;
        }

        return size;
    }

    private static boolean isMontserrat(ImFont font) {
        return font.equals(ImguiLoader.mediumMontserrat24) || font.equals(ImguiLoader.semiBoldMontserrat24);
    }

    float CalcTextSize(String text, ImFont font) {
        if (cachedText.containsKey(text)) {
            return cachedText.get(text);
        } else {
            float size = font.calcTextSizeAX(24, Integer.MAX_VALUE, Integer.MAX_VALUE, text);
            cachedText.put(text, size);
            return size;
        }
    }

    private void toLowercase(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i].toLowerCase();
        }
    }

    @Override
    public void render() {
        if (!Template.displayRender()) return;
        if (!this.isEnabled()) {
            firstFrame = true;
            return;
        }

        float maxWindowWidth;
        float windowHeight = mc.getWindow().getHeight();

        CopyOnWriteArrayList<Module> copy = new CopyOnWriteArrayList<>(cachedCopy);
        maxWindowWidth = 300;

        ImGui.getStyle().setWindowMinSize(maxWindowWidth + 10, windowHeight + 10);

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

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;

        if (Template.moduleManager.isModuleDisabled(AchillesSettingsModule.class))
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;

        ImGui.begin(this.getName(), imGuiWindowFlags);

        boolean isMinecraftFontEnabled = minecraftFont.isEnabled();

        ImFont font = UI.getFont(24, bold.isEnabled(), isMinecraftFontEnabled);
        ImGui.pushFont(font);
        ImGui.beginChild("ArrayList/Modules");

        float width = -1;
        ImVec2 preCursor = ImGui.getCursorPos();
        // pre draw

        ImGui.setCursorPos(preCursor.x, preCursor.y);

        boolean isWaveEnabled = waveEnabled.isEnabled();
        boolean isPositionRight = position.is(positionEnum.Right);
        boolean isPositionLeft = position.is(positionEnum.Left);

        boolean isLineEnabled = lineEnabled.isEnabled();
        boolean isLinePosLeft = linePosition.is(linePositionEnum.Left);

        float lineWidthValue = this.lineWidth.getFValue();
        float lineWidth = isLineEnabled && isLinePosLeft ? lineWidthValue : 0;
        float toggleSpeedValue = toggleSpeed.getFValue();
        float heightValue = height.getFValue();

        if (isPositionRight) width = maxWindowWidth - 30 - lineWidth;

        float windowPosX = ImGui.getWindowPosX();
        float windowPosY = ImGui.getWindowPosY();
        int mcWindowHeight = mc.getWindow().getFramebufferHeight();

        for (int i = 0; i < copy.size(); i++) {
            float predictedYPos = windowPosY + ImGui.getCursorPosY();
            if (predictedYPos - heightValue > mcWindowHeight || predictedYPos + heightValue < 0) {
                ImGui.setCursorPosY(ImGui.getCursorPosY() + heightValue);
                continue;
            }

            Module module = copy.get(i);
            Module nextModule = null;
            if (i + 1 < copy.size()) nextModule = copy.get(i + 1);

            String[] text = module.displayName;
            float textWidth = CalcTextSize(text, font);

            if (isPositionRight) {
                ImGui.setCursorPosX(width - textWidth + 10 + 5);
            } else {
                ImGui.setCursorPosX(0);
            }

            float percent;
            if (module.isEnabled()) {
                percent = 1f;
                Long moduleToggleOnTime = moduleToggleOn.get(module);

                if (moduleToggleOnTime != null) {
                    percent = Math.min((System.currentTimeMillis() - moduleToggleOnTime) / (toggleSpeedValue * 1000f), 1f);
                }
                percent = 1f - EasingUtil.easeOutCubic(percent);
            } else {
                percent = 1f;
                Long moduleToggleOffTime = moduleToggleOff.get(module);

                if (moduleToggleOffTime != null) {
                    percent = Math.min((System.currentTimeMillis() - moduleToggleOffTime) / (toggleSpeedValue * 1000f), 1f);
                }
                percent = EasingUtil.easeInCubic(percent);
            }

            float lineWidth1 = isLineEnabled && isLinePosLeft ? lineWidthValue : 0;
            float lineWidth2 = isLineEnabled && isPositionRight ? lineWidthValue : 0;

            float cursorX;

            if (isPositionRight) {
                cursorX = ImGui.getCursorPosX() + ((textWidth + 10 + lineWidth1) * percent);
            } else {
                cursorX = ImGui.getCursorPosX() - ((textWidth + 10 + lineWidth1) * percent);
            }

            float cursorY = ImGui.getCursorPosY();

            Color initColor = UI.getColorOne();

            if (isWaveEnabled) {
                if (rainbowEnabled.isEnabled()) {
                    initColor = ColorUtil.rainbow(
                            (int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(),
                            i * waveIndex.getIValue(),
                            rainbowSaturation.getFValue(),
                            rainbowBrightness.getFValue(),
                            1f
                    );
                } else {
                    initColor = ColorUtil.interpolateColorsBackAndForth(
                            (int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(),
                            i * waveIndex.getIValue(),
                            UI.getColorOne(),
                            UI.getColorTwo(),
                            false
                    );
                }
            }

            JColor initJColor = new JColor(initColor);
            float[] color = initJColor.getFloatColor();
            float[] color2 = initJColor.jDarker().jDarker().getFloatColor();
            if (isMinecraftFontEnabled) {
                color2 = new JColor(color[0] / 4, color[1] / 4, color[2] / 4, 1f).getFloatColor();
            }
            JColor suffixJColor = new JColor(suffixColor.getColor());
            float[] suffixColor1 = suffixJColor.getFloatColor();
            float[] suffixColor2 = suffixJColor.jDarker().jDarker().getFloatColor();
            if (isMinecraftFontEnabled) {
                suffixColor2 = new JColor(color[0] / 4, color[1] / 4, color[2] / 4, 1f).getFloatColor();
            }
            float[] bg = bgColor.getColor().getFloatColorWAlpha();

            int imDrawFlags = 0;

            boolean isLastModule = nextModule == null;

            if (isLastModule) {
                imDrawFlags |= isPositionLeft ? ImDrawFlags.RoundCornersBottomLeft : ImDrawFlags.RoundCornersBottomRight;
            } else {
                imDrawFlags |= isPositionLeft ? ImDrawFlags.RoundCornersBottomRight : ImDrawFlags.RoundCornersBottomLeft;
            }

            boolean isModuleFirst = i == 0;
            if (isModuleFirst) imDrawFlags |= ImDrawFlags.RoundCornersTop;

            float rounding = this.rounding.getFValue();
            float nextSize;

            String[] nextText = {};
            if (!isLastModule) {
                nextText = nextModule.displayName;
            }
            nextSize = CalcTextSize(nextText, font);
            if (!isLastModule && textWidth - nextSize < rounding) {
                rounding = textWidth - nextSize;
            }

            ImDrawList backgroundDrawList = ImGui.getBackgroundDrawList();
            if (outlineEnabled.isEnabled()) {
                float size = outlineSize.getFValue();
                float[] outlineColorF = outlineColor.getColor().getFloatColorWAlpha();
                float alpha = outlineColorF[3];
                float offsetY = 0;
                float offsetX = 1;

                if (isModuleFirst) offsetY = 1;
                if (isPositionRight) offsetX = 0;

                if (outlineInheritColor.isEnabled()) outlineColorF = initJColor.getFloatColor();
                float pMaxX = windowPosX + cursorX + textWidth + 10 + lineWidth1 + lineWidth2 + size;
                if (backgroundMask.isEnabled()) {
                    float bgSize = 100;
                    float nextWidth = nextSize + 10 + lineWidth1 + lineWidth2;
                    float currentWidth = textWidth + 10 + lineWidth1 + lineWidth2;

                    if (!isLastModule) {
                        if (isPositionRight)
                            backgroundDrawList.pushClipRect(
                                    windowPosX + cursorX - size,
                                    windowPosY + cursorY + heightValue,
                                    windowPosX + cursorX + currentWidth - nextWidth,
                                    windowPosY + cursorY + heightValue + heightValue,
                                    false
                            );
                        else
                            backgroundDrawList.pushClipRect(
                                    windowPosX + cursorX + nextWidth,
                                    windowPosY + cursorY + heightValue,
                                    windowPosX + cursorX + currentWidth + size,
                                    windowPosY + cursorY + heightValue + heightValue,
                                    false
                            );
                        backgroundDrawList.addRectFilled(
                                windowPosX + cursorX - size + offsetX,
                                windowPosY + cursorY - size + offsetY,
                                pMaxX,
                                windowPosY + cursorY + heightValue + size,
                                ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                                rounding
                        );
                        backgroundDrawList.popClipRect();
                    }

                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX - bgSize,
                            windowPosY + cursorY,
                            windowPosX + cursorX+1,
                            windowPosY + cursorY + heightValue,
                            false
                    );
                    backgroundDrawList.addRectFilled(
                            windowPosX + cursorX - size + offsetX,
                            windowPosY + cursorY - size + offsetY,
                            pMaxX,
                            windowPosY + cursorY + heightValue + size,
                            ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                            rounding
                    );
                    backgroundDrawList.popClipRect();

                    float clipRectMaxX = windowPosX + cursorX + textWidth + 10 + lineWidth1 + lineWidth2 + bgSize;
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX + textWidth + 10 + lineWidth1 + lineWidth2,
                            windowPosY + cursorY,
                            clipRectMaxX,
                            windowPosY + cursorY + heightValue,
                            false
                    );
                    backgroundDrawList.addRectFilled(
                            windowPosX + cursorX - size + offsetX,
                            windowPosY + cursorY - size + offsetY,
                            pMaxX,
                            windowPosY + cursorY + heightValue + size,
                            ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                            rounding
                    );
                    backgroundDrawList.popClipRect();
                    if (isModuleFirst) {
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - size,
                                windowPosY + cursorY - 30,
                                pMaxX,
                                windowPosY + cursorY + 1,
                                false
                        );
                        backgroundDrawList.addRectFilled(
                                windowPosX + cursorX - size + offsetX,
                                windowPosY + cursorY - size + offsetY,
                                pMaxX,
                                windowPosY + cursorY + heightValue + size,
                                ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                                rounding
                        );
                        backgroundDrawList.popClipRect();
                    }
                    if (isLastModule) {
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - bgSize,
                                windowPosY + cursorY + heightValue,
                                clipRectMaxX,
                                windowPosY + cursorY + heightValue + 30,
                                false
                        );
                        backgroundDrawList.addRectFilled(
                                windowPosX + cursorX - size + offsetX,
                                windowPosY + cursorY - size + offsetY,
                                pMaxX,
                                windowPosY + cursorY + heightValue + size,
                                ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                                rounding
                        );
                        backgroundDrawList.popClipRect();
                    }
                } else {
                    backgroundDrawList.addRectFilled(
                            windowPosX + cursorX - size + offsetX,
                            windowPosY + cursorY - size + offsetY,
                            pMaxX,
                            windowPosY + cursorY + heightValue + size,
                            ImGui.getColorU32(outlineColorF[0], outlineColorF[1], outlineColorF[2], alpha),
                            rounding
                    );
                }
            }

            float currentWidth = textWidth + 10 + lineWidth1 + lineWidth2;
            if (glowEnabled.isEnabled()) {
                easeFunctions easeMode = easeFunction.getMode();
                int glowSizeValue = glowSize.getIValue();
                float shift = glowShift.getFValue();
                float glowMultiplierF = glowMultiplier.getFValue();
                float glowThicknessF = glowThickness.getFValue();
                float roundingGlow = glowRounding.getFValue();

                float[] glowColorF;
                if (glowInheritColor.isEnabled()) {
                    glowColorF = initJColor.getFloatColor();
                } else {
                    glowColorF = glowColor.getColor().getFloatColorWAlpha();
                }

                float glowRadius = 100f;
                float nextWidth = nextSize + 10 + lineWidth1 + lineWidth2;

                int imGuiFlagsGlow = 0;
                if (isLastModule && i - 1 >= 0) {
                    imGuiFlagsGlow |= ImDrawFlags.RoundCornersBottom;

                    float prevWidth = CalcTextSize(copy.get(i - 1).displayName, font) + 10 + lineWidth1 + lineWidth2;

                    if (isPositionRight) {
                        // Top Left
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - glowRadius,
                                windowPosY + cursorY - heightValue,
                                windowPosX + cursorX - Math.abs(currentWidth - prevWidth),
                                windowPosY + cursorY,
                                false
                        );
                    } else {
                        // Top Right
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX + prevWidth,
                                windowPosY + cursorY - heightValue,
                                windowPosX + cursorX + prevWidth + glowRadius,
                                windowPosY + cursorY,
                                false
                        );
                    }

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Bottom
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX,
                            windowPosY + cursorY + heightValue,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + glowRadius,
                            false
                    );

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Right side
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth + glowRadius,
                            windowPosY + cursorY + glowRadius,
                            false
                    );

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Left side
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX - glowRadius,
                            windowPosY + cursorY,
                            windowPosX + cursorX,
                            windowPosY + cursorY + glowRadius,
                            false
                    );

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();
                } else if (isModuleFirst) {
                    imGuiFlagsGlow |= ImDrawFlags.RoundCornersTop;
                    imGuiFlagsGlow |= isPositionRight ? ImDrawFlags.RoundCornersBottomLeft : ImDrawFlags.RoundCornersBottomRight;

                    // Top
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX,
                            windowPosY + cursorY - glowRadius,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY,
                            false
                    );

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Right side
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY - glowRadius,
                            windowPosX + cursorX + currentWidth + glowRadius,
                            windowPosY + cursorY + heightValue,
                            false
                    );

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Left side
                    backgroundDrawList.pushClipRect(
                            windowPosX + cursorX - glowRadius,
                            windowPosY + cursorY - glowRadius,
                            windowPosX + cursorX,
                            windowPosY + cursorY + heightValue,
                            false
                    );

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    if (isPositionRight) {
                        // Bottom Left
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - glowRadius,
                                windowPosY + cursorY + heightValue,
                                windowPosX + cursorX + currentWidth - nextWidth,
                                windowPosY + cursorY + heightValue + heightValue,
                                false
                        );
                    } else {
                        // Bottom Right
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX + nextWidth,
                                windowPosY + cursorY + heightValue,
                                windowPosX + windowPosX + cursorX + nextWidth + glowRadius,
                                windowPosY + cursorY + heightValue + heightValue,
                                false
                        );
                    }

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();
                } else {
                    imGuiFlagsGlow |= isPositionRight ? ImDrawFlags.RoundCornersBottomLeft : ImDrawFlags.RoundCornersBottomRight;

                    float prevWidth = CalcTextSize(copy.get(i - 1).displayName, font) + 10 + lineWidth1 + lineWidth2;

                    if (isPositionRight) {
                        // Top Left
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - glowRadius,
                                windowPosY + cursorY - heightValue,
                                windowPosX + cursorX - Math.abs(currentWidth - prevWidth),
                                windowPosY + cursorY,
                                false
                        );
                    } else {
                        // Top Right
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX + prevWidth,
                                windowPosY + cursorY - heightValue,
                                windowPosX + cursorX + prevWidth + glowRadius,
                                windowPosY + cursorY,
                                false
                        );
                    }

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    if (isPositionRight) {
                        // Bottom Left
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - glowRadius,
                                windowPosY + cursorY + heightValue + 1,
                                windowPosX + cursorX + currentWidth - nextWidth,
                                windowPosY + cursorY + heightValue + heightValue,
                                false
                        );
                    } else {
                        // Bottom Right
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX + nextWidth,
                                windowPosY + cursorY + heightValue + 1,
                                windowPosX + cursorX + nextWidth + glowRadius,
                                windowPosY + cursorY + heightValue + heightValue,
                                false
                        );
                    }

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Left Side
                    if (isPositionRight) {
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - glowRadius,
                                windowPosY + cursorY,
                                windowPosX + cursorX,
                                windowPosY + cursorY + heightValue + 1,
                                false
                        );
                    } else {
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX - glowRadius,
                                windowPosY + cursorY,
                                windowPosX + cursorX,
                                windowPosY + cursorY + heightValue,
                                false
                        );
                    }

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();

                    // Right Side
                    if (isPositionRight) {
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX + currentWidth,
                                windowPosY + cursorY,
                                windowPosX + cursorX + currentWidth + glowRadius,
                                windowPosY + cursorY + heightValue,
                                false
                        );
                    } else {
                        backgroundDrawList.pushClipRect(
                                windowPosX + cursorX + currentWidth,
                                windowPosY + cursorY,
                                windowPosX + cursorX + currentWidth + glowRadius,
                                windowPosY + cursorY + heightValue + 1,
                                false
                        );
                    }

                    drawGlow(backgroundDrawList,
                            windowPosX + cursorX,
                            windowPosY + cursorY,
                            windowPosX + cursorX + currentWidth,
                            windowPosY + cursorY + heightValue,
                            imGuiFlagsGlow,
                            easeMode,
                            glowSizeValue,
                            shift,
                            glowMultiplierF,
                            glowThicknessF,
                            roundingGlow,
                            glowColorF
                    );

                    backgroundDrawList.popClipRect();
                }
            }

            ImGui.getWindowDrawList().addRectFilled(
                    windowPosX + cursorX,
                    windowPosY + cursorY,
                    windowPosX + cursorX + currentWidth,
                    windowPosY + cursorY + heightValue,
                    ImGui.getColorU32(bg[0], bg[1], bg[2], bg[3]), rounding,
                    imDrawFlags
            );

            if (blur.isEnabled()) {
                new Blur((int) (windowPosX + cursorX),
                        (int) (windowPosY + cursorY),
                        (int) (textWidth + 10 + lineWidth1 + lineWidth2),
                        (int) heightValue, blurRadius.getFValue());
            }

            if (isLineEnabled) {
                float v = lineVerticalOffset.getFValue();
                float r = lineRounding.getFValue();
                if (isLinePosLeft)

                    ImGui.getWindowDrawList().addRectFilled(
                            windowPosX + cursorX,
                            windowPosY + cursorY + v,
                            windowPosX + cursorX + lineWidth1,
                            windowPosY + cursorY - v + heightValue,
                            ImGui.getColorU32(color[0], color[1], color[2], 1f), r,
                            ImDrawFlags.RoundCornersRight
                    );
                else
                    ImGui.getWindowDrawList().addRectFilled(
                            windowPosX + cursorX + textWidth + 10,
                            windowPosY + cursorY + v,
                            windowPosX + cursorX + textWidth + 10 + lineWidthValue,
                            windowPosY + cursorY - v + heightValue,
                            ImGui.getColorU32(color[0], color[1], color[2], 1f), r,
                            ImDrawFlags.RoundCornersLeft
                    );
            }
            float offset = 1;
            if (isMinecraftFontEnabled) offset = 2;
            boolean isTextShadowEnabled = textShadow.isEnabled();
            if (suffix.isEnabled()) {
                String suffix = module.getSuffix();
                String[] noSuffix = module.getFullName();

                if (lowerCase.isEnabled()) {
                    suffix = suffix.toLowerCase();
                    toLowercase(noSuffix);
                }

                float noSuffixWidth = CalcTextSize(noSuffix, font);

                float suffixGap = isMontserrat(font) ? 1.7f : 0;

                if (isTextShadowEnabled) {
                    ImGui.setCursorPos(cursorX + offset + 5 + lineWidth1, cursorY + heightValue / 2 - 12 + offset);
                    ImGui.pushStyleColor(ImGuiCol.Text, color2[0], color2[1], color2[2], 1f);
                    for (String str : noSuffix) {
                        ImGui.text(str);
                        ImGui.sameLine(0, 0);
                    }
                    ImGui.popStyleColor();

                    ImGui.setCursorPos(cursorX + offset + 5 + lineWidth1 + noSuffixWidth + suffixGap, cursorY + heightValue / 2 - 12 + offset);
                    ImGui.pushStyleColor(ImGuiCol.Text, suffixColor2[0], suffixColor2[1], suffixColor2[2], 1f);
                    ImGui.text(suffix);
                    ImGui.popStyleColor();
                }

                ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                ImGui.setCursorPos(cursorX + 5 + lineWidth1, cursorY + heightValue / 2 - 12);
                for (String str : noSuffix) {
                    ImGui.text(str);
                    ImGui.sameLine(0, 0);
                }

                ImGui.setCursorPos(cursorX + 5 + lineWidth1 + noSuffixWidth + suffixGap, cursorY + heightValue / 2 - 12);
                ImGui.pushStyleColor(ImGuiCol.Text, suffixColor1[0], suffixColor1[1], suffixColor1[2], 1f);
                ImGui.text(suffix);
                ImGui.popStyleColor();

            } else {
                if (isTextShadowEnabled) {
                    ImGui.setCursorPos(cursorX + offset + 5 + lineWidth1, cursorY + heightValue / 2 - 12 + offset);
                    ImGui.pushStyleColor(ImGuiCol.Text, color2[0], color2[1], color2[2], 1f);
                    for (String str : text) {
                        ImGui.text(str);
                        ImGui.sameLine(0, 0);
                    }
                    ImGui.popStyleColor();
                }

                ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                ImGui.setCursorPos(cursorX + 5 + lineWidth1, cursorY + heightValue / 2 - 12);
                for (String str : text) {
                    ImGui.text(str);
                    ImGui.sameLine(0, 0);
                }
            }
            if (!pushOnAnimation.isEnabled()) percent = 0f;
            ImGui.setCursorPos(cursorX + 5 + lineWidth1, cursorY + heightValue * (1f - percent));
            ImGui.popStyleColor();

        }

        ImGui.endChild();
        super.position = ImGui.getWindowPos();
        isFocused = ImGui.isWindowFocused();

        ImGui.popFont();
        ImGui.end();
        if (firstFrame) firstFrame = false;
    }

    private void drawGlow(ImDrawList backgroundDrawList, float minX, float minY, float maxX, float maxY, int imGuiFlags, easeFunctions easeMode, int glowSizeI, float shift, float glowMultiplierF, float glowThicknessF, float rounding, float[] glowColorF) {
        for (int i = 1; i <= glowSizeI; i++) {
            float percent = 1f - ((float) i / glowSizeI);
            switch (easeMode) {
                case Cubic -> percent = EasingUtil.easeOutCubic(percent);
                case Quad -> percent = EasingUtil.easeOutQuad(percent);
                case Quart -> percent = EasingUtil.easeOutQuart(percent);
            }

            backgroundDrawList.addRect(
                    minX - shift, minY - shift,
                    maxX + shift, maxY + shift,
                    ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], 0.1f * percent),
                    rounding,
                    imGuiFlags,
                    percent * glowThicknessF + i * glowMultiplierF
            );
        }
    }

    @Override
    public String getName() {
        return "ArrayList";
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    private final Theme theme = new Theme() {

        @Override
        public void preRender() {
            float[][] colors = ImGui.getStyle().getColors();
            colors[ImGuiCol.Text] = new float[]{0.80f, 0.84f, 0.96f, 1.00f};
            colors[ImGuiCol.TextDisabled] = new float[]{0.58f, 0.60f, 0.70f, 1.00f};
            colors[ImGuiCol.WindowBg] = new float[]{0.09f, 0.09f, 0.15f, 1.00f};
            colors[ImGuiCol.ChildBg] = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.PopupBg] = new float[]{0.08f, 0.08f, 0.08f, 0.94f};
            colors[ImGuiCol.Border] = new float[]{0.43f, 0.43f, 0.50f, 0.50f};
            colors[ImGuiCol.BorderShadow] = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.FrameBg] = new float[]{0.90f, 0.27f, 0.33f, 0.59f};
            colors[ImGuiCol.FrameBgHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.85f};
            colors[ImGuiCol.FrameBgActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.TitleBg] = new float[]{0.04f, 0.04f, 0.04f, 1.00f};
            colors[ImGuiCol.TitleBgActive] = new float[]{0.46f, 0.15f, 0.18f, 1.00f};
            colors[ImGuiCol.TitleBgCollapsed] = new float[]{0.00f, 0.00f, 0.00f, 0.51f};
            colors[ImGuiCol.MenuBarBg] = new float[]{0.14f, 0.14f, 0.14f, 1.00f};
            colors[ImGuiCol.ScrollbarBg] = new float[]{0.02f, 0.02f, 0.02f, 0.53f};
            colors[ImGuiCol.ScrollbarGrab] = new float[]{0.31f, 0.31f, 0.31f, 1.00f};
            colors[ImGuiCol.ScrollbarGrabHovered] = new float[]{0.41f, 0.41f, 0.41f, 1.00f};
            colors[ImGuiCol.ScrollbarGrabActive] = new float[]{0.51f, 0.51f, 0.51f, 1.00f};
            colors[ImGuiCol.CheckMark] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.SliderGrab] = new float[]{0.77f, 0.23f, 0.27f, 1.00f};
            colors[ImGuiCol.SliderGrabActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.Button] = new float[]{0.90f, 0.27f, 0.33f, 0.45f};
            colors[ImGuiCol.ButtonHovered] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.ButtonActive] = new float[]{0.75f, 0.21f, 0.25f, 1.00f};
            colors[ImGuiCol.Header] = new float[]{0.90f, 0.27f, 0.33f, 0.32f};
            colors[ImGuiCol.HeaderHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.73f};
            colors[ImGuiCol.HeaderActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.Separator] = new float[]{0.42f, 0.44f, 0.52f, 1.00f};
            colors[ImGuiCol.SeparatorHovered] = new float[]{0.81f, 0.25f, 0.30f, 0.78f};
            colors[ImGuiCol.SeparatorActive] = new float[]{0.76f, 0.22f, 0.26f, 1.00f};
            colors[ImGuiCol.ResizeGrip] = new float[]{0.90f, 0.27f, 0.33f, 0.21f};
            colors[ImGuiCol.ResizeGripHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.79f};
            colors[ImGuiCol.ResizeGripActive] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
            colors[ImGuiCol.Tab] = new float[]{0.56f, 0.17f, 0.21f, 0.85f};
            colors[ImGuiCol.TabHovered] = new float[]{0.90f, 0.27f, 0.33f, 0.85f};
            colors[ImGuiCol.TabActive] = new float[]{0.70f, 0.22f, 0.26f, 1.00f};
            colors[ImGuiCol.TabUnfocused] = new float[]{0.15f, 0.07f, 0.07f, 0.97f};
            colors[ImGuiCol.TabUnfocusedActive] = new float[]{0.42f, 0.14f, 0.14f, 1.00f};
            colors[ImGuiCol.DockingPreview] = new float[]{0.90f, 0.27f, 0.33f, 0.70f};
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
            colors[ImGuiCol.TextSelectedBg] = new float[]{0.90f, 0.27f, 0.33f, 0.35f};
            colors[ImGuiCol.DragDropTarget] = new float[]{1.00f, 1.00f, 0.00f, 0.90f};
            colors[ImGuiCol.NavHighlight] = new float[]{0.90f, 0.27f, 0.33f, 1.00f};
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
            ImGui.getStyle().setWindowBorderSize(1f);
            ImGui.getStyle().setWindowPadding(0f, 0f);
            ImGui.getStyle().setFramePadding(0f, 0f);
            ImGui.getStyle().setCellPadding(0f, 0f);
            ImGui.getStyle().setItemSpacing(8, 4);
            ImGui.getStyle().setItemInnerSpacing(4, 4);
            ImGui.getStyle().setWindowBorderSize(1f);
            ImGui.getStyle().setFrameBorderSize(0f);

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
        }
    };

    public enum positionEnum {Left, Right}

    public enum linePositionEnum {Left, Right}

    public enum easeFunctions {Cubic, Quad, Quart}
}
