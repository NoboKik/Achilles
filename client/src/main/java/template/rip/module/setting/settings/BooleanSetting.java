package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.clickgui.ConfigParent;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ConfigModule;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

import static template.rip.Template.moduleManager;

public class BooleanSetting extends Setting {

	private boolean enabled;
	public final boolean defaultEnabled;
	  
	public BooleanSetting(Module parent, Description description, boolean enabled, String... name) {
		super(description, name);
	    this.parent = parent;
	    this.enabled = enabled;
		this.defaultEnabled = enabled;
		this.height = 26;
		if (parent != null) parent.addSettings(this);
	}

	public BooleanSetting(Module parent, boolean enabled, String... name) {
		this(parent, Description.of(), enabled, name);
	}

	public boolean isEnabled() {
	    return this.enabled;
	}
	  
	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	}
	
	public void toggle() {
	    this.enabled = !this.enabled;
	}

	@Override
	public void reset() {
		this.enabled = this.defaultEnabled;
	}

	public String getID() {
		return parent.getName()+"/"+getName();
	}

	public boolean isParentAnimatingOpening() {
		return moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.is(AchillesSettingsModule.styleEnum.Glass) && Template.moduleManager.getModule(parent.getClass()).isAnimatingOpening();
	}

	@Override
	public SettingType getType() {
		return SettingType.Boolean;
	}

	@Override
	public void render() {
		float[] color = JColor.getGuiColor().getFloatColor();
		ImGui.pushID(getID());
		ImGui.setCursorPosY(ImGui.getCursorPosY()+4);

		float x1 = ImGui.getCursorScreenPosX();
		float y1 = ImGui.getCursorScreenPosY() - 5;
		float y2 = ImGui.getCursorScreenPosY() + 25;

		ImVec2 pos1 = ImGui.getCursorPos().clone();
		ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
		ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);
		ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
		ImGui.setCursorPos(pos1.x, pos1.y);
		if (this.parent != moduleManager.getModule(ConfigModule.class)) {
			if (ImGui.button("", 190f, 18f)) {
				toggle();
			}
		} else {
			if (ImGui.button("", 80f, 18f)) {
				toggle();
			}
		}
		boolean isHovered = ImGui.isItemHovered();
		ImGui.setCursorPos(pos1.x, pos1.y);
        if (!isParentAnimatingOpening()) {
			if (isHovered) {
				ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.7f);
			} else {
				ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1.00f);
			}
		}
		if (!moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI))
			ImGui.setCursorPos(pos1.x+25, pos1.y);
		if (ConfigParent.getInstance().isOn)
			ImGui.setCursorPos(pos1.x+25, pos1.y);

		if (lastIsSearched) {
			ImGui.pushStyleColor(ImGuiCol.Text, AchillesMenu.searchRGB);
		}

		RenderUtils.drawTexts(getFullName());

		if (lastIsSearched) {
			ImGui.popStyleColor();
		}

		if (!isParentAnimatingOpening()) ImGui.popStyleColor();
		if (isEnabled()) {
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
		ImGui.pushFont(ImguiLoader.fontAwesome20);
		if (moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI)) ImGui.setCursorPosX(170);
		if (this.parent == moduleManager.getModule(ConfigModule.class))ImGui.setCursorPosX(pos1.x);
		ImGui.setCursorPosY(pos1.y);
		ImGui.text(this.enabled ? "\uF205" : "\uF204");
		ImGui.sameLine(0, 0);

		float x2 = ImGui.getCursorScreenPosX();

		ImGui.popFont();
		ImGui.popStyleColor(4);
		ImGui.setCursorPosX(pos1.x);
		ImGui.setCursorPosY(pos1.y+4+18);

		ImVec2 last = ImGui.getCursorPos();

		if (MathUtils.withinBox(x1, y1, x2, y2, ImGui.getMousePosX(), ImGui.getMousePosY())) {
			if (!getDescription().isEmpty()) {
				ToolTipHolder.setToolTip(getDescription().getContent());
			}
			if (KeyUtils.isKeyPressed(getResetKey())) {
				reset();
			}
		}
		ImGui.setCursorPos(last.x, last.y);

		ImGui.popID();
	}

	public BooleanSetting setAdvanced() {
		advanced = true;
		return this;
	}
}
