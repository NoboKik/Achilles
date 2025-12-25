package template.rip.module.modules.combat;

import net.minecraft.item.ItemStack;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;

import java.util.function.Predicate;

public class WindMacroModule extends Module {

    private final BooleanSetting goToPrevSlot = new BooleanSetting(this, true, "Go To Last Slot");
    private final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 0d, 1, 0d, 5d, 1d, "Switch Delay");
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");

    private int switchClock = 0;
    private int switchLast = 0;
    private int lastSlot = -1;
    private boolean pressed = false;
    private static final Predicate<ItemStack> windChargePred = is -> is.getName().getString().toLowerCase().contains("wind charge");

    public WindMacroModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
        pressed = false;
        switchClock = switchDelay.getRandomInt();
        switchLast = switchDelay.getRandomInt();
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (activateKey.isPressed()) {
            if (!pressed) {
                switchClock = switchDelay.getRandomInt();
            }
            pressed = true;
        }
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (mc.player == null)
            return;

        if (!pressed) {
            if (lastSlot != -1 && goToPrevSlot.isEnabled()) {
                if (switchLast > 0) {
                    switchLast--;
                    return;
                }
                mc.player.getInventory().selectedSlot = lastSlot;
                lastSlot = -1;
                switchLast = switchDelay.getRandomInt();
            }
            return;
        }

        if (windChargePred.test(mc.player.getMainHandStack()) && pressed) {
            if (switchClock > 0) {
                switchClock--;
                return;
            }
            mc.doItemUse();
            if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
            pressed = false;
            switchLast = switchDelay.getRandomInt();
        }

        int ss = InvUtils.getItemStackSlot(windChargePred);
        if (ss != -1) {
            if (lastSlot == -1)
                lastSlot = mc.player.getInventory().selectedSlot;
            InvUtils.setInvSlot(ss);
            switchClock = switchDelay.getRandomInt();
        }
    }
}
