package template.rip.api.rotation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.util.MathUtils;

import java.util.ArrayList;
import java.util.Comparator;

import static java.lang.Math.*;
import static template.rip.Template.mc;

public class RotationUtils {

    public static Rotation getRotations(Vec3d from, Vec3d to) {
        Vec3d delta = to.subtract(from);
        return getRotations(delta);
    }

    public static Rotation getRotations(Vec3d delta) {
        return new Rotation(getYaw(delta), getPitch(delta.y, delta.horizontalLength()));
    }

    private static double getYaw(Double fromX, Double fromZ, Double toX, Double toZ) {
        return getYaw(toX - fromX, toZ - fromZ);
    }

    private static double getYaw(Double deltaX, Double deltaZ) {
        return getYaw(new Vec2f(deltaX.floatValue(), deltaZ.floatValue()));
    }

    public static double getYaw(Vec3d delta) {
        return Math.toDegrees(atan2(delta.z, delta.x)) - 90;
    }

    public static double getYaw(Vec2f delta) {
        return Math.toDegrees(atan2(delta.y, delta.x)) - 90;
    }

    public static double getPitch(Double deltaY, Double dist) {
        return -Math.toDegrees(atan2(deltaY, dist));
    }

    public static double getAngleToRotation(Rotation from, Rotation rotation) {
        double currentYaw = MathHelper.wrapDegrees(from.yaw());
        double currentPitch = MathHelper.wrapDegrees(from.pitch());

        double diffYaw = MathHelper.wrapDegrees(currentYaw - rotation.yaw());
        double diffPitch = MathHelper.wrapDegrees(currentPitch - rotation.pitch());

        return sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
    }

    public static Rotation getLimitedRotation(Rotation from, Rotation to, double yawLimit, double pitchLimit) {
        Rotation rotDiff = closestDelta(from, to);
        double yawDiff = rotDiff.yaw();
        double pitchDiff = rotDiff.pitch();
        yawDiff = MathUtils.coerceIn(yawDiff, -yawLimit, yawLimit);
        pitchDiff = MathUtils.coerceIn(pitchDiff, -pitchLimit, pitchLimit);
        return new Rotation(MathHelper.wrapDegrees(from.yaw() + yawDiff), MathUtils.coerceIn(from.pitch() + pitchDiff, -90, 90));
    }

    public static double getAngleToRotation(Rotation rotation) {
        return getAngleToRotation(Template.rotationManager().isEnabled() ? Template.rotationManager().getClientRotation() : RotationUtils.entityRotation(mc.player), rotation);
    }

    public static Vec3d getPlayerLookVec(float yaw, float pitch) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public static Vec3d getPlayerLookVec(PlayerEntity player) {
        return getPlayerLookVec(player.getYaw(), player.getPitch());
    }

    public static Rotation entityRotation(Entity e, boolean tickDelta) {
        if (!tickDelta)
            return new Rotation(e.getYaw(), e.getPitch());
        return new Rotation(e.getYaw(mc.getRenderTickCounter().getTickDelta(false)), e.getPitch(mc.getRenderTickCounter().getTickDelta(false)));
    }

    public static Rotation entityRotation(Entity e) {
        return entityRotation(e, true);
    }

    public static void setEntityRotation(Entity e, Rotation rot) {
        e.setYaw(rot.fyaw());
        e.setPitch(rot.fpitch());
    }

    public static Vec3d forwardVector(Rotation rotation) {
        double yawRad = Math.toRadians(rotation.yaw());
        double pitchRad = Math.toRadians(rotation.pitch());
        return new Vec3d(sin(-yawRad) * cos(pitchRad), -sin(pitchRad), cos(-yawRad) * cos(pitchRad));
    }

    private static double getGcd() {
        float sensitivity = mc.options.getMouseSensitivity().getValue().floatValue() * 0.6F + 0.2F;
        float sensitivityPow3 = sensitivity * sensitivity * sensitivity;
        float sensitivityPow3Mult8 = sensitivityPow3 * 8.0f;

        return mc.options.getPerspective().isFirstPerson() && mc.player.isUsingSpyglass() ? sensitivityPow3 : sensitivityPow3Mult8;
    }

    public static Rotation calculateNewRotation(Rotation prevRotation, Pair<Double, Double> cursorDeltas) {
        double gcd = getGcd();
        Rotation rotationChange = new Rotation((cursorDeltas.getLeft() * gcd) * 0.15F, (cursorDeltas.getRight() * gcd) * 0.15F);
        Rotation newRotation = new Rotation(prevRotation.yaw() + rotationChange.yaw(), prevRotation.pitch() + rotationChange.pitch());
        newRotation = newRotation.withfPitch((float) min(max(newRotation.pitch(), -90F), 90F));
        return newRotation;
    }

