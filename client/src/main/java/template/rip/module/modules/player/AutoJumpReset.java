package template.rip.module.modules.player;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InputUtil;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class AutoJumpReset extends Module {

    private final NumberSetting jumpResetChance = new NumberSetting(this, 100, 0, 100, 1, "Jump Reset Chance");
    private final NumberSetting ticks = new NumberSetting(this, 9, 7, 9, 1, "Hurt tick to jump at");
    private final BooleanSetting onlyPlayers = new BooleanSetting(this, true, "Only against target");
    private final BooleanSetting stopWithFire = new BooleanSetting(this, true, "Fire Check");

    public AutoJumpReset(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!event.check)
            return;

        if (mc.player == null) {
            return;
        }
        if (mc.currentScreen instanceof HandledScreen) {
            return;
        }
        if (mc.player.isOnFire() && stopWithFire.isEnabled()) {
            return;
        }
        if (mc.player.getAttacker() != PlayerUtils.findFirstTarget() && onlyPlayers.isEnabled()) {
            return;
        }
        if (mc.player.isInsideWaterOrBubbleColumn()) {
            return;
        }
        if (mc.player.isInsideWall()) {
            return;
        }
        if (mc.player.isTouchingWater()) {
            return;
        }
        // we only get forward velocity when sprinting
        if (!mc.player.isSprinting()) {
            return;
        }

        if (mc.player.hurtTime == ticks.getIValue() && MathUtils.getRandomInt(0, 100) <= jumpResetChance.getIValue()) {
            event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
        }
    }
}
