package template.rip.deprecated;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class AutoPotModule extends Module {
    private final NumberSetting minHealth = new NumberSetting(this, 0.6, 0, 1, 0.01, "Min Health %");
    private final NumberSetting switchDelay = new NumberSetting(this, 0, 0, 10, 1, "Switch Delay");
    private final NumberSetting throwDelay = new NumberSetting(this, 0, 0, 10, 1, "Throw Delay");
    public enum modeEnum{Dynamic, Manual}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Manual, "Mode");

    private final BooleanSetting scroll = new BooleanSetting(this, false, "Scroll");
    private final BooleanSetting goToPrevSlot = new BooleanSetting(this, true, "Previous Slot");
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");

    public AutoPotModule() {
        super(Category.COMBAT, Description.of("Automatically throws health potions"), "AutoPot");
    }

    private int switchClock, throwClock, prevSlot;

    @Override
    public void onEnable() {
        switchClock = 0;
        throwClock = 0;
        prevSlot = -1;
    }

    private float healthPercent() {
        return Math.min((mc.player.getHealth() + mc.player.getAbsorptionAmount()) / mc.player.getMaxHealth(), 1.0f);
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.currentScreen != null)
            return;

        if (!nullCheck())
            return;

        if (activateKey.isPressed()) {
            if ((healthPercent() <= minHealth.getFValue() && mode.is(modeEnum.Dynamic)) || (mode.is(modeEnum.Manual))) {

                if (!InvUtils.isThatSplash(StatusEffects.INSTANT_HEALTH, mc.player.getMainHandStack())) {
                    if (switchClock < switchDelay.getValue()) {
                        switchClock++;
                        return;
                    }

                    if (goToPrevSlot.isEnabled() && prevSlot == -1) prevSlot = mc.player.getInventory().selectedSlot;

                    int potSlot = InvUtils.findSplash(StatusEffects.INSTANT_HEALTH);

                    if (potSlot != -1) {
                        if (scroll.isEnabled()) {
                            InvUtils.setInvSlot(InvUtils.scrollToSlot(potSlot));
                        } else {
                            InvUtils.setInvSlot(potSlot);
                        }

                        switchClock = 0;
                    }
                }

                if (InvUtils.isThatSplash(StatusEffects.INSTANT_HEALTH, mc.player.getMainHandStack())) {
                    if (throwClock < throwDelay.getValue()) {
                        throwClock++;
                        return;
                    }

                    if (Template.isClickSim()) Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());
                    ActionResult actionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    if (PlayerUtils.shouldSwingHand(actionResult))
                        mc.player.swingHand(Hand.MAIN_HAND);

                    throwClock = 0;
                }
            }
        } else if (prevSlot != -1) {
            InvUtils.setInvSlot(prevSlot);
            prevSlot = -1;
        }
    }
}
