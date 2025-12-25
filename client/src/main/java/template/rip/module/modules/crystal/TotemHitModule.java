package template.rip.module.modules.crystal;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class TotemHitModule extends Module {

    public final BooleanSetting onlyTotem = new BooleanSetting(this, true, "Only with totem");

    private boolean lastSprintState;
    private boolean shouldReSprint;

    public TotemHitModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            lastSprintState = mc.player.isSprinting();
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket packet) {
            switch (packet.getMode()) {
                case START_SPRINTING -> lastSprintState = true;
                case STOP_SPRINTING -> lastSprintState = false;
            }
        }
    }

    @EventHandler
    private void onAttackPre(AttackEntityEvent.Pre event) {
        if (!nullCheck())
            return;

        if (!mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) && onlyTotem.isEnabled())
            return;

        shouldReSprint = lastSprintState;
        if (lastSprintState) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
    }

    @EventHandler
    private void onAttackPost(AttackEntityEvent.Post event) {
        if (!nullCheck())
            return;

        if (!mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) && onlyTotem.isEnabled())
            return;

        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        if (shouldReSprint) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
}
