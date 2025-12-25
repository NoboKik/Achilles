package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImDouble;
import imgui.type.ImInt;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

public class MinMaxNumberSetting extends Setting {

	private double minValue;
	public final double defaultMinValue, defaultMaxValue;
	private double maxValue;
	public double minimum, maximum;
	private double increment;
	public final boolean decimal;
	private boolean prevDrag;
	private boolean isMinSliderNearest;

	public MinMaxNumberSetting(Module parent, Description description, double minValue, double maxValue, double minimum, double maximum, double increment, String... name) {
		super(description, name);
	    this.parent = parent;
	    this.minValue = minValue;
		this.defaultMinValue = minValue;
		this.maxValue = maxValue;
		this.defaultMaxValue = maxValue;
	    this.minimum = minimum;
	    this.maximum = maximum;
	    this.increment = increment;
		this.decimal = !(Math.floor(increment) == increment);
		this.height = 45;

		if (parent != null) parent.addSettings(this);
	}

	public MinMaxNumberSetting(Module parent, double minValue, double maxValue, double minimum, double maximum, double increment, String... name) {
		this(parent, Description.of(), minValue, maxValue, minimum, maximum, increment, name);
	}

	@Override
	public SettingType getType() {
		return SettingType.MinMaxNumber;
	}

	public double getMinValue() {
	    return this.minValue;
	}

	public float getFMinValue() {
		return (float) this.minValue;
	}

	public int getIMinValue() {
		return (int) this.minValue;
	}

	public double getMaxValue() {
		return this.maxValue;
	}

	public float getFMaxValue() {
		return (float) this.maxValue;
	}

	public int getIMaxValue() {
		return (int) this.maxValue;
	}
	  
	public void setMinValue(double minValue) {
	    double precision = 1.0D / this.increment;
	    this.minValue = Math.round(Math.max(this.minimum, Math.min(this.maximum, minValue)) * precision) / precision;
	}

	public void setMaxValue(double maxValue) {
		double precision = 1.0D / this.increment;
		this.maxValue = Math.round(Math.max(this.minimum, Math.min(this.maximum, maxValue)) * precision) / precision;
	}

	@Override
	public void reset() {
		this.minValue = this.defaultMinValue;
		this.maxValue = this.defaultMaxValue;
	}
	 
	public void increment(boolean positive) {
	    setMinValue(getMinValue() + (positive ? 1 : -1) * increment);
		setMinValue(getMaxValue() + (positive ? 1 : -1) * increment);
	}

	public boolean containsNumber(double number) {
		return minValue < number && number < maxValue;
	}

	public double getRandomDouble() {
		return MathUtils.getRandomDouble(minValue, maxValue);
	}

	public int getRandomInt() {
		return MathUtils.getRandomInt((int) minValue, (int) maxValue);
	}

	public double getLerpedFromMinAndMax(double delta) {
		return MathHelper.lerp((float) MathUtils.coerceIn(delta, 0, 1), (float) getMinValue(), (float) getMaxValue());
	}

	public double getLerpInMinAndMax(double number) {
		return (getMaxValue() - getMinValue() == 0) ? 0 : (MathUtils.coerceIn(number, getMinValue(), getMaxValue()) - getMinValue()) / (getMaxValue() - getMinValue());
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

		ImVec2 textPos = ImGui.getCursorPos();

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

		ImVec2 pos = ImGui.getCursorPos();

		float[] color = JColor.getGuiColor().getFloatColor();

		ImGui.setCursorPosX(pos.x + 4);
		ImGui.setCursorPosY(pos.y + 9);

		ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.38f, 0.43f, 0.55f, 0.6f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.38f, 0.43f, 0.55f, 0.6f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0.38f, 0.43f, 0.55f, 0.6f);

		ImGui.pushStyleVar(ImGuiStyleVar.DisabledAlpha, 1f);
		ImGui.beginDisabled();

		ImGui.getWindowDrawList().addRectFilled(
				ImGui.getCursorScreenPosX(),
				ImGui.getCursorScreenPosY(),
				ImGui.getCursorScreenPosX()+ 170f,
				ImGui.getCursorScreenPosY()+7f,
				ImGui.getColorU32(color[0], color[1], color[2], 0.3f),
				3f
		);

		ImGui.popStyleColor(3);

		ImGui.setCursorPosX(pos.x + 4);
		ImGui.setCursorPosY(pos.y + 9);

		ImGui.pushStyleColor(ImGuiCol.FrameBg, color[0], color[1], color[2], 1f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, color[0], color[1], color[2], 1f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive, color[0], color[1], color[2], 1f);

		double minPercentage = 1f - (getMaximum() - getMinValue()) / (getMaximum() - getMinimum());
		double percentage = (getMaxValue() - getMinValue()) / (getMaximum() - getMinimum());

		if (percentage != 0f) {
			float minX = ImGui.getCursorScreenPosX() + (float) (170f * minPercentage);
			float maxX = minX + (float) (170f * percentage);
			ImGui.getWindowDrawList().addRectFilled(minX, ImGui.getCursorScreenPosY(), maxX, ImGui.getCursorScreenPosY()+7f, ImGui.getColorU32(color[0], color[1], color[2], 1f), 3f);
		}

