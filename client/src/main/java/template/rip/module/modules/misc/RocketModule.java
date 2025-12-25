package template.rip.module.modules.misc;

import net.minecraft.util.Hand;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

import static net.minecraft.item.Items.FIREWORK_ROCKET;

public class RocketModule extends Module {

    private final BooleanSetting strict = new BooleanSetting(this, Description.of("Prevents you from updating your selected slot on the same tick you switch to it."), false, "Strict");

    private boolean isHolding;

    private int lastSlot;

    public RocketModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        isHolding = false;
        lastSlot = -1;

        if (!nullCheck()) {
            this.toggle();
            return;
        }

        if (!InvUtils.hasItemInHotbar(FIREWORK_ROCKET)) {
            this.noFireworks();
            return;
        }
    }

    @EventHandler
    private void onUpdate(PlayerTickEvent.Pre event) {
        if (!strict.isEnabled()) {
            // Save old selected slot
            int currSlot = mc.player.getInventory().selectedSlot;

            // Select firework
            InvUtils.selectItemFromHotbar(FIREWORK_ROCKET);

            // Update selected slot server-sided
            mc.interactionManager.syncSelectedSlot();

            // Perform right-click
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

            // Switch back to old selected slot
            mc.player.getInventory().selectedSlot = currSlot;

            // Disable module
            this.toggle();
            return;
        }

        if (!isHolding) {
            // Save old selected slot
            lastSlot = mc.player.getInventory().selectedSlot;

            // Select firework
            InvUtils.selectItemFromHotbar(FIREWORK_ROCKET);
            isHolding = true;
            return;
        }

        // Perform right-click
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        // Switch back to old selected slot
        mc.player.getInventory().selectedSlot = lastSlot;
        lastSlot = -1;

        // Disable Module
        this.toggle();
    }

    private void noFireworks() {
        Template.notificationManager().addNotification(new Notification("Module Disabled", 1000, "You don't have any firewworks in your hotbar!", this.getFullName()));
        this.toggle();
    }
}
