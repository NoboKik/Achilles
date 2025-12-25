package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImDouble;
import imgui.type.ImInt;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.SettingType;

public class AnyNumberSetting extends NumberSetting {

	public AnyNumberSetting(Module parent, double value, boolean decimal, Description description, String... name) {
		super(parent, description, value, 0, 0, 0, name);
		this.decimal = decimal;
		this.height = 50;
	}

	public AnyNumberSetting(Module parent, double value, boolean decimal, String... name) {
		this(parent, value, decimal, Description.of(), name);
	}

	@Override
	public SettingType getType() {
		return SettingType.AnyNumber;
	}

	@Override
	public void render() {
		ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

		float x1 = ImGui.getCursorScreenPosX();
		float y1 = ImGui.getCursorScreenPosY() - 5;
		float y2 = ImGui.getCursorScreenPosY() + 25;

		if (lastIsSearched) {
			ImGui.pushStyleColor(ImGuiCol.Text, AchillesMenu.searchRGB);
		}

		RenderUtils.drawTexts(getFullName());

		if (lastIsSearched) {
			ImGui.popStyleColor();
		}

		boolean changed;

		if (decimal) {
			ImDouble val = new ImDouble(this.value);

			ImGui.pushItemWidth(170f);
			changed = ImGui.inputDouble("", val);
			ImGui.popItemWidth();

			if (changed)
				this.value = val.doubleValue();
		} else {
			ImInt val = new ImInt(this.value.intValue());

			ImGui.pushItemWidth(170f);
			changed = ImGui.inputInt("", val);
			ImGui.popItemWidth();

			if (changed)
				this.value = val.doubleValue();
		}

		float x2 = ImGui.getCursorScreenPosX();

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

	public AnyNumberSetting setAdvanced() {
		advanced = true;
		return this;
	}
}