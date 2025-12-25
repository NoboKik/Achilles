package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

public class ColorSetting extends Setting {

    private final BooleanSetting rainbowSetting;

    private JColor color;
    public final JColor defaultColor;
    private boolean showPicker;
    private boolean rainbow;
    public final boolean defaultRainbow, alpha;
    public long lastOpen = 0L;
    public float pickerOpenProgress = 0f;

    public ColorSetting(Module parent, JColor color, boolean alpha, boolean rainbow, Description description, String... name) {
        super(description, name);
        this.parent = parent;
        this.color = color;
        this.defaultColor = color;
        this.showPicker = false;
        this.rainbow = rainbow;
        this.defaultRainbow = rainbow;
        this.alpha = alpha;

        String parentName = parent.getName();
        Module parent1 = parent;

        rainbowSetting = new BooleanSetting(null, rainbow, "Rainbow") {

            @Override
            public String getID() {
                return String.format("%s/%s/Rainbow", parentName, getName());
            }

            @Override
            public boolean isParentAnimatingOpening() {
                return parent1.isAnimatingOpening();
            }
        };

        parent.addSettings(this);
    }

    public ColorSetting(Module parent, JColor jColor, boolean alpha, Description description, String... name) {
        this(parent, jColor, alpha, false, description, name);
    }

    public ColorSetting(Module parent, JColor jColor, boolean alpha, String... name) {
        this(parent, jColor, alpha, false, Description.of(), name);
    }

