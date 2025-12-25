package template.rip.api.event.events;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import template.rip.api.event.Cancellable;

public class PacketEvent extends Cancellable {

	public static class Receive extends PacketEvent {

		public Packet<?> packet;
		public Packet<ClientPlayNetworkHandler> qPacket;

		public Receive(Packet<ClientPlayNetworkHandler> packet) {
			this.packet = packet;
			this.qPacket = packet;
		}
	}

	public static class Send extends PacketEvent {

		public Packet<?> packet;

		public Send(Packet<?> packet) {
			this.packet = packet;
		}
	}
}
