package template.rip.module.modules.misc;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class ResourcePackSpooferModule extends Module {

    public final BooleanSetting notify = new BooleanSetting(this, true, "Notification");

    public ResourcePackSpooferModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof ResourcePackSendS2CPacket pack) {
            event.cancel();
            new Thread(() -> {
                ClientPlayNetworkHandler cpnh = mc.getNetworkHandler();
                if (cpnh == null) {
                    return;
                }
                cpnh.sendPacket(new ResourcePackStatusC2SPacket(pack.id(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {
                }
                cpnh.sendPacket(new ResourcePackStatusC2SPacket(pack.id(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            }).start();
        }
    }
}
