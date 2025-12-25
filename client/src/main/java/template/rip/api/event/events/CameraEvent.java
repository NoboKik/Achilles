package template.rip.api.event.events;

import net.minecraft.client.render.Camera;

public class CameraEvent {

	public Camera camera;

	public CameraEvent(Camera camera) {
		this.camera = camera;
	}
}
