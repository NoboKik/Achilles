package template.rip.module.modules.combat;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;

public class SnowballThrowModule extends Module {
    private final MinMaxNumberSetting useDelay = new MinMaxNumberSetting(this, 0d, 1, 0d, 10d, 1d, "Use Delay");
    private final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 0d, 1, 0d, 10d, 1d, "Switch Delay");
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");

    private int prevSlot;
    private int placeClock, switchClock;
    private boolean selectedSnowballs;

    public SnowballThrowModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    private void reset() {
        prevSlot = -1;

        placeClock = useDelay.getRandomInt();
        switchClock = switchDelay.getRandomInt();

        selectedSnowballs = false;
    }

    @Override
    public void onEnable() {
        reset();
    }

    private int getSnowballSlot() {
        int snowSlot = InvUtils.getItemSlot(Items.SNOWBALL);
        if (snowSlot != -1) return snowSlot;

        return InvUtils.getItemSlot(Items.EGG);
    }

    private boolean checkStack(ItemStack handStack) {
        return handStack.isOf(Items.SNOWBALL) || handStack.isOf(Items.EGG);
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.currentScreen != null) return;

        if (!nullCheck())
            return;

        if (activateKey.isPressed()) {
            int snowSlot = getSnowballSlot();

            if (!checkStack(mc.player.getMainHandStack()))
                selectedSnowballs = false;
            if (!selectedSnowballs) {
                if (snowSlot == -1) return;

                if (switchClock > 0) {
                    switchClock--;
                    return;
                }

                prevSlot = mc.player.getInventory().selectedSlot;
                InvUtils.setInvSlot(snowSlot);

                selectedSnowballs = true;
                switchClock = switchDelay.getRandomInt();
            }
            if (checkStack(mc.player.getMainHandStack())) {
                if (placeClock > 0) {
                    placeClock--;
                    return;
                }

                if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                ActionResult interactionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }

                placeClock = useDelay.getRandomInt();
            }
        } else {
            if (mc.player.getInventory().selectedSlot != prevSlot && prevSlot != -1) InvUtils.setInvSlot(prevSlot);
            reset();
        }
    }
}
