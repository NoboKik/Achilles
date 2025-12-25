package template.rip.deprecated;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
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

import java.util.function.Predicate;

public class AutoHeadModule extends Module {
    private final NumberSetting minHealth = new NumberSetting(this, 0.6, 0, 1, 0.01, "Min Health %");
    private final NumberSetting switchDelay = new NumberSetting(this, 1, 0, 10, 1, "Switch Delay");
    private final NumberSetting useDelay = new NumberSetting(this, 1, 0, 10, 1, "Use Delay");
    public enum modeEnum{Auto, Manual}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Manual, "Mode");

    private final BooleanSetting scroll = new BooleanSetting(this, false, "Scroll");
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    private int switchClock, useClock, prevSlot;

    public AutoHeadModule() {
        super(Category.COMBAT, Description.of("Automatically eats Golden Heads"), "AutoHead");
    }

    @Override
    public void onEnable() {
        switchClock = 0;
        useClock = 0;
        prevSlot = -1;
    }

    @Override
    public String getSuffix() {
        return " "+mode.getDisplayName();
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

        Predicate<Item> headPred = theItem -> theItem == Items.PLAYER_HEAD;
        if (activateKey.isPressed() || (healthPercent() <= minHealth.getFValue() && mode.is(modeEnum.Auto))) {
            if (!headPred.test(mc.player.getMainHandStack().getItem())) {
                if (switchClock < switchDelay.getValue()) {
                    switchClock++;
                    return;
                }

                if (prevSlot == -1) prevSlot = mc.player.getInventory().selectedSlot;

                int soupSlot = InvUtils.getItemSlot(headPred);

                if (soupSlot != -1) {
                    if (scroll.isEnabled()) {
                        InvUtils.setInvSlot(InvUtils.scrollToSlot(soupSlot));
                    } else {
                        InvUtils.setInvSlot(soupSlot);
                    }

                    switchClock = 0;
                }
            }

            if (headPred.test(mc.player.getMainHandStack().getItem())) {
                if (useClock < useDelay.getValue()) {
                    useClock++;
                    return;
                }

                if (Template.isClickSim())
                    Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());
                ActionResult actionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (PlayerUtils.shouldSwingHand(actionResult))
                    mc.player.swingHand(Hand.MAIN_HAND);

                useClock = 0;
            }
        } else {
            InvUtils.getItemSlot(Items.BOWL);
            if (prevSlot != -1) {
                InvUtils.setInvSlot(prevSlot);
                prevSlot = -1;
            }
        }
    }
}
