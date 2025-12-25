package template.rip.api.event.events;

public class KeyPressEvent {

	public int key, scanCode, action;
	public long window;

	public KeyPressEvent(int key, int scanCode, int action, long window) {
		this.key = key;
		this.scanCode = scanCode;
		this.action = action;
		this.window = window;
	}
}
