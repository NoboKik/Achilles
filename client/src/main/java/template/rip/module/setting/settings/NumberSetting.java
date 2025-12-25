package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImDouble;
import imgui.type.ImInt;
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

import static template.rip.Template.moduleManager;

public class NumberSetting extends Setting {

	public Double value;
	public final double defaultValue;
	public double minimum;
	public double maximum;
	public double increment;
	public boolean decimal;
	  
	public NumberSetting(Module parent, Description description, double value, double minimum, double maximum, double increment, String... name) {
		super(description, name);
	    this.parent = parent;
	    this.value = value;
		this.defaultValue = value;
	    this.minimum = minimum;
	    this.maximum = maximum;
	    this.increment = increment;
		this.decimal = !(Math.floor(increment) == increment);
		this.height = 46;

		if (parent != null) parent.addSettings(this);
	}
	public NumberSetting(Module parent, double value, double minimum, double maximum, double increment, String... name) {
		this(parent, Description.of(), value, minimum, maximum, increment, name);
	}

	@Override
	public SettingType getType() {
		return SettingType.Number;
	}

	public double getValue() {
	    return this.value;
	}

	public float getFValue() {
		return this.value.floatValue();
	}

	public int getIValue() {
		return this.value.intValue();
	}
	  
	public void setValue(double value) {
	    double precision = 1.0D / this.increment;
	    this.value = Math.round(Math.max(this.minimum, Math.min(this.maximum, value)) * precision) / precision;
	}

	@Override
	public void reset() {
		this.value = this.defaultValue;
	}

	public void increment(boolean positive) {
	    setValue(getValue() + (positive ? 1 : -1) * increment);
	}
	  
	public double getMinimum() {
	    return this.minimum;
	}

	public void setMinimum(double minimum) {
	    this.minimum = minimum;
	}
	  
	public double getMaximum() {
	    return this.maximum;
	}
	
	public void setMaximum(double maximum) {
	    this.maximum = maximum;
	}
	  
	public double getIncrement() {
	    return this.increment;
	}
	  
	public void setIncrement(double increment) {
	    this.increment = increment;
	}

	@Override
	public void render() {
		ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

		ImVec2 prevPos = ImGui.getCursorPos().clone();

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

		ImVec2 pos = ImGui.getCursorPos().clone();

		float[] color = JColor.getGuiColor().getFloatColor();

		ImGui.setCursorPosX(pos.x + 4);
		ImGui.setCursorPosY(pos.y + 7);

		ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.38f, 0.43f, 0.55f, 0.6f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.38f, 0.43f, 0.55f, 0.6f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0.38f, 0.43f, 0.55f, 0.6f);

		ImGui.getStyle().setDisabledAlpha(1f);
		ImGui.pushStyleVar(ImGuiStyleVar.DisabledAlpha, 1f);
		ImGui.beginDisabled();

		ImGui.getWindowDrawList().addRectFilled(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), ImGui.getCursorScreenPosX() + 170f, ImGui.getCursorScreenPosY() + 7f, ImGui.getColorU32(color[0], color[1], color[2], 0.3f), 3f);

		ImGui.popStyleColor(3);

		ImGui.setCursorPosX(pos.x + 4);
		ImGui.setCursorPosY(pos.y + 7);

		double percentage = 1f - (getMaximum() - value) / (getMaximum() - getMinimum());
		ImGui.pushStyleColor(ImGuiCol.FrameBg, color[0], color[1], color[2], 1f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, color[0], color[1], color[2], 1f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive, color[0], color[1], color[2], 1f);

		if (percentage != 0f) {
			ImGui.getWindowDrawList().addRectFilled(
					ImGui.getCursorScreenPosX(),
					ImGui.getCursorScreenPosY(),
					ImGui.getCursorScreenPosX()+(float) (170f*percentage),
					ImGui.getCursorScreenPosY()+7f,
					ImGui.getColorU32(color[0], color[1], color[2], 1f),
					3f
			);
		}

		ImGui.popStyleColor(3);

		ImGui.endDisabled();

		ImGui.setCursorPos(pos.x, pos.y);

		boolean changed;

		if (decimal) {
			ImDouble val = new ImDouble(this.value);

			ImGui.getStyle().setGrabRounding(12);
			ImGui.getStyle().setGrabMinSize(15);
			ImGui.getStyle().setFramePadding(0, 1);

			ImGui.pushStyleColor(ImGuiCol.Text, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.FrameBg, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.SliderGrab, 1f, 1f, 1f, 1f);
			ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, 0.9f, 0.9f, 0.9f, 1f);

			ImGui.pushItemWidth(180f);
			changed = ImGui.sliderScalar("", ImGuiDataType.Double, val, minimum, maximum, "%.2f");
			ImGui.popItemWidth();

			ImGui.popStyleColor(6);

			ImGui.getStyle().setGrabRounding(4);
			ImGui.getStyle().setGrabMinSize(10);
			ImGui.getStyle().setFramePadding(4, 3);

			if (moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI)) {
				ImGui.setCursorPos(ImGui.getWindowSizeX() - 10f - ImGui.calcTextSize(val.doubleValue() + "").x, prevPos.y);
			} else {
				ImGui.setCursorPos(ImGui.getCursorPos().x + 180f - ImGui.calcTextSize(val.doubleValue() + "").x, prevPos.y);
			}

			ImGui.text(val.doubleValue()+"");
			ImGui.text(" ");
			ImGui.popStyleVar(1);

			if (changed) {
				this.value = val.doubleValue();
			}
		} else {
			ImInt val = new ImInt(this.value.intValue());

			ImGui.getStyle().setGrabRounding(12);
			ImGui.getStyle().setGrabMinSize(15);
			ImGui.getStyle().setFramePadding(0, 1);

			ImGui.pushStyleColor(ImGuiCol.Text, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.FrameBg, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0f, 0f, 0f, 0f);
			ImGui.pushStyleColor(ImGuiCol.SliderGrab, 1f, 1f, 1f, 1f);
			ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, 0.9f, 0.9f, 0.9f, 1f);

			ImGui.pushItemWidth(180f);
			changed = ImGui.sliderScalar("", ImGuiDataType.S32, val, (int) minimum, (int) maximum);
			ImGui.popItemWidth();

			ImGui.popStyleColor(6);

			ImGui.getStyle().setGrabRounding(4);
			ImGui.getStyle().setGrabMinSize(10);
			ImGui.getStyle().setFramePadding(4, 3);

			if (moduleManager.getModule(AchillesSettingsModule.class).mode.is(AchillesSettingsModule.modeEnum.ClickGUI)) {
				ImGui.setCursorPos(ImGui.getWindowSizeX() - 10f - ImGui.calcTextSize(val.doubleValue() + "").x, prevPos.y);
			} else {
				ImGui.setCursorPos(ImGui.getCursorPos().x + 180f - ImGui.calcTextSize(val.doubleValue() + "").x, prevPos.y);
			}

			ImGui.text(val.doubleValue()+"");
			ImGui.text(" ");
			ImGui.popStyleVar(1);

			if (changed) {
				this.value = val.doubleValue();
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

		ImGui.setCursorPosY(ImGui.getCursorPosY() + 2);

		ImGui.popID();
	}

	public NumberSetting setAdvanced() {
		advanced = true;
		return this;
	}
}