package template.rip.api.event.events;

import net.minecraft.network.packet.Packet;

public class BacktrackEvent {

    public Packet<?> packet;
    public boolean latency, backtrack, dynamicLatency, skip;
    public Long latencyTime;

    public BacktrackEvent(Packet<?> packet, boolean latency, boolean dynamicLatency, Long latencyTime, boolean backtrack, boolean skip) {
        this.packet = packet;
        this.latency = latency;
        this.dynamicLatency = dynamicLatency;
        this.latencyTime = latencyTime;
        this.backtrack = backtrack;
        this.skip = skip;
    }
}
