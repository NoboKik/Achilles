package template.rip.module.modules.blatant;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import template.rip.Template;
import template.rip.api.blockesp.WorldRenderContext;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.player.SprintModule;
import template.rip.module.setting.settings.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

@SuppressWarnings({"DataFlowIssue", "unused"})//NullPointerException
public class ScaffoldRecodeModule extends Module {

    public final BooleanSetting place = new BooleanSetting(this, true, "Place");
    public enum rotateEnum {Normal, Telly}
    public final ModeSetting<rotateEnum> rotateMode = new ModeSetting<>(this, Description.of("Normal: Same old scaffold that never (without omni-sprint) sprints\nTelly: A much faster scaffold mode that takes advantage of the sprint jump speed boost"), rotateEnum.Normal, "Rotate Mode");
    public enum sameYModeEnum {Off, Normal, Hypixel, Hypixel_2}
    public final ModeSetting<sameYModeEnum> sameY = new ModeSetting<>(this, sameYModeEnum.Normal, "Same Y");
    public final MinMaxNumberSetting offTicks = (MinMaxNumberSetting) new MinMaxNumberSetting(this, 4, 11, 0, 11, 1, "OffGround Ticks")
            .setAdvanced()
            .addConditionMode(sameY, sameYModeEnum.Hypixel);
    public final NumberSetting maxPlaces = new NumberSetting(this, 1, 1, 4, 1, "Max Places").setAdvanced();
    public final BooleanSetting selectBlock = new BooleanSetting(this, true, "Select Blocks");
    public final BooleanSetting softRaycast = new BooleanSetting(this, Description.of("Sets your pitch to \"Soft Raycast Pitch\" and raycasts based off of yaw only"), false, "Soft Raycast");
    public final AnyNumberSetting softRaycastPitch = new AnyNumberSetting(this, 85.2, true, "Soft Raycast Pitch");
    public final NumberSetting oldPitchLenience = new NumberSetting(this, Description.of("Determines how far the player must be from the block before calculating a new pitch\nHigh values will cause the scaffold to fail more often\nLow values will change the pitch more often (and be more blatant)"), 2.4, 0, 5, 0.1, "Last Pitch Leniency").setAdvanced();
    public final BooleanSetting oldGrimNoRots = new BooleanSetting(this, false, "OldGrim No Rots").setAdvanced();
    public final DividerSetting sneaking = new DividerSetting(this, false, "Sneaking");
    public final BooleanSetting onlyPitchWhenSneak = new BooleanSetting(this, false, "Sneak when rotate pitch");
    public final MinMaxNumberSetting sneakEveryX = new MinMaxNumberSetting(this, 0, 0, 0, 10, 1, "Sneak Every X Blocks");
    public final MinMaxNumberSetting sneakTicks = new MinMaxNumberSetting(this, 2, 3, 0, 20, 1, "UnSneak delay ticks");
    public final DividerSetting visualsDiv = new DividerSetting(this, false, "Visuals");
    public final BooleanSetting allVisuals = new BooleanSetting(this, true, "All Visuals Toggle");
    public final ColorSetting targetVec = new ColorSetting(this, new JColor(Color.DARK_GRAY, 200), true, "Target Vector");
    public final ColorSetting targetBlock = new ColorSetting(this, new JColor(Color.WHITE, 100), true, "Target Block");
    public final ColorSetting targetSide = new ColorSetting(this, new JColor(Color.RED, 50), true, "Target Side");
    public final BooleanSetting debugVisuals = new BooleanSetting(this, Description.of("You don't want these"), false, "Debug Visuals");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();

    public final RegistrySetting<Item> bannedBlocks = new RegistrySetting<Item>(Arrays.asList(Items.ICE, Items.BLUE_ICE, Items.PACKED_ICE, Items.LADDER, Items.COBWEB), this, Registries.ITEM, RegistrySetting.scaffoldPredicate, "Banned blocks");

    private int sneakLeft, blockCount, offGroundTicks, startY, lastLandY;
    private boolean didTopPlace;
    private BlockPos topBlock;
    private double lastNonNullYaw, lastPitch;
    private BlockPos lastBlock = BlockPos.ORIGIN;
    private Vec3d lastTarget = Vec3d.ZERO;
    private HitResultObject moveDirHitResultObject, bruteHitResultObject, lastYawHitResultObject;
    private HitResult scaffoldHitResult;
    private ArrayList<HitResultObject> pitchAttemptHitResultObjects = new ArrayList<>();
    private final ArrayList<Float> pitches = new ArrayList<>();
    private static final ArrayList<BlockPos> targetBlocks = new ArrayList<>();

