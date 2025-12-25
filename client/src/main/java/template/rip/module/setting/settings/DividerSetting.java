package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static template.rip.Template.moduleManager;

public class DividerSetting extends Setting {

    private boolean open;
    public final List<Setting> settings = new ArrayList<>();
    public long lastOpen = 0L;
    public float settingsOpenProgress = 0f;

    public DividerSetting(Module parent, boolean open, Description description, String... name) {
        super(description, name);
        this.parent = parent;
        this.open = open;
        this.height = 26;
        if (parent != null) parent.addSettings(this);
    }

    public DividerSetting(Module parent, boolean open, String... name) {
        this(parent, open, Description.of(), name);
    }

    public void addSetting(Setting... settings1) {
        settings.addAll(Arrays.asList(settings1));
        settings.forEach(setting ->  this.parent.renderSettings.remove(setting));
    }

    @Override
    public SettingType getType() {
        return SettingType.Divider;
    }

    private float getTotalSettingsHeight() {
        float totalHeight = 0;

        for (Setting setting : settings) {
            totalHeight += setting.getHeight();
        }

        return totalHeight;
    }

    @Override
    public float getHeight() {
        return isOpen() ? this.height + getTotalSettingsHeight() : this.height;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void toggle() {
        this.open = !this.open;
        this.lastOpen = System.currentTimeMillis();

        if (this.open) {
            this.settingsOpenProgress = 0;
        }
    }

    @Override
    public void reset() {
        this.open = false;
    }

    @Override
    public void render() {
        if (Template.moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.Menu) || Template.moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.is(AchillesSettingsModule.styleEnum.Flat)) {
            float[] color = JColor.getGuiColor().getFloatColor();
            ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

            ImGui.setCursorPosY(ImGui.getCursorPosY() + 4);
            ImVec2 pos1 = ImGui.getCursorPos().clone();
            ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
            ImGui.setCursorPos(pos1.x, pos1.y);
            if (ImGui.button("", 190f, 18f)) {
                toggle();
            }
            if (ImGui.isItemHovered() && !getDescription().isEmpty()) {
                ToolTipHolder.setToolTip(getDescription().getContent());
            }
            boolean isHovered = ImGui.isItemHovered();
            ImGui.setCursorPos(pos1.x, pos1.y);
            if (isHovered) {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.7f);
                //ImGui.pushStyleColor(ImGuiCol.Text, 1f,1f,1f,0.7f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1.00f);
                //ImGui.pushStyleColor(ImGuiCol.Text, 1f,1f,1f,1f);
            }
            if (!moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI))
                ImGui.setCursorPos(pos1.x + 25, pos1.y);
            ImGui.pushFont(ImguiLoader.mediumPoppins18);

            if (lastIsSearched) {
                ImGui.pushStyleColor(ImGuiCol.Text, ColorSetting.getRainbow(0, 255).getRGB());
            }

            RenderUtils.drawTexts(getFullName());

            if (lastIsSearched) {
                ImGui.popStyleColor();
            }

