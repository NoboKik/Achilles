package template.rip.module.modules.blatant;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class SpinAuraModule extends Module {

    public enum modeEnum{Normal, Silent}
    public final ModeSetting<modeEnum> rotMode = new ModeSetting<>(this, modeEnum.Silent, "Rotation Mode");
    public final NumberSetting reach = new NumberSetting(this, 3, 0, 6, 0.1, "Reach");
    public final NumberSetting ticksForSpin = new NumberSetting(this, 20, 0, 100, 1, "Ticks for Full Rot");
    public final NumberSetting spinsPerAttackCooldown = new NumberSetting(this, 1, 1, 5, 1, "Spins Per Cooldown");
    public final NumberSetting randomness = new NumberSetting(this, 5, 0.0, 50, 0.1, "Random cooldown %");
    public final NumberSetting spinPitch = new NumberSetting(this, 90, -90, 90, 0.1, "Spin Pitch");
    public final NumberSetting randomPitch = new NumberSetting(this, 1, 0.0, 5d, 0.1, "Random pitch");
    public final BooleanSetting jumpOnSprint = new BooleanSetting(this, false, "Jump when Sprint");

    public SpinAuraModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!nullCheck())
            return;

        if (mc.player.isSprinting() && jumpOnSprint.isEnabled()) {
            event.input.jump();
        }
    }

    @EventHandler
    private void onMouse(MouseUpdateEvent.Pre event) {
        if (!nullCheck())
            return;

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null) {
            return;
        }

        Vec3d point = MathUtils.closestPointToBox(target.getBoundingBox());

        float coolDown;
        if (mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(point) <= reach.value) {
            float scaledCooldownProgress = MathHelper.clamp(((float) mc.player.lastAttackedTicks + mc.getRenderTickCounter().getTickDelta(false) + (MathUtils.getRandomFloat(-randomness.getFValue(), randomness.getFValue()) / 100f))
                / mc.player.getAttackCooldownProgressPerTick(), 0.0f, 1f) * spinsPerAttackCooldown.getFValue();
            coolDown = (float) (scaledCooldownProgress - Math.floor(scaledCooldownProgress));
        } else {
            coolDown = MathHelper.clamp(((mc.player.age + mc.getRenderTickCounter().getTickDelta(false) + (MathUtils.getRandomFloat(-randomness.getFValue(), randomness.getFValue()) / 100f)) % ticksForSpin.getFValue()) / ticksForSpin.getFValue(), 0.0f, 1.0f);
        }
        Rotation targetRot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), point);
        float pitch;
        if (coolDown == 1 || coolDown == 0) {
            pitch = targetRot.fpitch();
        } else {
            pitch = spinPitch.getFValue();
        }
        Rotation finalRot = RotationUtils.correctSensitivity(new Rotation(MathHelper.wrapDegrees(targetRot.fyaw() + MathHelper.lerp(coolDown, 0, 360)), MathUtils.coerceIn(pitch + MathUtils.getRandomDouble(-randomPitch.value, randomPitch.value), -90, 90)));

        switch (rotMode.getMode()) {
            case Normal -> RotationUtils.setEntityRotation(mc.player, finalRot);
            case Silent -> Template.rotationManager().setRotation(finalRot);
        }
    }
}