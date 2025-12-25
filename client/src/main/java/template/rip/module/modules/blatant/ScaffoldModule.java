package template.rip.module.modules.blatant;

import com.google.common.collect.Streams;
import net.minecraft.block.AirBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static template.rip.module.modules.blatant.AntiVoidModule.isClutching;

public class ScaffoldModule extends Module {

    public enum modeEnum{Normal, Static, Static_Yaw, Fruitberries, Telly};
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Normal, "Scaffold Mode");
    public final MinMaxNumberSetting yawSpeed = new MinMaxNumberSetting(this, 10, 10, 0.1, 10d, 0.1, "Yaw Speeds");
    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 10, 10, 0.1, 10d, 0.1, "Pitch Speeds");
    public final MinMaxNumberSetting placeYRange = new MinMaxNumberSetting(this, 0.5, 0.8, 0, 1, 0.01, "Place Y range");
    public final MinMaxNumberSetting placeDelay = new MinMaxNumberSetting(this, 1, 1, 0, 5, 1, "Place delay");
    public final BooleanSetting sneak = new BooleanSetting(this, false, "Sneak");
    public final BooleanSetting stopMove = new BooleanSetting(this, false, "Stop move");
    public final BooleanSetting godBridge = new BooleanSetting(this, false, "GodBridge Jump");
    public final BooleanSetting godBridgeSneak = new BooleanSetting(this, false, "GodBridge Sneak");
    public final BooleanSetting breezily = new BooleanSetting(this, false, "Breezily");
    public final BooleanSetting safeWalk = new BooleanSetting(this, false, "SafeWalk");
    public final BooleanSetting autoJump = new BooleanSetting(this, false, "AutoJump");
    public final BooleanSetting visuals = new BooleanSetting(this, true, "Visuals");
    public final NumberSetting gbPredict = new NumberSetting(this, 0.55, 0.1, 3, 0.1, "GodBridge predict").setAdvanced();
    public final NumberSetting fpPredict = new NumberSetting(this, 0.55, 0.1, 2, 0.1, "Fruitberries predict").setAdvanced();
    public final NumberSetting fbTicks = new NumberSetting(this, 2, 0, 5, 1, "Fruitberries stopS ticks").setAdvanced();
    public final MinMaxNumberSetting unSneak = new MinMaxNumberSetting(this, 75, 100, 0, 500, 1, "UnSneak delays");
    public final BooleanSetting selectBlock = new BooleanSetting(this, true, "Select block");
    public final BooleanSetting guaranteedPlace = new BooleanSetting(this, false, "Guaranteed Place");
    public final BooleanSetting sameY = new BooleanSetting(this, false, "SameY");
    public final BooleanSetting hypixelSameY = new BooleanSetting(this, false, "Hypixel SameY");
    public final BooleanSetting constantSameY = new BooleanSetting(this, false, "Constant SameY");
    public final MinMaxNumberSetting offTicks = new MinMaxNumberSetting(this, 4, 11, 0, 11, 1, "OffGround Ticks").setAdvanced();
    public final RegistrySetting<Item> bannedBlocks = new RegistrySetting<Item>(Arrays.asList(Items.ICE, Items.BLUE_ICE, Items.PACKED_ICE, Items.LADDER, Items.COBWEB), this, Registries.ITEM, RegistrySetting.scaffoldPredicate, "Banned blocks");
    public final BooleanSetting sprint = new BooleanSetting(this, false, "Sprint");
    public final BooleanSetting hypixelSprint = new BooleanSetting(this, false, "Hypixel Sprint");
    public final BooleanSetting hypixelSprintJump = new BooleanSetting(this, false, "Hypixel SprintJump");
    public final BooleanSetting hypixelSpoof = new BooleanSetting(this, false, "Hypixel Spoof");
    public final BooleanSetting skipRaycast = new BooleanSetting(this, false, "Skip RayCast");
    public final BooleanSetting playerRots = new BooleanSetting(this, false, "Player rotations");
    public final BooleanSetting yawOffset = new BooleanSetting(this, false, "Yaw Offset");
    public final BooleanSetting onlyWhenPlace = new BooleanSetting(this, false, "Only Rotate When Place");
    public final BooleanSetting grimNoRots = new BooleanSetting(this, false, "Grim No Rots");
    //public enum speedModeEnum{None, MineBlaze, Custom, Hypixel};
    //public final ModeSetting<speedModeEnum> speedMode = new ModeSetting<>("Speed Mode", this, speedModeEnum.None, speedModeEnum.values());

    //private final NumberSetting speed = new NumberSetting("Speed", this, 0.2, -0.2, 0.2, 0.001);
    public final BooleanSetting tower = new BooleanSetting(this, false, "Tower");
    public final NumberSetting towerPush = new NumberSetting(this, 0.0, -1, 1, 0.01, "Tower Push");
    public final BooleanSetting hypixelTower = new BooleanSetting(this, false, "Hypixel Tower");
    public final NumberSetting towerMotion = new NumberSetting(this, 335, 300, 500, 1, "Tower Motion").setAdvanced();
    public final NumberSetting towerRandom = new NumberSetting(this, 0, 0, 100, 1, "Tower Randomness").setAdvanced();
    public final NumberSetting towerTick = new NumberSetting(this, 5, 3, 8, 1, "Tower Tick").setAdvanced();
    public final BooleanSetting towerSpoof = new BooleanSetting(this, false, "Tower Spoof");
    public final BooleanSetting towerStrafe = new BooleanSetting(this, false, "Tower Strafe");
    //private final BooleanSetting towerJumping = new BooleanSetting("Tower Jumping", this, false);
    //private final NumberSetting towerHeight = new NumberSetting("Tower Height", this, 0.4, 0.1, 1, 0.01);
    //private final BooleanSetting positioning = new BooleanSetting("Positioning for tower", this, false);

    public final NumberSetting edgeDistance = new NumberSetting(this, 0.2, 0.1, 0.5, 0.01, "Edge Distance");
    public final NumberSetting breezilyDist = new NumberSetting(this, 0.1, 0.0, 0.3, 0.01, "Breezily Distance");
    private Vec3d bpos = null;
    private boolean place = false;
    private Box b = null;
    private Box lastB = null;
    private double cx;
    private double cy;
    private double cz;
    private Float yaw = null;
    private Float pitch = null;
    private Float lastYaw = null;
    private Color color = Color.WHITE;
    private int timer = 0;
    private boolean diagGB = false;
    private boolean onground = false;
    private  long sneakTimer = 0;
    private int delay = placeDelay.getRandomInt();
    private boolean hasMoved = false;
    private boolean hasHeldSpace = false;
    public static boolean allowSprint = true;
    private final ArrayList<BlockPos> targetBlocks = new ArrayList<>();
    private ArrayList<BlockPos> blockBl = new ArrayList<>();
    private boolean right;
    private BlockPos cachedBest;
    private boolean firstPlace = false;
    private boolean inPosition = false;
    private boolean stopS = false;
    private int ticks = 0;
    private int places = 0;
    private int offGroundTicks = 0;
    private int lastLandY = 0;
    private boolean donePositioning = false;
    private Vec3d targetWalk = null;
    private Vec3d vector = null;
    private int startY = 0;
    private int lastGroundY = 0;
    private boolean wasOnGroundSinceSpace = true;

    public static long lastJump = System.currentTimeMillis();
    public static long lastEnable = System.currentTimeMillis();
    public static long lastDisable = System.currentTimeMillis();
    private HitResult hit = null;
    private boolean shouldSpoofGround = true;

    public ScaffoldModule(Category category, Description description, String name) {
        super(category, description, name);

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -3; y <= 1; y++) {
                    targetBlocks.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        bpos = null;
        b = null;
        lastB = null;
        yaw = null;
        pitch = null;
        timer = 0;
        diagGB = false;
        onground = false;
        delay = placeDelay.getRandomInt();
        right = false;
        sneakTimer = 0;
        firstPlace = false;
        inPosition = false;
        stopS = false;
        targetWalk = null;
        vector = null;
        hasMoved = false;
        lastYaw = null;
        hit = null;
        places = 0;
        donePositioning = false;
        if (mc.player != null) startY = mc.player.getBlockPos().getY();
        if (mc.player != null) lastGroundY = mc.player.getBlockPos().getY();
        cachedBest = null;
        blockBl = new ArrayList<>();
        lastEnable = System.currentTimeMillis();
        hasHeldSpace = false;
    }

    @Override
    public void onDisable() {
        lastDisable = System.currentTimeMillis();
        allowSprint = true;
    }

    @EventHandler
    private void updateCrosshair(UpdateCrosshairEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            hit = PlayerUtils.getHitResult(mc.player, e -> false, Template.rotationManager().yaw(), Template.rotationManager().pitch());
        }
    }

    @EventHandler
    public void onFastTick(FastTickEvent event) {
        cachedBest = getBest();
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        BlockPos bee = cachedBest;
        if (bee != null && mc.world.isAir(mc.player.getBlockPos().down())) b = BlockUtils.blockBox(bee);
        else b = null;

        if (b != null)
            lastB = b;

        if (lastB == null)
            return;

        if (visuals.isEnabled())
            RenderUtils.Render3D.renderBox(lastB, Color.WHITE, 50, event.context);


        if (b != null) bpos = MathUtils.Vec3dWithY(MathUtils.closestPointToBox(getEye(), lastB), bee.getY() + 0.5);
        else bpos = null;

        if (visuals.isEnabled()) {
            Vec3d vector = new Vec3d(cx, cy, cz);
            RenderUtils.Render3D.renderBox(new Box(vector.subtract(0.05, 0.05, 0.05), vector.add(0.05, 0.05, 0.05)), color, 150, event.context);
        }
    }

    @EventHandler
    private void onWalkForward(WalkingForwardEvent event) {
        if (!allowSprint) event.forward = false;
        if (sprint.isEnabled() && PlayerUtils.isPressingMoveInput(false))
            event.forward = true;
    }

    @EventHandler
    private void onSafeWalk(SafeWalkEvent event) {
        if (safeWalk.isEnabled() || (hypixelTower.isEnabled() && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)))
            event.safe = true;
    }

    private Vec3d getEye() {
        return mc.player.getEyePos();
    }

    private BlockPos getBest() {
        return getBest(false);
    }

    private BlockPos getBest(boolean placeInsideSelf) {
        ClientPlayerEntity p = mc.player;
        if (p == null) {
            return null;
        }
        BlockPos bPos = p.getBlockPos().down();
        ClientWorld w = mc.world;
        if (w == null) {
            return null;
        }

        double dist = Double.MAX_VALUE;
        BlockPos best = null;
        for (BlockPos blocks : targetBlocks) {
            blocks = bPos.add(blocks);

            if (blockBl.contains(blocks) && (!hypixelTower.isEnabled() || !KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE))) {
                continue;
            }
            //if (blocks.getY() == startY && !sameY() && hypixelSameY.isEnabled() && sameY.isEnabled()) continue;
            if (!w.isAir(blocks) && (blocks.getManhattanDistance(bPos) != 0 || placeInsideSelf) && !BlockUtils.isBlockClickable(blocks)) {
                double disty = mc.player.getPos().add(mc.player.getVelocity()).distanceTo(blocks.toCenterPos());

                Box blox = BlockUtils.blockBox(blocks);
                if (blox == null)
                    continue;

                Vec3d clo = MathUtils.closestPointToBox(getEye(), blox);
                if (disty < dist && PlayerUtils.canVectorBeSeen(getEye(), clo) && getEye().distanceTo(clo) <= 4.5) {
                    dist = disty;
                    best = blocks;
                }
            }
        }

        return best;
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (!event.check || !nullCheck())
            return;

        switch (mode.getMode()) {
            case Static:
            case Static_Yaw: {
                if (yaw != null && pitch != null) {
                    float yawDiff = Math.abs(MathHelper.wrapDegrees(yaw - Template.rotationManager().yaw()));
                    float pitchDiff = Math.abs(MathHelper.wrapDegrees(pitch - Template.rotationManager().pitch()));
                    if (yawDiff <= 5 && pitchDiff <= 5 && getBest(true) != null && PlayerUtils.isPressingMoveInput()) {
                        event.input.movementForward = -1;

                        Direction moveDir = Direction.fromHorizontalDegrees(MathHelper.wrapDegrees(PlayerUtils.getMoveDirection() + 180));
                        if (vector == null)
                            vector = getBest(true).toCenterPos();
                        Vec3d vec = vector.subtract(mc.player.getPos());
                        boolean move = true;
                        double posOffset = isYawOffset() ? 0.1 : 0;

                        if (!hasMoved) {
                            switch (moveDir) {
                                case EAST: {
                                    if (vec.x >= -posOffset)
                                        move = false;
                                    break;
                                }
                                case WEST: {
                                    if (vec.x <= posOffset)
                                        move = false;
                                    break;
                                }
                                case SOUTH: {
                                    if (vec.z >= -posOffset)
                                        move = false;
                                    break;
                                }
                                case NORTH: {
                                    if (vec.z <= posOffset)
                                        move = false;
                                    break;
                                }
                            }
                        }
                        if (move)
                            hasMoved = true;

                        if (!diagGB && move)
                            event.input.movementSideways = -1;
                    } else InputUtil.setSneaking(event.input.playerInput, true);
                    if (!firstPlace) InputUtil.setSneaking(event.input.playerInput, true);
                } else return;
                break;
            }

            case Fruitberries: {
                if (PlayerUtils.isPressingMoveInput()) {
                    if (!inPosition) {
                        if (yaw != null && pitch != null && getBest(true) != null) {
                            float yawDiff = Math.abs(MathHelper.wrapDegrees(yaw - Template.rotationManager().yaw()));
                            float pitchDiff = Math.abs(MathHelper.wrapDegrees(pitch - Template.rotationManager().pitch()));
                            if (yawDiff <= 5 && pitchDiff <= 5) {
                                if (targetWalk == null)
                                    targetWalk = getBest(true).toCenterPos().offset(Direction.fromHorizontalDegrees(yaw), 0.5);
                                Pair<Double, Double> pr = PlayerUtils.correctedInputForPos(targetWalk);

                                boolean done = true;
                                if (pr.getRight() != 0) {
                                    event.input.movementSideways = (float) (double) pr.getRight();
                                    done = false;
                                }
                                if (pr.getLeft() != 0) {
                                    event.input.movementForward = (float) (double) pr.getLeft();
                                    done = false;
                                }
                                if (done)
                                    inPosition = true;
                            }
                        }
                        return;
                    } else {
                        event.input.movementForward = stopS && ticks < mc.player.age ? 0 : -1;

                        Box adjustedBox = mc.player.getBoundingBox().offset(0, -0.5, 0).offset(mc.player.getVelocity().multiply(fpPredict.value));
                        Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));
                        if (!blockCollisions.findAny().isPresent())
                            event.input.jump();
                    }
                }
                break;
            }
            case Telly: {
                Box ab = mc.player.getBoundingBox().offset(0, -0.5, 0).offset(mc.player.getVelocity().multiply(gbPredict.value));
                Stream<VoxelShape> bc = Streams.stream(mc.world.getBlockCollisions(mc.player, ab));
                if ((mc.player.isSprinting() || !bc.findAny().isPresent()))
                    event.input.jump();
                break;
            }
        }

        if (false && !donePositioning) {
            Vec3d edgeVec = PlayerUtils.blockEdgeVec();
            if (edgeVec != mc.player.getPos()) {
                Pair<Double, Double> pair = PlayerUtils.correctedInputForPos(edgeVec);
                boolean hasMove = false;
                if (mc.player.getPos().distanceTo(edgeVec) >= 0.26) {
                    if (pair.getLeft() != 0) {
                        event.input.movementForward = (float) (double) pair.getLeft();
                        hasMove = true;
                    }

                    if (pair.getRight() != 0) {
                        event.input.movementSideways = (float) (double) pair.getRight();
                        hasMove = true;
                    }
                }
                if (!hasMove && mc.player.getPos().distanceTo(edgeVec) != 0) {
                    mc.player.setVelocity(Vec3d.ZERO);
                    donePositioning = true;
                }
            }
        }

        if (autoJump.isEnabled() && PlayerUtils.isPressingMoveInput() && firstPlace)
            event.input.jump();

        if (!mc.player.isOnGround() && isSneak()) {
            sneakTimer = System.currentTimeMillis() + unSneak.getRandomInt();
        } else {
            // 0.55 works mostly
            Box adjustedBox = mc.player.getBoundingBox().offset(0, -0.5, 0).offset(mc.player.getVelocity().multiply(gbPredict.value));
            if (Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox)).findAny().isEmpty()) {
                if (godBridge.isEnabled()) {
                    event.input.jump();
                    if (onground) timer = (int) placeDelay.getMaximum();
                    onground = false;
                }
                if (isGbSneak())
                    sneakTimer = System.currentTimeMillis() + unSneak.getRandomInt();
            } else {
                onground = true;
            }

            if (PlayerUtils.blockEdgeDist() != 0 && PlayerUtils.blockEdgeDist() <= edgeDistance.value) {
                if (isSneak()) {
                    sneakTimer = System.currentTimeMillis() + unSneak.getRandomInt();
                }
                if (stopMove.isEnabled() && !event.input.playerInput.sneak()) {
                    event.input.movementSideways = 0;
                    event.input.movementForward = Math.max(event.input.movementForward, 0f);
                }
            }
            if (breezily.isEnabled() && PlayerUtils.isPressingMoveInput()) {
                Direction moveDir = Direction.fromHorizontalDegrees(MathHelper.wrapDegrees(Template.rotationManager().yaw() - (!diagGB ? 45 : 0) + 180));
                Vec3d vec = mc.player.getBlockPos().toCenterPos().subtract(mc.player.getPos());

                double dist = breezilyDist.value;
                double offset = -0.3;
                switch (moveDir) {
                    case EAST: {
                        if (vec.z >= dist + offset)
                            right = true;
                        if (vec.z <= -dist + offset)
                            right = false;
                        break;
                    }
                    case WEST: {
                        if (vec.z >= dist - offset)
                            right = false;
                        if (vec.z <= -dist - offset)
                            right = true;
                        break;
                    }
                    case SOUTH: {
                        if (vec.x >= dist - offset)
                            right = false;
                        if (vec.x <= -dist - offset)
                            right = true;
                        break;
                    }
                    case NORTH: {
                        if (vec.x >= dist + offset)
                            right = true;
                        if (vec.x <= -dist + offset)
                            right = false;
                        break;
                    }
                }

                if (right) {
                    event.input.movementForward = -1;
                    event.input.movementSideways = 0;
                } else {
                    event.input.movementForward = 0;
                    event.input.movementSideways = -1;
                }
            }
        }
        if (System.currentTimeMillis() < sneakTimer)
            InputUtil.setSneaking(event.input.playerInput, true);
    }

    @EventHandler
    public void onTickPost(PlayerTickEvent.Post event) {
        shouldSpoofGround = false;
    }

    @EventHandler
    public void onTick(PlayerTickEvent.Pre event) {
        if (mc.player.isOnGround()) {
            offGroundTicks=0;
            lastLandY = mc.player.getBlockY()-1;
            lastGroundY = mc.player.getBlockY();
        } else offGroundTicks++;

        if (!hasHeldSpace && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            lastJump = System.currentTimeMillis();
        }
        if (hasHeldSpace && !KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            onEnable();
            lastEnable = 0L;
        }
        if (mc.player.isOnGround()) wasOnGroundSinceSpace = true;

        if (hypixelTower.isEnabled() && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            //inAirTicks = mc.player.isOnGround() ? 0 : inAirTicks + 1;
//
            //double x = mc.player.getVelocity().x;
            //double y = mc.player.getVelocity().y;
            //double z = mc.player.getVelocity().z;
            //if (offGroundTicks == 6) y=-1.0;
            //shouldSpoofGround = true;
            //mc.player.setVelocity(x,y,z);
            allowSprint = !towerStrafe.isEnabled();
            if (mc.player.isOnGround() && towerStrafe.isEnabled()) {
                strafe(0.4);
            } else if (mc.player.isOnGround()) {
                double x = mc.player.getVelocity().x;
                double y = mc.player.getVelocity().y;
                double z = mc.player.getVelocity().z;
                mc.player.setVelocity(x*0.92,y,z*0.92);
            }
            double x = mc.player.getVelocity().x;
            double y = mc.player.getVelocity().y;
            double z = mc.player.getVelocity().z;

            if (offGroundTicks == towerTick.getFValue()) y = -(towerMotion.getValue()/10000+ towerRandom.getFValue()*Math.random());


            if (lastJump < System.currentTimeMillis()+500L) {
                if (towerSpoof.isEnabled()) shouldSpoofGround = true;
                mc.player.setVelocity(x,y,z);
            }
        } else {
            allowSprint = true;
        }
        hasHeldSpace = KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE);

        if (PlayerUtils.isMoving()) {
            if (sprint.isEnabled() && hypixelSprint.isEnabled() && mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x*0.92, mc.player.getVelocity().y, mc.player.getVelocity().z*0.92);
            }
            if (mode.is(modeEnum.Fruitberries)) {
                if (yaw == null && pitch == null) {
                    yaw = (float) MathUtils.round(MathHelper.wrapDegrees(PlayerUtils.getMoveDirection() + 180f), 90.0);
                    pitch = 75.2f;
                    diagGB = true;
                }
            } else {
                if (yaw == null) {
                    double moveYaw = MathUtils.round(MathHelper.wrapDegrees(PlayerUtils.getMoveDirection() + 180f), 45.0);
                    if (breezily.isEnabled())
                        moveYaw = MathUtils.round(MathHelper.wrapDegrees(PlayerUtils.getMoveDirection() + 180f), 90.0);
                    boolean offset = ((mode.is(modeEnum.Normal) || mode.is(modeEnum.Static) || mode.is(modeEnum.Static_Yaw)) && isYawOffset());
                    diagGB = moveYaw % 90 != 0 || !offset;
                    yaw = (float) moveYaw + (diagGB ? 0 : 45);
                }
                if (pitch == null) {
                    pitch = mode.is(modeEnum.Static) ? !isYawOffset() ? 78f : safeWalk.isEnabled() || stopMove.isEnabled() ? 81f : breezily.isEnabled() ? 76.5f : 75.6f : 80f;
                }
            }
            // Comment out speed mode
            /*if (firstPlace) {
            switch (speedMode.getMode()) {
                case MineBlaze -> {
                    Direction moveDir = Direction.fromHorizontalDegrees(MathHelper.wrapDegrees(Template.rotationManager().yaw() - (diagGB ? 0 : 45)));

//                    mc.inGameHud.getChatHud().addMessage(Text.of(moveDir.getName()));
                    double moveSpeed = 0.15321679421194379; // Sprint walk speed
                    double x = switch (moveDir) {
                        case EAST -> -moveSpeed;
                        case WEST -> moveSpeed;
                        default -> 0;

                    };
                    double z = switch (moveDir) {
                        case NORTH -> moveSpeed;
                        case SOUTH -> -moveSpeed;
                        default -> 0;

                    };
                    Vec3d vec = new Vec3d(Math.abs(x) > Math.abs(mc.player.getVelocity().x) ? x : mc.player.getVelocity().x, mc.player.getVelocity().y, Math.abs(z) > Math.abs(mc.player.getVelocity().z) ? z : mc.player.getVelocity().z);
                    if (!mc.player.isSneaking())
                        mc.player.setVelocity(vec);
                }
                case Custom -> {
                    Direction moveDir = Direction.fromHorizontalDegrees(MathHelper.wrapDegrees(Template.rotationManager().yaw() - (diagGB ? 0 : 45)));

                    double moveSpeed = speed.getValue();
                    double x = switch (moveDir) {
                        case EAST -> -moveSpeed;
                        case WEST -> moveSpeed;
                        default -> 0;
                    };
                    double z = switch (moveDir) {
                        case NORTH -> moveSpeed;
                        case SOUTH -> -moveSpeed;
                        default -> 0;
                    };
                    Vec3d vec = new Vec3d(Math.abs(x) > Math.abs(mc.player.getVelocity().x) ? x : mc.player.getVelocity().x, mc.player.getVelocity().y, Math.abs(z) > Math.abs(mc.player.getVelocity().z) ? z : mc.player.getVelocity().z);
                    if (!mc.player.isSneaking())
                        mc.player.setVelocity(vec);
                }
                case None -> {}
            }
            }*/
        }

        if (place && mc.currentScreen == null && nullCheck()) {
            Predicate<Item> placeBlocks = BlockUtils.placeableBlocks();
            int slot = InvUtils.getItemSlot(placeBlocks);
            if (slot != -1 && selectBlock.isEnabled() && !placeBlocks.test(mc.player.getMainHandStack().getItem()))
                InvUtils.setInvSlot(slot);

            boolean bl = skipRaycast.isEnabled() || grimNoRots.isEnabled();
            boolean canplace = goodHit(hit);
            bl |= canplace;

            ClientPlayerEntity cpe = PlayerUtils.predictState(2, mc.player).getLeft();
            HitResult hr = PlayerUtils.getHitResult(cpe, cpe.getYaw(), cpe.getPitch());
            if ((!(hr instanceof BlockHitResult) || hr.getType() == HitResult.Type.MISS || !goodDir(((BlockHitResult) hr).getSide())) && canplace && guaranteedPlace.isEnabled())
                timer = delay;

            if (bl && timer >= delay && !(mc.player.getMainHandStack().isEmpty() && mc.player.getOffHandStack().isEmpty())) {
                placeBlock();
                place = false;
                timer = 0;
                firstPlace = true;
                delay = placeDelay.getRandomInt();
            } else timer++;
        }

        //if (PlayerUtils.isMoving()) {
        //    if (mc.player.input.playerInput.jump() && false && mc.player.isOnGround()) {
        //        double x = mc.player.getVelocity().x;
        //        double y = mc.player.getVelocity().y;
        //        double z = mc.player.getVelocity().z;
        //        y = 0.4;
        //        x *= .65;
        //        z *= .65;
        //    }
        //}
    }

    //@EventHandler
    //public void onPostTick(TickEvent.Post event) {
    //    if (mc.player.input.playerInput.jump() && false && towerJumping.isEnabled() && mc.player.isOnGround()) {
    //        double x = mc.player.getVelocity().x;
    //        double z = mc.player.getVelocity().z;
    //        mc.player.setVelocity(x, towerHeight.getFValue(), z);
    //    }
    //}

    private void placeBlock() {
        BlockHitResult bhr;
        Box box = null;
        BlockPos bee = cachedBest;
        boolean grim = false;
        if (bee != null)
            box = BlockUtils.blockBox(bee);
        if (grimNoRots.isEnabled() && yaw != null && pitch != null) {
            HitResult hr = PlayerUtils.getHitResult(mc.player, e -> true, yaw, pitch);
            if (hr instanceof BlockHitResult result && hr.getType() == HitResult.Type.BLOCK) {
                bhr = result;
                grim = true;
            } else {
                return;
            }
        } else if (skipRaycast.isEnabled() && bpos != null && box != null) {
            bhr = PlayerUtils.rayCast(getEye(), MathUtils.closestPointToBox(bpos, box.contract(0.05)), mc.player);
        } else if (hit instanceof BlockHitResult result && hit.getType() == HitResult.Type.BLOCK) {
            bhr = result;
        } else {
            return;
        }
        if (blockBl.contains(bhr.getBlockPos())) {
            if (!hypixelTower.isEnabled() || !KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
                return;
            }
        }

        if (Template.isClickSim()) {
            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
        }
        if (grim) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
        }
        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
            mc.player.swingHand(Hand.MAIN_HAND);
            places++;
        }
        if (grim) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), Template.rotationManager().yaw(), Template.rotationManager().pitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        }
        if (/*offGroundTicks < 4 &&*/ hypixelSameY.isEnabled() && sameY.isEnabled()) {
            BlockPos pos = bhr.getBlockPos().offset(bhr.getSide());
            if (!(hypixelTower.isEnabled() && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)) && pos.getY() == startY && wasOnGroundSinceSpace) {
                blockBl.add(pos);
            }
        }

        //if (false && places % 3 == 0) {
        //    Vec3d edge = PlayerUtils.blockEdgeVec();
