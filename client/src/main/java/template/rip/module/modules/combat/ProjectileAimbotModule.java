package template.rip.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

public class ProjectileAimbotModule extends Module {

    public enum rotationModeEnum{Silent, Normal}
    public final ModeSetting<rotationModeEnum> rotationMode = new ModeSetting<>(this, rotationModeEnum.Silent, "Rotation Mode");

    public final MinMaxNumberSetting yawSpeed = new MinMaxNumberSetting(this, 0.5, 1d, 0.1, 10d, 0.1, "Horizontal Speeds");
    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 0.5, 1d, 0.1, 10d, 0.1, "Vertical Speeds");
    public final NumberSetting boxY = new NumberSetting(this, 0.4, 0, 1, 0.01, "Box Y");
    public final NumberSetting minPredict = new NumberSetting(this, 5, 0, 25, 1, "Prediction Min");
    public final NumberSetting maxPredict = new NumberSetting(this, 100, 0, 500, 1, "Prediction Max");
    private final BooleanSetting render = new BooleanSetting(this, true, "Render box");
    private final BooleanSetting autoSwitch = new BooleanSetting(this, true, "Auto Rod");
    public final MinMaxNumberSetting range = new MinMaxNumberSetting(this, 3.5d, 5d, 0.1, 12d, 0.1, "Rod Aim Range");
    public final MinMaxNumberSetting autoSwitchDelay = new MinMaxNumberSetting(this, 25, 75, 0, 200, 1, "Rod Switch Delay");
    public final MinMaxNumberSetting autoThrowDelay = new MinMaxNumberSetting(this, 25, 75, 0, 200, 1, "Rod Throw Delay");
    public final NumberSetting reducePreHit = new NumberSetting(this, 2d, 0d, 6d, 1d, "Rod Predict Ticks").setAdvanced();
    private final BooleanSetting relativeVelo = new BooleanSetting(this, true, "Relative Velocity").setAdvanced();
    private int lastSlot;
    private long switchTimer, throwTimer;
    private Pair<List<Vec3d>, Box> boxPair;
    private final Predicate<Item> autoRodPred = i -> i == Items.FISHING_ROD || i == Items.EGG || i == Items.SNOWBALL;
    private Rotation rot = new Rotation(0, 0);

    public ProjectileAimbotModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
        switchTimer = System.currentTimeMillis() + autoSwitchDelay.getRandomInt();
        throwTimer = System.currentTimeMillis() + autoThrowDelay.getRandomInt();
        boxPair = null;
    }

    @Override
    public void onDisable() {
        if (lastSlot != -1) {
            InvUtils.setInvSlot(lastSlot);
        }
    }

    @EventHandler
    private void onMouse(MouseUpdateEvent.Pre event) {
        LivingEntity e = PlayerUtils.findFirstLivingTargetOrNull(true);
        if (e == null || mc.player == null|| mc.currentScreen != null || boxPair == null || !mc.options.useKey.isPressed())
            return;

        ItemStack stack = mc.player.getMainHandStack();
        Box bee = boxPair.getRight();

        Vec3d center = bee.getCenter();
        float pitch = (float) calcPitch(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).subtract(center).horizontalLength(), MathHelper.lerp(boxY.value, bee.minY, bee.maxY) - mc.player.getEyeY(), stack);

        float yaw = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), center).fyaw();

        double yawStrength = yawSpeed.getRandomDouble() / 25.0;
        double pitchStrength = pitchSpeed.getRandomDouble() / 25.0;

        yaw = MathHelper.lerpAngleDegrees((float) yawStrength, rotationMode.is(rotationModeEnum.Normal) ? mc.player.getYaw() : Template.rotationManager().getClientRotation().fyaw(), yaw);
        pitch = MathHelper.lerpAngleDegrees((float) pitchStrength, rotationMode.is(rotationModeEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().fpitch(), pitch);

        rot = RotationUtils.correctSensitivity(new Rotation(yaw, pitch));

        if (!range.containsNumber(MathUtils.closestPosBoxDistance(e.getBoundingBox())) && autoSwitch.isEnabled() && InvUtils.usableStack(mc.player, Items.FISHING_ROD) != null)
            return;

        if (rotationMode.is(rotationModeEnum.Silent)) {
            Template.rotationManager().setRotation(rot);
        } else if (rotationMode.is(rotationModeEnum.Normal)) {
            Pair<Double, Double> pair = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(rot, RotationUtils.entityRotation(mc.player)));
            mc.player.changeLookDirection(pair.getLeft(), pair.getRight());
        }
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        LivingEntity le = PlayerUtils.findFirstLivingTargetOrNull(true);

        if (le == null || mc.player == null || boxPair == null || mc.currentScreen != null)
            return;

        Box bee = boxPair.getRight();

        if (!range.containsNumber(MathUtils.closestPosBoxDistance(le.getBoundingBox())) && autoSwitch.isEnabled() && InvUtils.usableStack(mc.player, Items.FISHING_ROD) != null)
            return;

        if (render.isEnabled())
            RenderUtils.Render3D.renderBox(bee, Color.WHITE, 100, event.context);
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        LivingEntity e = PlayerUtils.findFirstLivingTargetOrNull();

        if (e == null || mc.player == null || mc.currentScreen != null || !mc.player.canSee(e))
            return;

        boxPair = calcBox(e, mc.player.getMainHandStack());
        handleAuto(e, mc.player.getMainHandStack());
    }

    private void handleAuto(LivingEntity target, ItemStack stack) {
        if (!autoSwitch.isEnabled())
            return;

        if ((boxPair == null && autoRodPred.test(mc.player.getMainHandStack().getItem())) || !range.containsNumber(MathUtils.closestPosBoxDistance(target.getBoundingBox()))) {
            if (lastSlot != -1 && canSwitch()) {
                InvUtils.setInvSlot(lastSlot);
                lastSlot = -1;
            }
            return;
        }

        int slot = InvUtils.getItemSlot(autoRodPred);
        if (slot == -1)
            return;

        if (!autoRodPred.test(stack.getItem())) {
            if (canSwitch()) {
                if (lastSlot == -1) {
                    lastSlot = mc.player.getInventory().selectedSlot;
                }
                InvUtils.setInvSlot(slot);
            }
            return;
        }

        if (boxPair == null)
            return;

        int hurt = Math.max(0, target.hurtTime - reducePreHit.getIValue());
        if (hurt == 0 && Math.abs(Template.rotationManager().yaw() - rot.yaw()) <= 1 && Math.abs(Template.rotationManager().pitch() - rot.pitch()) <= 1) {
            if (canThrow()) {
                ActionResult interactionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    private boolean canSwitch() {
        if (autoSwitchDelay.getRandomInt() == 0)
            return true;
        if (switchTimer < System.currentTimeMillis()) {
            switchTimer = System.currentTimeMillis() + autoSwitchDelay.getRandomInt();
            return true;
        }
        return false;
    }

    private boolean canThrow() {
        if (autoThrowDelay.getRandomInt() == 0)
            return true;
        if (throwTimer < System.currentTimeMillis()) {
            throwTimer = System.currentTimeMillis() + autoThrowDelay.getRandomInt();
            return true;
        }
        return false;
    }

    // from tarasande
    // https://en.wikipedia.org/wiki/Projectile_motion#Angle_%CE%B8_required_to_hit_coordinate_(x,_y)
    private double calcPitch(double dist, Double deltaY, ItemStack is)  {
        double PROJECTILE_GRAVITY = 0.006;
        double velocity = is.getItem() instanceof BowItem ? BowItem.getPullProgress(mc.player.getItemUseTime()) : 1;
        double root = sqrt(velocity * velocity * velocity * velocity - PROJECTILE_GRAVITY * (PROJECTILE_GRAVITY * dist * dist + 2 * deltaY * velocity * velocity));
        // Use the negated one first, because it's usually better
        return -Math.toDegrees(atan2(velocity * velocity /*+/-*/ - root, PROJECTILE_GRAVITY * dist));
    }

    private Pair<List<Vec3d>, Box> calcBox(Entity entity, ItemStack is)  {
        if (is == null || is.isEmpty())
            return null;

        Item item = is.getItem();
        Pair<List<Vec3d>, HitResult> predicted = null;

        if (item instanceof BowItem) {
            predicted = ProjectileUtilities.predictBow(mc.world, mc.player, maxPredict.getIValue(), relativeVelo.isEnabled());
        } else if (item instanceof FishingRodItem) {
            predicted = ProjectileUtilities.predictRod(mc.world, mc.player, maxPredict.getIValue());;
        } else if (item instanceof CrossbowItem) {
            predicted = CrossbowItem.isCharged(is) ? ProjectileUtilities.predictCrossbowArrows(mc.world, mc.player, is, maxPredict.getIValue()).get(0) : null;
        } else if (item instanceof EggItem || item instanceof SnowballItem) {
            predicted = ProjectileUtilities.throwableItem(mc.world, mc.player, is, maxPredict.getIValue(), relativeVelo.isEnabled());
        }

        if (predicted == null) {
            return null;
        }

        if (!(entity instanceof PlayerEntity pe)) {
            return null;
        }

        Box one = PlayerUtils.predictState(Math.min(minPredict.getIValue() + predicted.getLeft().size(), maxPredict.getIValue()) - 1, pe).getLeft().getBoundingBox();
        Box two = PlayerUtils.predictState(Math.min(minPredict.getIValue() + predicted.getLeft().size(), maxPredict.getIValue()), pe).getLeft().getBoundingBox();

        Vec3d min = PlayerUtils.minBox(two);
        Vec3d mintwo = min.add(PlayerUtils.minBox(one).subtract(PlayerUtils.minBox(two)).multiply(mc.getRenderTickCounter().getTickDelta(false)));

        return new Pair<>(predicted.getLeft(),new Box(mintwo, mintwo.add(PlayerUtils.maxBox(pe.getBoundingBox()).subtract(PlayerUtils.minBox(pe.getBoundingBox())))));
    }
}