    public ScaffoldRecodeModule(Category category, Description description, String name) {
        super(category, description, name);
        visualsDiv.addSetting(allVisuals, debugVisuals, targetVec, targetBlock, targetSide);
        sneaking.addSetting(onlyPitchWhenSneak, sneakEveryX, sneakTicks);
        softRaycastPitch.addConditionBoolean(softRaycast, true);

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -4; y <= 1; y++) {
                    targetBlocks.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        sneakLeft = 0;
        blockCount = 0;
        lastNonNullYaw = 0;
        lastPitch = 86;
        lastTarget = Vec3d.ZERO;
        lastBlock = BlockPos.ORIGIN;
        offGroundTicks = 0;
        didTopPlace = false;
        if (mc.player != null) {
            startY = mc.player.getBlockPos().getY() - 1;
            if (selectBlock.isEnabled()) {
                int blockSlot = InvUtils.getItemSlot(BlockUtils.placeableBlocksNew());
                if (blockSlot != -1) {
                    InvUtils.setInvSlot(blockSlot);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        double amount = 0;
        for (float pitch : pitches) {
            amount += pitch;
        }
//        mc.inGameHud.getChatHud().addMessage(Text.of("Avg: " + MathUtils.round(amount / pitches.size(), 2)));
        pitches.clear();
    }

    private Vec3d getEye() {
        return mc.player.getEyePos();
    }

    private Vec3d getEyeWith(double y) {
        Vec3d eye = getEye();
        return new Vec3d(eye.x, y, eye.z);
    }

    private BlockPos getWorst() {
        return getWorst(bp -> true);
    }

    private BlockPos getWorst(Predicate<BlockPos> blockPredicate) {
        BlockPos bPos = mc.player.getBlockPos().down();

        int dist = Short.MAX_VALUE;
        BlockPos worst = null;
        for (BlockPos targetBlock : targetBlocks) {
            BlockPos position = bPos.add(targetBlock);
            if (!mc.world.getBlockState(position).isAir() && bPos.getManhattanDistance(position) < dist && blockPredicate.test(position)) {
                worst = position;
                dist = bPos.getManhattanDistance(position);
            }
        }
        return worst;
    }

    /*private ArrayList<Pair<BlockPos, Direction>> getWorstBlocks() {
        BlockPos player = mc.player.getBlockPos().down();
        BlockPos bp, last = null;
        ArrayList<Pair<BlockPos, Direction>> bps = new ArrayList<>();
        while ((bp = getWorst(bps.stream().map(p -> p.first().offset(p.second())).collect(Collectors.toList()))) != null
                && (last == null || last.getManhattanDistance(bp) != 0)) {
            last = bp;
            Direction dir = PlayerUtils.getClosestFace(bp, player, false);
            if (dir == null) {
                continue;
            }
            bps.add(Pair.of(bp, dir));
        }
        return bps;
    }*/

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!nullCheck() || !allVisuals.isEnabled()) {
            return;
        }
        /*ArrayList<Pair<BlockPos, Direction>> bps = getWorstBlocks();
        for (Pair<BlockPos, Direction> pr : bps) {
            ArrayList<Vec3d> corners = PlayerUtils.cornersOfFace(pr.value(), pr.key());
            int closest = -1;
            double dist = Short.MAX_VALUE;
            for (int i = 0; i < corners.size(); i++) {
                Vec3d corn = corners.get(i);
                double d = mc.player.getPos().distanceTo(corn);
                if (d < dist) {
                    closest = i;
                    dist = d;
                }
            }
            for (int i = 0; i < corners.size(); i++) {
                Vec3d corn = corners.get(i);
                RenderUtils.Render3D.renderBox(
                        new Box(corn.subtract(0.1, 0.1, 0.1), corn.add(0.1, 0.1, 0.1)),
                        closest == i ? Color.GREEN : Color.WHITE,
                        100,
                        event.context
                );
            }
        }*/

        JColor vecCol = targetVec.getColor();
        RenderUtils.Render3D.renderBox(lastTarget.subtract(0.1, 0.1, 0.1), lastTarget.add(0.1, 0.1, 0.1), vecCol, vecCol.getAlpha(), event.context);

        if (debugVisuals.isEnabled()) {
            if (moveDirHitResultObject != null) {
                moveDirHitResultObject.render(moveDirHitResultObject.success ? Color.ORANGE : Color.YELLOW, 200, event.context);
            }
            if (bruteHitResultObject != null) {
                bruteHitResultObject.render(bruteHitResultObject.success ? Color.CYAN : Color.BLUE, 150, event.context);
            }
            if (lastYawHitResultObject != null) {
                lastYawHitResultObject.render(lastYawHitResultObject.success ? Color.MAGENTA : Color.PINK, 100, event.context);
            }
            for (HitResultObject phro : pitchAttemptHitResultObjects) {
                phro.render(phro.success ? Color.WHITE : Color.BLACK, phro.success ? 150 : 50, event.context);
            }
            if (scaffoldHitResult instanceof BlockHitResult bhr) {
                renderLine(getEye(), bhr.getPos(), canPlace(lastBlock, bhr) ? Color.DARK_GRAY : Color.BLUE, 150, event.context);
            }
            Double moveDir = getExactMoveDirection();
            if (moveDir != null) {
                RenderUtils.Render3D.renderLineTo(
                    getEye(),
                    getEye().add(RotationUtils.forwardVector(new Rotation(moveDir, 0)).multiply(3)),
                    canPlace(lastBlock, PlayerUtils.getHitResult(mc.player, (float) lastNonNullYaw, (float) lastPitch)) ? Color.GREEN : Color.BLUE, 1f, event.context);
            }
        }


        if (lastBlock != null) {
            Direction dir = PlayerUtils.getClosestFace(lastBlock, mc.player.getBlockPos().down(), false);
            JColor blockCol = targetBlock.getColor();
            JColor sideCol = targetSide.getColor();
            renderBox(
                new Box(MathUtils.vec3iToVec3d(lastBlock), MathUtils.vec3iToVec3d(lastBlock).add(1, 1, 1)),
                blockCol,
                blockCol.getAlpha(),
                dir,
                sideCol,
                sideCol.getAlpha(),
                event.context
            );
        }
    }

    private Vec3d applyTargetXZ(BlockPos pos, Vec3d vec3d, Direction placeSide) {
        Box box = BlockUtils.blockBox(pos);
        if (box == null) {
            return vec3d;
        }
        Vec3d closest = MathUtils.closestPointToBox(getEye(), box);
        closest = switch (placeSide.getAxis()) {
            case X -> new Vec3d(pos.getX() + (placeSide.getOffsetX() == 1 ? placeSide.getOffsetX() : 0), closest.getY(), closest.getZ());
            case Z -> new Vec3d(closest.getX(), closest.getY(), pos.getZ() + (placeSide.getOffsetZ() == 1 ? placeSide.getOffsetZ() : 0));
            default -> closest;
        };
        return new Vec3d(closest.x, vec3d.y, closest.z);
    }

    private Double getExactMoveDirection() {
        Vec2f vec = PlayerUtils.computeMovementInput();
        if (vec.x == 0 && vec.y == 0) {
            return null;
        }
        vec = new Vec2f(vec.y, vec.x);
        return RotationUtils.getYaw(vec) + (mc.player.getYaw());
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!event.check) {
            return;
        }

        if (sneakLeft > 0) {
            InputUtil.setSneaking(event.input.playerInput, true);
            sneakLeft--;
        }

        if (rotateMode.is(rotateEnum.Telly) && event.input.getMovementInput().length() != 0) {
            event.input.jump();
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }
        if (!mc.player.isOnGround()) offGroundTicks++;
        else {
            offGroundTicks = 0;
            didTopPlace = false;
            lastLandY = mc.player.getBlockPos().getY() - 1;
        }
        BlockPos pos = sameY.is(sameYModeEnum.Hypixel_2) ? getWorst(bp -> bp.getY() == startY) : getWorst();

        lastBlock = pos;

        rotate(pos);

        scaffoldHitResult = mc.crosshairTarget = PlayerUtils.getHitResult(mc.player, (float) lastNonNullYaw, (float) lastPitch);
        int places = maxPlaces.getIValue();
        while (places > 0) {
            if (place.isEnabled() && canPlace(pos, scaffoldHitResult) && (mc.currentScreen == null || !disableInScreens.isEnabled())) {
                place(pos);
            }
            places--;
        }
    }

