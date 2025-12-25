package template.rip.module.modules.misc;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.modules.blatant.SpeedModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class FlagDetectorModule extends Module {

    public final BooleanSetting speed = new BooleanSetting(this, true, "Speed");
    public final BooleanSetting blockToggle = new BooleanSetting(this, true, "Block Toggle");
    public final NumberSetting blockTime = new NumberSetting(this, 1.5, 0, 3, 0.1, "Block Time");

    public static long nextToggle = 0L;

    public FlagDetectorModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!nullCheck()) return;

        if (event.packet instanceof PlayerPositionLookS2CPacket wrapper) {
            Vec3d loc = new Vec3d(wrapper.change().position().x, wrapper.change().position().y, wrapper.change().position().z);

            if (loc.distanceTo(mc.player.getPos()) < 3) toggleModules();
        }
    }

    public void toggleModules() {
        SpeedModule speedModule = Template.moduleManager.getModule(SpeedModule.class);
        if (speed.isEnabled() && speedModule != null && speedModule.isEnabled()) speedModule.setEnabled(false);
        nextToggle = blockToggle.isEnabled() ? 0L : System.currentTimeMillis() + (blockTime.getIValue() * 1000L);
    }
}
