package template.rip.deprecated;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.dimension.DimensionType;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.crystal.AutoHitCrystalModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.NumberSetting;

import static template.rip.api.util.CrystalUtils.canPlaceCrystalServer;
import static template.rip.api.util.CrystalUtils.isCrystalBroken;

public class AutoCrystalModule extends Module {
    static final PlayerInteractEntityC2SPacket.InteractTypeHandler ATTACK = new PlayerInteractEntityC2SPacket.InteractTypeHandler() {

        @Override
        public PlayerInteractEntityC2SPacket.InteractType getType() {
            return PlayerInteractEntityC2SPacket.InteractType.ATTACK;
        }

        @Override
        public void handle(PlayerInteractEntityC2SPacket.Handler handler) {
            handler.attack();
        }

        @Override
        public void write(PacketByteBuf buf) {
        }
    };
    public final BooleanSetting onRmb = new BooleanSetting(this, true, "On RMB");
    public final BooleanSetting noCountGlitch = new BooleanSetting(this, Description.of("Correctly decrements crystals"), true, "No Count Glitch");
    public final BooleanSetting noBounce = new BooleanSetting(this, Description.of("Prevents crystals from bouncing in your hotbar"), true, "No Bounce");
    public final MinMaxNumberSetting placeDelay = new MinMaxNumberSetting(this, 15, 45, 0, 200, 1, "Place delays");
    public final MinMaxNumberSetting breakDelay = new MinMaxNumberSetting(this, 20, 40, 0, 200, 1, "Break delays");
    public final BooleanSetting uspvpHit = new BooleanSetting(this, false, "USPVP/MCPVP Break");
    public final NumberSetting fastMode = new NumberSetting(this, 0d, 0d, 100d, 1d, "Fast Mode Chance");
    public final NumberSetting hurtTime = new NumberSetting(this, Description.of("Only breaks crystals when your target is at or below this hurttime, recommended for preserving crystals"), 10d, 0d, 10d, 1d, "Target HurtTime break").setAdvanced();
    public final BooleanSetting idPredict = new BooleanSetting(this, Description.of("Predicts the crystal's ID before it spawns in server side"), false, "ID predict (can kick)").setAdvanced();
    public final BooleanSetting onlyWhenSafe = new BooleanSetting(this, Description.of("Only predicts when other players are unlikely to spawn new entities"), false, "Safety ID predict check").setAdvanced();
    public final BooleanSetting reachCheck = new BooleanSetting(this, Description.of("Only predicts when you've placed on the top of a block, helps with bypassing anticheats"), false, "Correct attack check").setAdvanced();
    public final NumberSetting idPredictOffset = new NumberSetting(this, 1, 0, 5, 1, "ID offset from highest").setAdvanced();
    public final NumberSetting packetsSend = new NumberSetting(this, 1, 1, 5, 1, "Amount of packets sent").setAdvanced();
    private long timer = System.currentTimeMillis();
    private int highestID = 0, placedSafe = 0;
    private DimensionType dt = null;
    private Box fakeCrystalBox = null;

    public AutoCrystalModule() {
        super(Category.CRYSTAL, Description.of("Automatically spams crystals"), "AutoCrystal");
    }

    @Override
    public void onEnable() {
        highestID = 0;
        placedSafe = 0;
        dt = null;
        timer = System.currentTimeMillis();
        fakeCrystalBox = null;
    }

    public boolean passedTime() {
        return timer <= System.currentTimeMillis();
    }

    public void reset(int time) {
        timer = System.currentTimeMillis() + time;
    }