    private void setCorrectedRotation() {
        Rotation rot = new Rotation(lastNonNullYaw, softRaycast.isEnabled() ? softRaycastPitch.getValue() : lastPitch);
        Rotation corrected = RotationUtils.calculateNewRotation(Template.rotationManager().rotation(), RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(rot, Template.rotationManager().rotation())));
        Template.rotationManager().setRotation(corrected);
    }

    private void idle(boolean doTellyYaw) {
        Double gemd = getExactMoveDirection();
        if (gemd != null) {
            lastNonNullYaw = MathHelper.wrapDegrees(gemd + (rotateMode.is(rotateEnum.Telly) && doTellyYaw && !mc.player.isSneaking() && mc.player.getHungerManager().getFoodLevel() > 6 ? 0 : 180));
        }

        if (!oldGrimNoRots.isEnabled()) {
            setCorrectedRotation();
        }
    }

    private void rotate(BlockPos worst) {
        boolean doTellyYaw = rotateMode.is(rotateEnum.Telly)
            && mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0, mc.player.getVelocity().getY() / 4, 0))).blocksMovement()
            || mc.player.getVelocity().getY() > 0 || (mc.player.isOnGround() && mc.player.input.playerInput.jump());

        if (worst == null) {
            idle(doTellyYaw);
            return;
        }

        Direction closestFaceDirection = PlayerUtils.getClosestFace(worst, mc.player.getBlockPos().down(), false);

        if (closestFaceDirection == null) {
            idle(doTellyYaw);
            return;
        }

        if (doTellyYaw) {
            Template.moduleManager.getOptModule(SprintModule.class).ifPresent(m -> m.setEnabled(true));
            idle(true);
            return;
        }

        Vec3d target = MathUtils
                .vec3iToVec3d(worst)
                .offset(closestFaceDirection, closestFaceDirection.getDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0)
                .add(closestFaceDirection.getAxis() != Direction.Axis.X ? 0.5 : 0.1, closestFaceDirection != Direction.UP ? 0.5 : 0, closestFaceDirection.getAxis() != Direction.Axis.Z ? 0.5 : 0);

        target = applyTargetXZ(worst, target, closestFaceDirection);

        Pair<Float, Vec3d> yawAndNewTarget = getYaw(target, worst);
        lastNonNullYaw = yawAndNewTarget.left();
        target = yawAndNewTarget.right();

        Pair<Double, Float> pairAndNewY = getPitch(target, worst, (float) lastNonNullYaw);
        lastPitch = pairAndNewY.right();
        pitches.add((float) lastPitch);
        target = MathUtils.Vec3dWithY(target, pairAndNewY.left());

        lastTarget = target;

        if (!oldGrimNoRots.isEnabled()) {
            setCorrectedRotation();
        }
    }

    private Pair<Float, Vec3d> getYaw(Vec3d target, BlockPos blockTarget) {
        Double moveDir = getExactMoveDirection();
        Vec3d start = getEyeWith(target.y);
        if (moveDir != null) {
            Vec3d moveDirStart = start.add(RotationUtils.forwardVector(new Rotation(moveDir, 0)).multiply(3));
            float moveYaw = RotationUtils.getRotations(moveDirStart, target).fyaw();
            moveDirHitResultObject = new HitResultObject(start, moveYaw, 0, blockTarget);
            if (moveDirHitResultObject.success) {
//                mc.inGameHud.getChatHud().addMessage(Text.of("MoveDir CanPlace " + mc.player.age));
                return Pair.of(moveYaw, moveDirHitResultObject.hitPos);
            }
        }
        lastYawHitResultObject = new HitResultObject(start, (float) lastNonNullYaw, 0, blockTarget);
        if (lastYawHitResultObject.success) {
//            mc.inGameHud.getChatHud().addMessage(Text.of("LastYaw CanPlace " + mc.player.age));
            return Pair.of((float) lastNonNullYaw, lastYawHitResultObject.hitPos);
        }
        float bruteYaw = RotationUtils.getRotations(start, target).fyaw();
        bruteHitResultObject = new HitResultObject(start, bruteYaw, 0, blockTarget);
        if (bruteHitResultObject.success) {
//            mc.inGameHud.getChatHud().addMessage(Text.of("BruteForce CanPlace " + mc.player.age));
            return Pair.of(bruteYaw, bruteHitResultObject.hitPos);
        }
//        mc.inGameHud.getChatHud().addMessage(Text.of("None CanPlace " + mc.player.age));
        return Pair.of((float) lastNonNullYaw, target);
    }

    private Pair<Double, Float> getPitch(Vec3d xz, BlockPos blockTarget, float yaw) {
        Vec3d eye = getEye();
        Vec3d bottom = MathUtils.Vec3dWithY(xz, blockTarget.getY());
        Vec3d top = MathUtils.Vec3dWithY(xz, blockTarget.getY() + 1);

        HitResult hr = PlayerUtils.getHitResult(mc.player, yaw, (float) lastPitch, eye);

        if (canPlace(blockTarget, hr) || keepLastPitch(blockTarget, hr, xz, eye, yaw)) {
            return Pair.of(hr.getPos().y, (float) lastPitch);
        }

        if (onlyPitchWhenSneak.isEnabled() && !mc.player.isSneaking()) {
            InputUtil.setSneaking(mc.player.input.playerInput, true);
            if (sneakLeft == 0) {
                sneakLeft += sneakTicks.getRandomInt();
            }
        }

        ArrayList<HitResultObject> pitchHROs = new ArrayList<>();
        double blockY = blockTarget.getY();
        for (double lerp = 0; lerp <= 0.95; lerp += 0.05) {
            Vec3d calcVec = MathUtils.smoothVec3d(bottom, top, (float) lerp);
            Vec3d delta = calcVec.subtract(eye);
            float calcPitch = (float) RotationUtils.getPitch(delta.y, delta.horizontalLength());
            blockY = calcVec.getY() - blockTarget.getY();

            HitResultObject currentPitch = new HitResultObject(eye, yaw, calcPitch, blockTarget);
            pitchHROs.add(currentPitch);

            if (currentPitch.success) {
//                mc.inGameHud.getChatHud().addMessage(Text.of("Good Pitch: " + MathUtils.round(calcPitch, 1) + " at lerp: " + lerp + " y: " + blockY));
                pitchAttemptHitResultObjects = pitchHROs;
                return Pair.of(blockTarget.getY() + blockY, calcPitch);
            }
        }
        pitchAttemptHitResultObjects = pitchHROs;

        return Pair.of(blockTarget.getY() + blockY, (float) lastPitch);
    }

    private void place(BlockPos worst) {
        if (selectBlock.isEnabled()) {
            Predicate<Item> pred = BlockUtils.placeableBlocksNew();
            int hotbar = InvUtils.getItemSlot(pred);
            if (hotbar != -1 && InvUtils.usableStack(mc.player, pred) == null) {
                InvUtils.setInvSlot(hotbar);
            }
        }

        if (oldGrimNoRots.isEnabled()) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), (float) lastNonNullYaw, (float) lastPitch, mc.player.isOnGround(), mc.player.horizontalCollision));
        }

        Pair<ActionResult, Hand> pr = placeAtHit(mc.player, (BlockHitResult) scaffoldHitResult);
        if (pr.first().isAccepted()) {
            blockCount++;
            if (sneakEveryX.getMaxValue() != 0 && blockCount >= sneakEveryX.getRandomInt()) {
                blockCount = 0;
                sneakLeft += sneakTicks.getRandomInt();
            }
        }

        if (oldGrimNoRots.isEnabled()) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        }

        if (((BlockHitResult) scaffoldHitResult).getSide() == Direction.UP) {
            didTopPlace = true;
            topBlock = ((BlockHitResult) scaffoldHitResult).getBlockPos().offset(((BlockHitResult) scaffoldHitResult).getSide());
        }
    }

    private Pair<ActionResult, Hand> placeAtHit(ClientPlayerEntity clientPlayerEntity, BlockHitResult blockHitResult) {
        for (Hand hand : Hand.values()) {
            ItemStack itemStack = clientPlayerEntity.getStackInHand(hand);
            if (!itemStack.isItemEnabled(mc.world.getEnabledFeatures())) {
                return Pair.of(ActionResult.PASS, hand);
            }
            int itemCount = itemStack.getCount();
            ActionResult actionResult2 = mc.interactionManager.interactBlock(clientPlayerEntity, hand, blockHitResult);
            if (actionResult2.isAccepted() && PlayerUtils.shouldSwingHand(actionResult2)) {
                clientPlayerEntity.swingHand(hand);
                if (!itemStack.isEmpty() && (itemStack.getCount() != itemCount || mc.interactionManager.hasCreativeInventory())) {
                    mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                }
                return Pair.of(actionResult2, hand);
            }
            if (actionResult2 == ActionResult.FAIL) {
                return Pair.of(actionResult2, hand);
            }
        }
        return Pair.of(ActionResult.PASS, null);
    }

    private boolean keepLastPitch(BlockPos blockTarget, HitResult hitResult, Vec3d xz, Vec3d eye, float yaw) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() == HitResult.Type.MISS) {
            return false;
        }

        if (blockHitResult.getBlockPos().getManhattanDistance(blockTarget) != 0) {
            return false;
        }

        if (hitResult.getPos().y - blockTarget.getY() != 1) {
            return false;
        }


        Vec3d bottom = MathUtils.Vec3dWithY(xz, blockTarget.getY());
        Vec3d top = MathUtils.Vec3dWithY(xz, blockTarget.getY() + 1);

        Vec3d topDelta = top.subtract(eye);
        Vec3d bottomDelta = bottom.subtract(eye);

        double tbDiff = RotationUtils.getPitch(bottomDelta.y, bottomDelta.horizontalLength()) - RotationUtils.getPitch(topDelta.y, topDelta.horizontalLength());

