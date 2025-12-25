package template.rip.api.event.events;

import net.minecraft.client.particle.Particle;
import template.rip.api.event.Cancellable;

public class ParticleEvent extends Cancellable {

	public Particle particle;

	public ParticleEvent(Particle particle) {
		this.particle = particle;
	}
}
