package template.rip.module.modules.crystal;

import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;

public class PearlMacroModule extends Module {

    private final BooleanSetting goToPrevSlot = new BooleanSetting(this, true, "Go To Last Slot");
    private final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 0d, 1, 0d, 5d, 1d, "Switch Delay");
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");

    private int switchClock = 0;
    private int switchLast = 0;
    private int lastSlot = -1;
    private int prevLastSlot = -1;
    private boolean pressed = false;

    public PearlMacroModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        prevLastSlot = -1;
        pressed = false;
        switchClock = switchDelay.getRandomInt();
        switchLast = switchDelay.getRandomInt();
    }

    @EventHandler
    private void onInput(HandleInputEvent.Pre event) {
        if (activateKey.isPressed()) {
            if (!pressed) {
                switchClock = switchDelay.getRandomInt();
            }
            pressed = true;
        }
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player == null) return;

        if (!pressed) {
            if (prevLastSlot != -1 && goToPrevSlot.isEnabled()) {
                if (switchLast > 0) {
                    switchLast--;
                    return;
                }
                mc.player.getInventory().selectedSlot = prevLastSlot;
                prevLastSlot = -1;
                switchLast = switchDelay.getRandomInt();
            }
            lastSlot = mc.player.getInventory().selectedSlot;
            return;
        }

        if (mc.player.getInventory().getMainHandStack().getItem() instanceof EnderPearlItem && pressed) {
            if (switchClock > 0) {
                switchClock--;
                return;
            }
            mc.doItemUse();
            if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
            pressed = false;
            switchLast = switchDelay.getRandomInt();
        }

        if (InvUtils.getItemSlot(Items.ENDER_PEARL) != -1) {
            if (prevLastSlot == -1) {
                prevLastSlot = lastSlot;
            }
            InvUtils.setInvSlot(InvUtils.getItemSlot(Items.ENDER_PEARL));
            switchClock = switchDelay.getRandomInt();
        }
    }
}
