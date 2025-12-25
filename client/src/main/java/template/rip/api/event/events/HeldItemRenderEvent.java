package template.rip.api.event.events;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import template.rip.api.event.Cancellable;

public class HeldItemRenderEvent extends Cancellable {

	public float progress;
	public MatrixStack matrices;
	public Arm arm;

	public static class Swing extends HeldItemRenderEvent {

		public Swing(MatrixStack matrices, Arm arm, float progress) {
			this.matrices = matrices;
			this.arm = arm;
			this.progress = progress;
		}
	}

	public static class Equip extends HeldItemRenderEvent {

		public Equip(MatrixStack matrices, Arm arm, float progress) {
			this.matrices = matrices;
			this.arm = arm;
			this.progress = progress;
		}
	}
}
