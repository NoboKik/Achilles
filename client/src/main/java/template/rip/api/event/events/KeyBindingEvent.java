package template.rip.api.event.events;

import net.minecraft.client.option.KeyBinding;

public class KeyBindingEvent {

	private boolean changed;
	private boolean pressed;
	public KeyBinding key;

	public KeyBindingEvent(KeyBinding key, boolean pressed) {
		this.changed = false;
		this.key = key;
		this.pressed = pressed;
	}

	public void setPressed(boolean pressed) {
		if (this.pressed != pressed) {
			this.pressed = pressed;
			this.changed = true;
		}
	}

	public boolean isPressed() {
		return pressed;
	}

	public boolean isChanged() {
		return changed;
	}
}
