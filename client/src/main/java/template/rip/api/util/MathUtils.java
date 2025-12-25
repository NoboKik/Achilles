package template.rip.api.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static template.rip.Template.mc;

public class MathUtils {

    public static double coerceIn(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int getRandomInt(int from, int to) {
        if (from >= to) return from;
        return ThreadLocalRandom.current().nextInt(from, to + 1);
    }

    public static double roundNumber(double number, double roundTo) {
        return (Math.round(number * roundTo) / roundTo);
    }

    public static double getRandomDouble(double from, double to) {
        if (from >= to) return from;
        return ThreadLocalRandom.current().nextDouble(from, to);
    }

    public static float getRandomFloat(float from, float to) {
        if (from >= to) return from;
        return (float) ThreadLocalRandom.current().nextDouble(from, to);
    }

    public static boolean passedTime(double time, long timer) {
        return timer + time <= System.currentTimeMillis();
    }

    public static double getAverage(double int1, double int2) {
        return (int1 + int2) / 2;
    }

    public static boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static String[] split(String str) {
        int amount = (int) Math.ceil(str.length() / 3.0);
        String[] split = new String[amount];
        for (int i = 0; i < amount; i++) {
            split[i] = str.substring(i == 0 ? 0 : i * 3, Math.min(i * 3 + 3, str.length()));
        }
        return split;
    }

    public static Vec3d getBestAimPoint(Box box) {
        Vec3d start = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        if (box.minX < start.x && start.x < box.maxX && box.minZ < start.z && start.z < box.maxZ)
            return new Vec3d(box.minX + (box.maxX - box.minX) / 2.0, coerceIn(start.y, box.minY, box.maxY),box.minZ + (box.maxZ - box.minZ) / 2.0);

        return closestPointToBox(start, box);
    }

    public static Box boxAtPos(Box box, Vec3d pos) {
        box = box.offset(new Vec3d(-box.minX, -box.minY, -box.minZ));
        box = box.offset(pos);
        box = box.offset(-box.getLengthX() / 2.0, 0, -box.getLengthZ() / 2.0);
        return box;
    }

    public static double closestPosBoxDistance(Vec3d start, Box box) {
        return start.distanceTo(closestPointToBox(start, box));
    }

    public static double closestPosBoxDistance(Box box) {
        return mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(closestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box));
    }

    public static Vec3d closestPointToBox(Vec3d start, Box box) {
        return new Vec3d(coerceIn(start.x, box.minX, box.maxX), coerceIn(start.y, box.minY, box.maxY), coerceIn(start.z, box.minZ, box.maxZ));
    }

