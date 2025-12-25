package template.rip.module.modules.combat;

import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import template.rip.api.event.events.KeyBindingEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class AutoBowModule extends Module {

    public final BooleanSetting bow = new BooleanSetting(this, true, "Bows");
    public final NumberSetting bowCharge = new NumberSetting(this, 0.9, 0.1, 1, 0.1, "Bow charge");
    public final BooleanSetting crossbow = new BooleanSetting(this, true, "Crossbows");
    public final NumberSetting crossbowDelay = new NumberSetting(this, 1, 1, 5, 1, "Crossbow delay");
    public final BooleanSetting mainHand = new BooleanSetting(this, true, "MainHand (cross-)bows");
    public final BooleanSetting offHand = new BooleanSetting(this, true, "OffHand (cross-)bows");

    private int delay = 0;
    private boolean pressed = false;

    public AutoBowModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        pressed = false;
        delay = 0;
    }

    @EventHandler
    private void onKeyBind(KeyBindingEvent event) {
        if (!nullCheck())
            return;
        if (event.key == mc.options.useKey && !pressed) {
            event.setPressed(false);
            if (!mc.player.isUsingItem())
                pressed = true;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) {
            return;
        }

        if (bow.isEnabled() && InvUtils.isHoldingItem(Items.BOW, true) && (mc.player.getMainHandStack().getItem() instanceof BowItem
                && mainHand.isEnabled()) || (mc.player.getMainHandStack().getUseAction() == UseAction.NONE
                && offHand.isEnabled() && mc.player.getOffHandStack().getItem() instanceof BowItem)
                && BowItem.getPullProgress(mc.player.getItemUseTime()) >= bowCharge.value) {
            pressed = false;
        }

        if (crossbow.isEnabled() && InvUtils.isHoldingItem(Items.CROSSBOW, true)) {
            ItemStack is = null;
            if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem && mainHand.isEnabled()) {
                is = mc.player.getMainHandStack();
            } else {
                if (mc.player.getMainHandStack().getUseAction() == UseAction.NONE && offHand.isEnabled() && mc.player.getOffHandStack().getItem() instanceof CrossbowItem) {
                    is = mc.player.getOffHandStack();
                }
            }

            if (is == null)
                return;

            if (!mc.player.isUsingItem() && CrossbowItem.isCharged(is)) {
                delay++;
                if (delay >= crossbowDelay.value) {
                    pressed = false;
                    delay = 0;
                }
            }
            if ((CrossbowItem.getPullTime(is, mc.player) + crossbowDelay.getIValue()) <= mc.player.getItemUseTime()) {
                pressed = false;
            }
        }
    }
}