    public static ArrayList<Pair<Double, Double>> approximateCursorDeltas(Rotation deltaRotation) {
        float gcd = (float) (getGcd() * 0.15F);
        float targetX = (float) (-deltaRotation.yaw() / gcd);
        float targetY = (float) (-deltaRotation.pitch() / gcd);
        ArrayList<Pair<Double, Double>> array = new ArrayList<>();
        array.add(new Pair<>(floor(targetX), floor(targetY)));
        array.add(new Pair<>(ceil(targetX), floor(targetY)));
        array.add(new Pair<>(ceil(targetX), ceil(targetY)));
        array.add(new Pair<>(floor(targetX), ceil(targetY)));
        return array;
    }
    public static Rotation addNoise(Rotation rotation, double yawNoise, double pitchNoise) {
        return new Rotation(MathHelper.wrapDegrees(rotation.yaw() + MathUtils.getRandomDouble(-yawNoise, yawNoise)), MathUtils.coerceIn(rotation.pitch() + MathUtils.getRandomDouble(-pitchNoise, pitchNoise), -90, 90));
    }

    public static Pair<Double, Double> approximateRawCursorDeltas(Rotation deltaRotation) {
        float gcd = (float) (getGcd() * 0.15F);
        float targetX = (float) (-deltaRotation.yaw() / gcd);
        float targetY = (float) (-deltaRotation.pitch() / gcd);
        return new Pair<>((double) targetX, (double) targetY);
    }

    public static Rotation closestDelta(Rotation one, Rotation other) {
        return new Rotation(MathHelper.wrapDegrees(other.yaw() - one.yaw()), other.pitch() - one.pitch());
    }

    public static double fov(Rotation one, Rotation other) {
        return dist(closestDelta(one, other));
    }

    private static double dist(Rotation deltaRotation) {
        return sqrt(deltaRotation.yaw() * deltaRotation.yaw() + deltaRotation.pitch() * deltaRotation.pitch());
    }

    public static Rotation correctSensitivity(Rotation rotToFix, Rotation preference) {
        Rotation prevRotation = Template.rotationManager().isEnabled() ? Template.rotationManager().getClientRotation() : mc.player != null ? new Rotation(mc.player.getYaw(), mc.player.getPitch()) : new Rotation(0.0, 0.0);
        return correctSensitivity(rotToFix, preference, prevRotation);
    }

    public static Rotation correctSensitivity(Rotation rotToFix) {
        return correctSensitivity(rotToFix, null);
    }

    public static Rotation correctSensitivity(Rotation rotToFix, Rotation preference, Rotation prevRotation) {
        Rotation deltaRotation = closestDelta(rotToFix, prevRotation);
        ArrayList<Pair<Double, Double>> cursorDeltas = approximateCursorDeltas(deltaRotation);
        ArrayList<Rotation> newRotations = new ArrayList<>();
        cursorDeltas.forEach(rots -> newRotations.add(calculateNewRotation(prevRotation, rots)));

        if (preference != null) {
            // In case a module prefers a specific rotation, like the one intersecting with an object, we return that one.
            Rotation preferred = !newRotations.isEmpty() ? newRotations.get(0) : null;
            if (preferred != null)
                return preferred;
        }
        newRotations.sort(Comparator.comparing(rot -> fov(rotToFix, rot) * 10));
        return newRotations.get(0);
    }

    public static Rotation getSmoothRotation(Rotation from, Rotation to, double speed) {
        return new Rotation(
                MathHelper.lerpAngleDegrees((float) speed, from.fyaw(), to.fyaw()),
                MathHelper.lerpAngleDegrees((float) speed, from.fpitch(), to.fpitch())
        );
    }

    public static Rotation getDiff(Rotation rotation1, Rotation rotation2) {
        double yaw = MathHelper.wrapDegrees(Math.abs(rotation1.yaw() - rotation2.yaw()));
        double pitch = MathUtils.coerceIn(Math.abs(rotation1.pitch() - rotation2.pitch()), -90, 90);

        return new Rotation(yaw, pitch);
    }

    public static double getTotalDiff(Rotation rotation1, Rotation rotation2) {
        Rotation diff = getDiff(rotation1, rotation2);

        return diff.yaw() + diff.pitch();
    }
}
