package template.rip.api.event.events;

import net.minecraft.network.packet.Packet;

public class BlinkEvent {

    public Packet<?> packet;
    public boolean latency, blink, skip;
    public Long latencyTime;

    public BlinkEvent(Packet<?> packet, boolean latency, Long latencyTime, boolean blink, boolean skip) {
        this.packet = packet;
        this.latency = latency;
        this.latencyTime = latencyTime;
        this.blink = blink;
        this.skip = skip;
    }
}
