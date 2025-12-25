package template.rip.api.event.events;

import net.minecraft.entity.Entity;
import template.rip.api.event.Cancellable;

public class VelocityEvent extends Cancellable {

    public Entity entity;
    public double x;
    public double y;
    public double z;

    public static class Pre extends VelocityEvent {

        public Pre(Entity entity, double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.entity = entity;
        }
    }

    public static class Post extends VelocityEvent {

        public Post(Entity entity, double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.entity = entity;
        }
    }
}