            ImGui.popFont();
            ImGui.popStyleColor();
            //ImVec2 pos2 = ImGui.getCursorPos().clone();
            if (Boolean.TRUE) {
                if (isHovered) {
                    ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 0.7f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                }
            } else {
                if (isHovered) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.38f, 0.43f, 0.55f, 0.7f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.38f, 0.43f, 0.55f, 1f);
                }
            }
            ImGui.pushFont(ImguiLoader.fontAwesome16);
            //ImGui.setCursorPos(pos.x, pos.y);
            if (moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI)) ImGui.setCursorPosX(175);
            ImGui.setCursorPosY(pos1.y);
            ImGui.text(this.open ? "\uF077" : "\uF078");
            ImGui.popFont();
            ImGui.popStyleColor(4);
            ImGui.setCursorPosX(pos1.x);
            ImGui.setCursorPosY(pos1.y + 4 + 18);
            ImGui.popID();

            if (isOpen()) {
                for (Setting setting : settings) {
                    setting.render();
                }
            }
        } else {
            float[] color = JColor.getGuiColor().getFloatColor();
            ImGui.pushID(parent.getName() + "/" + this.getName());
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 4);
            ImVec2 pos1 = ImGui.getCursorPos().clone();
            ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
            ImGui.setCursorPos(pos1.x, pos1.y);
            if (ImGui.button("", 190f, 18f)) {
                toggle();
            }

            boolean isHovered = ImGui.isItemHovered();
            ImGui.setCursorPos(pos1.x, pos1.y);
            if (!parent.isAnimatingOpening()) {
                if (isHovered) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.7f);
                    //ImGui.pushStyleColor(ImGuiCol.Text, 1f,1f,1f,0.7f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1.00f);
                    //ImGui.pushStyleColor(ImGuiCol.Text, 1f,1f,1f,1f);
                }
            }
            if (!moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI))
                ImGui.setCursorPos(pos1.x + 25, pos1.y);
            ImGui.pushFont(ImguiLoader.mediumPoppins18);

            if (lastIsSearched) {
                ImGui.pushStyleColor(ImGuiCol.Text, ColorSetting.getRainbow(0, 255).getRGB());
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, 1f, 1f, 1f, 1f);
            }

            RenderUtils.drawTexts(getFullName());

            ImGui.popStyleColor();

            ImGui.popFont();
            if (!parent.isAnimatingOpening()) ImGui.popStyleColor();
            //ImVec2 pos2 = ImGui.getCursorPos().clone();

            if (isHovered) {
                ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 0.7f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
            }

            ImGui.pushFont(ImguiLoader.fontAwesome16);
            //ImGui.setCursorPos(pos.x, pos.y);
            if (moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI)) ImGui.setCursorPosX(175);
            ImGui.setCursorPosY(pos1.y);
            ImGui.text(this.open ? "\uF077" : "\uF078");
            ImGui.popFont();
            ImGui.popStyleColor(4);
            ImGui.setCursorPosX(pos1.x);
            ImGui.setCursorPosY(pos1.y + 4 + 18);

            boolean prevAnimation = parent.isAnimatingOpening();
            if (isOpen()) {
                float settingsHeight = getTotalSettingsHeight();

                if (settingsOpenProgress == settingsHeight) {
                    float percentageSettingsOpacity = (float) Math.sin((((double) Math.min((System.currentTimeMillis() - 250L) - lastOpen, 250L) / 250L) * Math.PI) / 2);

                    parent.setAnimatingOpening(percentageSettingsOpacity != 1);

                    ImVec4 colorText = ImGui.getStyle().getColor(ImGuiCol.Text);
                    ImGui.pushStyleColor(ImGuiCol.Text, colorText.x, colorText.y, colorText.z, colorText.w * percentageSettingsOpacity);
                    for (Setting setting : settings) {
                        setting.render();
                    }
                    ImGui.popStyleColor();
                } else {
                    float percentageSettings = (float) Math.sin((((double) Math.min(System.currentTimeMillis() - lastOpen, 250L) / 250L) * Math.PI) / 2);
                    float height = settingsHeight * percentageSettings;

                    ImGui.dummy(1f, height);
                    settingsOpenProgress = height;
                }
            } else if (settingsOpenProgress != 0) {
                float settingsHeight = getTotalSettingsHeight();
                float percentageSettingsOpacity = 1f - (float) Math.sin((((double) Math.min(System.currentTimeMillis() - lastOpen, 200L) / 200L) * Math.PI) / 2);

                if (percentageSettingsOpacity != 0) {
                    parent.setAnimatingOpening(percentageSettingsOpacity != 1);

                    boolean isAnimationDone = percentageSettingsOpacity == 1;
                    if (!isAnimationDone) {
                        ImVec4 colorText = ImGui.getStyle().getColor(ImGuiCol.Text);
                        ImGui.pushStyleColor(ImGuiCol.Text, colorText.x, colorText.y, colorText.z, colorText.w * percentageSettingsOpacity);
                    }
                    for (Setting setting : settings) {
                        setting.render();
                    }
                    if (!isAnimationDone) ImGui.popStyleColor();
                } else {
                    float percentageSettings = 1f - (float) Math.sin((((double) Math.min((System.currentTimeMillis() - 200L) - lastOpen, 250L) / 250L) * Math.PI) / 2);
                    parent.setAnimatingOpening(percentageSettings != 1);
                    float height = settingsHeight * percentageSettings;

                    ImGui.dummy(1f, height);

                    settingsOpenProgress = height;
                }
            }

            parent.setAnimatingOpening(prevAnimation);
            ImGui.popID();
        }
    }

    public DividerSetting setAdvanced() {
        advanced = true;
        return this;
    }
}
