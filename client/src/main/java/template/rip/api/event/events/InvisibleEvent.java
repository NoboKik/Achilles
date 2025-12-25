package template.rip.api.event.events;

import net.minecraft.entity.Entity;

public class InvisibleEvent {

	public Entity entity;
	public boolean invisible;

	public InvisibleEvent(Entity entity, boolean invisible) {
		this.entity = entity;
		this.invisible = invisible;
	}
}
