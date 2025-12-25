package template.rip.api.util;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.util.Pair;
import template.rip.Template;
import template.rip.api.event.events.BlinkEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static template.rip.Template.mc;

public class BlinkUtil {

    private final List<Pair<Packet<?>, Long>> packets = Collections.synchronizedList(new ArrayList<>());
    public boolean latency, blink;
    public long lastLatency;

    public BlinkUtil() {}

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        BlinkEvent blinkEvent = new BlinkEvent(event.packet,false, lastLatency,false, false);
        Template.EVENTBUS.post(blinkEvent);
        if (blinkEvent.skip || event.packet == null || event.isCancelled() || !(mc.player != null && mc.world != null && mc.interactionManager != null && mc.getNetworkHandler() != null) || event.packet instanceof QueryPingC2SPacket)
            return;

        if (!latency && blinkEvent.latency) {
            latency = true;
            dumpPackets(false);
        }
        if (blinkEvent.latencyTime != lastLatency) {
            lastLatency = blinkEvent.latencyTime;
            dumpPackets(false);
        }
        if (!blink && blinkEvent.blink) {
            blink = true;
            dumpPackets(false);
        }
        if (blinkEvent.latency) {
            synchronized (packets) {
                packets.add(new Pair<>(event.packet, System.currentTimeMillis() + lastLatency));
                dumpPackets(true);
                event.cancel();
                return;
            }
        }
        if (blinkEvent.blink) {
            synchronized (packets) {
                packets.add(new Pair<>(event.packet, 0L));
                event.cancel();
                return;
            }
        }
        blink = false;
        latency = false;
        dumpPackets(false);
    }

    public void dumpPackets(boolean latency) {
        if (mc.getNetworkHandler() == null)
            return;

        synchronized (packets) {
            int i = 0;
            while (!packets.isEmpty() && (!latency || (packets.get(0).getRight() < System.currentTimeMillis())) && i < 10000) {
                Pair<Packet<?>, Long> pkt = packets.get(0);
                try {
                    if (pkt != null)
                        Template.sendNoEvent(pkt.getLeft());
                } catch (Exception ignored) {
                }
                try {
                    if (!packets.isEmpty())
                        packets.remove(0);
                } catch (Exception ignored) {
                }

                i++;
            }
            if (!latency) {
                packets.clear();
            }
        }
    }
}
