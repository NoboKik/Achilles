package template.rip.api.event.events;

import net.minecraft.text.Text;
import template.rip.api.event.Cancellable;

public class SubTitleEvent extends Cancellable {

	public Text text;

	public SubTitleEvent(Text text) {
		this.text = text;
	}
}