    @Override
    public SettingType getType() {
        return SettingType.Color;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public JColor getValue() {
        return color;
    }

    public JColor getColor() {
        if (rainbow) {
            JColor value = this.getValue();
            return getRainbow(0, value.getAlpha(), value.getSaturation(), value.getBrightness());
        }
        return color;
    }

    public static JColor getRainbow(int incr, int alpha, float saturation, float brightness) {
        return JColor.fromHSB(
                ((System.currentTimeMillis() + incr * 200) % (360 * 20)) / (360f * 20),
                saturation,
                brightness
        ).setAlpha(alpha);
    }

    public static JColor getRainbow(int incr, int alpha) {
        return getRainbow(incr, alpha, 0.5f, 1.0f);
    }

    public void setColor(JColor color, boolean rainbow) {
        this.color = color;
        this.rainbow = rainbow;

        rainbowSetting.setEnabled(rainbow);
    }

    @Override
    public void reset() {
        this.color = this.defaultColor;
        this.rainbow = this.defaultRainbow;

        rainbowSetting.setEnabled(rainbow);
    }

    @Override
    public float getHeight() {
        return showPicker ? 322 : 52;
    }

    @Override
    public void render() {
        ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

        float[] floatColor = getColor().getFloatColorWAlpha();

        float x1 = ImGui.getCursorScreenPosX();
        float y1 = ImGui.getCursorScreenPosY();

        if (lastIsSearched) {
            ImGui.pushStyleColor(ImGuiCol.Text, AchillesMenu.searchRGB);
        }

        RenderUtils.drawTexts(getFullName());

        if (lastIsSearched) {
            ImGui.popStyleColor();
        }

        ImVec2 last = ImGui.getCursorPos();
        ImGui.sameLine(0, 0);

        float x2 = ImGui.getCursorScreenPosX();
        ImGui.setCursorPos(last.x, last.y);

        if (ImGui.colorButton(this.getName(), floatColor, ImGuiColorEditFlags.NoTooltip)) {
            showPicker = !showPicker;
            lastOpen = System.currentTimeMillis();
        }

        // Height: 322 - 52 = 270
        float pickerHeight = 270f;

        boolean isNewGui = Template.moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.Menu) || Template.moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.is(AchillesSettingsModule.styleEnum.Flat);
        if (showPicker) {
            if (isNewGui) {
                rainbowSetting.render();

                int imGuiColorEditFlags = 0;
                imGuiColorEditFlags |= ImGuiColorEditFlags.NoLabel;
                imGuiColorEditFlags |= ImGuiColorEditFlags.NoSmallPreview;
                imGuiColorEditFlags |= ImGuiColorEditFlags.NoSidePreview;

                ImGui.pushItemWidth(180f);

                if (alpha) {
                    ImGui.colorPicker4("", floatColor, imGuiColorEditFlags);
                } else {
                    ImGui.colorPicker3("", floatColor, imGuiColorEditFlags);
                }

                ImGui.popItemWidth();

                setColor(new JColor(valid(floatColor[0]), valid(floatColor[1]), valid(floatColor[2]), valid(floatColor[3])), rainbowSetting.isEnabled());
            } else {
                if (pickerOpenProgress == pickerHeight) {
                    float percentageSettingsOpacity = (float) Math.sin((((double) Math.min((System.currentTimeMillis() - 250L) - lastOpen, 250L) / 250L) * Math.PI) / 2);

                    parent.setAnimatingOpening(percentageSettingsOpacity != 1);

                    boolean isAnimationDone = percentageSettingsOpacity == 1;

                    if (!isAnimationDone) {
                        ImVec4 colorText = ImGui.getStyle().getColor(ImGuiCol.Text);
                        ImGui.pushStyleColor(ImGuiCol.Text, colorText.x, colorText.y, colorText.z, colorText.w * percentageSettingsOpacity);
                    }
                    rainbowSetting.render();

                    int imGuiColorEditFlags = 0;
                    imGuiColorEditFlags |= ImGuiColorEditFlags.NoLabel;
                    imGuiColorEditFlags |= ImGuiColorEditFlags.NoSmallPreview;
                    imGuiColorEditFlags |= ImGuiColorEditFlags.NoSidePreview;

                    ImGui.pushItemWidth(180f);

                    if (alpha) {
                        ImGui.colorPicker4("", floatColor, imGuiColorEditFlags);
                    } else {
                        ImGui.colorPicker3("", floatColor, imGuiColorEditFlags);
                    }

                    ImGui.popItemWidth();
                    if (!isAnimationDone) ImGui.popStyleColor();

                    setColor(new JColor(valid(floatColor[0]), valid(floatColor[1]), valid(floatColor[2]), valid(floatColor[3])), rainbowSetting.isEnabled());
                } else {
                    float percentageSettings = (float) Math.sin((((double) Math.min(System.currentTimeMillis() - lastOpen, 250L) / 250L) * Math.PI) / 2);

                    float height = pickerHeight * percentageSettings;
                    ImGui.dummy(1f, height);

                    pickerOpenProgress = height;
                }
            }
        } else if (pickerOpenProgress != 0) {
            float percentageSettingsOpacity = 1f - (float) Math.sin((((double) Math.min(System.currentTimeMillis() - lastOpen, 200L) / 200L) * Math.PI) / 2);

            if (percentageSettingsOpacity != 0) {
                parent.setAnimatingOpening(percentageSettingsOpacity != 1);

                ImVec4 colorText = ImGui.getStyle().getColor(ImGuiCol.Text);

                ImGui.pushStyleColor(ImGuiCol.Text, colorText.x, colorText.y, colorText.z, colorText.w * percentageSettingsOpacity);
                rainbowSetting.render();

                int imGuiColorEditFlags = 0;
                imGuiColorEditFlags |= ImGuiColorEditFlags.NoLabel;
                imGuiColorEditFlags |= ImGuiColorEditFlags.NoSmallPreview;
                imGuiColorEditFlags |= ImGuiColorEditFlags.NoSidePreview;

                ImGui.pushItemWidth(180f);

                if (alpha) {
                    ImGui.colorPicker4("", floatColor, imGuiColorEditFlags);
                } else {
                    ImGui.colorPicker3("", floatColor, imGuiColorEditFlags);
                }

                ImGui.popItemWidth();

                setColor(new JColor(valid(floatColor[0]), valid(floatColor[1]), valid(floatColor[2]), valid(floatColor[3])), rainbowSetting.isEnabled());
                ImGui.popStyleColor();
            } else {
                float percentageSettings = 1f - (float) Math.sin((((double) Math.min(System.currentTimeMillis()-200L - lastOpen, 250L) / 250L) * Math.PI) / 2);
                parent.setAnimatingOpening(percentageSettings != 1);

                float height = pickerHeight * percentageSettings;

                ImGui.dummy(1f, height);

                pickerOpenProgress = height;
            }
        }

        float y2 = ImGui.getCursorScreenPosY();

        if (MathUtils.withinBox(x1, y1, x2, y2, ImGui.getMousePosX(), ImGui.getMousePosY())) {
            if (!getDescription().isEmpty()) {
                ToolTipHolder.setToolTip(getDescription().getContent());
            }
            if (KeyUtils.isKeyPressed(getResetKey())) {
                reset();
            }
        }

        ImGui.popID();
    }

    public ColorSetting setAdvanced() {
        advanced = true;
        return this;
    }

    private float valid(float f) {
        if (f>1f) return 1f;
        else if (f < 0f) return 0f;
        return f;
    }
}