    public void placeCrystal() {
        if (!nullCheck())
            return;

        if (passedTime()) {
            if (mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                BlockHitResult blockHit;
                if (mc.crosshairTarget instanceof BlockHitResult && (blockHit = ((BlockHitResult) mc.crosshairTarget)).getType() == HitResult.Type.BLOCK) {
                    if (canPlaceCrystalServer(blockHit.getBlockPos(), true)) {
                        if (Template.isClickSim())
                            Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());

                        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                        if (result.isAccepted() && PlayerUtils.shouldSwingHand(result)) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }

                        if (uspvpHit.isEnabled()) {
                            BlockPos pos = blockHit.getBlockPos().up();
                            fakeCrystalBox = new Box(MathUtils.Vec3dWithY(pos.south().west().toCenterPos(), pos.getY()), MathUtils.Vec3dWithY(pos.north().east().toCenterPos(), pos.getY() + 2));
                        }

                        if (blockHit.getSide() == Direction.UP) {
                            placedSafe++;
                        }

                        reset(placeDelay.getRandomInt());
                    }
                } else if (mc.crosshairTarget instanceof EntityHitResult) {
                    EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
                    if (MathUtils.getRandomInt(1, 100) <= fastMode.getIValue()) {
                        if (entityHit.getEntity() instanceof SlimeEntity) {
//                            HitResult hitResult = PlayerUtils.getHitResult(mc.player, e -> !e.isInvisible());

//                            if (hitResult instanceof EntityHitResult entityHitNoInvisibles && entityHitNoInvisibles.getEntity() instanceof EndCrystalEntity crystal) {
//                                fastPlace(crystal);
//                            }
                        } else if (entityHit.getEntity() instanceof EndCrystalEntity) {
                            fastPlace((EndCrystalEntity) entityHit.getEntity());
                        }
                    }
                }
            }
        }
    }

    public void fastPlace(EndCrystalEntity crystal) {
        if (mc.player == null || mc.interactionManager == null || mc.world == null || mc.getNetworkHandler() == null)
            return;

        if (passedTime()) {
            if (isCrystalBroken(crystal)) {
                double reach = PlayerUtils.getReachDistance();
                Vec3d cameraPosVec = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
                Vec3d rotationVec = RotationUtils.getPlayerLookVec(mc.player);
                Vec3d range = cameraPosVec.add(rotationVec.x * reach, rotationVec.y * reach, rotationVec.z * reach);

                BlockHitResult blockHit = mc.world.raycast(new RaycastContext(cameraPosVec, range, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
                BlockState blockState = mc.world.getBlockState(blockHit.getBlockPos());

                if (blockState.isOf(Blocks.OBSIDIAN) || blockState.isOf(Blocks.BEDROCK)) {
                    if (Template.isClickSim())
                        Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());

                    ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                    if (result.isAccepted() && PlayerUtils.shouldSwingHand(result)) { /*result.shouldSwingHand())*/
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if (blockHit.getSide() == Direction.UP)
                        placedSafe++;

                    reset(placeDelay.getRandomInt());
                }
            }
        }
    }

    public boolean isSafe() {
        if (mc.world == null)
            return false;

        for (PlayerEntity pe : mc.world.getPlayers()) {
            if (pe == mc.player)
                continue;

            if (badHand(pe.getMainHandStack().getItem()) || badHand(pe.getOffHandStack().getItem()))
                return false;
        }

        return true;
    }

    public boolean badHand(Item i) {
        return (i instanceof EndCrystalItem || i instanceof BowItem || i instanceof CrossbowItem || i instanceof SnowballItem || i instanceof EggItem || i instanceof FishingRodItem || i instanceof SpawnEggItem || i instanceof ExperienceBottleItem || i instanceof SplashPotionItem || i instanceof PickaxeItem || i instanceof ShovelItem);
    }

    public void updateID() {
        if (mc.world == null)
            return;

        if (dt != mc.world.getDimension()) {
            highestID = 0;
            dt = mc.world.getDimension();
        }

        int highest = highestID;
        for (Entity e : mc.world.getEntities()) {

            if (e.getId() > highest) {
                highest = e.getId();
            }

            //earthHack had this in case of a minecraft moment
            if (highest > highestID) {
                highestID = highest;
            }

        }
    }

    public void breakCrystal() {
        if (mc.player == null || mc.interactionManager == null)
            return;

        Entity target = PlayerUtils.findFirstTarget();
        if (target instanceof LivingEntity && ((LivingEntity) target).hurtTime > hurtTime.getIValue() && target.isAlive())
            return;

        if (passedTime()) {
            if (mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                if (mc.crosshairTarget instanceof EntityHitResult result && (result.getEntity() instanceof EndCrystalEntity || result.getEntity() instanceof SlimeEntity)) {
                    if (isCrystalBroken(result.getEntity()))
                        return;

                    if (Template.isClickSim())
                        MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());

                    mc.interactionManager.attackEntity(mc.player, result.getEntity());
                    mc.player.swingHand(Hand.MAIN_HAND);

                    reset(breakDelay.getRandomInt());
                } else if (uspvpHit.isEnabled()) {
                    if (inPredictBox() && passedTime()) {
                        if (Template.isClickSim())
                            MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());

                        BlockHitResult bhr;
                        if (mc.crosshairTarget instanceof BlockHitResult && (bhr = ((BlockHitResult) mc.crosshairTarget)).getType() == HitResult.Type.BLOCK) {
                            BlockPos blockPos = bhr.getBlockPos();
                            if (!mc.world.getBlockState(blockPos).isAir()) {
                                mc.interactionManager.attackBlock(blockPos, bhr.getSide());
                            }
                        } else {
                            if (mc.interactionManager.hasLimitedAttackSpeed()) {
                                mc.attackCooldown = 10;
                            }
                            mc.player.resetLastAttackedTicks();
                        }

                        mc.player.swingHand(Hand.MAIN_HAND);

                        fakeCrystalBox = null;

                        reset(breakDelay.getRandomInt());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntity(PacketEvent.Receive event) {
        Packet<?> p = event.packet;
        if (p instanceof EntitySpawnS2CPacket)
            checkID(((EntitySpawnS2CPacket)p).getEntityId());

        if (p instanceof EntityPositionS2CPacket)
            checkID(((EntityPositionS2CPacket)p).entityId());

        if (p instanceof EntityAnimationS2CPacket)
            checkID(((EntityAnimationS2CPacket)p).getEntityId());

        if (p instanceof EntityVelocityUpdateS2CPacket)
            checkID(((EntityVelocityUpdateS2CPacket)p).getEntityId());

        if (p instanceof EntityTrackerUpdateS2CPacket)
            checkID(((EntityTrackerUpdateS2CPacket)p).id());

        if (p instanceof EntityDamageS2CPacket)
            checkID(((EntityDamageS2CPacket)p).entityId());

        if (p instanceof EntityPassengersSetS2CPacket)
            checkID(((EntityPassengersSetS2CPacket)p).getEntityId());
    }

    public void checkID(int id) {
        if (id > highestID)
            highestID = id;
    }

    private boolean inPredictBox() {
        if (fakeCrystalBox == null) {
            return false;
        }
        Vec3d cameraPosVec = mc.player.getCameraPosVec(1f);
        double d = Math.max(PlayerUtils.getReachDistance(), 3.0);
        Vec3d rotationVec = RotationUtils.getPlayerLookVec(mc.player.getYaw(), mc.player.getPitch());
        Vec3d vec3d3 = cameraPosVec.add(rotationVec.x * d, rotationVec.y * d, rotationVec.z * d);
        return fakeCrystalBox.raycast(cameraPosVec, vec3d3).isPresent();
    }

   /* @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (!nullCheck()) {
            return;
        }
        if (fakeCrystalBox == null) {
            return;
        }
        RenderUtils.Render3D.renderBox(fakeCrystalBox, inPredictBox() ? Color.GREEN : Color.RED, 75, event.context);
    }*/

    @EventHandler
    public void onTick(TickEvent.Post event) {
        updateID();
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        AutoHitCrystalModule ahc = Template.moduleManager.getModule(AutoHitCrystalModule.class);
        if (!KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()) && onRmb.isEnabled() && (ahc == null || !ahc.isEnabled() || !ahc.fullAuto.isEnabled() || !ahc.crystalling))
            return;

        if (mc.currentScreen != null || !nullCheck())
            return;

        if (idPredict.isEnabled() && (!reachCheck.isEnabled() || placedSafe > 0) && (isSafe() || !onlyWhenSafe.isEnabled())) {
            updateID();
            placedSafe--;
            for (int i = 0; i < packetsSend.getIValue(); i++) {
                int id = highestID + idPredictOffset.getIValue() + i;
                Entity e = mc.world.getEntityById(id);
                if (e == null || e instanceof EndCrystalEntity) {
                    PlayerInteractEntityC2SPacket pkt = new PlayerInteractEntityC2SPacket(id, mc.player.isSneaking(), ATTACK);
                    mc.getNetworkHandler().sendPacket(pkt);
                    if (Template.isClickSim())
                        Template.mouseSimulation().mouseClick(mc.options.attackKey.boundKey.getCode());
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
        breakCrystal();
        placeCrystal();
    }
}
