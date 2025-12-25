package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.type.ImInt;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModeSetting<T extends Enum<?>> extends Setting {

	public int index;
	public final int defaultIndex;

	public List<T> modes;

	@SuppressWarnings("unchecked cast")
	public ModeSetting(Module parent, Description description, T defaultMode, String... name) {
		super(description, name);
	    this.parent = parent;
	    this.modes = Arrays.stream((T[]) defaultMode.getDeclaringClass().getEnumConstants()).collect(Collectors.toList());
		if (!this.modes.contains(defaultMode)) {
			this.modes = new ArrayList<>(this.modes);
			this.modes.add(defaultMode);
		}
	    this.index = this.modes.indexOf(defaultMode);
		this.defaultIndex = this.index;
		this.height = 52;
		parent.addSettings(this);
	}

	public ModeSetting(Module parent, T defaultMode, String... name) {
		this(parent, Description.of(), defaultMode, name);
	}

	public T getMode() {
	    if (this.index != -1) {
			return this.modes.get(this.index);
		}

		return this.modes.get(this.defaultIndex);
	}

	public String getDisplayName() {
		return getDisplayName(this.index != -1 ? this.modes.get(this.index) : this.modes.get(this.defaultIndex));
	}
	public String getDisplayName(Enum<?> mode) {
		return String.valueOf(mode).replace("_", " ").replace("$", ".").replace("К", "").replace("Е", "-");
	}

	@Override
	public SettingType getType() {
		return SettingType.Mode;
	}
	  
	public void setMode(int index) {
		this.index = Math.max(0, Math.min(modes.size() - 1, index));
	}

	public void setMode(CharSequence charSequence) {
		for (int modeIndex = 0; modeIndex < modes.size(); modeIndex++) {
			String[] names = MathUtils.split(modes.get(modeIndex).toString());

			StringBuilder internalBuilder = new StringBuilder();
			for (String n : names) {
				internalBuilder.append(n.charAt(0));
			}
			CharSequence modeCharSequence = internalBuilder.toString();

			if (modeCharSequence.length() == charSequence.length()) {
				boolean match = true;
				for (int i = 0; i < charSequence.length(); i++) {
					if (modeCharSequence.charAt(i) != charSequence.charAt(i)) {
						match = false;
						break;
					}
				}

				if (match) {
					this.index = modeIndex;
					break;
				}
			}
		}
	}
	  
	public boolean is(T mode) {
	    return (this.index == modes.indexOf(mode));
	}

	@Override
	public void reset() {
		this.index = this.defaultIndex;
	}

	@Override
	public void render() {
		float[] c = JColor.getGuiColor().getFloatColor();

		ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));
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

		ImInt currentItem = new ImInt(this.index);

		ImGui.pushItemWidth(180f);
		ImGui.pushStyleColor(ImGuiCol.FrameBg,          c[0], c[1], c[2], 0.8f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered,   c[0], c[1], c[2], 0.7f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive,    c[0], c[1], c[2], 0.6f);
		ImGui.pushStyleColor(ImGuiCol.Button,           c[0], c[1], c[2], 0.8f);
		ImGui.pushStyleColor(ImGuiCol.ButtonHovered,    c[0], c[1], c[2], 0.7f);
		ImGui.pushStyleColor(ImGuiCol.ButtonActive,     c[0], c[1], c[2], 0.6f);
		ImGui.pushStyleColor(ImGuiCol.Header,        0.21f, 0.24f, 0.31f, 0.4f);
		ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.21f, 0.24f, 0.31f, 0.3f);
		ImGui.pushStyleColor(ImGuiCol.HeaderActive,  0.21f, 0.24f, 0.31f, 0.2f);

		String[] strings = new String[modes.size()];
		for (int i = 0; i < modes.size(); i++) {
			strings[i] = getDisplayName(modes.get(i));
		}
		ImGui.combo("", currentItem, strings, 9999999);

		ImGui.popStyleColor(9);
		ImGui.popItemWidth();

		this.index = currentItem.get();

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

	public ModeSetting<T> setAdvanced() {
		advanced = true;
		return this;
	}
}