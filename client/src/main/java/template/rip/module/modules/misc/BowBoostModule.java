package template.rip.module.modules.misc;

import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import template.rip.Template;
import template.rip.api.event.events.DamageEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InputUtil;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class BowBoostModule extends Module {

    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 4, 6, 0.1, 10d, 0.1, "Pitch Speed");

    public enum modeEnum{Normal, Silent}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Silent, "Rotation Mode");

    public final NumberSetting randomRotAmount = new NumberSetting(this, 0.33, 0.0, 5d, 0.1, "Camera shake amount").setAdvanced();
    public final NumberSetting pitch = new NumberSetting(this, 60, 0, 90, 1, "Target pitch");

    private boolean press = false;
    private boolean shot = false;
    private boolean toDisable = false;
    private Long timer = System.currentTimeMillis();
    private int lastSlot = -1;
    private boolean rotate = false;

    public BowBoostModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        press = false;
        shot = false;
        toDisable = false;
        timer = System.currentTimeMillis();
        rotate = false;

        if (nullCheck()) {
            lastSlot = mc.player.getInventory().selectedSlot;

            if (InvUtils.hasItemInHotbar(Items.BOW)) {
                InvUtils.setInvSlot(InvUtils.getItemSlot(Items.BOW));
                super.enable();
            }
        }
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        if (!nullCheck())
            return;
        if (event.damaged == mc.player && shot) {
            toDisable = true;
            timer = System.currentTimeMillis() + 500;
            setEnabled(false);
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!nullCheck())
            return;

        event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
        event.input.movementForward = 1f;

        if (rotate) {
            double pitchStrength = pitchSpeed.getRandomDouble() / 10.0;
            double pitchRot = MathHelper.lerpAngleDegrees((float) pitchStrength, mode.is(modeEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().fpitch(), -pitch.getFValue());

            Rotation targetRot = new Rotation(mc.player.getYaw(), pitchRot);
            double r = randomRotAmount.value / 10.0;
            targetRot = new Rotation(targetRot.yaw() + MathUtils.getRandomDouble(-r, r), MathUtils.coerceIn(targetRot.pitch() + MathUtils.getRandomDouble(-r, r), -90, 90));

            targetRot = RotationUtils.correctSensitivity(targetRot);

            if (mode.is(modeEnum.Normal)) {
                Pair<Double, Double> pair = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(targetRot, RotationUtils.entityRotation(mc.player)));
                mc.player.changeLookDirection(pair.getLeft(), pair.getRight());
            } else {
                Template.rotationManager().setRotation(targetRot);
            }
        }

        if (toDisable && timer < System.currentTimeMillis())
            setEnabled(false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!nullCheck())
            return;

        rotate = false;

        if (shot) {
            if (SlotUtils.isHotbar(lastSlot)) {
                InvUtils.setInvSlot(lastSlot);
                lastSlot = -1;
            }
            return;
        }

        if (!InvUtils.canUseItem(mc.player, Items.BOW))
            return;

        boolean check = mc.player.getVelocity().y > 0.1 && !mc.player.isOnGround() && mc.player.getItemUseTime() <= 2;
        if (!press && check) {
            press = true;
        }
        mc.options.useKey.setPressed(press && check);
        if (press && !check) {
            shot = true;
        }
        rotate = true;
    }
}