		ImGui.popStyleColor(3);
		ImGui.popStyleVar(1);

		ImGui.endDisabled();

		ImGui.setCursorPos(pos.x, pos.y);

		double minValuePercentage = (getMinValue() - getMinimum()) / (getMaximum() - getMinimum());
		double maxValuePercentage = (getMaxValue() - getMinimum()) / (getMaximum() - getMinimum());

		double minValuePosX = ImGui.getWindowPosX() + pos.x + (135f * minValuePercentage);
		double maxValuePosX = ImGui.getWindowPosX() + pos.x + (135f * maxValuePercentage);

		double mouseX = ImGui.getMousePosX();

		double minValueDistance = Math.max(minValuePosX, mouseX) - Math.min(minValuePosX, mouseX);
		double maxValueDistance = Math.max(maxValuePosX, mouseX) - Math.min(maxValuePosX, mouseX);

		if (!prevDrag)
			isMinSliderNearest = minValueDistance < maxValueDistance;

		Number minVal, maxVal;

		ImGui.pushStyleColor(ImGuiCol.Text, 0f, 0f, 0f, 0f);
		ImGui.pushStyleColor(ImGuiCol.FrameBg, 0f, 0f, 0f, 0f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0f, 0f, 0f, 0f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0f, 0f, 0f, 0f);
		ImGui.pushStyleColor(ImGuiCol.SliderGrab, 1f, 1f, 1f, 1f);
		ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, 0.9f, 0.9f, 0.9f, 1f);

		if (decimal) {
			minVal = new ImDouble(this.minValue);
			maxVal = new ImDouble(this.maxValue);

			ImGui.pushID(parent.getName() + "/" + this.getName() + "/1Slider");
			ImGui.pushItemWidth(180f);

			ImGui.sliderScalar("", ImGuiDataType.Double, isMinSliderNearest ? (ImDouble) minVal :  (ImDouble) maxVal, minimum, maximum, "%.1f");

			prevDrag = ImGui.isItemHovered() && KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);

			ImGui.popItemWidth();
			ImGui.popID();

			ImGui.pushID(parent.getName() + "/" + this.getName() + "/2Slider");
			ImGui.pushItemWidth(180f);
			ImGui.setCursorPos(pos.x, pos.y);

			ImGui.sliderScalar("", ImGuiDataType.Double, isMinSliderNearest ? (ImDouble) maxVal :  (ImDouble) minVal, minimum, maximum, "%.1f");

			ImGui.popItemWidth();
			ImGui.popID();

			ImGui.popStyleColor(6);

			if (minVal.doubleValue() > maxVal.doubleValue() && maxValue != maxVal.doubleValue()) {
				((ImDouble) minVal).set(maxVal.doubleValue());
			}

			if (maxVal.doubleValue() < minVal.doubleValue() && minValue != minVal.doubleValue()) {
				((ImDouble) maxVal).set(minVal.doubleValue());
			}

			this.minValue = minVal.doubleValue();
			this.maxValue = maxVal.doubleValue();

			String text = this.minValue + "-" + this.maxValue;

			ImGui.setCursorPos(textPos.x + 180f - ImGui.calcTextSize(text).x, textPos.y);
			ImGui.text(text);
		} else {
			minVal = new ImInt((int) this.minValue);
			maxVal = new ImInt((int) this.maxValue);

			ImGui.pushID(String.format("%s/%s/1Slider", parent.getName(), this.getName()));
			ImGui.pushItemWidth(180f);

			ImGui.sliderScalar("", ImGuiDataType.S32, isMinSliderNearest ? (ImInt) minVal : (ImInt) maxVal, (int) minimum, (int) maximum);

			prevDrag = ImGui.isItemHovered() && KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);

			ImGui.popItemWidth();
			ImGui.popID();

			ImGui.pushID(String.format("%s/%s/2Slider", parent.getName(), this.getName()));
			ImGui.pushItemWidth(180f);
			ImGui.setCursorPos(pos.x, pos.y);

			ImGui.sliderScalar("", ImGuiDataType.S32, isMinSliderNearest ? (ImInt) maxVal : (ImInt) minVal, (int) minimum, (int) maximum);

			ImGui.popStyleColor(6);
			ImGui.popItemWidth();
			ImGui.popID();

			if (minVal.intValue() > maxVal.intValue() && maxValue != maxVal.intValue()) {
				((ImInt) minVal).set(maxVal.intValue());
			}

			if (maxVal.intValue() < minVal.intValue() && minValue != minVal.intValue()) {
				((ImInt) maxVal).set(minVal.intValue());
			}

			this.minValue = minVal.intValue();
			this.maxValue = maxVal.intValue();

			String text = (int) this.minValue + "-" + (int) this.maxValue;

			ImGui.setCursorPos(textPos.x + 180f - ImGui.calcTextSize(text).x, textPos.y);
			ImGui.text(text);
		}

		ImGui.getStyle().setGrabRounding(4);
		ImGui.getStyle().setGrabMinSize(10);
		ImGui.getStyle().setFramePadding(4, 3);

		ImGui.setCursorPos(textPos.x, textPos.y + 50);

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

	public MinMaxNumberSetting setAdvanced() {
		advanced = true;
		return this;
	}
}