//
        //    BlockHitResult noob = PlayerUtils.rayCast(edge, mc.player.getBlockPos().down().toCenterPos(), mc.player);
//
        //    if (Template.isClickSim())
        //        Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());
//
        //    ActionResult fail100 = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, noob);
        //    if ((fail100.isAccepted() && fail100.shouldSwingHand())) {
        //        mc.player.swingHand(Hand.MAIN_HAND);
        //        places++;
        //    }
        //}
        if (bhr.getSide() == Direction.UP && bhr.getBlockPos().getY()+1 == mc.player.getBlockY()) {
            return;
        }
        if (tower.isEnabled() && mc.player.input.playerInput.jump() && !mc.player.isOnGround() && bhr.getSide() == Direction.UP && interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult) /*&& towerPushing.isEnabled()*/) {
            double x = mc.player.getVelocity().x;
            double z = mc.player.getVelocity().z;
            mc.player.setVelocity(x, -towerPush.getFValue(), z);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (hypixelSpoof.isEnabled() && event.packet instanceof PlayerMoveC2SPacket wrapper && mc.player.isOnGround() && mc.player.age % 2 == 0) {
            event.cancel();
            Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY()+0.035, mc.player.getZ(), wrapper.yaw, wrapper.pitch, false, mc.player.horizontalCollision));
        }
        if (shouldSpoofGround) {
            if (event.packet instanceof PlayerMoveC2SPacket.Full wrapper) {
                event.setCancelled(true);
                Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                event.setCancelled(true);
                Template.sendNoEvent(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround wrapper) {
                event.setCancelled(true);
                Template.sendNoEvent(new PlayerMoveC2SPacket.LookAndOnGround(wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.OnGroundOnly) {
                event.setCancelled(true);
                Template.sendNoEvent(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
            }
        }
        if (false && KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()) && donePositioning) {
            if (!(event.packet instanceof PlayerInteractBlockC2SPacket)) return;
            // 0.08 is player gravity
            if (mc.player.getVelocity().y > -0.08) {
                if (((PlayerInteractBlockC2SPacket) event.packet).getBlockHitResult().getBlockPos().equals(
                        new BlockPos(mc.player.getBlockPos().getX(), (int) (mc.player.getPos().getY() - 1.4), mc.player.getBlockPos().getZ()))) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.4, mc.player.getVelocity().z);
                }
            }
        }
    }

    @EventHandler
    public void onRotation(MouseUpdateEvent.Post event) {
        if (!nullCheck())
            return;

        if (bpos != null && getEye().distanceTo(bpos) <= 4.5 * 4.5 && mc.player.getPos().add(mc.player.getVelocity()).getY() > bpos.y && (!onlyWhenPlace.isEnabled() || !place || timer >= delay)) {
            BlockPos best = cachedBest;
            if (grimNoRots.isEnabled()) {
                if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {

                    if (best != null && bpos != null) {
                        Vec3d delta = bpos.subtract(getEye());

                        if (delta.getX() != 0.0 && delta.getZ() != 0.0) {
                            yaw = RotationUtils.getRotations(getEye(), bpos).fyaw();
                        }

                        if (yaw != null) {
                            HitResult hr = PlayerUtils.getHitResult(mc.player, yaw, pitch != null ? pitch : mc.player.getPitch());
                            if (hr != null) {
                                cx = hr.getPos().x;
                                cz = hr.getPos().z;
                            } else {
                                cx = bpos.x;
                                cz = bpos.z;
                            }
                        } else {
                            cx = bpos.x;
                            cz = bpos.z;
                        }

                        Float bestPitch = getPitch(best);
                        if (bestPitch != null) {
                            pitch = bestPitch;
                        }
                    }

                    place = true;
                    color = Color.GREEN;
                } else color = Color.RED;
                return;
            }

            switch (mode.getMode()) {
                case Fruitberries: {
                    if (inPosition) {
                        color = Color.RED;
                        if (goodHit(hit)) {

                            if (mc.player.getVelocity().y - 0.08 >= 0) {
                                place = true;
                                color = Color.GREEN;
                                // place while going up
                                stopS = true;
                                if (ticks < mc.player.age)
                                    ticks = mc.player.age + fbTicks.getIValue();
                            }

                            if (mc.player.getVelocity().y - 0.08 <= 0) {
                                place = true;
                                color = Color.GREEN;
                                // place while going down
                                stopS = false;
                            }
                        }

                        Vec3d h = hit.getPos();
                        cx = h.x;
                        cy = h.y;
                        cz = h.z;
                    }
                    break;
                }
                case Static: {
                    if (mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() instanceof AirBlock && hit != null) {
                        Vec3d h = hit.getPos();
                        cx = h.x;
                        cy = h.y;
                        cz = h.z;
                        color = Color.GREEN;
                        place = true;
                    } else color = Color.RED;
                    break;
                }
                case Static_Yaw: {
                    if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {

                        if (hit != null) {
                            Vec3d h = hit.getPos();
                            cx = h.x;
                            cy = h.y;
                            cz = h.z;
                        }

                        if (best != null) {
                            Float bestPitch = getPitch(best);
                            if (bestPitch != null && !skipRaycast.isEnabled()) {
                                pitch = bestPitch;
                            }
                        }

                        place = true;
                        color = Color.GREEN;
                    } else color = Color.RED;
                    break;
                }
                case Normal:
                case Telly: {
                    if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {

                        if (best != null && bpos != null) {
                            Vec3d delta = bpos.subtract(getEye());

                            if (delta.getX() != 0.0 && delta.getZ() != 0.0) {
                                yaw = RotationUtils.getRotations(getEye(), bpos).fyaw();
                                if (mode.is(modeEnum.Normal) && MathUtils.round(MathHelper.wrapDegrees(yaw), 45.0) == MathUtils.round(MathHelper.wrapDegrees(yaw), 90.0) && isYawOffset()) {
                                    float pos = yaw + 45f;
                                    float neg = yaw - 45f;
                                    if (lastYaw != null) {
                                        if (Math.abs(lastYaw - neg) < Math.abs(lastYaw - pos)) {
                                            yaw = neg;
                                        } else {
                                            yaw = pos;
                                        }
                                    } else yaw = pos;

                                }
                                lastYaw = yaw;
                            }

                            if (yaw != null) {
                                HitResult hr = PlayerUtils.getHitResult(mc.player, yaw, pitch != null ? pitch : mc.player.getPitch());
                                if (hr != null) {
                                    cx = hr.getPos().x;
                                    cz = hr.getPos().z;
                                } else {
                                    cx = bpos.x;
                                    cz = bpos.z;
                                }
                            } else {
                                cx = bpos.x;
                                cz = bpos.z;
                            }

                            Float bestPitch = getPitch(best);
                            if (bestPitch != null && !skipRaycast.isEnabled()) {
                                pitch = bestPitch;
                            }
                        }

                        place = true;
                        color = Color.GREEN;
                    } else color = Color.RED;
                }
            }
        } else if (grimNoRots.isEnabled()) {
            return;
        }

        if (pitch != null) {
            float startPitch = playerRots.isEnabled() ? mc.player.getPitch() : Template.rotationManager().pitch();

            double pitchStrength = pitchSpeed.getRandomDouble() / 10.0;
            double rotPitch = MathHelper.lerpAngleDegrees((float) pitchStrength, startPitch, pitch);

            Rotation finalrot = RotationUtils.correctSensitivity(new Rotation(mc.player.getYaw(), rotPitch));

            if (playerRots.isEnabled()) mc.player.setPitch(finalrot.fpitch());
            else Template.rotationManager().setPitch(finalrot.fpitch());
        }

        double yawStrength = yawSpeed.getRandomDouble() / 10.0;
        Float rotYaw = null;

        if (mode.is(modeEnum.Telly) && (mc.player.getVelocity().y > 0 || mc.player.isOnGround()))
            rotYaw = MathHelper.lerpAngleDegrees((float) yawStrength, playerRots.isEnabled() ? mc.player.getYaw() : Template.rotationManager().getClientRotation().fyaw(), Template.rotationManager().yyyaw);
        else if (yaw != null)
            rotYaw = MathHelper.lerpAngleDegrees((float) yawStrength, playerRots.isEnabled() ? mc.player.getYaw() : Template.rotationManager().getClientRotation().fyaw(), yaw);

        if (rotYaw != null) {
            Rotation finalrot = RotationUtils.correctSensitivity(new Rotation(rotYaw, mc.player.getPitch()));
            if (playerRots.isEnabled()) mc.player.setYaw(finalrot.fyaw());
            else Template.rotationManager().setYaw(finalrot.fyaw());
        }
    }

    private boolean goodHit(HitResult hr) {
        return hr instanceof BlockHitResult && hr.getType() == HitResult.Type.BLOCK && goodDir(((BlockHitResult)hr).getSide());
    }

    private boolean sameY() {
        if (hypixelTower.isEnabled() && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE))
            return false;

        if (hypixelSameY.isEnabled() && sameY.isEnabled() && !constantSameY.isEnabled()) {
            return offGroundTicks >= offTicks.getIMinValue() && offGroundTicks <= offTicks.getIMaxValue();
        }

        if (hypixelSameY.isEnabled() && sameY.isEnabled() && constantSameY.isEnabled()) {
            if (lastLandY == startY) return mc.player.fallDistance > 0.95;
            else return mc.player.fallDistance > 0.2 && mc.player.fallDistance < 0.25;
        }

        if (hypixelSprintJump.isEnabled()) return places % 4 == 0;
        return sameY.isEnabled();
    }

    private boolean goodDir(Direction dir) {
        //if (placeAmount.getValue() != 0 && sameY.isEnabled()) {
        //    if (places % placeAmount.getIValue() == 0) return dir == Direction.UP;
        //    else return dir != Direction.UP && dir != Direction.DOWN;
        //} else {
        //    if (dir == Direction.UP) return mc.player.input.playerInput.jump() && !mode.is(modeEnum.Fruitberries) && !sameY();
        //    else return dir != Direction.DOWN;
        //}
        if (hypixelTower.isEnabled() && KeyUtils.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            return dir != Direction.DOWN;
        }

        if (hypixelSameY.isEnabled() && sameY.isEnabled()) {
            if (dir == Direction.UP) return sameY();
            else return dir != Direction.DOWN;
        } else {
            if (dir == Direction.UP) return mc.player.input.playerInput.jump() && !mode.is(modeEnum.Fruitberries) && !sameY();
            else return dir != Direction.DOWN;
        }
    }

    private Float getPitch(BlockPos target) {
        if (onlyWhenPlace.isEnabled()) {
            Box adjustedBox = mc.player.getBoundingBox().offset(0, -0.5, 0).offset(mc.player.getVelocity().multiply(2));
            if (Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox)).findAny().isPresent() && PlayerUtils.isMoving()) {
                return pitch;
            }
        }
        if (yaw != null && pitch != null && goodHit(PlayerUtils.getHitResult(mc.player, e -> true, yaw, pitch))) {
            return pitch;
        }
        double diff = Double.MAX_VALUE;
        Float best = null;
        Vec3d saveVec = null;
        double size = lastB.maxY - lastB.minY;
        for (double y = size * placeYRange.getMinValue(); y <= size * placeYRange.getMaxValue(); y += 0.05) {
            Vec3d vec = MathUtils.Vec3dWithY(MathUtils.closestPointToBox(getEye(), lastB.contract(0.075)), target.getY() + y);
            float tpitch = RotationUtils.getRotations(getEye(), vec).fpitch();

            double difference = Math.abs((pitch == null ? mc.player.getPitch() : pitch) - tpitch);
            HitResult hr = null;
            if (yaw != null)
                hr = PlayerUtils.getHitResult(mc.player, e -> true, yaw, tpitch);
            if (difference < diff && (yaw == null || (goodHit(hr)))) {
                diff = difference;
                best = tpitch;
                saveVec = vec;
            }

        }
        if (saveVec != null) {
            cy = saveVec.y;
        }

        return best;
    }

    public boolean isYawOffset() {
        if (isClutching && Template.moduleManager.isModuleEnabled(AntiVoidModule.class)) return false;
        else return yawOffset.isEnabled();
    }

    public boolean isSneak() {
        if (isClutching && Template.moduleManager.isModuleEnabled(AntiVoidModule.class)) return false;
        else return sneak.isEnabled();
    }

    public boolean isGbSneak() {
        if (isClutching && Template.moduleManager.isModuleEnabled(AntiVoidModule.class)) return false;
        else return godBridgeSneak.isEnabled();
    }

    public void strafe(double multiplier) {
        if (!PlayerUtils.isPressingMoveInput(false))
            return;
        double rad = Math.toRadians(PlayerUtils.getExactMoveDirection() + 90);
        double moveSpeed = 0.15321679421194379 * multiplier;
        double x = Math.cos(rad) * moveSpeed;
        double z = Math.sin(rad) * moveSpeed;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }
}
