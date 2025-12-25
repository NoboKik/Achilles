package template.rip.module.modules.combat;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.MouseDeltaEvent;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.player.CombatBotModule;
import template.rip.module.setting.settings.*;

public class AimAssistModule extends Module {

    public enum modeHEnum{Normal, Assist_Only, Off}
    public final ModeSetting<modeHEnum> modeH = new ModeSetting<>(this, modeHEnum.Normal, "Horizontal");
    public final MinMaxNumberSetting yawSpeed = new MinMaxNumberSetting(this, 4, 8, 0.1, 10d, 0.1, "Horizontal Speeds");
    public enum modeVEnum{Normal, Assist_Only, Off}
    public final ModeSetting<modeVEnum> modeV = new ModeSetting<>(this, modeVEnum.Normal, "Vertical");

    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 4, 8, 0.1, 10d, 0.1, "Vertical Speeds");
    public final BooleanSetting holdLeftClick = new BooleanSetting(this, Description.of("Requires you to hold left click to use."), false, "Hold Left Click").setAdvanced();
    public final BooleanSetting minimalPitch = new BooleanSetting(this, Description.of("Only changes pitch if you're not facing the target's box"), true, "Minimal Pitch movement").setAdvanced();
    public final DividerSetting rotationSettings = new DividerSetting(this, true, Description.of("Additional advanced settings regarding to rotation and targetting"), "Rotation settings (adv)");
    public enum modeEnum{Normal, Silent}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Change player's rotation\nSilent: Change client's fake rotation"), modeEnum.Normal, "Rotation Mode");

    public enum targetModeEnum{Optimal, Center}
    public final ModeSetting<targetModeEnum> targetMode = new ModeSetting<>(this, Description.of("Reachable UnOptimal: Aim between the closest and farthest point to the box\nOptimal: Aim at the closest point to the box\nCenter: Aim at the center of the box"), targetModeEnum.Optimal, "Target Pos Mode");

    public final NumberSetting randomRotAmount = new NumberSetting(this, Description.of("How much to shake your camera by"), 0.5, 0.0, 5d, 0.1, "Camera shake amount").setAdvanced();
    public final NumberSetting boxScale = new NumberSetting(this, Description.of("How much to shrink the target's box when considering rotations"), 0.05, 0.0, 1d, 0.01, "Target box shrink").setAdvanced();
    public final NumberSetting prediction = new NumberSetting(this, Description.of("How much to predict the target's future position by"), 1, 0.0, 3d, 0.01, "Prediction amount").setAdvanced();
    public enum speedModeEnum{Slow, Normal/*, Instant*/}
    public final ModeSetting<speedModeEnum> speedMode = new ModeSetting<>(this, speedModeEnum.Slow, "Aim Speed Mode");
    public final BooleanSetting maxDistance = new BooleanSetting(this, true, "Max Aim Distance");
    public final NumberSetting distance = new NumberSetting(this, 8d, 3d, 16d, 0.1d, "Aim Distance");
    public final NumberSetting fov = new NumberSetting(this, Description.of("Only attacks targets within this FOV"), 180d, 1d, 360d, 1d, "FOV");
    public final BooleanSetting onlyWeapon = new BooleanSetting(this, true, "Only Weapon");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();
    public Box lastBox = null;
    public Vec3d lastTargetPos;
    private double mouseX = 0;
    private double mouseY = 0;

    public AimAssistModule(Category category, Description description, String name) {
        super(category, description, name);
        rotationSettings.addSetting(mode, targetMode, randomRotAmount, boxScale, prediction, speedMode);
    }

    @Override
    public String getSuffix() {
        return " "+mode.getDisplayName();
    }

    private boolean isHoldingWeapon() {
        if (mc.player == null)
            return false;

        ItemStack heldItem = mc.player.getMainHandStack();

        return heldItem.getItem() instanceof SwordItem || heldItem.getItem() instanceof AxeItem;
    }

    @Override
    public void onEnable() {
        mouseX = 0;
        mouseY = 0;
        lastBox = null;
        lastTargetPos = null;
    }

    @EventHandler
    public void mouseDelta(MouseDeltaEvent event) {
        mouseX = MathUtils.coerceIn(mouseX + event.deltaX, -10, 10);
        mouseY = MathUtils.coerceIn(mouseY + event.deltaY, -10, 10);
    }

    @EventHandler
    public void onMouseUpdate(MouseUpdateEvent.Post event) {
        if ((mc.currentScreen == null || !disableInScreens.isEnabled()) &&
                (!holdLeftClick.isEnabled() || KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))) {
            LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();

            if (target == null || !nullCheck() || (maxDistance.isEnabled() && MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox()) > distance.value))
                return;

            if (!isHoldingWeapon() && onlyWeapon.isEnabled())
                return;

            if (mc.crosshairTarget instanceof EntityHitResult && ((EntityHitResult) mc.crosshairTarget).getEntity() == target && mc.player.input.getMovementInput().length() == 0.0 && PlayerUtils.lastPosVec(target).equals(target.getPos())) {
                // prevent rotating back
                if (mode.is(modeEnum.Silent))
                    Template.rotationManager().setRotation(Template.rotationManager().getClientRotation());
                return;
            }

            {
                if (AutoHealModule.stopAA()) {
                    lastTargetPos = null;
                    return;
                }

                lastTargetPos = getTargetPos(
                    PlayerUtils
                        .renderBox(target)
                        .contract(boxScale.value)
                        .offset(target.getPos()
                            .subtract(PlayerUtils
                                .lastPosVec(target))
                            .multiply(prediction.value))
                );
            }


            Vec3d targetPos = lastTargetPos;

            if (targetPos == null) {
                return;
            }

            Rotation targetRot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), targetPos);
            if (RotationUtils.getAngleToRotation(targetRot) > fov.getValue() / 2)
                return;

            double yawStrength = switch(speedMode.getMode()) {
//                case Instant -> 1;
                case Normal -> yawSpeed.getRandomDouble() / 75.0;
                case Slow -> yawSpeed.getRandomDouble() / 500.0;
            };

            double pitchStrength = switch(speedMode.getMode()) {
//                case Instant -> 1;
                case Normal -> pitchSpeed.getRandomDouble() / 75.0;
                case Slow -> pitchSpeed.getRandomDouble() / 500.0;
            };

            double yaw = MathHelper.lerpAngleDegrees((float) yawStrength, mode.is(modeEnum.Normal) ? mc.player.getYaw() : Template.rotationManager().getClientRotation().fyaw(), targetRot.fyaw());
            double pitch = MathHelper.lerpAngleDegrees((float) pitchStrength, mode.is(modeEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().fpitch(), targetRot.fpitch());

            boolean cancelH = false;
            boolean cancelV = false;

            HitResult hr = PlayerUtils.getHitResult(mc.player, e -> e == target, targetRot.fyaw(), mode.is(modeEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().fpitch());
            if (hr instanceof EntityHitResult && minimalPitch.isEnabled())
                cancelV = true;

            targetRot = new Rotation(yaw, pitch);
            Pair<Double, Double> pair = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(targetRot, mode.is(modeEnum.Normal) ? RotationUtils.entityRotation(mc.player) : Template.rotationManager().getClientRotation()));

            if (modeH.is(modeHEnum.Assist_Only)) {
                if (mouseX >= 0 && pair.getLeft() <= 0)
                    cancelH = true;
                if (mouseX <= 0 && pair.getLeft() >= 0)
                    cancelH = true;
            }
            if (modeH.is(modeHEnum.Off)) {
                cancelH = true;
            }

            if (modeV.is(modeVEnum.Assist_Only)) {
                if (mouseY >= 0 && pair.getRight() <= 0)
                    cancelV = true;
                if (mouseY <= 0 && pair.getRight() >= 0)
                    cancelV = true;
            }
            if (modeV.is(modeVEnum.Off)) {
                cancelV = true;
            }

            if (cancelH)
                targetRot = new Rotation(mode.is(modeEnum.Normal) ? mc.player.getYaw() : Template.rotationManager().getClientRotation().fyaw(), targetRot.pitch());
            if (cancelV)
                targetRot = new Rotation(targetRot.yaw(), mode.is(modeEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().pitch());

            if (!cancelV || !cancelH) {
                double r = randomRotAmount.value / 25.0;
                targetRot = new Rotation(targetRot.yaw() + MathUtils.getRandomDouble(-r, r), MathUtils.coerceIn(targetRot.pitch() + MathUtils.getRandomDouble(-r, r), -90, 90));
            }

            targetRot = RotationUtils.correctSensitivity(targetRot);

            if (mode.is(modeEnum.Normal)) {
                pair = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(targetRot, RotationUtils.entityRotation(mc.player)));
                mc.player.changeLookDirection(pair.getLeft(), pair.getRight());
            } else {
                Template.rotationManager().setRotation(targetRot);
            }
        }
    }
    public Vec3d getTargetPos(Box box) {
        lastBox = box;

        ClientPlayerEntity pl = mc.player;

        Vec3d start = pl == null ? lastTargetPos : pl.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        Vec3d close = MathUtils.closestPointToBox(start, box);

        CombatBotModule cbm = Template.moduleManager.getModule(CombatBotModule.class);
        if (cbm != null && cbm.isEnabled() && cbm.runAndEat.isEnabled()) {
            AutoEatModule aem = Template.moduleManager.getModule(AutoEatModule.class);
            if (aem != null && aem.eating && pl != null && !pl.isUsingItem() && !cbm.isMinDistance()) {
                return MathUtils.Vec3dWithY(close, start.getY()).add(RotationUtils.forwardVector(RotationUtils.getRotations(MathUtils.Vec3dWithY(close, start.getY()), start)).multiply(MathUtils.Vec3dWithY(close, start.getY()).distanceTo(start) + 3));
            }
        }

        if (box.contains(start))
            return new Vec3d(box.minX + (box.maxX - box.minX) / 2.0, close.y, box.minZ + (box.maxZ - box.minZ) / 2.0);

        return switch (targetMode.getMode()) {
            case Optimal -> close;
            case Center -> box.getCenter();
        };
    }
}