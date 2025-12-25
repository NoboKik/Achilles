package template.rip.api.event.events;

import net.minecraft.entity.Entity;
import template.rip.api.event.Cancellable;

public class AttackEntityEvent extends Cancellable {

    public static class Pre extends AttackEntityEvent {

        public Entity target;

        public Pre(Entity target) {
            this.target = target;
        }
    }

    public static class Post extends AttackEntityEvent {

        public Entity target;

        public Post(Entity target) {
            this.target = target;
        }
    }
}
