package template.rip.module.modules.blatant;

import net.minecraft.block.SlabBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
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

import static template.rip.module.modules.misc.FlagDetectorModule.nextToggle;

public class SpeedModule extends Module {

    public enum modeEnum{/*Hypixel, */HypixelLatest, HypixelGround, HypixelStrafe, HypixelTrig, Rotation, BlocksMC, Hoplite, Grim, Vulcan, Strafe};
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.HypixelGround, "Mode");

    // Hypixel
    //public final NumberSetting horizontalAcc = new NumberSetting("Horizontal", this, 1, 0, 1, 0.01);
    //public final NumberSetting verticalAcc = new NumberSetting("Vertical", this, 1, 0, 1, 0.01);
    //public final NumberSetting least = new NumberSetting("Least", this, 1, 0, 1, 0.01);
    //public final BooleanSetting lowhop = new BooleanSetting("LowHop", this, false);

    // Hypixel Latest
    public final NumberSetting hypLatestBoost = new NumberSetting(this, 1, 1, 1.8, 0.01, "LH Boost");
    public final NumberSetting hypLatestStrafeBoost = new NumberSetting(this, 1.1, 0.8, 1.1, 0.01, "LH Strafe Boost");
    public final NumberSetting hypLatestSpeedBoost = new NumberSetting(this, 0.01, 0.01, 0.1, 0.01, "LH Speed Boost");
    // public final NumberSetting hypLatestGroundDist = new NumberSetting("Ground Dist", this, 0.5, 0, 1, 0.01);
    public final BooleanSetting hypLatestFastFall = new BooleanSetting(this, false, "LH Fast Fall");
    public final BooleanSetting hypLatestDamageBoost = new BooleanSetting(this, false, "LH Damage Boost");

    // Hypixel Ground
    public final NumberSetting acc = new NumberSetting(this, 1, 1, 1.8, 0.01, "GH Boost");
    public final NumberSetting speedBoost = new NumberSetting(this, 0.01, 0.01, 0.1, 0.01, "GH Speed Boost");
    public final BooleanSetting fastFall = new BooleanSetting(this, false, "GH Fast Fall");
    public final BooleanSetting damageBoost = new BooleanSetting(this, false, "GH Damage Boost");

    // Hypixel Strafe
    public final NumberSetting hypStrafeBoost = new NumberSetting(this, 1, 1, 1.8, 0.01, "SH Boost");
    public final NumberSetting strafeBoost = new NumberSetting(this, 1, 0.5, 1.8, 0.01, "SH Strafe Boost");
    public final NumberSetting sideBoost = new NumberSetting(this, 1, 0.5, 1.8, 0.01, "Side Boost");
    public final NumberSetting strafeMaxBoost = new NumberSetting(this, 1.8, 0.5, 1.8, 0.01, "Max Boost");
    public final NumberSetting strafeFallDist = new NumberSetting(this, 0, 0, 1, 0.01, "Strafe Fall Dist");
    public final NumberSetting hypStrafeSpeedBoost = new NumberSetting(this, 0.01, 0.01, 0.1, 0.01, "SH Speed Boost");
    //public final NumberSetting hypStrafeMaxOffground = new NumberSetting("Max Offground", this, 6, 4, 11, 1);
    //public final NumberSetting hypStrafeFastFallTick = new NumberSetting("Fast Fall Tick", this, 6, 4, 11, 1);
    public final BooleanSetting hypStrafeFastFall = new BooleanSetting(this, false, "H Fast Fall");

    public final BooleanSetting hypStrafeStrafe = new BooleanSetting(this, true, "Multiply Strafe ");
    public final BooleanSetting hypStrafeStopStrafe = new BooleanSetting(this, false, "Stop Strafe ");
    public final BooleanSetting hypStrafeDupPacket = new BooleanSetting(this, false, "Duplicate Packet ");
    public final BooleanSetting hypStrafeSpoof = new BooleanSetting(this, false, "Spoof Ground ");
    public final BooleanSetting hypStrafeSemi = new BooleanSetting(this, false, "Semi ");
    public final BooleanSetting hypStrafeSidewaysOnly = new BooleanSetting(this, false, "Sideways only ");
    public final BooleanSetting hypStrafeFlexible = new BooleanSetting(this, false, "Flexible ");
    public final BooleanSetting hypStrafeStopToggle = new BooleanSetting(this, false, "Stop on toggle ");
    public final BooleanSetting hypStrafeKeepStrafe = new BooleanSetting(this, false, "Keep Strafe ");

    // Hypixel Trig
    public final NumberSetting trigAccBoost = new NumberSetting(this, 1, 1, 1.8, 0.01, "T Boost");
    public final NumberSetting trigPreBoost = new NumberSetting(this, 1, 0.5, 1.8, 0.01, "Pre Boost");
    public final NumberSetting trigBoost = new NumberSetting(this, 1, 0.5, 1.8, 0.01, "Trig Boost");
    public final BooleanSetting trigFlexible = new BooleanSetting(this, true, "Trig Flexible");
    public final BooleanSetting keepTrig = new BooleanSetting(this, true, "Keep Trig");
    public final NumberSetting trigStrength = new NumberSetting(this, 70, 0, 100, 1, "Trig Amount");
    public final NumberSetting strafeLimit = new NumberSetting(this, 4, 0, 10, 1, "Strafe Limit");
    public final NumberSetting trigMinSpeed = new NumberSetting(this, 0, 0, 1.0, 0.01, "Min Speed");

    // Rotation
    public final NumberSetting rotBoost = new NumberSetting(this, 1, 1, 1.8, 0.01, "R Boost");
    public final BooleanSetting rotGround = new BooleanSetting(this, false, "Only Ground ");

    // BlocksMC
    public final BooleanSetting lowhopbmc = new BooleanSetting(this, false, "LowHop ");

    // Grim
    public final NumberSetting maxCollisions = new NumberSetting(this, Description.of("Grim gives some leniency per collision.\nUse 0 to disable the limit."), 0, 0, 10, 1, "Max Collisions");
    public final NumberSetting leniencePerEntity = new NumberSetting(this, Description.of("The amount of leniency per collision.\n0.08 works well and doesn't cause flags"), 0.08, 0, 0.08, 0.01, "Lenience");
    public final NumberSetting distance = new NumberSetting(this, Description.of("The distance to entity to count a collision"), 1, 0, 1.5, 0.1, "Distance");
    public final BooleanSetting targetStrafe = new BooleanSetting(this, false, "Target Strafe");
    public final NumberSetting targetDistance = new NumberSetting(this, Description.of("The distance to strafe towards target"), 1, 0, 1.5, 0.1, "Target Distance");
    public final BooleanSetting grimGround = new BooleanSetting(this, true, "Ground");
    public final BooleanSetting grimAir = new BooleanSetting(this, true, "Air");

    // Vulcan
    public final NumberSetting vulcanAcc = new NumberSetting(this, 1.8, 1, 1.8, 0.01, "V Speed");
    public final BooleanSetting vulcanFastFall = new BooleanSetting(this, false, "V Fast Fall");
    public final BooleanSetting vulcanStrafe = new BooleanSetting(this, false, "Strafe ");

    // Strafe
    public final NumberSetting strafeSpeed = new NumberSetting(this, 1, 1, 10, 0.01, "S Speed");

    public final BooleanSetting jump = new BooleanSetting(this, true, "AutoJump");

    private boolean isColliding = false;
    private boolean wasGround = false;
    private int onGroundTicks = 0;
    private int offGroundTicks = 0;
    private double lastKb = 0;
    private Vec2f lastMoveInput;
    private Vec2f strafeMoveInput;
    private boolean wasStrafed = false;
    private boolean strafeAfter = false;
    private double lastSpeed = 0;
    private boolean shouldSpoofGround = false;
    private boolean tickSpoofGround = false;
    private boolean didStrafe = false;
    private int trigStrafeCount = 0;
    private float groundDist = 0;
    private boolean readyFastFall = false;

    public SpeedModule(Category category, Description description, String name) {
        super(category, description, name);

        hypLatestBoost.addConditionMode(mode, modeEnum.HypixelLatest);
        hypLatestStrafeBoost.addConditionMode(mode, modeEnum.HypixelLatest);
        hypLatestSpeedBoost.addConditionMode(mode, modeEnum.HypixelLatest);
        //hypLatestGroundDist.addConditionMode(mode, modeEnum.HypixelLatest);
        hypLatestDamageBoost.addConditionMode(mode, modeEnum.HypixelLatest);
        hypLatestFastFall.addConditionMode(mode, modeEnum.HypixelLatest);

        acc.addConditionMode(mode, modeEnum.HypixelGround);
        fastFall.addConditionMode(mode, modeEnum.HypixelGround);
        damageBoost.addConditionMode(mode, modeEnum.HypixelGround);
        speedBoost.addConditionMode(mode, modeEnum.HypixelGround);

        hypStrafeBoost.addConditionMode(mode, modeEnum.HypixelStrafe);
        strafeBoost.addConditionMode(mode, modeEnum.HypixelStrafe);
        strafeFallDist.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeSpeedBoost.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeFastFall.addConditionMode(mode, modeEnum.HypixelStrafe);
        //hypStrafeFastFallTick.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeStrafe.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeStopStrafe.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeDupPacket.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeSpoof.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeSidewaysOnly.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeFlexible.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeStopToggle.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeKeepStrafe.addConditionMode(mode, modeEnum.HypixelStrafe);
        sideBoost.addConditionMode(mode, modeEnum.HypixelStrafe);
        hypStrafeSemi.addConditionMode(mode, modeEnum.HypixelStrafe);
        strafeMaxBoost.addConditionMode(mode, modeEnum.HypixelStrafe);
        //hypStrafeMaxOffground.addConditionMode(mode, modeEnum.HypixelStrafe);

        rotBoost.addConditionMode(mode, modeEnum.Rotation);
        rotGround.addConditionMode(mode, modeEnum.Rotation);

        //horizontalAcc.addConditionMode(mode, "Hypixel");
        //verticalAcc.addConditionMode(mode, "Hypixel");
        //least.addConditionMode(mode, "Hypixel");
        //lowhop.addConditionMode(mode, "Hypixel");

        maxCollisions.addConditionMode(mode, modeEnum.Grim);
        leniencePerEntity.addConditionMode(mode, modeEnum.Grim);
        distance.addConditionMode(mode, modeEnum.Grim);
        targetStrafe.addConditionMode(mode, modeEnum.Grim);
        targetDistance.addConditionMode(mode, modeEnum.Grim);
        grimGround.addConditionMode(mode, modeEnum.Grim);
        grimAir.addConditionMode(mode, modeEnum.Grim);

        lowhopbmc.addConditionMode(mode, modeEnum.BlocksMC);

        vulcanAcc.addConditionMode(mode, modeEnum.Vulcan);
        vulcanFastFall.addConditionMode(mode, modeEnum.Vulcan);
        vulcanStrafe.addConditionMode(mode, modeEnum.Vulcan);

        keepTrig.addConditionMode(mode, modeEnum.HypixelTrig);
        trigBoost.addConditionMode(mode, modeEnum.HypixelTrig);
        trigPreBoost.addConditionMode(mode, modeEnum.HypixelTrig);
        strafeLimit.addConditionMode(mode, modeEnum.HypixelTrig);
        trigStrength.addConditionMode(mode, modeEnum.HypixelTrig);
        trigAccBoost.addConditionMode(mode, modeEnum.HypixelTrig);
        trigFlexible.addConditionMode(mode, modeEnum.HypixelTrig);
        trigMinSpeed.addConditionMode(mode, modeEnum.HypixelTrig);

        strafeSpeed.addConditionMode(mode, modeEnum.Strafe);
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    @Override
    protected void enable() {
        if (nextToggle > System.currentTimeMillis() || !Template.isBlatant())
            return;

        super.enable();
        offGroundTicks = 0;
        wasGround = false;
        readyFastFall = false;
        lastMoveInput = PlayerUtils.computeMovementInput();
        strafeMoveInput = PlayerUtils.computeMovementInput();
    }

    @Override
    protected void disable() {
        super.disable();
        if (mc.player == null) return;
        if (mode.is(modeEnum.HypixelStrafe) && hypStrafeStopToggle.isEnabled()) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (mc.player == null || !event.check)
            return;

        if (jump.isEnabled() && mc.player.isOnGround() && PlayerUtils.isMoving() && !mode.is(modeEnum.Grim) && !mode.is(modeEnum.Hoplite)) {
            event.input.jump();
        }
        if (jump.isEnabled() && mc.player.isOnGround() && PlayerUtils.isMoving() && (mode.is(modeEnum.Grim) || mode.is(modeEnum.Hoplite)) && isColliding) {
            event.input.jump();
        }
    }

    @EventHandler
    public void onTick(PlayerTickEvent.Post event) {
        tickSpoofGround = false;
    }

    private float getDistanceToGround() {
        //if (mc.player.fallDistance > 0) return mc.player.fallDistance;
        //else return groundDist;
        if (mc.player.isOnGround()) return 0;
        if (mc.player.fallDistance > 1) return 0;
        return 1 - mc.player.fallDistance;
        /*
        for (int i = (int) (mc.player.getPos().y); i > -1; i--) {
            BlockPos pos = new BlockPos((int) mc.player.getPos().x, i, (int) mc.player.getPos().z);
            if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) {
                return (float) (mc.player.getPos().y - pos.getY());
            }
        }
        return 0;
         */
    }
    
    @EventHandler
    public void onTick(PlayerTickEvent.Pre event) {
        if (!nullCheck()) return;
        if (mc.player.isOnGround()) {
            wasGround = true;
            offGroundTicks = 0;
            onGroundTicks++;
            groundDist = 0;
        } else {
            offGroundTicks++;
            onGroundTicks = 0;
            groundDist += (float) mc.player.getVelocity().y;
            if (offGroundTicks == 8) readyFastFall = true;
            //if (mc.player.fallDistance != 0) groundDist = mc.player.fallDistance;
        }
        /*
        if (mode.is(modeEnum.Hypixel)) {
            if (mc.player.isOnGround()) {
                if (PlayerUtils.isPressingMoveInput()) fullStrafe(0.15321679421194379);
            } else {
                double horizontalMod = BASE_HORIZONTAL_MODIFIER*horizontalAcc.getValue() + HORIZONTAL_SPEED_AMPLIFIER*horizontalAcc.getValue() * getSpeedEffect();

                double yMod = mc.player.getVelocity().y < 0 && mc.player.fallDistance < 1 ? VERTICAL_SPEED_AMPLIFIER*verticalAcc.getValue() : 0.0;

                mc.player.setVelocity(mc.player.getVelocity().multiply(1.0 + horizontalMod, 1.0 + yMod, 1.0 + horizontalMod));
            }

            if (mc.player.isOnGround() && mc.player.input.playerInput.jump()) {
                double atLeast = AT_LEAST*least.getValue() + SPEED_EFFECT_CONST * getSpeedEffect();
                double speed = sqrtSpeed();
                if (speed < atLeast) speed = atLeast;

                if (PlayerUtils.isPressingMoveInput()) fullStrafe(speed);
            }
            //if (!mc.player.isOnGround() && mc.player.getVelocity().y > 0 && mc.player.getVelocity().y < 0.01 && lowhop.isEnabled()) {
            //    mc.player.setVelocity(mc.player.getVelocity().x, -0.08, mc.player.getVelocity().z);
            //}
            if (lowhop.isEnabled()) {
                if (!wasGround) return;
                double x = mc.player.getVelocity().x;
                double y = mc.player.getVelocity().y;
                double z = mc.player.getVelocity().z;
                if (offGroundTicks == 4) y = mc.player.getVelocity().y - 0.03;
                if (offGroundTicks == 6) y = mc.player.getVelocity().y - 0.084;

                mc.player.setVelocity(x,y,z);
            }
        }
         */
        switch (mode.getMode()) {
            case HypixelLatest -> {
                if (mc.player.isOnGround()) {
                    fullStrafe((0.15321679421194379 + hypLatestSpeedBoost.getFValue() * getSpeedEffect()) * hypLatestBoost.getValue());
                    didStrafe = false;
                } else if (getDistanceToGround() < (hypLatestFastFall.isEnabled() ? 0.67 : 0.55) && !didStrafe) {
                    fullStrafe(sqrtSpeed() * hypLatestStrafeBoost.getFValue());
                    didStrafe = true;
                    //if (mc.player.fallDistance < 1f-hypLatestGroundDist.getFValue() && mc.player.fallDistance != 0) didStrafe = false;
                }
                
                if (!mc.player.isOnGround() && hypLatestFastFall.isEnabled()) {
                    if (mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() instanceof SlabBlock) {
                        return;
                    }
                    if (mc.player.hurtTime == 0 && readyFastFall) {
                        if (offGroundTicks == 4) {
                            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y - 0.03, mc.player.getVelocity().z);
                        } else if (offGroundTicks == 6) {
                            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y - 0.2, mc.player.getVelocity().z);
                        }
                    }
                }
            }
            
            case HypixelGround -> {
                if (mc.player.isOnGround()) {
                    fullStrafe((0.15321679421194379 + speedBoost.getFValue() * getSpeedEffect()) * acc.getValue());
                }
                if (fastFall.isEnabled()) {
                    if (!wasGround) return;
                    double x = mc.player.getVelocity().x;
                    double y = mc.player.getVelocity().y;
                    double z = mc.player.getVelocity().z;
                    
                    if (offGroundTicks == 5) y -= 0.1905189780583944;
                    if (offGroundTicks == 4) y -= 0.03;
                    if (offGroundTicks == 6) y *= 1.01;
                    
                    mc.player.setVelocity(x, y, z);
                }
                if (damageBoost.isEnabled() && mc.player.hurtTime == 9 && lastKb != 0) {
                    double rad = Math.toRadians(PlayerUtils.getMoveDirection() + 90);
                    //double moveSpeed = 0.15321679421194379; // Sprint walk speed
                    double x = Math.cos(rad) * lastKb * 0.9;
                    double z = Math.sin(rad) * lastKb * 0.9;
                    Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
                    mc.player.setVelocity(vec);
                    lastKb = 0;
                }
            }
            
            case HypixelTrig -> {
                if (mc.player.isOnGround()) {
                    fullStrafe((0.15321679421194379 + 0.01 * getSpeedEffect()) * trigAccBoost.getValue());
                    lastMoveInput = PlayerUtils.computeMovementInput();
                    wasStrafed = false;
                    strafeMoveInput = null;
                    trigStrafeCount = 0;
                } else if (mc.player.fallDistance > 0
                        && (!wasStrafed || (keepTrig.isEnabled() && lastMoveInput.equals(PlayerUtils.computeMovementInput())))
                        && (strafeMoveInput != null && (!strafeMoveInput.equals(PlayerUtils.computeMovementInput())) || trigFlexible.isEnabled())
                        && sqrtSpeed() > trigMinSpeed.getFValue()) {
                    if (keepTrig.isEnabled() && !wasStrafed) strafeMoveInput = PlayerUtils.computeMovementInput();
                    
                    double startingTrig = MathHelper.wrapDegrees(PlayerUtils.getMotionDirection()); // assuming it's 0
                    double endingTrig = MathHelper.wrapDegrees(PlayerUtils.getExactMoveDirection()); // assuming it's 90
                    
                    double diff = MathHelper.wrapDegrees(endingTrig - startingTrig); // = 90 - 0
                    diff = diff * trigStrength.getValue() / 100; // 90 (assuming 100%)

                    double trig = MathHelper.wrapDegrees(startingTrig + diff); // 0 + 90 = 90 should be right
                    if (!wasStrafed) {
                        mc.player.setVelocity(
                            trigPreBoost.getFValue() * mc.player.getVelocity().x,
                            mc.player.getVelocity().y,
                            trigPreBoost.getFValue() * mc.player.getVelocity().z
                        );
                        lastMoveInput = PlayerUtils.computeMovementInput();
                    }
                    
                    double speed = sqrtSpeed() * trigBoost.getFValue();
                    if (trigStrafeCount <= strafeLimit.getIValue()) {
                        strafeDir(speed, trig);
                        trigStrafeCount++;
                    }
                    wasStrafed = true;
                    //mc.player.sendMessage(Text.of("start:"+Math.ceil(startingTrig)+" end: "+Math.ceil(endingTrig)+" trig: "+trig));
                }
            }
            
            case Rotation -> {
                Template.rotationManager().setRotation(new Rotation(PlayerUtils.getExactMoveDirection(), Template.rotationManager().getClientRotation().pitch()));
                if (rotGround.isEnabled()) {
                    if (mc.player.isOnGround()) {
                        fullStrafe((0.15321679421194379 + 0.01 * getSpeedEffect()) * rotBoost.getValue());
                    } else {
                        fullStrafe(sqrtSpeed());
                    }
                } else {
                    fullStrafe((0.15321679421194379 + 0.01 * getSpeedEffect()) * rotBoost.getValue());
                }
            }
            
            case HypixelStrafe -> {
                if (mc.player.isOnGround()) {
                    fullStrafe((0.15321679421194379 + hypStrafeSpeedBoost.getFValue() * getSpeedEffect()) * hypStrafeBoost.getValue());
                    lastMoveInput = PlayerUtils.computeMovementInput();
                    wasStrafed = false;
                } else {
                    if (strafeAfter) {
                        strafeAfter = false;
                        if (hypStrafeDupPacket.isEnabled()) {
                            Template.sendNoEvent(
                                new PlayerMoveC2SPacket.Full(
                                    mc.player.getX(),
                                    mc.player.getY(),
                                    mc.player.getZ(),
                                    (float) PlayerUtils.getExactMoveDirection(),
                                    mc.player.getPitch(),
                                    hypStrafeSpoof.isEnabled() || mc.player.isOnGround(),
                                    mc.player.horizontalCollision
                                )
                            );
                        }
                        double speed = (0.15321679421194379) * strafeBoost.getValue();
                        if (hypStrafeStrafe.isEnabled()) speed = lastSpeed * strafeBoost.getValue();
                        if (speed > strafeMaxBoost.getFValue()) speed = strafeMaxBoost.getFValue();
                        if (PlayerUtils.isBothStrafe()) speed = speed * sideBoost.getFValue();
                        
                        if (hypStrafeKeepStrafe.isEnabled()) {
                            strafeDir(speed, PlayerUtils.getMoveDirFromVec(lastMoveInput));
                        } else {
                            if (hypStrafeSemi.isEnabled()) semiStrafe(speed);
                            else fullStrafe(speed);
                        }
                    }
                    if (mc.player.fallDistance > strafeFallDist.getFValue() && (!wasStrafed || hypStrafeKeepStrafe.isEnabled())
                            && (!hypStrafeSidewaysOnly.isEnabled() || mc.player.input.movementSideways != 0)
                            && (!hypStrafeFlexible.isEnabled() || !lastMoveInput.equals(PlayerUtils.computeMovementInput()))) {
                        if (!wasStrafed) lastMoveInput = PlayerUtils.computeMovementInput();
                        wasStrafed = true;
                        if (hypStrafeStopStrafe.isEnabled()) {
                            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
                            strafeAfter = true;
                            lastSpeed = sqrtSpeed();
                        } else {
                            double speed = (0.15321679421194379) * strafeBoost.getValue();
                            if (hypStrafeStrafe.isEnabled()) speed = sqrtSpeed() * strafeBoost.getValue();
                            if (speed > strafeMaxBoost.getFValue()) speed = strafeMaxBoost.getFValue();
                            if (PlayerUtils.isBothStrafe()) speed = speed * sideBoost.getFValue();
                            
                            if (hypStrafeKeepStrafe.isEnabled()) {
                                strafeDir(speed, PlayerUtils.getMoveDirFromVec(lastMoveInput));
                            } else {
                                if (hypStrafeSemi.isEnabled()) semiStrafe(speed);
                                else fullStrafe(speed);
                            }
                            
                            if (hypStrafeDupPacket.isEnabled()) {
                                Template.sendNoEvent(
                                    new PlayerMoveC2SPacket.Full(
                                        mc.player.getX(),
                                        mc.player.getY(),
                                        mc.player.getZ(),
                                        (float) PlayerUtils.getExactMoveDirection(),
                                        mc.player.getPitch(),
                                        hypStrafeSpoof.isEnabled() || mc.player.isOnGround(),
                                            mc.player.horizontalCollision
                                    )
                                );
                            }
                        }
                    }
                }
                
                
                if (hypStrafeFastFall.isEnabled()) {
                    //if (!wasGround) return;
                    //double x = mc.player.getVelocity().x;
                    //double y = mc.player.getVelocity().y;
                    //double z = mc.player.getVelocity().z;
                    //if (offGroundTicks == hypStrafeFastFallTick.getIValue()) y = Math.min(y-0.0335, -0.0335);
                    //if (offGroundTicks > hypStrafeFastFallTick.getIValue() && offGroundTicks < hypStrafeMaxOffground.getFValue()) y = mc.player.getVelocity().y - 0.0335;
                    
                    //mc.player.setVelocity(x,y,z);
                    if (mc.player.isOnGround() ||  mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() instanceof SlabBlock) {
                        return;
                    }
                    
                    if (mc.player.hurtTime == 0 && readyFastFall && hypLatestFastFall.isEnabled()) {
                        if (offGroundTicks == 4) {
                            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y - 0.03, mc.player.getVelocity().z);
                        } else if (offGroundTicks == 6) {
                            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y - 0.2, mc.player.getVelocity().z);
                        }
                    }
                }
            }
            
            case Grim -> {
                if (mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f) return;
                if (!(grimGround.isEnabled() && mc.player.isOnGround() || grimAir.isEnabled() && !mc.player.isOnGround()))
                    return;
                int collisions = 0;
                Box box = mc.player.getBoundingBox().expand(distance.getValue());
                Box targetBox = mc.player.getBoundingBox().expand(targetDistance.getValue());
                LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
                
                for (Entity entity : mc.world.getEntities())
                    if (canCauseSpeed(entity) && box.intersects(entity.getBoundingBox())) collisions++;
                
                isColliding = collisions > 0;
                if (collisions > 0) {
                    if (collisions > maxCollisions.getIValue() && maxCollisions.getIValue() != 0)
                        collisions = maxCollisions.getIValue();
                    double yaw = Math.toRadians(PlayerUtils.getMoveDirection());
                    double boost = leniencePerEntity.getValue() * collisions;
                    mc.player.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
                } else if (target != null && targetBox.intersects(target.getBoundingBox()) && targetStrafe.isEnabled()) {
                    Rotation targetRot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox().getCenter());
                    double yaw = Math.toRadians(targetRot.fyaw());
                    double boost = leniencePerEntity.getValue();
                    mc.player.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
                }
            }
            
            case Hoplite -> {
                if (mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f) return;
                int collisions = 0;
                Box box = mc.player.getBoundingBox().expand(0.15);
                
                for (Entity entity : mc.world.getEntities()) {
                    Box entityBox = entity.getBoundingBox();
                    if (canCauseSpeed(entity) && box.intersects(entityBox)) {
                        collisions++;
                    }
                }
                isColliding = collisions > 0;
                if (collisions > 0) {
                    if (collisions > maxCollisions.getIValue() && maxCollisions.getIValue() != 0)
                        collisions = maxCollisions.getIValue();
                    double yaw = Math.toRadians(PlayerUtils.getMoveDirection());
                    double boost = 0.07 * collisions;
                    mc.player.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
                }
            }
            
            case BlocksMC -> {
                if (PlayerUtils.isMoving()) {
                    fullStrafe(sqrtSpeed() + 0.015f * getSpeedEffect());
                } else {
                    mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
                }
                if (lowhopbmc.isEnabled()) {
                    double x = mc.player.getVelocity().x;
                    double y = mc.player.getVelocity().y;
                    double z = mc.player.getVelocity().z;
                    if (offGroundTicks == 4) y = mc.player.getVelocity().y - 0.03;
                    if (offGroundTicks == 6) y = mc.player.getVelocity().y - 0.084;
                    
                    mc.player.setVelocity(x, y, z);
                }
            }
            
            case Vulcan -> {
                if (mc.player.isOnGround()) {
                    fullStrafe((0.15321679421194379 + 0.008 * getSpeedEffect()) * vulcanAcc.getValue());
                }
                //if (!mc.player.isOnGround() && mc.player.getVelocity().y > 0 && mc.player.getVelocity().y < 0.01 && lowhop.isEnabled()) {
                //    mc.player.setVelocity(mc.player.getVelocity().x, -0.08, mc.player.getVelocity().z);
                //}
                if (vulcanFastFall.isEnabled() && PlayerUtils.isPressingMoveInput()) {
                    double x = mc.player.getVelocity().x;
                    double y = mc.player.getVelocity().y;
                    double z = mc.player.getVelocity().z;
                    if (mc.player.isOnGround() && offGroundTicks == 0 && mc.player.input.playerInput.jump()) y = y + 0.411546;
                    
                    if (offGroundTicks == 4) y = mc.player.getVelocity().y - 0.2;
                    if (offGroundTicks == 6) y = mc.player.getVelocity().y - 0.2;
                    
                    mc.player.setVelocity(x, y, z);
                }
                if (vulcanStrafe.isEnabled() && mc.player.fallDistance > 0) {
                    fullStrafe(sqrtSpeed());
                }
            }
            
            case Strafe -> {
                if (PlayerUtils.isMoving()) {
                    fullStrafe(strafeSpeed.getValue());
                } else {
                    mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
                }
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (!nullCheck())
            return;
        if (event.packet instanceof EntityVelocityUpdateS2CPacket wrapper) {
            if (mc.player == null || mc.world.getEntityById(wrapper.getEntityId()) == null) return;

            Vec3d vec = new Vec3d(wrapper.getVelocityX(), 0, wrapper.getVelocityZ());
            lastKb = vec.normalize().length();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (!nullCheck())
            return;

        boolean shouldSpoof = shouldSpoofGround || tickSpoofGround;
        
        if (vulcanStrafe.isEnabled() && mode.is(modeEnum.Vulcan)) shouldSpoof = mc.player.fallDistance > 0.2;
        
        if (shouldSpoof) {
            if (event.packet instanceof PlayerMoveC2SPacket.Full wrapper) {
                event.setCancelled(true);
                shouldSpoofGround = false;
                Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                event.setCancelled(true);
                shouldSpoofGround = false;
                Template.sendNoEvent(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround wrapper ) {
                event.setCancelled(true);
                shouldSpoofGround = false;
                Template.sendNoEvent(new PlayerMoveC2SPacket.LookAndOnGround(wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.OnGroundOnly) {
                event.setCancelled(true);
                shouldSpoofGround = false;
                Template.sendNoEvent(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
            }
        }
        if ((hypLatestFastFall.isEnabled() && mode.is(modeEnum.HypixelLatest)) || (hypStrafeFastFall.isEnabled() && mode.is(modeEnum.HypixelStrafe))) {
            if (!mc.player.isOnGround()) return;
            if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                event.setCancelled(true);
                Template.sendNoEvent(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 5.0E-12, mc.player.getZ(), true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.Full wrapper) {
                event.setCancelled(true);
                Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 5.0E-12, mc.player.getZ(), wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            }
        }
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.player && entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity);
    }

    public int getSpeedEffect() {
        return mc.player.getStatusEffect(StatusEffects.SPEED) != null ? mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() : 0;
    }

    public double sqrtSpeed() {
        return Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    public void strafe() {
        if (!PlayerUtils.isPressingMoveInput())
            return;

        double rad = Math.toRadians(PlayerUtils.getMoveDirection() + 90);
        double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.abs(Math.cos(rad) * moveSpeed) > Math.abs(mc.player.getVelocity().x) ? Math.cos(rad) * moveSpeed : mc.player.getVelocity().x;
        double z = Math.abs(Math.sin(rad) * moveSpeed) > Math.abs(mc.player.getVelocity().z) ? Math.sin(rad) * moveSpeed : mc.player.getVelocity().z;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }

    public void strafe(double moveSpeed) {
        if (!PlayerUtils.isPressingMoveInput())
            return;

        double rad = Math.toRadians(PlayerUtils.getMoveDirection() + 90);
        //double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.abs(Math.cos(rad) * moveSpeed) > Math.abs(mc.player.getVelocity().x) ? Math.cos(rad) * moveSpeed : mc.player.getVelocity().x;
        double z = Math.abs(Math.sin(rad) * moveSpeed) > Math.abs(mc.player.getVelocity().z) ? Math.sin(rad) * moveSpeed : mc.player.getVelocity().z;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }

    public boolean shouldTargetStrafe() {
        TargetStrafeModule module = Template.moduleManager.getModule(TargetStrafeModule.class);
        if (module == null || !module.isEnabled() || (module.spaceBar.isEnabled() && !mc.player.input.playerInput.jump())) return false;
        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null) return false;
        return !(MathUtils.closestPosBoxDistance(mc.player.getPos(), target.getBoundingBox()) > module.minDist.getValue());
    }

    public void fullStrafe(double moveSpeed) {
        if (!PlayerUtils.isPressingMoveInput(false))
            return;

        TargetStrafeModule module = Template.moduleManager.getModule(TargetStrafeModule.class);
        double rad;
        if (module != null && shouldTargetStrafe()) rad = Math.toRadians(module.getDirection()+90);
        else rad = Math.toRadians(PlayerUtils.getExactMoveDirection() + 90);
        //double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.cos(rad) * moveSpeed;
        double z = Math.sin(rad) * moveSpeed;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }

    public void semiStrafe(double moveSpeed) {
        if (!PlayerUtils.isPressingMoveInput(false))
            return;

        double rad = Math.toRadians(PlayerUtils.getSemiMoveDirection() + 90);
        //double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.cos(rad) * moveSpeed;
        double z = Math.sin(rad) * moveSpeed;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }

    public void strafeDir(double moveSpeed, double dir) {
        if (!PlayerUtils.isPressingMoveInput(false))
            return;

        double rad = Math.toRadians(dir + 90);
        //double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.cos(rad) * moveSpeed;
        double z = Math.sin(rad) * moveSpeed;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }
}
