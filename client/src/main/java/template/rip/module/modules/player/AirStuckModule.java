package template.rip.module.modules.player;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import template.rip.api.event.events.MovementTickEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class AirStuckModule extends Module {

    private final BooleanSetting sendRots = new BooleanSetting(this, Description.of("Sends rotation packets so you won't flag missed box/rotation place on anticheats"), false, "Send Rotation Packets");
    private final BooleanSetting grimAbuse = new BooleanSetting(this, Description.of("Stops being stuck for one tick after getting flagged"), false, "Grim Abuse");
    private final BooleanSetting disableOnFlag = new BooleanSetting(this, Description.of("Disables when you get flagged (teleported) by the server"), false, "Disable On Flag");
    private final BooleanSetting disableWhenOnGround = new BooleanSetting(this, Description.of("Disables when you touch the ground"), false, "Disable On Ground");

    private boolean allowNext;

    public AirStuckModule(Category category, Description description, String name) {
        super(category, description, name);
        disableOnFlag.addConditionBoolean(grimAbuse, false);
        grimAbuse.addConditionBoolean(disableOnFlag, false);
    }

    @Override
    public void onEnable() {
        allowNext = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!sendRots.isEnabled()) {
            event.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (disableWhenOnGround.isEnabled() && mc.player.isOnGround()) {
            setEnabled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(MovementTickEvent event) {
        if (sendRots.isEnabled()) {
            if (allowNext) {
                allowNext = false;
            } else {
                event.cancel();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            if (disableOnFlag.isEnabled()) {
                setEnabled(false);
            }
            if (grimAbuse.isEnabled()) {
                allowNext = true;
            }
        }
    }
}
