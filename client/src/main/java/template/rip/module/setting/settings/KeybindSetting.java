package template.rip.module.setting.settings;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.MousePressEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

public class KeybindSetting extends Setting {

	private int code;
	public final int defaultCode;
	private boolean hold, pressed;
	public final boolean canHold;
	private boolean isButtonWasPressed;

	public boolean isModuleKeyBind;

	public KeybindSetting(Module parent, int code, String... name) {
		this(parent, code, Description.of(), name);
	}

	public KeybindSetting(Module parent, int code, Boolean hold, String... name) {
		this(parent, code, hold, Description.of(), name);
	}

	public KeybindSetting(Module parent, int code, Description description, String... name) {
		this(parent, code, null, description, name);
	}

	public KeybindSetting(Module parent, int code, Boolean hold, Description description, String... name) {
		super(description, name);
		this.code = code;
		this.defaultCode = code;
		this.parent = parent;
		this.height = 52;
		this.isButtonWasPressed = false;
		
		if (hold != null) {
			canHold = true;
			this.hold = hold;
		} else {
			canHold = false;
			this.hold = true;
		}
		if (parent != null) {
			parent.addSettings(this);
		}
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean setPressed) {
		pressed = setPressed;
	}

	public void onPress(boolean glfwPress) {
		if (hold) {
			if (glfwPress != pressed) {
				pressed = glfwPress;
			}
		} else if (glfwPress) {
			pressed = !pressed;
		}
	}

	public int getCode() {
		return this.code;
	}

	public void setKeyCode(int code) {
		this.code = code;
	}

	public void setHold(boolean hold) {
		if (canHold) {
			this.hold = hold;
		}
	}

	public boolean isHold() {
		return this.hold;
	}

	public KeybindSetting setAsModuleKeyBind() {
		isModuleKeyBind = true;
		return this;
	}

	@Override
	public void reset() {
		this.code = this.defaultCode;
	}

	@Override
	public SettingType getType() {
		return SettingType.KeyBind;
	}

	@Override
	public void render() {
		ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

		ImVec2 pos = ImGui.getCursorPos().clone();

		if (lastIsSearched) {
			ImGui.pushStyleColor(ImGuiCol.Text, AchillesMenu.searchRGB);
		}

		RenderUtils.drawTexts(getFullName());

		if (lastIsSearched) {
			ImGui.popStyleColor();
		}

		float textOffset = -4;

		ImGui.setCursorPosY(ImGui.getCursorPosY() + textOffset);

		if (!isButtonWasPressed) {
			String kName = KeyUtils.getKeyName(getCode());
			isButtonWasPressed = ImGui.button(kName);
			if (isPressed() && !isModuleKeyBind) {
				ImVec2 prev = ImGui.getCursorPos();
				ImGui.setCursorPosX(ImGui.calcTextSize(kName + "       ").x);
				ImFont font = ImGui.getFont();
				ImGui.popFont();
				ImGui.pushFont(ImguiLoader.poppins12);

				float textY = pos.y + ((prev.y - pos.y) / 1.6f);
				ImGui.setCursorPosY(textY);
				ImGui.text("(pressed)");

				ImGui.setCursorPos(prev.x, prev.y);
				ImGui.popFont();
				ImGui.pushFont(font);
			}
		} else {
			ImGui.button("Press key...");
			Template.moduleManager.binding = true;
			Template.EVENTBUS.subscribe(this);
		}

		if (ImGui.isItemHovered()) {
			if (!getDescription().isEmpty()) {
				ToolTipHolder.setToolTip(getDescription().getContent());
			}
			if (KeyUtils.isKeyPressed(GLFW.GLFW_KEY_BACKSPACE)) {
				reset();
			}
		}

		ImGui.setCursorPosY(ImGui.getCursorPosY() - textOffset);

		if (canHold) {

			String mode = (this.hold ? "Hold" : "Toggle");

			ImVec2 size = ImGui.calcTextSize(mode);
			ImGui.setCursorPos(pos.x + 180 - size.x, pos.y);
			ImGui.text(mode);

			ImGui.pushFont(ImguiLoader.fontAwesome20);

			if (hold) {
				float[] color = JColor.getGuiColor().getFloatColor();
				ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
			} else {
				ImGui.pushStyleColor(ImGuiCol.Text, 0.38f, 0.43f, 0.55f, 1f);
			}

			ImGui.setCursorPos(pos.x + 140f, pos.y);
			if (ImGui.invisibleButton("", 45f, 50f)) {
				hold = !hold;
			}

			ImGui.setCursorPos(pos.x + 160, pos.y + 20);
			ImGui.text(this.hold ? "\uF205" : "\uF204");

			ImGui.popFont();
			ImGui.popStyleColor();
		}

		ImGui.popID();
	}

	@EventHandler(priority = EventPriority.LOW)
	private void onKeyPress(KeyPressEvent event) {
		if (event.action != GLFW.GLFW_RELEASE) {
			isButtonWasPressed = false;
			Template.moduleManager.binding = false;
			Template.EVENTBUS.unsubscribe(this);

			if (event.key == GLFW.GLFW_KEY_ESCAPE) {
				return;
			}

			setKeyCode(event.key == GLFW.GLFW_KEY_DELETE ? -1 : event.key);
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	private void onClick(MousePressEvent event) {
		onKeyPress(new KeyPressEvent(event.button, 0, event.action, 0));
	}

	public KeybindSetting setAdvanced() {
		advanced = true;
		return this;
	}
}