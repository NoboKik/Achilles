package template.rip.api.event.events;

import net.minecraft.client.gui.DrawContext;

public class HudRenderEvent {

	public DrawContext context;
	public float tickDelta;

	public HudRenderEvent(DrawContext context, float tickDelta) {
		this.context = context;
		this.tickDelta = tickDelta;
	}
}
