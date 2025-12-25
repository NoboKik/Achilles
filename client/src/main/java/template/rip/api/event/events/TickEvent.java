package template.rip.api.event.events;

import template.rip.MixinMethods;
import template.rip.api.event.Cancellable;

public class TickEvent extends Cancellable {

    public static class Pre extends TickEvent {

        public long tick;

        public Pre() {
            this.tick = MixinMethods.tickCount;;
        }
    }

    public static class Post extends TickEvent { }
}
