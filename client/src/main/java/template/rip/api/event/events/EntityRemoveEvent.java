package template.rip.api.event.events;

import net.minecraft.entity.Entity;
import template.rip.api.event.Cancellable;

public class EntityRemoveEvent extends Cancellable {

    public Entity entity;

    public EntityRemoveEvent(Entity entity) {
        this.entity = entity;
    }
}
