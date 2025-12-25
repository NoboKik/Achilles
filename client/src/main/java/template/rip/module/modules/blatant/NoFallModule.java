package template.rip.module.modules.blatant;

import com.google.common.collect.Streams;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import template.rip.Template;
import template.rip.api.event.events.BlinkEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.RenderTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.stream.Stream;

public class NoFallModule extends Module {

    public enum modeEnum {Grim, GrimNew, Hoplite, Blink, Hypixel, Motion}

    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Grim, "Mode");
    public final NumberSetting motionAmount = new NumberSetting(this, 0.15, 0, 0.5, 0.01, "Motion Amount");
    public final NumberSetting motionDistance = new NumberSetting(this, 2, 0, 3, 0.01, "Motion Distance");
    private double fallDistance;
    private double lastFallDistance;
    private boolean blinking;
    private boolean prevOnGround;
    private boolean enabled = false;
    private double timerSpeed = 1;
    private boolean stopNextTick = false;
    private int ticks;

    public NoFallModule(Category category, Description description, String name) {
        super(category, description, name);
        motionDistance.addConditionMode(mode, modeEnum.Motion);
        motionAmount.addConditionMode(mode, modeEnum.Motion);
    }

    @Override
    public String getSuffix()
    {
        return " " + mode.getMode().name();
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant()) {
            return;
        }

        super.enable();

        if (mc.player != null) {
            this.fallDistance = mc.player.fallDistance;
            this.lastFallDistance = mc.player.fallDistance;
        }

        timerSpeed = 1;
    }

    @Override
    protected void disable() {
        super.disable();

        if (this.blinking) {
            Template.blinkUtil().blink = false;
            Template.blinkUtil().dumpPackets(false);
            this.blinking = false;
        }

        timerSpeed = 1;
        ticks = 0;
    }

    @EventHandler
    public void onTPS(RenderTickEvent event)
    {
        event.TPS = (int) Math.round(timerSpeed * 20.0);
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (stopNextTick) {
            stopNextTick = false;
            timerSpeed = 1;
        }
        if (!nullCheck()) {
            return;
        }

        switch (mode.getMode()) {
            case Grim -> {
                if (!mc.options.sneakKey.isPressed()) {
                    if (mc.player.fallDistance >= 1.0 && !mc.player.isOnGround()) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 0.0001, mc.player.getZ(), mc.player.getYaw(), PlayerUtils.randomPitch(), false, mc.player.horizontalCollision));
                        mc.player.fallDistance = 0f;
                    }
                }
            }
            case Hoplite -> {
                if (!mc.player.isOnGround() && mc.player.fallDistance > 2f) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 1.0E-9, mc.player.getZ(), mc.player.getYaw(),
                            PlayerUtils.randomPitch(), false, mc.player.horizontalCollision));
                    mc.player.onLanding();
                }
            }
            case Blink -> {
                if (Template.moduleManager.isModuleEnabled(ScaffoldModule.class)) {
                    return;
                }
                if (mc.player.isOnGround()) {
                    this.fallDistance = 0.0;
                } else if (mc.player.getVelocity().y < 0.0) {
                    this.fallDistance -= mc.player.getVelocity().y;
                }

                if (mc.player.isOnGround()) {
                    if (this.blinking) {
                        this.blinking = false;
                    }
                    this.prevOnGround = true;
                    return;
                }
                if (this.prevOnGround) {
                    if (this.shouldBlink()) {
                        this.blinking = true;
                    }
                    this.prevOnGround = false;
                }
                if (!PlayerUtils.isBlockUnder() || !(this.fallDistance >= 3.0)) {
                    return;
                }
                //Template.sendNoEvent(new PlayerMoveC2SPacket.OnGroundOnly(
                //        true
                //));
                this.fallDistance = 0.0;
                return;
            }
            case Hypixel -> {
                //if (mc.player.isOnGround()) {
                //    wasOnGround = true;
                //} else if (wasOnGround) {
                //    wasOnGround = false;
                //    Box box = mc.player.getBoundingBox();
                //    Box adjustedBox = setMinY(box.offset(0, -0.5, 0), 50);

                //    Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

                //    if (blockCollisions.findAny().isPresent()) {
                //        if (mc.player.getVelocity().y < 0) {
                //            enabled = true;
                //            this.blinking = true;
                //        }
                //    }
                //}
                if (!mc.player.isOnGround()) {
                    return;
                }
                Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player,
                        setMinY(mc.player.getBoundingBox().offset(0, -0.5, 0), 50)));
                if (!blockCollisions.findAny().isPresent()) {
                    return;
                }
                if (mc.player.fallDistance >= 2.5) {
                    timerSpeed = 0.5;
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
                    mc.player.fallDistance = 0;
                    stopNextTick = true;
                }
                /*
                if (player.fallDistance >= 2.5 && !player.isFallingToVoid()) {
                Timer.requestTimerSpeed(0.5f, Priority.IMPORTANT_FOR_PLAYER_LIFE, ModuleNoFall)
                network.sendPacket(MovePacketType.ON_GROUND_ONLY.generatePacket().apply {
                    onGround = true
                })
                player.fallDistance = 0F
                waitTicks(1)
                Timer.requestTimerSpeed(1f, Priority.NORMAL, ModuleNoFall)
                }
                 */
            }
            case Motion -> {
                Box box = mc.player.getBoundingBox();
                Box adjustedBox = setMinY(box.offset(0, -0.5, 0), 50);

                Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

                if (blockCollisions.findAny().isEmpty()) {
                    return;
                }

                if (!mc.player.isOnGround() && mc.player.fallDistance - lastFallDistance > motionDistance.getFValue()) {
                    lastFallDistance = mc.player.fallDistance;

                    double motion = motionAmount.getFValue();
                    mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y + motion, mc.player.getVelocity().z);
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
                } else if (mc.player.isOnGround()) {
                    lastFallDistance = 0;
                }
            }
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Send event)
    {
        if (!nullCheck()) {
            return;
        }

        switch (mode.getMode()) {
            /*
             * This doesn't work on loysia since they disabled setbacks
             * This does bypass latest Astro (tested on donutsmp.net) and latest Grim (tested on mccislands.net)
             */
            case GrimNew -> {
                if (event.packet instanceof PlayerMoveC2SPacket && mc.player.fallDistance >= 2.6) {
                    // Cancel the current movement packet
                    event.cancel();

                    // First send the movement packet
                    Template.sendNoEvent(event.packet);

                    // Then send a useItem packet right after the movement packet to create an illegal packet order setback
                    // which in return resets fall damage
                    ActionResult interactionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }

                    // Setbacks at least 2 times before resetting
                    if (ticks++ >= 2) {
                        mc.player.fallDistance = 0;
                        ticks = 0;
                    }
                }
            }
        }
    }


    private Box setMinY(Box box, double minY) {
        return new Box(box.minX, box.minY - minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @EventHandler
    private void onNiggaBlink(BlinkEvent event) {
        if (!nullCheck()) {
            return;
        }

        if (this.blinking) {
            event.blink = true;
        }

        if (enabled) {
            if (event.packet instanceof PlayerMoveC2SPacket.Full wrapper) {
                event.skip = true;
                Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                event.skip = true;
                Template.sendNoEvent(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround wrapper) {
                event.skip = true;
                Template.sendNoEvent(new PlayerMoveC2SPacket.LookAndOnGround(wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
            } else if (event.packet instanceof PlayerMoveC2SPacket.OnGroundOnly) {
                event.skip = true;
                Template.sendNoEvent(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
            }
        }

        if (mc.player.isOnGround() && enabled) {
            this.blinking = false;
            enabled = false;
        }
    }

    private boolean shouldBlink() {
        return !mc.player.isOnGround() && !PlayerUtils.isBlockUnder(3) && PlayerUtils.isBlockUnder();
    }
}
