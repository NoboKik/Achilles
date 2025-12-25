package template.rip.api.util;

import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.util.Pair;
import template.rip.Template;
import template.rip.api.event.events.BacktrackEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static template.rip.Template.mc;

public class BacktrackUtil {

    private final List<Pair<Packet<ClientPlayNetworkHandler>, Long>> packets = Collections.synchronizedList(new ArrayList<>());
    public boolean latency, backtrack, dynamicLatency;
    public long lastLatency;

    public BacktrackUtil() {}

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.getNetworkHandler() == null || mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        if (mc.getNetworkHandler().getPhase() != NetworkPhase.PLAY && mc.getNetworkHandler().getPhase() != NetworkPhase.LOGIN) {
            synchronized (packets) {
                packets.clear();
            }
            return;
        }
        BacktrackEvent be = new BacktrackEvent(event.packet, false, false, lastLatency, false, false);
        Template.EVENTBUS.post(be);
        if (be.skip || event.packet == null || event.isCancelled() || event.packet instanceof QueryPingC2SPacket)
            return;

        if (!latency && be.latency) {
            latency = true;
            dumpPackets();
        }
        if (!dynamicLatency && be.dynamicLatency) {
            dynamicLatency = true;
            dumpPackets();
        }
        if (be.latencyTime != lastLatency) {
            lastLatency = be.latencyTime;
            if (!dynamicLatency) {
                dumpPackets();
            }
        }
        if (!backtrack && be.backtrack) {
            backtrack = true;
            dumpPackets();
        }
        if (be.latency) {
            synchronized (packets) {
                packets.add(new Pair<>(event.qPacket, System.currentTimeMillis()));
            }
            dumpPackets(true);
            event.cancel();
            return;
        }
        if (be.dynamicLatency) {
            synchronized (packets) {
                packets.add(new Pair<>(event.qPacket, System.currentTimeMillis()));
            }
            dumpPackets(true);
            event.cancel();
            return;
        }
        if (be.backtrack) {
            synchronized (packets) {
                packets.add(new Pair<>(event.qPacket, 0L));
            }
            event.cancel();
            return;
        }
        dynamicLatency = false;
        backtrack = false;
        latency = false;
        dumpPackets();
    }

    public void dumpPackets() {
        dumpPackets(false);
    }

    public void dumpPackets(boolean latency) {
        if (mc.getNetworkHandler() == null)
            return;

        synchronized (packets) {
            int i = 0;
            while (!packets.isEmpty() && (!latency || (packets.get(0).getRight() + lastLatency < System.currentTimeMillis())) && i < 10000) {
                Pair<Packet<ClientPlayNetworkHandler>, Long> pkt = packets.get(0);
                try {
                    Packet<ClientPlayNetworkHandler> pkta = pkt.getLeft();
                    pkta.apply(mc.getNetworkHandler());
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