//            mc.inGameHud.getChatHud().addMessage(Text.of("allowed lastPitch with diff: " + MathUtils.round(tbDiff, 3)));
        return tbDiff < oldPitchLenience.getValue();

//        mc.inGameHud.getChatHud().addMessage(Text.of("diff top & bottom: " + MathUtils.round(tbDiff, 3)));
    }

    private boolean sameY() {
        if (sameY.is(sameYModeEnum.Normal)) return true;
        return rotateMode.is(rotateEnum.Telly) && !(mc.options.jumpKey.isPressed() && mc.player.input.getMovementInput().length() == 0);
    }

    private boolean shouldUpDir() {
        return offGroundTicks >= offTicks.getIMinValue() && offGroundTicks <= offTicks.getIMaxValue();
    }

    private boolean canPlaceHere(BlockHitResult bhr) {
        BlockPos pos = bhr.getBlockPos().offset(bhr.getSide());
        int y = pos.getY();
        if (sameY.is(sameYModeEnum.Hypixel)) {
            boolean off = shouldUpDir();
            if (off && !didTopPlace) {
                return y == startY + 1;
            } else return y == startY;
        }
        if (sameY.is(sameYModeEnum.Hypixel_2)) {
            if (bhr.getBlockPos() == topBlock || didTopPlace && y == startY + 1)
                return false;
            if (lastLandY == startY) {
                if (mc.player.fallDistance > 0.1) return y == startY + 1;
                else return y != startY + 1;
            } else if (lastLandY == startY + 1) {
                if (mc.player.fallDistance > 1.12) return y == startY + 1;
                else return y != startY + 1;
            } else return false;
        }
        return true;
    }

    private boolean canPlace(BlockPos at, HitResult hitResult) {
        if (hitResult instanceof BlockHitResult result && hitResult.getType() == HitResult.Type.BLOCK && at != null) {
            if (at.getManhattanDistance(result.getBlockPos()) == 0) {
                if (!canPlaceHere(result)) return false;
                
                if (result.getSide() == Direction.UP && sameY()) {
                    return false;
                }
                //else if (bhr.getSide() != Direction.UP && upDir()) {
                //    return false;
                //}
                Hand hand = InvUtils.usableHand(mc.player, BlockUtils.placeableBlocksNew());
                Item i;
                if (hand != null && (i = mc.player.getStackInHand(hand).getItem()) instanceof BlockItem) {
                    return ((BlockItem) i).canPlace(new ItemPlacementContext(mc.player, hand, mc.player.getMainHandStack(), result), mc.world.getBlockState(result.getBlockPos()));
                } else { //so we still rotate
                    return true;
                }
            }
        }
        return false;
    }

    public class HitResultObject {

        public final Vec3d eyePos, hitPos;
        public final float yaw, pitch;
        public final boolean success;
        public final HitResult hitResult;
        public final HitResult.Type hitType;

        public HitResultObject(Vec3d eyePos, float yaw, float pitch, BlockPos currentWorst) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.hitResult = PlayerUtils.getHitResult(mc.player, yaw, pitch, (this.eyePos = eyePos));
            if (this.hitResult instanceof BlockHitResult) {
                this.hitPos = this.hitResult.getPos();
            } else {
                this.hitPos = null;
            }
            this.hitType = this.hitResult.getType();
            this.success = canPlace(currentWorst, this.hitResult);
        }

        public void render(Color color, int alpha, WorldRenderContext context) {
            renderLine(eyePos, hitPos, color, alpha, context);
        }
    }

    private void renderLine(Vec3d start, Vec3d to, Color color, int alpha, WorldRenderContext context) {
        if (start == null || to == null) {
            return;
        }
        RenderUtils.Render3D.renderLineTo(start, to, color.getRed(), color.getGreen(), color.getBlue(), alpha, 1f, context);
    }

    private void renderBox(Box box, Color color, int alpha, Direction sideWithDiffColor, Color sideColor, int sideAlpha, WorldRenderContext context) {
        // copy and pasted from my optimal aim mod, I skidded from myself
        Camera cam = context.camera();

        MatrixStack matstack = new MatrixStack();

        matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
        matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));

        Vec3d targetpos = new Vec3d(box.minX, box.minY, box.minZ).subtract(cam.getPos());
        matstack.translate(targetpos.x, targetpos.y, targetpos.z);

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        Matrix4f posMat = matstack.peek().getPositionMatrix();
        Tessellator tessy = Tessellator.getInstance();
        BufferBuilder buffy = tessy.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int[] aColors = new int[] {red, green, blue, alpha};

        //up
        if (Direction.UP.equals(sideWithDiffColor)) {
            aColors = new int[] {sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), sideAlpha};
        }
        buffy.vertex(posMat, x1, y2, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y2, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y2, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y2, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        if (Direction.UP.equals(sideWithDiffColor)) {
            aColors = new int[] {red, green, blue, alpha};
        }

        //north
        if (Direction.NORTH.equals(sideWithDiffColor)) {
            aColors = new int[] {sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), sideAlpha};
        }
        buffy.vertex(posMat, x1, y1, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y2, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y2, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y1, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        if (Direction.NORTH.equals(sideWithDiffColor)) {
            aColors = new int[] {red, green, blue, alpha};
        }

        //west
        if (Direction.WEST.equals(sideWithDiffColor)) {
            aColors = new int[] {sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), sideAlpha};
        }
        buffy.vertex(posMat, x1, y1, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y2, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y2, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y1, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        if (Direction.WEST.equals(sideWithDiffColor)) {
            aColors = new int[] {red, green, blue, alpha};
        }

        //down
        if (Direction.DOWN.equals(sideWithDiffColor)) {
            aColors = new int[] {sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), sideAlpha};
        }
        buffy.vertex(posMat, x1, y1, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y1, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y1, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y1, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        if (Direction.DOWN.equals(sideWithDiffColor)) {
            aColors = new int[] {red, green, blue, alpha};
        }

        //east
        if (Direction.EAST.equals(sideWithDiffColor)) {
            aColors = new int[] {sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), sideAlpha};
        }
        buffy.vertex(posMat, x2, y1, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y2, z1).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y2, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y1, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        if (Direction.EAST.equals(sideWithDiffColor)) {
            aColors = new int[] {red, green, blue, alpha};
        }

        //south
        if (Direction.SOUTH.equals(sideWithDiffColor)) {
            aColors = new int[] {sideColor.getRed(), sideColor.getGreen(), sideColor.getBlue(), sideAlpha};
        }
        buffy.vertex(posMat, x1, y1, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x1, y2, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y2, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        buffy.vertex(posMat, x2, y1, z2).color(aColors[0], aColors[1], aColors[2], aColors[3]);
        if (Direction.SOUTH.equals(sideWithDiffColor)) {
            aColors = new int[] {red, green, blue, alpha};
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        BufferRenderer.drawWithGlobalProgram(buffy.end());

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}