package template.rip.module.modules.client;


import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;

public class TransactionsModule extends Module {

    public TransactionsModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof CommonPingS2CPacket) {
            info("§c[Transaction] §f" + ((CommonPingS2CPacket)event.packet).getParameter());
        }
    }
}