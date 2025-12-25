package template.rip.api.event.events;

public class CharPressEvent {

	public char[] keys;
	public long window;

	public CharPressEvent(char[] keys, long window) {
		this.keys = keys;
		this.window = window;
	}
}
