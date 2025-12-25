package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

public class ButtonSetting extends Setting {

    public final Runnable runnable;
    private final String fullName;

    public ButtonSetting(Runnable runnable, Module parent, String... name) {
        this(runnable, parent, Description.of(), name);
    }

    public ButtonSetting(Runnable runnable, Module parent, Description description, String... name) {
        super(description, name);
        StringBuilder sb = new StringBuilder();
        for (String n : name) {
            sb.append(n);
        }
        this.fullName = sb.toString();
        this.parent = parent;
        this.runnable = runnable;
        this.height = 30;

        parent.addSettings(this);
    }

    protected String getButtonName() {
        return fullName;
    }

    @Override
    public SettingType getType() {
        return SettingType.Button;
    }

    @Override
    public void render() {
        ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

        if (lastIsSearched) {
            ImGui.pushStyleColor(ImGuiCol.Text, AchillesMenu.searchRGB);
        }

        if (ImGui.button(getButtonName()))
            runnable.run();
        if (ImGui.isItemHovered() && !getDescription().isEmpty()) {
            ToolTipHolder.setToolTip(getDescription().getContent());
        }

        if (lastIsSearched) {
            ImGui.popStyleColor();
        }

        ImGui.popID();
    }

    public ButtonSetting setAdvanced() {
        advanced = true;
        return this;
    }
}
