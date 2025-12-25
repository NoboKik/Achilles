package template.rip.module.modules.blatant;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InfiniteAuraModule extends Module {

    public enum modeEnum{Full, Position}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Full, "Packet Mode");
    public enum interactModeEnum{Attack, Interact, Interact_Sneak}
    public final ModeSetting<interactModeEnum> interactMode = new ModeSetting<>(this, interactModeEnum.Attack, "Action Mode");

    public final NumberSetting blocksPerPacket = new NumberSetting(this, 1, 0, 15, 0.1, "Blocks Per Packet");
    public final NumberSetting reach = new NumberSetting(this, 3, 0, 6, 0.1, "Reach");
    public final ColorSetting color = new ColorSetting(this, new JColor(255, 175, 175, 50), true, "Color setting");
    public final NumberSetting maxTries = new NumberSetting(this, 50, 0, 150, 1, "Max Tries");
    public final NumberSetting cooldown = new NumberSetting(this, 0.85, 0, 1, 0.1, "Attack Cooldown");
    public final NumberSetting teleportCooldown = new NumberSetting(this, 5, 0, 60, 1, "Teleport Tick Cooldown");
    public final NumberSetting packetsPerTick = new NumberSetting(this, 5, 0, 20, 0.1, "Packets Per Tick");
    public final BooleanSetting instant = new BooleanSetting(this, false, "Instant (1.8)");
    private final List<Vec3d> positions = new ArrayList<>();
    private final List<Box> boxes = new ArrayList<>();
    private int timer = 0;
    private double packets = 0;
    private int stage = 0;
    private boolean reverse = false;

    public InfiniteAuraModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;
        if (!Template.isAdvanced()) {
            Template.notificationManager().addNotification(new Notification("Advanced module", 15000, "This module only works on no anticheat servers"));
            return;
        }
        super.enable();
    }

    @Override
    public void onEnable() {
        reset(true);
    }

    private void reset(boolean full) {
        positions.clear();
        boxes.clear();
        timer = 0;
        stage = 0;
        reverse = false;
        if (full) {
            packets = 0;
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet && packet.changesPosition() && stage > 0) {
            event.cancel();
        }
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!nullCheck())
            return;

        Vec3d last = mc.player.getPos();
        for (Vec3d pos : positions) {
            RenderUtils.Render3D.renderLineTo(last, pos, color.getColor(), 1f, event.context);
            last = pos;
        }
        for (Box box : boxes) {
            Color col = color.getColor();
            if (boxes.indexOf(box) == stage - 1) {
                col = Color.green;
            }
            RenderUtils.Render3D.renderBox(box, col, color.getColor().getAlpha(), event.context);
        }
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (timer < teleportCooldown.getIValue()) {
            timer++;
            return;
        }
        if (stage <= 0) {
            reset(false);
        }
        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull(true);
        packets += packetsPerTick.value;
        tickInfinite((int) packets, target, target == null || maxTries.value * blocksPerPacket.value < mc.player.distanceTo(target) || !PlayerUtils.canVectorBeSeen(mc.player.getPos(), MathUtils.closestPointToBox(mc.player.getPos(), target.getBoundingBox())));
        packets -= (int) packets;
    }

    private void tickInfinite(int ticks, LivingEntity target, boolean forceReverse) {
        if (instant.isEnabled()) {
            if (target != null) {
                reset(true);
                positions.add(mc.player.getPos());
                for (int i = 0; i < maxTries.getIValue(); i++) {
                    Vec3d current = positions.get(positions.size() - 1);
                    Rotation rot = RotationUtils.getRotations(current, target.getPos());
                    Vec3d newPos = current.add(RotationUtils.forwardVector(rot).multiply(blocksPerPacket.value));
                    positions.add(newPos);
                    boxes.add(MathUtils.boxAtPos(mc.player.getBoundingBox(), newPos));
                    Packet<?> pkt = switch (mode.getMode()) {
                        case Full -> new PlayerMoveC2SPacket.Full(newPos.x, newPos.y, newPos.z, Template.rotationManager().yaw(), Template.rotationManager().pitch(), mc.player.isOnGround(), mc.player.horizontalCollision);
                        case Position -> new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, mc.player.isOnGround(), mc.player.horizontalCollision);
                    };
                    mc.getNetworkHandler().sendPacket(pkt);
                    if (MathUtils.closestPosBoxDistance(current, target.getBoundingBox()) < reach.value && mc.player.getAttackCooldownProgress(0.5f) > cooldown.value) {
                        switch (interactMode.getMode()) {
                            case Attack: {
                                mc.interactionManager.attackEntity(mc.player, target);
                                mc.player.swingHand(Hand.MAIN_HAND);
                                break;
                            }
                            case Interact:
                            case Interact_Sneak: mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.interact(target, interactMode.is(interactModeEnum.Interact_Sneak), Hand.MAIN_HAND)); break;
                        }

                        mc.player.swingHand(Hand.MAIN_HAND);
                        break;
                    }
                }
                List<Vec3d> backwards = new ArrayList<>();
                for (int i = positions.size() - 1; i > 0; i--) {
                    backwards.add(positions.get(i));
                }
                for (Vec3d vec : backwards) {
                    Packet<?> pkt = switch (mode.getMode()) {
                        case Full -> new PlayerMoveC2SPacket.Full(vec.x, vec.y, vec.z, Template.rotationManager().yaw(), Template.rotationManager().pitch(), mc.player.isOnGround(), mc.player.horizontalCollision);
                        case Position -> new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, mc.player.isOnGround(), mc.player.horizontalCollision);
                    };
                    mc.getNetworkHandler().sendPacket(pkt);
                }
                Vec3d back = mc.player.getPos();
                Packet<?> pkt = switch (mode.getMode()) {
                    case Full -> new PlayerMoveC2SPacket.Full(back.x, back.y, back.z, Template.rotationManager().yaw(), Template.rotationManager().pitch(), mc.player.isOnGround(), mc.player.horizontalCollision);
                    case Position -> new PlayerMoveC2SPacket.PositionAndOnGround(back.x, back.y, back.z, mc.player.isOnGround(), mc.player.horizontalCollision);
                };
                mc.getNetworkHandler().sendPacket(pkt);
            }
        } else {
            if (!reverse && !forceReverse) {
                if (positions.isEmpty()) {
                    positions.add(mc.player.getPos());
                }
                int startStage = stage;
                for (stage = startStage; stage < startStage + ticks; stage++) {
                    Vec3d current = positions.get(positions.size() - 1);
                    Rotation rot = RotationUtils.getRotations(current, MathUtils.closestPointToBox(current, target.getBoundingBox()));
                    Vec3d newPos = current.add(RotationUtils.forwardVector(rot).multiply(blocksPerPacket.value));
                    positions.add(newPos);

                    boxes.add(MathUtils.boxAtPos(mc.player.getBoundingBox(), newPos));
                    Packet<?> pkt = switch (mode.getMode()) {
                        case Full -> new PlayerMoveC2SPacket.Full(newPos.x, newPos.y, newPos.z, Template.rotationManager().yaw(), Template.rotationManager().pitch(), mc.player.isOnGround(), mc.player.horizontalCollision);
                        case Position -> new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, mc.player.isOnGround(), mc.player.horizontalCollision);
                    };
                    Template.sendNoEvent(pkt);
                    if (MathUtils.closestPosBoxDistance(current, target.getBoundingBox()) < reach.value && mc.player.getAttackCooldownProgress(0.5f) > cooldown.value) {
                        switch (interactMode.getMode()) {
                            case Attack : {
                                mc.interactionManager.attackEntity(mc.player, target);
                                mc.player.swingHand(Hand.MAIN_HAND);
                                break;
                            }
                            case Interact :
                            case Interact_Sneak : mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.interact(target, interactMode.is(interactModeEnum.Interact_Sneak), Hand.MAIN_HAND)); break;
                        }
                        reverse = true;
                        break;
                    }
                }
            } else {
                int startStage = stage;
                for (stage = startStage; stage > Math.max(startStage - ticks, 0); stage--) {
                    Vec3d vec = positions.get(stage);
                    Packet<?> pkt = switch (mode.getMode()) {
                        case Full -> new PlayerMoveC2SPacket.Full(vec.x, vec.y, vec.z, Template.rotationManager().yaw(), Template.rotationManager().pitch(), mc.player.isOnGround(), mc.player.horizontalCollision);
                        case Position -> new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, mc.player.isOnGround(), mc.player.horizontalCollision);
                    };
                    Template.sendNoEvent(pkt);
                }
            }
        }
    }
}
