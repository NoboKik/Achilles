package template.rip.api.event.events;

import net.minecraft.client.input.Input;
import template.rip.api.event.Cancellable;

import static template.rip.Template.mc;

public class InputEvent extends Cancellable {

    public Input input;
    public boolean check;

    public InputEvent(Input input) {
        this.input = input;
        this.check = (mc.player != null && input == mc.player.input);
    }
}
