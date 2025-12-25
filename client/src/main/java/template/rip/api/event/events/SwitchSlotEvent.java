package template.rip.api.event.events;

import net.minecraft.client.option.KeyBinding;

import static template.rip.Template.mc;

@SuppressWarnings("all")
public class SwitchSlotEvent {

    private int switchSlotTo;
    private boolean modSwitch;

    public SwitchSlotEvent() {
        this.switchSlotTo = -1;
        this.modSwitch = false;
    }

    public boolean switchSlot(int to) {
        if (to >= 0 && to <= 8) {
            switchSlotTo = to;
            modSwitch = true;
            return true;
        }
        return false;
    }

    public void apply() {
        if (!modSwitch) {
            return;
        }
        for (KeyBinding k : mc.options.hotbarKeys) {
            while (k.wasPressed());
        }
        if (mc.player == null) {
            return;
        }
        mc.player.getInventory().selectedSlot = switchSlotTo;
    }
}
