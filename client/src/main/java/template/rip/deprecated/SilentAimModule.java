package template.rip.deprecated;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.FastTickEvent;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.object.FakePlayerEntity;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;

public class SilentAimModule extends Module {
    public final BooleanSetting onlyWeapon = new BooleanSetting(this, true, "Only Weapon");
    public final NumberSetting maxDistance = new NumberSetting(this, 4d, 3d, 5d, 0.1d, "Max Distance");
    public final BooleanSetting lookAtNearest = new BooleanSetting(this, false, "Look At Nearest box's Corner");
    public final NumberSetting minYawSpeed = new NumberSetting(this, 2d, 1d, 10d, 0.1d, "Min Horizontal Speed");
    public final NumberSetting maxYawSpeed = new NumberSetting(this, 4d, 1d, 10d, 0.1d, "Max Horizontal Speed");
    public final NumberSetting minPitchSpeed = new NumberSetting(this, 1d, 0.5d, 10d, 0.1d, "Min Vertical Speed");
    public final NumberSetting maxPitchSpeed = new NumberSetting(this, 2d, 0.5d, 10d, 0.1d, "Max Vertical Speed");
    public final NumberSetting fov = new NumberSetting(this, 90d, 1d, 360d, 1d, "FOV");
    public final BooleanSetting fovCircle = new BooleanSetting(this, true, "FOV Circle");

    public SilentAimModule() {
        super(Category.COMBAT, Description.of("Automatically aims at players"), "SilentAim");
    }

    private LivingEntity targetPlayer;

    @Override
    public void onEnable() {
        targetPlayer = null;
    }

    @EventHandler
    public void onFastTick(FastTickEvent event) {
        if (mc.currentScreen == null && mc.player != null) {
            if (Template.rotationManager().isEnabled()) mc.player.setHeadYaw((float) Template.rotationManager().getRealRotation().yaw());

            Item mainHandItem = mc.player.getMainHandStack().getItem();

            if (!(mainHandItem instanceof AxeItem || mainHandItem instanceof SwordItem) && onlyWeapon.isEnabled()) {
                return;
            }

            targetPlayer = PlayerUtils.findNearestEntity(mc.player, maxDistance.getFValue(), true);

            if (targetPlayer == null || targetPlayer.isInvisible() || targetPlayer instanceof FakePlayerEntity) {
                return;
            }

            Vec3d targetPlayerPos = targetPlayer.getPos();

            if (lookAtNearest.isEnabled()) {
                double halfHitboxSize = (targetPlayer.getBoundingBox().minX / 2) - 0.01d;

                double offsetX = (mc.player.getX() - targetPlayer.getX()) > 0 ? halfHitboxSize : -halfHitboxSize;
                double offsetZ = (mc.player.getZ() - targetPlayer.getZ()) > 0 ? halfHitboxSize : -halfHitboxSize;

                targetPlayerPos = targetPlayerPos.add(offsetX, 0, offsetZ);
            }

            Rotation targetRot = RotationUtils.getRotations(mc.player.getEyePos(), targetPlayerPos);

            if (RotationUtils.getAngleToRotation(targetRot) > fov.getValue() / 2) {
                return;
            }

            float randomiseYaw = (float) MathUtils.getRandomDouble(0, 0.2);
            float randomisePitch = (float) MathUtils.getRandomDouble(0, 0.2);

            float yawStrength = (float) (MathUtils.getRandomDouble(minYawSpeed.getValue(), maxYawSpeed.getValue()) / 50);
            float pitchStrength = (float) (MathUtils.getRandomDouble(minPitchSpeed.getValue(), maxPitchSpeed.getValue()) / 50);

            Rotation clientRotation = Template.rotationManager().getClientRotation();

            float yaw = MathHelper.lerpAngleDegrees(yawStrength, (float) clientRotation.yaw(), (float) targetRot.yaw()) + randomiseYaw;
            float pitch = MathHelper.lerpAngleDegrees(pitchStrength, (float) clientRotation.pitch(), (float) targetRot.pitch()) + randomisePitch;

            Template.rotationManager().setRotation(yaw, pitch);
        }
    }
    @EventHandler
    private void onHudRender(HudRenderEvent event) {
        if (fovCircle.isEnabled()) {
            RenderUtils.Render2D.renderCircle(event.context.getMatrices(), new Color(255, 255, 255, 80), mc.getWindow().getScaledWidth() / 2, mc.getWindow().getScaledHeight() / 2, fov.getFValue() * 2, 60);
        }
    }
}
