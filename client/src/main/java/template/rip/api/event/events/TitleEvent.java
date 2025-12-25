package template.rip.api.event.events;

import net.minecraft.text.Text;
import template.rip.api.event.Cancellable;

public class TitleEvent extends Cancellable {

	public Text text;

	public TitleEvent(Text text) {
		this.text = text;
	}
}
