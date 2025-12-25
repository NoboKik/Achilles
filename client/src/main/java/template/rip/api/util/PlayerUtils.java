package template.rip.api.util;

import com.google.common.collect.Streams;
import imgui.ImGui;
import me.sootysplash.bite.BiteMap;
import me.sootysplash.bite.CharSeq;
import me.sootysplash.bite.TypeObject;
import net.minecraft.block.AirBlock;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import template.rip.Template;
import template.rip.api.object.FakePlayerEntity;
import template.rip.api.object.Path;
import template.rip.api.rotation.RotationUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.modules.blatant.CriticalsModule;
import template.rip.module.modules.client.TargetsModule;
import template.rip.module.modules.combat.ReachModule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static template.rip.Template.mc;
import static template.rip.api.rotation.RotationUtils.getYaw;

@SuppressWarnings("unchecked")
public class PlayerUtils {

    public static BiteMap friends = BiteMap.newInstance();
    public static BiteMap focusTargets = BiteMap.newInstance();

    public static ExecutorService threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new SynchronousQueue<>());

    public static HashMap<String, HashMap<String, String>> cachedTiers = new HashMap<>();
    public static HashMap<String, String> tierApiResponses = new HashMap<>();
    public static HashMap<String, Future<?>> tierTasks = new HashMap<>();

    public static HashMap<String, Integer> playerHeadsTextures = new HashMap<>();
    public static HashMap<String, ByteBuffer> playerHeadBuffers = new HashMap<>();
    public static HashMap<String, Future<?>> playerHeadImageTasks = new HashMap<>();
    public static boolean soundDisabled;
    public static boolean particleDisabled;
    private static ArrayList<Entity> espTargets = new ArrayList<>();
    private static ArrayList<Entity> targets = new ArrayList<>();
    public static Identifier fallbackTexture;

    public static boolean isFriend(PlayerEntity player) {
        return friends.get(player.getUuidAsString()) != null;
    }
    public static boolean isFriend(UUID player) {
        return friends.get(String.valueOf(player)) != null;
    }
    public static boolean isFocus(PlayerEntity player) {
        return focusTargets.get(player.getUuidAsString()) != null;
    }

    public static HashMap<String, Identifier> playerFaceTextures = new HashMap<>();

    public static Vec2f computeMovementInput() {
        float forward = 0F;
        if (mc.options.forwardKey.isPressed())
            forward += 1F;
        if (mc.options.backKey.isPressed())
            forward -= 1F;

        float sideways = 0F;
        if (mc.options.leftKey.isPressed())
            sideways += 1F;
        if (mc.options.rightKey.isPressed())
            sideways -= 1F;
        return new Vec2f(forward, sideways);
    }

    public static Vec2f computeSemiInput() {
        float forward = 0F;
        if (mc.options.forwardKey.isPressed())
            forward += 1F;
        if (mc.options.backKey.isPressed())
            forward -= 1F;

        float sideways = 0F;
        if (mc.options.leftKey.isPressed())
            sideways += 1F;
        if (mc.options.rightKey.isPressed())
            sideways -= 1F;

        if (sideways != 0F && forward != 0F)
            sideways = 0F;

        return new Vec2f(forward, sideways);
    }

    public static boolean isBothStrafe() {
        float forward = 0F;
        if (mc.options.forwardKey.isPressed())
            forward += 1F;
        if (mc.options.backKey.isPressed())
            forward -= 1F;

        float sideways = 0F;
        if (mc.options.leftKey.isPressed())
            sideways += 1F;
        if (mc.options.rightKey.isPressed())
            sideways -= 1F;

        return sideways != 0 && forward != 0;
    }

    public static Vec2f computeForwardInput() {
        float forward = 0F;
        if (mc.options.forwardKey.isPressed())
            forward += 1F;
        if (mc.options.backKey.isPressed())
            forward -= 1F;
        return new Vec2f(forward, 0);
    }

    public static Double yOfClosestGround() {
        BlockHitResult bhr = rayCast(mc.player.getPos(), MathUtils.Vec3dWithY(mc.player.getPos(), -64), mc.player);
        return switch (bhr.getType()) {
            case BLOCK -> bhr.getPos().y;
            case MISS, ENTITY -> null;
        };
    }

    public static double getMoveDirection() {
        Vec2f vec = computeMovementInput();
        vec = new Vec2f(vec.y, vec.x);
        return getYaw(vec) + (PlayerUtils.isMoving() ? 0 : 90) + (Template.rotationManager().isEnabled() ? Template.rotationManager().getRealRotation().fyaw() : mc.player.getYaw());
    }

    public static double getExactMoveDirection() {
        Vec2f vec = computeMovementInput();
        vec = new Vec2f(vec.y, vec.x);
        return getYaw(vec) + (mc.player.getYaw());
    }

    public static double getMotionDirection() {
        Vec2f vec = new Vec2f((float) mc.player.getVelocity().x, (float) mc.player.getVelocity().z);
        double yaw = getYaw(vec);

        if (yaw >= 180) yaw = yaw - 360;
        return yaw;
    }

    public static double getSemiMoveDirection() {
        Vec2f vec = computeSemiInput();
        return getMoveDirFromVec(vec);
    }

    public static double getMoveDirFromVec(Vec2f vec) {
        vec = new Vec2f(vec.y, vec.x);
        return getYaw(vec) + (mc.player.getYaw());
    }

    public static double getForwardMoveDirection() {
        Vec2f vec = computeForwardInput();
        return getMoveDirFromVec(vec);
    }

    public static boolean canCrit(PlayerEntity pe) {
        return canCrit(pe, false);
    }

    public static boolean canCrit(PlayerEntity pe, boolean closeGroundCheck) {
        if (pe == null) return false;
        if (closeGroundCheck) {
            BlockHitResult bhr = PlayerUtils.rayCast(pe.getPos(), pe.getPos().subtract(0.0, 3.0, 0.0), pe);
            double y = pe.getY() - bhr.getPos().getY();
            if (y <= 0.15) {
                return false;
            }
        }

        return (!pe.hasStatusEffect(StatusEffects.BLINDNESS) &&
                !pe.isClimbing() &&
                !pe.isTouchingWater() &&
                !pe.hasVehicle() &&
                !pe.isOnGround() &&
                pe.fallDistance > 0.065f &&
                pe.getAttackCooldownProgress(0.5f) >= 0.7 &&
                pe.movementMultiplier.lengthSquared() < 1.0E-7);
    }

    // for checking if we won't be critting anytime soon
    public static boolean canCritStatic(PlayerEntity pe) {
        if (pe == null) return false;

        return (!pe.hasStatusEffect(StatusEffects.BLINDNESS) && !pe.isClimbing() && !pe.isTouchingWater() && !pe.hasVehicle() && pe.movementMultiplier.lengthSquared() < 1.0E-7);
    }

    public static boolean canCriticalsModule() {
        CriticalsModule crit = Template.moduleManager.getModule(CriticalsModule.class);
        if (crit.isEnabled()) {
            if (crit.mode.is(CriticalsModule.modeEnum.OldGrim_OffGround)) {
                return !mc.player.isOnGround();
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean canVectorBeSeen(Vec3d start, Vec3d end) {
        return !(rayCast(start, end, mc.player).getType() == HitResult.Type.BLOCK);
    }

    public static boolean isBlockedByShield(LivingEntity attacker, LivingEntity shielder, boolean instaBlock) {
        if (attacker == null || shielder == null)
            return false;

        if (shielder.isBlocking() || (instaBlock && shielder.getActiveItem().isOf(Items.SHIELD))) {
            Vec3d vec3d2 = shielder.getRotationVec(1F);
            Vec3d vec3d3 = attacker.getPos().relativize(shielder.getPos());
            vec3d3 = new Vec3d(vec3d3.x, 0.0, vec3d3.z).normalize();
            return vec3d3.dotProduct(vec3d2) < 0.0;
        }

        return false;
    }

    public static Pair<Double, Double> correctedInputForPos(Vec3d pos) {
        Pair<Double, Double> pr = inputForPos(pos);
        double forward = pr.getLeft().isInfinite() ? 0 : pr.getLeft() > 0.1 ? Math.ceil(Math.min(pr.getLeft(), 1)) : pr.getLeft() < -0.1 ? Math.floor(Math.max(pr.getLeft(), -1)) : 0;
        double side = pr.getRight().isInfinite() ? 0 : pr.getRight() > 0.1 ? Math.ceil(Math.min(pr.getRight(), 1)) : pr.getRight() < -0.1 ? Math.floor(Math.max(pr.getRight(), -1)) : 0;
        return new Pair<>(forward, side);
    }

    // may give infinite values as inputs, caution advised
    public static Pair<Double, Double> inputForPos(Vec3d pos) {
        float yaw = Template.rotationManager().yaw();
        return preciseInputForPos(pos, MathHelper.wrapDegrees((int) MathUtils.round(yaw, 45.0)));
    }

    private static Pair<Double, Double> preciseInputForPos(Vec3d pos, int yaw) {
        return switch (yaw) {
            case 0 -> new Pair<>(-(mc.player.getZ() - pos.z), -(mc.player.getX() - pos.x));
            case -180, 180 -> new Pair<>((mc.player.getZ() - pos.z), (mc.player.getX() - pos.x));
            case 90 -> new Pair<>((mc.player.getX() - pos.x), -(mc.player.getZ() - pos.z));
            case -90 -> new Pair<>(-(mc.player.getX() - pos.x), (mc.player.getZ() - pos.z));
            case 45 -> new Pair<>(-(-(mc.player.getX() - pos.x) + (mc.player.getZ() - pos.z)), -(mc.player.getX() - pos.x) - (mc.player.getZ() - pos.z));
            case -45 -> new Pair<>(-(mc.player.getX() - pos.x) - (mc.player.getZ() - pos.z), (-(mc.player.getX() - pos.x) + (mc.player.getZ() - pos.z)));
            case 135 -> new Pair<>(-(-(mc.player.getX() - pos.x) - (mc.player.getZ() - pos.z)), -(-(mc.player.getX() - pos.x) + (mc.player.getZ() - pos.z)));
            case -135 -> new Pair<>((-(mc.player.getX() - pos.x) + (mc.player.getZ() - pos.z)), -(-(mc.player.getX() - pos.x) - (mc.player.getZ() - pos.z)));
            default -> new Pair<>(0.0, 0.0);
        };
    }

    public static boolean isMoving() {
        if (mc.player == null)
            return false;
        return mc.player.input.getMovementInput().length() != 0f;
    }

    public static boolean entityStill(Entity e) {
        return e.prevX == e.getX() && e.prevY == e.getY() && e.prevZ == e.getZ();
    }

    public static double blockEdgeDist() {
        return mc.player.getPos().distanceTo(blockEdgeVec());
    }

    public static Vec3d blockEdgeVec() {
        return blockEdgePair().getRight();
    }

    public static Pair<Box, Vec3d> blockEdgePair() {
        Vec3d pos = mc.player.getPos();
        Pair<Box, Vec3d> fail = new Pair<>(null, pos);

        Box playerBox = mc.player.getBoundingBox().offset(0.0, -0.5, 0.0);
        Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, playerBox));
        Optional<VoxelShape> blocks = blockCollisions.findAny();

        if (blocks.isEmpty())
            return fail;

        VoxelShape vs = blocks.get();
        if (vs.isEmpty())
            return fail;

        Box b = vs.getBoundingBox();
        if (b == null)
            return fail;

        b = b.expand(mc.player.getBoundingBox().getLengthX() / 2.0, 0, mc.player.getBoundingBox().getLengthZ() / 2.0);

        double distance = Double.MAX_VALUE;
        Vec3d finalVec = pos;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN || dir == Direction.UP)
                continue;
            Vec3d vec = MathUtils.closestPointToBox(pos.offset(dir, 2).add(0.0, -0.5, 0.0), b);
            double dist = pos.distanceTo(vec);
            if (dist < distance && mc.world.isAir(BlockPos.ofFloored(vec))) {
                distance = dist;
                finalVec = MathUtils.Vec3dWithY(vec, pos.y);

            }
        }

        return new Pair<>(b, finalVec);
    }

    public static Pair<Box, Vec3d> blockEdgePairFor(Vec3d pos, LivingEntity ent) {
        Pair<Box, Vec3d> fail = new Pair<>(null, pos);

        Box playerBox = ent.getBoundingBox().offset(0.0, -0.5, 0.0);
        Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(ent, playerBox));
        Optional<VoxelShape> blocks = blockCollisions.findAny();

        if (blocks.isEmpty())
            return fail;

        VoxelShape vs = blocks.get();
        if (vs.isEmpty())
            return fail;

        Box b = vs.getBoundingBox();
        if (b == null)
            return fail;

        b = b.expand(ent.getBoundingBox().getLengthX() / 2.0, 0, ent.getBoundingBox().getLengthZ() / 2.0);

        double distance = Double.MAX_VALUE;
        Vec3d finalVec = pos;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN || dir == Direction.UP)
                continue;
            Vec3d vec = MathUtils.closestPointToBox(pos.offset(dir, 2).add(0.0, -0.5, 0.0), b);
            double dist = pos.distanceTo(vec);
            if (dist < distance && mc.world.isAir(BlockPos.ofFloored(vec))) {
                distance = dist;
                finalVec = MathUtils.Vec3dWithY(vec, pos.y);

            }
        }

        return new Pair<>(b, finalVec);
    }

    public static boolean isPressingMoveInput(boolean jump) {
        return mc.options.forwardKey.isPressed() || mc.options.rightKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.backKey.isPressed() || (jump && mc.options.jumpKey.isPressed());
    }

    public static boolean isPressingMoveInput() {
        return isPressingMoveInput(true);
    }

    public static float randomPitch() {
        float pch = Template.rotationManager().isEnabled() ? Template.rotationManager().getClientRotation().fpitch() : mc.player.getPitch();
        return (float) Math.max(-90.0, Math.min(ThreadLocalRandom.current().nextDouble(pch - 1.0, pch + 1.0), 90.0));
    }

    public static boolean isClientSided(Entity entity) {
        return entity instanceof FakePlayerEntity;
    }

    public static boolean isTeammate(PlayerEntity one, PlayerEntity other) {
        switch (Template.moduleManager.getModule(TargetsModule.class).teammatesMode.getMode()) {
            case Name_Color : {
                OrderedText orderedDisplayName = other.getDisplayName().asOrderedText();
                OrderedText orderedLocalDisplayName = one.getDisplayName().asOrderedText();

                // copy and pasted from old teamsModule that pycat made (I think)
                // Checking only 1st character because returning false after 1st
                return orderedLocalDisplayName.accept(((index, style, codePoint) -> {
                    TextColor localColor = style.getColor();

                    if (localColor != null) {
                        return orderedDisplayName.accept(((index1, style1, codePoint1) -> {
                            TextColor color = style1.getColor();

                            return color != null && color.equals(localColor);
                        }));
                    } else {
                        return false;
                    }
                }));
            }
            case Vanilla_Teams : {
                return one.isTeammate(other);
            }
            default : {
                return false;
            }
        }
    }

    public static Vec3d lastPosVec(Entity e) {
        return new Vec3d(e.prevX, e.prevY, e.prevZ);
    }

    public static Box renderBox(Entity e) {
        return MathUtils.boxAtPos(e.getBoundingBox(), MathUtils.smoothVec3d(lastPosVec(e), e.getPos(), mc.getRenderTickCounter().getTickDelta(false)));
    }

    public static Vec3d lastPosVecEye(Entity e) {
        return new Vec3d(e.prevX, e.prevY + e.getEyeHeight(e.getPose()), e.prevZ);
    }

    public static float getReachDistance() {
        ReachModule reachModule = Template.moduleManager.getModule(ReachModule.class);
        if (AchillesMenu.isClientEnabled() && reachModule != null && reachModule.isEnabled()) {
            return Math.max(reachModule.blockReach.getFValue(), reachModule.entityReach.getFValue());
        }
        if (mc.interactionManager.getCurrentGameMode().isCreative()) {
            return 5.0f;
        }
        return 4.5f;
    }

    public static Vec3d minBox(Box b) {
        return new Vec3d(b.minX, b.minY, b.minZ);
    }

    public static Vec3d maxBox(Box b) {
        return new Vec3d(b.maxX, b.maxY, b.maxZ);
    }

    public static void addFriend(PlayerEntity player) {
        if (!isFriend(player))
            friends.add(player.getUuidAsString(), player.getName().getString());
    }

    public static Pair<ClientPlayerEntity, ArrayList<Vec3d>> predictState(int count, PlayerEntity baseEntity) {
        return predictState(count, baseEntity, cpe -> {});
    }

    public static Pair<ClientPlayerEntity, ArrayList<Vec3d>> predictState(int count, PlayerEntity baseEntity, Consumer<ClientPlayerEntity> onPreTick) {
        return predictState(count, baseEntity, e -> false, baseEntity == mc.player ? mc.player.input : getClosestInput(baseEntity), onPreTick);
    }

    // from tarasande (I hate k*tlin)
    public static Pair<ClientPlayerEntity, ArrayList<Vec3d>> predictState(int count, PlayerEntity baseEntity, Predicate<ClientPlayerEntity> abortWhen, Input input, Consumer<ClientPlayerEntity> onPreTick) {
        if (baseEntity == null)
            baseEntity = mc.player;
        Vec3d selfVelocity = baseEntity.getVelocity();
        Vec3d localVelocity = mc.player.getVelocity();

        boolean wasSoundDisabled = soundDisabled;
        soundDisabled = true;

        PlayerEntity finalBaseEntity = baseEntity;
        ClientPlayerEntity playerEntity = mc.interactionManager.createPlayer(mc.world, new StatHandler(), new ClientRecipeBook());
//        ClientPlayerEntity playerEntity = new ClientPlayerEntity(
//                mc,
//                mc.world,
//                new ClientPlayNetworkHandler(mc, mc.getNetworkHandler().getConnection(), new ClientConnectionState(
//                        mc.getGameProfile(),
//                        mc.getTelemetryManager().createWorldSession(false  , null, null),
//                        ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager(),
//                        FeatureFlags.DEFAULT_ENABLED_FEATURES,
//                        null,
//                        mc.serverInfo,
//                        mc.currentScreen,
//                        this.serverCookies,
//                        null,
//                        Map.of(),
//                        ServerLinks.EMPTY
//                )) {
//                    @Override
//                    public void sendPacket(Packet<?> packet) {}
//
//                    @Override
//                    public FeatureSet getEnabledFeatures() {
//                        return mc.getNetworkHandler().getEnabledFeatures();
//                    }
//                },
//                new StatHandler(),
//                new ClientRecipeBook(),
//                false,
//                false
//        ) {
//            @Override
//            public float getHealth() {
//                return finalBaseEntity.getMaxHealth(); // we are invincible
//            }
//
//            @Override
//            public void tickMovement() {
//                fallDistance = 0F;
//                super.tickMovement();
//            }
//
//            @Override
//            public boolean isCamera() {
//                return true;
//            }
//
//            @Override
//            public void playSound(SoundEvent sound, float volume, float pitch) {
//            }
//        };

        playerEntity.input = new Input();
        playerEntity.input.movementForward = input.movementForward;
        playerEntity.input.movementSideways = input.movementSideways;
        playerEntity.input.playerInput = new PlayerInput(
                input.playerInput.forward(),
                input.playerInput.backward(),
                input.playerInput.left(),
                input.playerInput.right(),
                input.playerInput.jump(),
                input.playerInput.sneak(),
                input.playerInput.sprint()
        );

        playerEntity.init();
        playerEntity.copyPositionAndRotation(baseEntity);
        playerEntity.copyFrom(baseEntity);

        if (baseEntity == mc.player && Template.rotationManager().isEnabled()) {
            RotationUtils.setEntityRotation(playerEntity, Template.rotationManager().getClientRotation());
        }

        playerEntity.setOnGround(baseEntity.isOnGround()); // scary

        if (baseEntity == mc.player) {
            playerEntity.setVelocity(baseEntity.getVelocity());
        } else {
            playerEntity.setVelocity(Vec3d.ZERO);
        }

        playerEntity.setPose(baseEntity.getPose());
        playerEntity.jumpingCooldown = baseEntity.jumpingCooldown;
        playerEntity.submergedInWater = baseEntity.isSubmergedInWater();
        playerEntity.touchingWater = baseEntity.isTouchingWater();
        playerEntity.setSwimming(baseEntity.isSwimming());
        playerEntity.setSprinting(baseEntity.isSprinting());
        playerEntity.setSneaking(baseEntity.isSneaking());
        playerEntity.verticalCollision = baseEntity.verticalCollision;
        playerEntity.horizontalCollision = baseEntity.horizontalCollision;
        playerEntity.collidedSoftly = baseEntity.collidedSoftly;

        if (baseEntity == mc.player) {
            playerEntity.autoJumpEnabled = mc.player.isAutoJumpEnabled();
            playerEntity.ticksToNextAutoJump = mc.player.ticksToNextAutoJump;
        } else {
            playerEntity.autoJumpEnabled = false; // Who plays with that?
            // TODO maybe that makes it more human?
        }

        ArrayList<Vec3d> list = new ArrayList<>();

        boolean prevParticlesEnabled = particleDisabled;
        particleDisabled = true;

        for (int i = 0; i < count; i++) {
            onPreTick.accept(playerEntity);
            playerEntity.resetPosition();
            playerEntity.age++;
            playerEntity.tick();
            list.add(playerEntity.getPos());
            if (abortWhen.test(playerEntity))
                break;
        }

        soundDisabled = wasSoundDisabled;
        particleDisabled = prevParticlesEnabled;

        baseEntity.setVelocity(selfVelocity);

        mc.player.setVelocity(localVelocity);  // certain modifications assume that there is only one ClientPlayerEntity

        return new Pair<>(playerEntity, list);
    }

    public static Input initInput(float movementForward, float movementSidways) {
        Input i = new Input();
        i.movementForward = movementForward;
        i.movementSideways = movementSidways;
        return i;
    }

    public static Input initInput(float movementForward, float movementSidways, boolean jumping, boolean sneaking) {
        Input i = initInput(movementForward, movementSidways);
        i.playerInput = InputUtil.setValues(i.playerInput, jumping, sneaking);
        //i.jumping = jumping;
        //i.sneaking = sneaking;
        return i;
    }

    public static Input getClosestInput(PlayerEntity baseEntity) {
        if (Objects.equals(lastPosVec(baseEntity), baseEntity.getPos()))
            return initInput(0F, 0F);
        Vec3d prevServerPos = lastPosVec(baseEntity);
        Vec3d velocity = baseEntity.getPos().subtract(prevServerPos);

        Pair<Input, Double> best = null;
        for (Input iput : allInputs()) {
            Input input = initInput(iput.movementForward, iput.movementSideways, !baseEntity.isOnGround(), baseEntity.isSneaking());
            boolean standStill = input.movementForward == 0F && input.movementSideways == 0F;
            if (velocity.horizontalLengthSquared() > 0.0 && standStill)
                continue;

            Vec3d nextPos = (standStill) ? new Vec3d(0.0, 0.0, 0.0) : Entity.movementInputToVelocity(new Vec3d(iput.getMovementInput().x, 0, iput.getMovementInput().y), 1F, baseEntity.getYaw());

            double distance = velocity.distanceTo(nextPos);
            if (best == null || best.getRight() > distance)
                best = new Pair<>(input, distance);
        }
        return best != null ? best.getLeft() : null;
    }

    public static ArrayList<Input> allInputs() {
        ArrayList<Input> list = new ArrayList<>();
        for (float forward = -1; forward <= 1; forward++) {
            for (float sideways = -1; sideways <= 1; sideways++) {
                list.add(initInput(forward, sideways));
            }
        }
        return list;
    }

    public static BlockHitResult rayCast(Vec3d start, Vec3d end, PlayerEntity pe) {
        return mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, pe));
    }

    public static BlockHitResult rayCast(Vec3d start, Vec3d end) {
        return rayCast(start, end, mc.player);
    }

    @Nullable
    public static Direction getClosestFace(BlockPos of, BlockPos to, boolean onlyHorizontal) {
        BlockPos diff = of.subtract(to);
        BlockPos compare = new BlockPos(Math.abs(diff.getX()), Math.abs(diff.getY()), Math.abs(diff.getZ()));
        if (compare.getX() >= compare.getY() && compare.getX() >= compare.getZ() && compare.getX() != 0) {
            if (diff.getX() == compare.getX()) {
                return Direction.WEST;
            }
            return Direction.EAST;
        } else if (compare.getZ() >= compare.getY() && compare.getZ() >= compare.getX() && compare.getZ() != 0) {
            if (diff.getZ() == compare.getZ()) {
                return Direction.NORTH;
            }
            return Direction.SOUTH;
        } else if (compare.getY() >= compare.getX() && compare.getY() >= compare.getZ() && compare.getY() != 0 && !onlyHorizontal) {
            if (diff.getY() == compare.getY()) {
                return Direction.DOWN;
            }
            return Direction.UP;
        }
        return null;
    }

    public static ArrayList<Vec3d> cornersOfFace(Direction side, BlockPos where) {
        ArrayList<Vec3d> vectors = new ArrayList<>();
        boolean add = side.getDirection() == Direction.AxisDirection.POSITIVE;
        switch (side.getAxis()) {
            case X : {
                Vec3d min = MathUtils.vec3iToVec3d(where).offset(side, add ? 1 : 0);
                vectors.add(min);
                vectors.add(min.add(0, 0, 0.5));
                vectors.add(min.add(0, 0, 1));
                vectors.add(min.add(0, 1, 0));
                vectors.add(min.add(0, 1, 0.5));
                vectors.add(min.add(0, 1, 1));
                break;
            }
            case Y : {
                Vec3d min = MathUtils.vec3iToVec3d(where).offset(side, add ? 1 : 0);
                vectors.add(min);
                vectors.add(min.add(0.5, 0, 0));
                vectors.add(min.add(1, 0, 0));
                vectors.add(min.add(0, 0, 1));
                vectors.add(min.add(0.5, 0, 1));
                vectors.add(min.add(1, 0, 1));
                break;
            }
            case Z : {
                Vec3d min = MathUtils.vec3iToVec3d(where).offset(side, add ? 1 : 0);
                vectors.add(min);
                vectors.add(min.add(0.5, 0, 0));
                vectors.add(min.add(1, 0, 0));
                vectors.add(min.add(0, 1, 0));
                vectors.add(min.add(0.5, 1, 0));
                vectors.add(min.add(1, 1, 0));
                break;
            }
        }
        return vectors;
    }

    public static void removeFriend(PlayerEntity player) {
        if (isFriend(player))
            friends.remove(player.getUuidAsString());
    }

    public static void addFocus(PlayerEntity player) {
        if (!isFocus(player))
            focusTargets.add(player.getUuidAsString(), player.getName().getString());
    }

    public static void addFFAFocus(PlayerEntity player) {
        if (!isFocus(player)) {
            clearFocusTargets();
            focusTargets.add(player.getUuidAsString(), player.getName().getString());
        }
    }

    public static Path path(BlockPos currentPos, BlockPos targetPos, int maxTries) {
        return getDirectPath(currentPos, targetPos, maxTries);
    }

    private static Path getDirectPath(BlockPos currentPos, BlockPos targetPos, int maxTries) {
        ArrayList<Vec3d> corners = new ArrayList<>();
        ArrayList<Pair<Vec3d, Vec3d>> lines = new ArrayList<>();
        Vec3d currentCorner = MathUtils.vec3iToVec3d(currentPos).add(0.5, 0.0, 0.5);
        corners.add(currentCorner);
        Direction lastDir = null;
        for (int i = 0; i < maxTries; i++) {
            Direction newDir = null;
            int currentDistance = Integer.MAX_VALUE;
            BlockPos newCurrent = null;
            for (Direction dir : Direction.values()) {
                BlockPos vector = currentPos.add(dir.getVector());
                int newDist = vector.getManhattanDistance(targetPos);
                if (mc.world.getBlockState(vector).isAir() && mc.world.getBlockState(vector.up()).isAir() && newDist < currentDistance) {
                    currentDistance = newDist;
                    newCurrent = vector;
                    newDir = dir;
                }
            }

            if (newCurrent != null) {
                currentPos = newCurrent;
            }

            if (lastDir != newDir && newDir != null) {
                lastDir = newDir;
                Vec3d newCorner = MathUtils.vec3iToVec3d(newCurrent).add(0.5, 0.0, 0.5);
                lines.add(new Pair<>(currentCorner, newCorner));
                corners.add(newCorner);
                currentCorner = newCorner;
            }

            if (currentPos.getManhattanDistance(targetPos) == 0) {
                Vec3d newCorner = MathUtils.vec3iToVec3d(currentPos).add(0.5, 0.0, 0.5);
                lines.add(new Pair<>(currentCorner, newCorner));
                corners.add(newCorner);
                return new Path(currentPos, targetPos, lines, corners);
            }

            if (!mc.world.isChunkLoaded(currentPos)) {
                Vec3d newCorner = MathUtils.vec3iToVec3d(currentPos).add(0.5, 0.0, 0.5);
                lines.add(new Pair<>(currentCorner, newCorner));
                corners.add(newCorner);
                return new Path(currentPos, targetPos, lines, corners);
            }
        }
        return new Path(currentPos, targetPos, lines, corners);
    }

    /*public static List<Text> scoreboardLines(ScoreboardObjective objective) {
        List<Text> texts = new ArrayList<>();

        Scoreboard scoreboard = objective.getScoreboard();
        Collection<ScoreboardPlayerScore> collection = scoreboard.getAllPlayerScores(objective);
        List<ScoreboardPlayerScore> list = collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());
        collection = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, collection.size() - 15)) : list;
        ArrayList<com.mojang.datafixers.util.Pair<ScoreboardPlayerScore, MutableText>> list2 = Lists.newArrayListWithCapacity(collection.size());

        texts.add(objective.getDisplayName());

        for (ScoreboardPlayerScore scoreboardPlayerScore : collection) {
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            MutableText text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
            list2.add(com.mojang.datafixers.util.Pair.of(scoreboardPlayerScore, text2));
        }
        Collections.reverse(list2);
        for (com.mojang.datafixers.util.Pair<ScoreboardPlayerScore, MutableText> pair : list2) {
            ScoreboardPlayerScore scoreboardPlayerScore2 = pair.getFirst();
            Text text3 = pair.getSecond();
            String string = "" + Formatting.RED + scoreboardPlayerScore2.getScore();

            texts.add(text3);
            // the below is for the annoying red text on scoreboards
//            System.out.println(string);
        }

        return texts;
    }*/

    public static void clearFocusTargets() {
        Set<Map.Entry<CharSeq, TypeObject>> sets = focusTargets.entrySet();
        for (Map.Entry<CharSeq, TypeObject> str : sets) {
            focusTargets.remove(str.getKey());
        }
    }

    public static void removeFocus(PlayerEntity player) {
        if (isFocus(player))
            focusTargets.remove(player.getUuidAsString());
    }

    public static Entity findFirstTarget() {
        return findFirstTarget(false);
    }

    public static Entity findFirstTarget(boolean esp) {
        ArrayList<Entity> copy = findTargets(esp);
        if (copy.isEmpty()) {
            return null;
        } else {
            return copy.get(0);
        }
    }

    public static LivingEntity findFirstLivingTargetOrNull(boolean esp) {
        if (findFirstTarget(esp) instanceof LivingEntity entity) {
            return entity;
        }
        return null;
    }

    public static LivingEntity findFirstLivingTargetOrNull() {
        return findFirstLivingTargetOrNull(false);
    }

    public static ArrayList<Entity> findTargets() {
        return findTargets(false);
    }

    public static ArrayList<Entity> findTargets(boolean esp) {
        return new ArrayList<>(esp ? espTargets : targets);
    }

    public static void processTargets() {
        ClientWorld world = mc.world;
        ClientPlayerEntity toPlayer = mc.player;
        if (world == null || toPlayer == null)
            return;

        TargetsModule targetsModule = Template.moduleManager.getModule(TargetsModule.class);

        if (targetsModule == null)
            return;

        ArrayList<Entity> espEnts = new ArrayList<>();
        ArrayList<Entity> ents = new ArrayList<>();

        for (Entity e : world.getEntities()) {
            if (e == null)
                continue;

            double distance = MathUtils.closestPosBoxDistance(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), e.getBoundingBox());

            if (e != toPlayer) {
                if (!targetsModule.other.isEnabled()) {
                    if (e instanceof PlayerEntity && !targetsModule.players.isEnabled())
                        continue;

                    if (e instanceof AnimalEntity && !targetsModule.animals.isEnabled())
                        continue;

                    if (e instanceof MobEntity && !targetsModule.mobs.isEnabled())
                        continue;

                    if (e instanceof ArmorStandEntity)
                        continue;
                } else {
                    if (!targetsModule.targetOptions.selected.contains(e.getType()))
                        continue;
                }

                if (e.getName().equals(toPlayer.getName()))
                    continue;

                espEnts.add(e);

                if (distance >= targetsModule.search.getIValue())
                    continue;
                if (e instanceof PlayerEntity && isTeammate(toPlayer, (PlayerEntity) e) && !targetsModule.teammates.isEnabled())
                    continue;
                if (e instanceof PlayerEntity && isFriend((PlayerEntity) e) && !targetsModule.friends.isEnabled())
                    continue;
                if (e instanceof PlayerEntity && targetsModule.tabList.isEnabled() && !PlayerUtils.isInTabList((PlayerEntity) e))
                    continue;
                if (targetsModule.onlySee.isEnabled() && (e.isInvisible() || !toPlayer.canSee(e)))
                    continue;

                ents.add(e);
            }
        }

        ents.sort(Comparator.comparing(entity -> {
            double i = switch (targetsModule.modeP.getMode()) {
                case Distance -> MathUtils.closestPosBoxDistance(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox());
                case Health -> entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 1000;
                case HurtTime -> entity instanceof LivingEntity ? ((LivingEntity) entity).hurtTime : 1000;
                case FOV -> RotationUtils.getAngleToRotation(RotationUtils.getRotations(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), MathUtils.closestPointToBox(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox())));
                case Armor -> entity instanceof LivingEntity ? ((LivingEntity) entity).getArmor() : 1000;
            };

            double t = switch (targetsModule.modeS.getMode()) {
                case Distance -> MathUtils.closestPosBoxDistance(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox());
                case Health -> entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 1000;
                case HurtTime -> entity instanceof LivingEntity ? ((LivingEntity) entity).hurtTime : 1000;
                case FOV -> RotationUtils.getAngleToRotation(RotationUtils.getRotations(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), MathUtils.closestPointToBox(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox())));
                case Armor -> entity instanceof LivingEntity ? ((LivingEntity) entity).getArmor() : 1000;
            };

            i += t / 25;
            if (entity instanceof PlayerEntity && isFocus(((PlayerEntity) entity))) {
                i += 0;
            } else {
                i += Integer.MAX_VALUE;
            }

            return i;
        }));

        espEnts.sort(Comparator.comparing(entity -> {
            double i = switch (targetsModule.modeP.getMode()) {
                case Distance -> MathUtils.closestPosBoxDistance(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox());
                case Health -> entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 1000;
                case HurtTime -> entity instanceof LivingEntity ? ((LivingEntity) entity).hurtTime : 1000;
                case FOV -> RotationUtils.getAngleToRotation(RotationUtils.getRotations(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), MathUtils.closestPointToBox(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox())));
                case Armor -> entity instanceof LivingEntity ? ((LivingEntity) entity).getArmor() : 1000;
            };

            double t = switch (targetsModule.modeS.getMode()) {
                case Distance -> MathUtils.closestPosBoxDistance(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox());
                case Health -> entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 1000;
                case HurtTime -> entity instanceof LivingEntity ? ((LivingEntity) entity).hurtTime : 1000;
                case FOV -> RotationUtils.getAngleToRotation(RotationUtils.getRotations(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), MathUtils.closestPointToBox(toPlayer.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), entity.getBoundingBox())));
                case Armor -> entity instanceof LivingEntity ? ((LivingEntity) entity).getArmor() : 1000;
            };

            i += t / 25;
            if (entity instanceof PlayerEntity && isFocus(((PlayerEntity) entity))) {
                i += 0;
            } else {
                i += Integer.MAX_VALUE;
            }

            return i;
        }));

        targets = new ArrayList<>(ents);
        espTargets = new ArrayList<>(espEnts);
    }
    
    public static boolean isInTabList(PlayerEntity player) {
        for (PlayerListEntry entry : mc.player.networkHandler.getPlayerList()) {
            if (entry.getProfile().getName().equals(player.getName().getString())) {
                return true;
            }
        }
        return false;
    }

    public static void npe() {
        String s = null;
        String b = s.toString();
    }

    public static LivingEntity findNearestEntity(PlayerEntity toPlayer, float range, boolean seeOnly) {
        float minRange = Float.MAX_VALUE;
        LivingEntity minEntity = null;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity) {
                if (((LivingEntity) entity).isDead())
                    continue;

                if (entity instanceof PlayerEntity && isFriend(((PlayerEntity) entity)))
                    continue;

                float distance = entity.distanceTo(toPlayer);

                if (entity != toPlayer && distance <= range && (!seeOnly || toPlayer.canSee(entity))) {
                    if (distance < minRange) {
                        minRange = distance;
                        minEntity = (LivingEntity) entity;
                    }
                }
            }
        }

        return minEntity;
    }

    public static <T extends Entity> List<T> findNearestEntities(Class<T> findEntity, PlayerEntity toPlayer, float range, boolean seeOnly) {
        List<T> entities = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!findEntity.isAssignableFrom(entity.getClass())) continue;

            if (entity instanceof PlayerEntity && isFriend((PlayerEntity) entity)) continue;

            float distance = entity.distanceTo(toPlayer);

            if (entity != toPlayer && distance <= range && (!seeOnly || toPlayer.canSee(entity))) {
                entities.add((T) entity);
            }
        }

        return entities;
    }

    @Nullable
    public static ItemStack findShield(PlayerEntity player) {
        ItemStack result = null;

        for (Hand hand : Hand.values()) {
            ItemStack handStack = player.getStackInHand(hand);
            if (handStack.isOf(Items.SHIELD)) {
                result = handStack;
            }
        }

        return result;
    }

    public static boolean stopSprint(boolean check) {
        if (!mc.player.isSprinting() && check)
            return false;

        mc.player.setSprinting(false);
        mc.options.sprintKey.setPressed(false);
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        return true;
    }

    public static boolean startSprint(boolean check) {
        if ((mc.player.isSprinting() || mc.player.input.movementForward <= 0 || !mc.player.canSprint()) && check)
            return false;

        mc.player.setSprinting(true);
        mc.options.sprintKey.setPressed(true);
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        return true;
    }

    public static void attackEntity(Entity entity) {
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    public static GameMode getGameMode(PlayerEntity player) {
        if (player == null) return null;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) return null;
        return playerListEntry.getGameMode();
    }

    public static void imagePlayerHead(String playerUUID, float width, float height, float rounding) {
        if (playerHeadsTextures.containsKey(playerUUID)) {
            ImGui.image(playerHeadsTextures.get(playerUUID), width, height);
            return;
        }

        if (!playerHeadImageTasks.containsKey(playerUUID)) {
            playerHeadImageTasks.put(playerUUID, threadPool.submit(() -> {
                try {
                    URL playerSkin = PlayerSkinResolver.resolvePlayerSkin(playerUUID);
                    BufferedImage skinBufferedImg;
                    URL backup = PlayerSkinResolver.resolvePlayerSkin("bd346dd5ac1c427d87e873bdd4bf3e13");

                    if (playerSkin != null) {
                        skinBufferedImg = ImageIO.read(playerSkin);
                    } else if (backup != null) {
                        skinBufferedImg = ImageIO.read(backup);
                    } else {
                        skinBufferedImg = ImageIO.read(PlayerUtils.class.getResourceAsStream("assets/steve.png"));      // doesnt work
                    }

                    ByteBuffer buffer = BufferUtils.createByteBuffer(256);      // 8 * 8 * 4

                    for (int y = 8; y < 16; y++) {
                        for (int x = 8; x < 16; x++) {
                            Color c = new Color(skinBufferedImg.getRGB(x, y));

                            buffer.put((byte) c.getRed());      // Red component
                            buffer.put((byte) c.getGreen());    // Green component
                            buffer.put((byte) c.getBlue());     // Blue component
                            buffer.put((byte) c.getAlpha());    // Alpha component
                        }
                    }

                    buffer.flip();

                    playerHeadBuffers.put(playerUUID, buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        if (playerHeadImageTasks.get(playerUUID).isDone()) {
            int textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

            //Setup wrap mode
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            //Setup texture scaling filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            //Send texel data to OpenGL
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16, 8, 8, 0, GL_RGBA, GL_BYTE, playerHeadBuffers.get(playerUUID));

            playerHeadsTextures.put(playerUUID, textureID);

            ImGui.image(textureID, width, height);
        }
    }

    public static void imagePlayerHead2(String playerUUID, float width, float height, float rounding) {
        if (playerHeadsTextures.containsKey(playerUUID)) {
            ImGui.getWindowDrawList().addImageRounded(playerHeadsTextures.get(playerUUID),
                    ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX()+width,
                    ImGui.getCursorScreenPosY()+height,
                    0, 0,
                    1, 1,
                    ImGui.getColorU32(1,1,1,1),
                    rounding);
            return;
        }

        if (!playerHeadImageTasks.containsKey(playerUUID)) {
            playerHeadImageTasks.put(playerUUID, threadPool.submit(() -> {
                try {
                    URL playerSkin = PlayerSkinResolver.resolvePlayerSkin(playerUUID);
                    BufferedImage skinBufferedImg;
                    URL backup = PlayerSkinResolver.resolvePlayerSkin("bd346dd5ac1c427d87e873bdd4bf3e13");

                    if (playerSkin != null) {
                        skinBufferedImg = ImageIO.read(playerSkin);
                    } else if (backup != null) {
                        skinBufferedImg = ImageIO.read(backup);
                    } else {
                        skinBufferedImg = ImageIO.read(PlayerUtils.class.getResourceAsStream("assets/steve.png"));      // doesnt work
                    }

                    ByteBuffer buffer = BufferUtils.createByteBuffer(256);      // 8 * 8 * 4

                    for (int y = 8; y < 16; y++) {
                        for (int x = 8; x < 16; x++) {
                            Color c = new Color(skinBufferedImg.getRGB(x, y));

                            buffer.put((byte) c.getRed());      // Red component
                            buffer.put((byte) c.getGreen());    // Green component
                            buffer.put((byte) c.getBlue());     // Blue component
                            buffer.put((byte) c.getAlpha());    // Alpha component
                        }
                    }

                    buffer.flip();

                    playerHeadBuffers.put(playerUUID, buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        if (playerHeadImageTasks.get(playerUUID).isDone()) {
            int textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

            //Setup wrap mode
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            //Setup texture scaling filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            //Send texel data to OpenGL
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16, 8, 8, 0, GL_RGBA, GL_BYTE, playerHeadBuffers.get(playerUUID));

            playerHeadsTextures.put(playerUUID, textureID);

            ImGui.getWindowDrawList().addImage(playerHeadsTextures.get(playerUUID),
                    ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX()+width,
                    ImGui.getCursorScreenPosY()+height);
        }
    }

    public static Identifier getOrCreateHead(String playerUUID) {
        if (playerFaceTextures.containsKey(playerUUID)) {
            return playerFaceTextures.get(playerUUID);
        }

        if (!tierTasks.containsKey(playerUUID)) {
            tierTasks.put(playerUUID, threadPool.submit(() -> {
                try {
                    if (fallbackTexture == null) {
                        fallbackTexture = Template.cTextureManager.registerDynamicTexture("face-itsmesooty", new NativeImageBackedTexture(NativeImage.read(PlayerUtils.class.getClassLoader().getResourceAsStream("assets/Sootysplash.png"))));
                    }

                    URL playerSkin = PlayerSkinResolver.resolvePlayerSkin(playerUUID);
                    InputStream stream = null;

                    if (playerSkin != null) {
                        stream = playerSkin.openStream();
                    } else if (fallbackTexture != null) {
                        playerFaceTextures.put(playerUUID, fallbackTexture);
                        return;
                    }

                    if (stream == null) {
                        return;
                    }

                    Identifier id = Template.cTextureManager.registerDynamicTexture("face-" + playerUUID + "-e", new NativeImageBackedTexture(NativeImage.read(stream)));

                    playerFaceTextures.put(playerUUID, id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        return null;
    }

    @Nullable
    public static HashMap<String, String> processTiers(String ign) {
        if (cachedTiers.containsKey(ign))
            return cachedTiers.get(ign);


        if (!tierTasks.containsKey(ign)) {
            tierTasks.put(ign, threadPool.submit(() -> {
                tierApiResponses.put(ign, callURL(String.format("https://mctiers.com/api/search_profile/%s", ign)));
            }));
            return null;
        }

        if (tierTasks.get(ign).isDone()) {
            String response = tierApiResponses.get(ign);
            HashMap<String, String> tiers = new HashMap<>();

            tiers.put("Pot:", parseTier("\"pot\":{\"tier\":", response));
            tiers.put("UHC:", parseTier("\"uhc\":{\"tier\":", response));
            tiers.put("Sword:", parseTier("\"sword\":{\"tier\":", response));
            tiers.put("Axe:", parseTier("\"axe\":{\"tier\":", response));
            tiers.put("SMP:", parseTier("\"smp\":{\"tier\":", response));
            tiers.put("Neth:", parseTier("\"neth_pot\":{\"tier\":", response));
            tiers.put("CPvP:", parseTier("\"vanilla\":{\"tier\":", response));
            cachedTiers.put(ign, tiers);

            List<String> toremove = new ArrayList<>();
            for (Map.Entry<String, String> s : tiers.entrySet()) {
                if (s.getValue().equals("N/A")) {
                    toremove.add(s.getKey());
                }
            }
            toremove.forEach(tiers::remove);

            return tiers;
        }

        return null;
    }

    private static String parseTier(String tierString, String from) {
        String t = "N/A";
        if (from.contains(tierString)) {
            int index = from.indexOf(tierString);
            int tier = Integer.parseInt(from.substring(index + tierString.length(), index + tierString.length() + 1));
            String pos = ",\"pos\":";
            int posIndex = from.indexOf(pos, index);
            int lowOrHigh = Integer.parseInt(from.substring(posIndex + pos.length(), posIndex + pos.length() + 1));
            if (lowOrHigh == 1) {
                t = "LT" + tier;
            } else {
                t = "HT" + tier;
            }
        }
        return t;
    }

    private static String callURL(String urlStr) {
        String response = "";
        //https://www.w3schools.in/java/examples/read-url-content-in-java
        try {
            String parseLine; /* variable definition */
            /* create objects */
            URL URL = new URL(urlStr);
            BufferedReader br = new BufferedReader(new InputStreamReader(URL.openStream()));

            while ((parseLine = br.readLine()) != null) {
                /* read each line */
                response = response.concat(parseLine);
            }
            br.close();

        } catch (IOException ignored) {
        }

        return response;
    }

    public static int getColorFromMC(String s) {
        return switch (s) {
            case "§0" -> ImGui.getColorU32(0f, 0f, 0f, 1f);
            case "§1" -> ImGui.getColorU32(0f, 0f, 170f / 255f, 1f);
            case "§2" -> ImGui.getColorU32(0f, 170f / 255f, 0f, 1f);
            case "§3" -> ImGui.getColorU32(0f, 170f / 255f, 170f / 255f, 1f);
            case "§4" -> ImGui.getColorU32(170f / 255f, 0f, 0f, 1f);
            case "§5" -> ImGui.getColorU32(170f / 255f, 0f, 170f / 255f, 1f);
            case "§6" -> ImGui.getColorU32(170f / 255f, 170f / 255f, 0f, 1f);
            case "§7" -> ImGui.getColorU32(170f / 255f, 170f / 255f, 170f / 255f, 1f);
            case "§8" -> ImGui.getColorU32(85f / 255f, 85f / 255f, 85f / 255f, 1f);
            case "§9" -> ImGui.getColorU32(85f / 255f, 85f / 255f, 1f, 1f);
            case "§a" -> ImGui.getColorU32(85f / 255f, 1f, 85f / 255f, 1f);
            case "§b" -> ImGui.getColorU32(85f / 255f, 1f, 1f, 1f);
            case "§c" -> ImGui.getColorU32(1f, 85f / 255f, 85f / 255f, 1f);
            case "§d" -> ImGui.getColorU32(1f, 85f / 255f, 1f, 1f);
            case "§e" -> ImGui.getColorU32(1f, 1f, 85f / 255f, 1f);
            case "§f" -> ImGui.getColorU32(1f, 1f, 1f, 1f);
            default -> -69420;
        };
        //if (s.equals("§k")) return -1;
        //if (s.equals("§l")) return -2;
        //if (s.equals("§m")) return -3;
        //if (s.equals("§n")) return -4;
        //if (s.equals("§o")) return -5;
        //if (s.equals("§r")) return -6;
        //if (s.equals("§k")) return -1;
        //if (s.equals("§l")) return -2;
        //if (s.equals("§m")) return -1;
        //if (s.equals("§n")) return -1;
        //if (s.equals("§o")) return -1;
        //if (s.equals("§r")) return -1;
    }

    public static float[] getFloatColorFromMC(String s) {
        return switch (s) {
            case "§0" -> new float[]{0f, 0f, 0f, 1f};
            case "§1" -> new float[]{0f, 0f, 170f / 255f, 1f};
            case "§2" -> new float[]{0f, 170f / 255f, 0f, 1f};
            case "§3" -> new float[]{0f, 170f / 255f, 170f / 255f, 1f};
            case "§4" -> new float[]{170f / 255f, 0f, 0f, 1f};
            case "§5" -> new float[]{170f / 255f, 0f, 170f / 255f, 1f};
            case "§6" -> new float[]{170f / 255f, 170f / 255f, 0f, 1f};
            case "§7" -> new float[]{170f / 255f, 170f / 255f, 170f / 255f, 1f};
            case "§8" -> new float[]{85f / 255f, 85f / 255f, 85f / 255f, 1f};
            case "§9" -> new float[]{85f / 255f, 85f / 255f, 1f, 1f};
            case "§a" -> new float[]{85f / 255f, 1f, 85f / 255f, 1f};
            case "§b" -> new float[]{85f / 255f, 1f, 1f, 1f};
            case "§c" -> new float[]{1f, 85f / 255f, 85f / 255f, 1f};
            case "§d" -> new float[]{1f, 85f / 255f, 1f, 1f};
            case "§e" -> new float[]{1f, 1f, 85f / 255f, 1f};
            default -> new float[]{1f, 1f, 1f, 1f};
        };
        //if (s.equals("§k")) return new float[]{-1f};
        //if (s.equals("§l")) return new float[]{-2f};
        //if (s.equals("§m")) return new float[]{-1f};
        //if (s.equals("§n")) return new float[]{-1f};
        //if (s.equals("§o")) return new float[]{-1f};
        //if (s.equals("§r")) return new float[]{-1f};
    }

    public static void drawMcText(Text text) {
        String s = text.getString();
        //int lastColor = 0xFFFFFFFF;
        float[] lastFloatColor = new float[]{1f, 1f, 1f, 1f};
        //int lastModifier = -1;
        for (int i = 0; i < s.length(); i++) {
            String c = "" + s.charAt(i);
            if (i != 0 && ("" + s.charAt(i - 1)).equals("§")) continue;
            if (c.equals("§")) {
                String mcColor = c + s.charAt(i + 1);
                lastFloatColor = getFloatColorFromMC(mcColor);
                //if (getColorFromMC(mcColor) > 0) {
                //    //lastColor = getColorFromMC(mcColor);
                //    lastFloatColor = getFloatColorFromMC(mcColor);
                //}
                //else {
                //    lastModifier = getColorFromMC(mcColor);
                //}
            } else {
                ImGui.pushFont(ImguiLoader.poppins24);
                ImGui.textColored(lastFloatColor[0], lastFloatColor[1], lastFloatColor[2], 1f, c);
                ImGui.sameLine(0, 0);
                ImGui.popFont();
                //} else if (lastModifier == -2) {
                //    ImGui.pushFont(ImguiLoader.getMediumPoppins24());
                //    ImGui.textColored(lastColor, c);
                //    ImGui.sameLine(0,0);
                //    ImGui.popFont();
                //}
            }
        }
    }

    public static void drawMcTextColorless(Text text) {
        String s = text.getString();
        for (int i = 0; i < s.length(); i++) {
            String c = "" + s.charAt(i);
            if (i != 0 && ("" + s.charAt(i - 1)).equals("§")) continue;
            if (c.equals("§")) {
                continue;
            }
            if (i != 0) ImGui.sameLine(0, 0);
            ImGui.text(c);
        }
    }

    public static void doItemUseHand(Hand hand) {
        ActionResult actionResult3;
        ItemStack itemStack = mc.player.getStackInHand(hand);
        if (!itemStack.isItemEnabled(mc.world.getEnabledFeatures())) {
            return;
        }
        if (mc.crosshairTarget != null) {
            switch (mc.crosshairTarget.getType()) {
                case ENTITY: {
                    EntityHitResult entityHitResult = (EntityHitResult)mc.crosshairTarget;
                    Entity entity = entityHitResult.getEntity();
                    if (!mc.world.getWorldBorder().contains(entity.getBlockPos())) {
                        return;
                    }
                    ActionResult actionResult = mc.interactionManager.interactEntityAtLocation(mc.player, entity, entityHitResult, hand);
                    if (!actionResult.isAccepted()) {
                        actionResult = mc.interactionManager.interactEntity(mc.player, entity, hand);
                    }
                    if (!actionResult.isAccepted()) {
                        break;
                    }
                    if (PlayerUtils.shouldSwingHand(actionResult)) {
                        mc.player.swingHand(hand);
                    }
                    return;
                }
                case BLOCK: {
                    BlockHitResult blockHitResult = (BlockHitResult)mc.crosshairTarget;
                    int i = itemStack.getCount();
                    ActionResult actionResult2 = mc.interactionManager.interactBlock(mc.player, hand, blockHitResult);
                    if (actionResult2.isAccepted()) {
                        if (PlayerUtils.shouldSwingHand(actionResult2)) {
                            mc.player.swingHand(hand);
                            if (!itemStack.isEmpty() && (itemStack.getCount() != i || mc.interactionManager.hasCreativeInventory())) {
                                mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                            }
                        }
                        return;
                    }
                    if (actionResult2 != ActionResult.FAIL) {
                        break;
                    }
                    return;
                }
            }
        }
        if (itemStack.isEmpty() || !(actionResult3 = mc.interactionManager.interactItem(mc.player, hand)).isAccepted()) {
            return;
        }
        if (PlayerUtils.shouldSwingHand(actionResult3)) {
            mc.player.swingHand(hand);
        }
        mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
    }

    public static HitResult getHitResult(PlayerEntity entity, Predicate<Entity> pred, float yaw, float pitch, double reach, boolean throughWalls, double boxExpansion, Vec3d eyes) {
        HitResult result = null;

        if (entity != null) {
            if (mc.world != null) {
                double d = Math.max(PlayerUtils.getReachDistance(), reach);

                Vec3d rotationVec = RotationUtils.getPlayerLookVec(yaw, pitch);
                Vec3d range = eyes.add(rotationVec.x * d, rotationVec.y * d, rotationVec.z * d);
                result = mc.world.raycast(new RaycastContext(eyes, range, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));

                boolean bl = false;
                double e = d;
                if (mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                    e = 6.0;
                    d = e;
                } else {
                    if (d > reach) {
                        bl = true;
                    }
                }

                e *= e;
                if (result != null) {
                    e = result.getPos().squaredDistanceTo(eyes);
                }

                Vec3d vec3d3 = eyes.add(rotationVec.x * d, rotationVec.y * d, rotationVec.z * d);
                Box box = entity.getBoundingBox().stretch(rotationVec.multiply(d)).expand(1.0, 1.0, 1.0);
                EntityHitResult entityHitResult = getEntityHitResult(entity, eyes, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.canHit() && pred.test(entityx), e, boxExpansion, throughWalls);
                if (entityHitResult != null) {
                    Vec3d vec3d4 = entityHitResult.getPos();
                    double g = eyes.squaredDistanceTo(vec3d4);
                    if (bl && g > reach * reach) {
                        result = BlockHitResult.createMissed(vec3d4, Direction.getFacing(rotationVec.x, rotationVec.y, rotationVec.z), BlockPos.ofFloored(vec3d4));
                    } else if (g < e || result == null || throughWalls) {
                        result = entityHitResult;
                    }
                }
            }
        }

        return result;
    }

    public static HitResult getHitResult(PlayerEntity player, Predicate<Entity> pred, float yaw, float pitch, double reach, boolean throughWalls, double boxExpansion) {
        return getHitResult(player, pred, yaw, pitch, reach, throughWalls, boxExpansion, player.getCameraPosVec(1f));
    }

    public static HitResult getHitResult(PlayerEntity player, Predicate<Entity> pred, float yaw, float pitch, double reach, boolean throughWalls) {
        return getHitResult(player, pred, yaw, pitch, reach, throughWalls, 0);
    }

    public static HitResult getHitResult(PlayerEntity player, Predicate<Entity> pred, float yaw, float pitch) {
        return getHitResult(player, pred, yaw, pitch, 3.0, false);
    }

    public static HitResult getHitResult(PlayerEntity player, float yaw, float pitch) {
        return getHitResult(player, e -> true, yaw, pitch);
    }

    public static HitResult getHitResult(PlayerEntity player, float yaw, float pitch, Vec3d eyes) {
        return getHitResult(player, e -> true, yaw, pitch, 3.0, false, 0, eyes);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double reach, double boxExpansion, boolean throughWalls) {
        World world = entity.getWorld();
        double e = reach;
        Entity entity2 = null;
        Vec3d vec3d = null;
        for (Entity entity3 : world.getOtherEntities(entity, box, predicate)) {
            Vec3d vec3d2;
            double f;
            Box box2 = entity3.getBoundingBox().expand(entity3.getTargetingMargin()).expand(boxExpansion);
            Optional<Vec3d> optional = box2.raycast(min, max);
            // if our eyes are inside a bounding box
            if (box2.contains(min)) {
                if (e < 0.0) continue;
                entity2 = entity3;
                vec3d = optional.orElse(min);
                e = 0.0;
                continue;
            }

            // if we missed the box, or they're out of range (or farther than our current target), we continue
            if (optional.isEmpty() || ((f = min.squaredDistanceTo(vec3d2 = optional.get())) >= e && !throughWalls) && e != 0.0)
                continue;


            if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                if (e != 0.0) continue;
                entity2 = entity3;
                vec3d = vec3d2;
                continue;
            }

            entity2 = entity3;
            vec3d = vec3d2;
            e = f;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec3d);
    }

    public static boolean isBlockUnder() {
        if (mc.player.getPos().y < 0.0) {
            return false;
        }
        for (int offset = 0; offset < (int)mc.player.getPos().y + 2; offset += 2) {
             Box bb = mc.player.getBoundingBox().offset(0.0, -offset, 0.0);
            if (Streams.stream(mc.world.getBlockCollisions(mc.player, bb)).findAny().isEmpty()) continue;
            return true;
        }
        return false;
    }

    public static boolean isBlockUnder(int distance) {
        for (int y = (int)mc.player.getPos().y; y >= (int)mc.player.getPos().y - distance; --y) {
            if (mc.world.getBlockState(new BlockPos((int) mc.player.getPos().x, y, (int) mc.player.getPos().z)).getBlock() instanceof AirBlock) continue;
            return true;
        }
        return false;
    }

    public static boolean shouldSwingHand(ActionResult result) {
        return result == ActionResult.SUCCESS || result == ActionResult.SUCCESS_SERVER;
    }
}
