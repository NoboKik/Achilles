package template.rip.module.modules.misc;

import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.AnyNumberSetting;

public class PingSpoofModule extends Module {

    public final AnyNumberSetting lagDelay = new AnyNumberSetting(this, 200, false, "Lag Delay");

    public PingSpoofModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof KeepAliveS2CPacket packet) {
            new Thread(() -> {
                try {
                    Thread.sleep(lagDelay.getIValue());
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().getConnection().send(new KeepAliveC2SPacket(packet.getId()));
                    }
                } catch (Exception ignored) {
                }
            }).start();
            event.cancel();
        }
    }
}