    public static Vec3d closestPointToBox(Box box) {
        return closestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box);
    }

    public static Vec3d fitVecInBox(Vec3d vec, Box box) {
        return new Vec3d(coerceIn(vec.x, box.minX, box.maxX), coerceIn(vec.y, box.minY, box.maxY), coerceIn(vec.z, box.minZ, box.maxZ));
    }

    public static Vec3d farthestPointToBox(Vec3d start, Box box) {
        Vec3d closest = closestPointToBox(start, box);
        Vec3d farthest = new Vec3d(box.minX + (box.maxX - closest.x), box.minY + (box.maxY - closest.y), box.minZ + (box.maxZ - closest.z));
        return new Vec3d(coerceIn(farthest.x, box.minX, box.maxX), coerceIn(farthest.y, box.minY, box.maxY), coerceIn(farthest.z, box.minZ, box.maxZ));
    }

    public static Vec3d smoothVec3d(Vec3d start, Vec3d end, float tickDelta) {
        double x = MathHelper.lerp(tickDelta, start.x, end.x);
        double y = MathHelper.lerp(tickDelta, start.y, end.y);
        double z = MathHelper.lerp(tickDelta, start.z, end.z);
        return new Vec3d(x, y, z);
    }

    @SuppressWarnings("all")
    public static void div0() {
        int i = 1 / 0;
    }

    // credit: https://www.baeldung.com/java-levenshtein-distance
    public static int calculate(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), dp[i - 1][j] + 1, dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    @Nullable
    public static Vec3d bestAimPointToBlock(Vec3d from, BlockPos toBlockPos, Direction direction) {
        if (mc.world == null || mc.world.getBlockState(toBlockPos).isAir()) {
            return null;
        }

        Vec3i dir2dVec = direction.getVector();
        Vec3d dir3dVec = new Vec3d(dir2dVec.getX(), dir2dVec.getY(), dir2dVec.getZ());

        BlockPos nextBlockPos = toBlockPos.add(dir2dVec);
        if (mc.world.getBlockState(nextBlockPos).isAir()) {
            Vec3d block3dVec = toBlockPos.toCenterPos().add(dir3dVec.multiply(0.5d));
            int invertedDirOffset = direction.getDirection().offset() * -1;
            Vec3d invertedDir3dVec = dir3dVec.add(invertedDirOffset, invertedDirOffset, invertedDirOffset).negate();

            Vec3d startBox = block3dVec.add(invertedDir3dVec.multiply(0.5d));
            Vec3d endBox = block3dVec.add(invertedDir3dVec.multiply(-0.5d));
            Box box = new Box(startBox, endBox);

            Vec3d bestAimPoint = getBestAimPoint(box);

            Rotation rotation = RotationUtils.getRotations(from, bestAimPoint);

            HitResult hitResult = PlayerUtils.getHitResult(mc.player, e -> true, rotation.fyaw(), rotation.fpitch());

            if (hitResult instanceof BlockHitResult && ((BlockHitResult) hitResult).getBlockPos().equals(toBlockPos) && ((BlockHitResult) hitResult).getSide().equals(direction)) {
                return bestAimPoint;
            } else {
                return null;    // Unable to aim at the block's direction
            }
        } else {
            return null;    // Unable to place block on this direction because there's already one
        }
    }

    @Nullable
    public static Direction directionOfVelocity() {
        Vec3d velocity = mc.player.getVelocity();

        double maxVel = velocity.getX();
        Direction.Axis axis = Direction.Axis.X;
        Direction.AxisDirection axisDirection = maxVel > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;

        if (Math.abs(velocity.getY()) > Math.abs(maxVel)) {
            maxVel = velocity.getY();
            axis = Direction.Axis.Y;
            axisDirection = maxVel > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        }

        if (Math.abs(velocity.getZ()) > Math.abs(maxVel)) {
            maxVel = velocity.getZ();
            axis = Direction.Axis.Z;
            axisDirection = maxVel > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        }

        if (maxVel == 0) {
            return null;
        } else {
            return Direction.from(axis, axisDirection);
        }
    }

    @Nullable
    public static Direction getPlaceDirection(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            BlockPos nextBlock = blockPos.offset(direction);
            BlockState blockState = mc.world.getBlockState(nextBlock);

            if (blockState.isAir() || !blockState.getFluidState().isEmpty()) continue;

            return direction;
        }

        return null;
    }

    @Nullable
    public static Direction closestPlaceDirection(BlockPos blockPos) {
        double minDistance = Double.MAX_VALUE;
        Direction closestDir = null;

        for (Direction direction : Direction.values()) {
            BlockPos nextBlock = blockPos.offset(direction);
            BlockState blockState = mc.world.getBlockState(nextBlock);

            if (blockState.isAir()) continue;

            double distance = mc.player.getPos().squaredDistanceTo(nextBlock.toCenterPos());

            if (minDistance > distance) {
                minDistance = distance;
                closestDir = direction;
            }
        }

        return closestDir;
    }

    public static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    public static double round(double value, double to) {
        return Math.round(value / to) * to;
    }

    public static double floor(double value, double to) {
        return Math.floor(value / to) * to;
    }

    public static double ceil(double value, double to) {
        return Math.ceil(value / to) * to;
    }

    public static Vec3d Vec3dWithY(Vec3d vec, double y) {
        return new Vec3d(vec.x, y, vec.z);
    }

    public static Vec3d Vec3fToVec3d(Vector3f vec) {
        return new Vec3d(vec.x, vec.y, vec.z);
    }

    public static boolean withinBox(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public static boolean withinBox(float x1, float y1, float x2, float y2, float mouseX, float mouseY) {
        return mouseX >= x1 && mouseX <= x2&& mouseY >= y1 && mouseY <= y2;
    }

    public static boolean withinBoundsRange(float var, float var2, float range) {
        return var >= var2 - range && var <= var2 + range;
    }

    public static Vec3d vec3iToVec3d(Vec3i vec) {
        return new Vec3d(vec.getX(), vec.getY(), vec.getZ());
    }

    public static String getFormattedCaller() {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        // 0 = getStackTrace
        // 1 = getFormattedCaller
        // 2 = callerOfgetFormattedCaller
        // 3 = callerOfCallerOfgetFormattedCaller
        return String.format("%s.%s:%s", ste.getClassName().substring(ste.getClassName().lastIndexOf(".") + 1), ste.getMethodName(), ste.getLineNumber());
    }
}
