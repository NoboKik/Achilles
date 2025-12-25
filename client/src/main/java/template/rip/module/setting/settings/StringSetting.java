package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.type.ImString;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

import static template.rip.Template.moduleManager;

public class StringSetting extends Setting {

	private ImString currentString = new ImString();
	private String content;
	public final String defaultContent;

	public StringSetting(String content, Module parent, Description description, String... name) {
		super(description, name);
	    this.parent = parent;
	    this.content = content;
		this.defaultContent = content;
		this.height = 26;
		if (parent != null) parent.addSettings(this);
	}

	@Override
	public SettingType getType() {
		return SettingType.String;
	}

	public StringSetting(String content, Module parent, String... name) {
		this(content, parent, Description.of(), name);
	}

	public String getContent() {
	    return this.content;
	}
	  
	public void setContent(String content) {
	    this.content = content;
	}

	@Override
	public void reset() {
		this.content = this.defaultContent;
	}

	public String getID() {
		return parent.getName()+"/"+getName();
	}

	@Override
	public void render() {
		ImGui.pushID(getID());
		ImGui.setCursorPosY(ImGui.getCursorPosY()+4);

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

		if (currentString.isEmpty())
			currentString.set(content);
		ImGui.pushItemWidth(170f);
		ImGui.inputText("", currentString);
		ImGui.popItemWidth();

		moduleManager.typing = ImGui.isItemFocused();
		if (ImGui.isItemFocused()) {
			if (ImGui.isKeyDown(ImGui.getIO().getKeyMap(ImGuiKey.Backspace)) && currentString.isNotEmpty()) {
				currentString.set(currentString.get().substring(0, currentString.get().length() - 1));
			}
		}
		content = currentString.get();

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

	public StringSetting setAdvanced() {
		advanced = true;
		return this;
	}
}
