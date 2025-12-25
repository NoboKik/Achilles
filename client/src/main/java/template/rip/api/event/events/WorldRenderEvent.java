package template.rip.api.event.events;

import template.rip.api.blockesp.WorldRenderContext;

public class WorldRenderEvent {

	public WorldRenderContext context;

	public WorldRenderEvent(WorldRenderContext context) {
		this.context = context;
	}
}
