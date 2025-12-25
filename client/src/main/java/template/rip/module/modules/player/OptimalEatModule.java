package template.rip.module.modules.player;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import template.rip.api.event.events.KeyBindingEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

public class OptimalEatModule extends Module {

    public final NumberSetting delayTicks = new NumberSetting(this, 1, 0, 5, 1, "Delay Ticks");

    boolean updated = false;

    public OptimalEatModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
     private void onKeyBind(KeyBindingEvent event) {
        if (mc.player == null || event.key != mc.options.useKey) return;

        if (mc.player.getItemUseTimeLeft() <= -delayTicks.getIValue() && updated && mc.player.isUsingItem() && event.isPressed() && mc.player.getActiveItem().get(DataComponentTypes.FOOD) != null) {
            event.setPressed(false);
            updated = false;
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof InventoryS2CPacket && !updated) {
            updated = true;
        }
    }
}
