package template.rip.api.event.events;

import net.minecraft.client.gui.screen.Screen;
import template.rip.api.event.Cancellable;

public class SetScreenEvent extends Cancellable {

    public Screen screen;

    public SetScreenEvent(Screen screen) {
        this.screen = screen;
    }
}
