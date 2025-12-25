package template.rip.module.modules.client;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.modules.blatant.SpeedModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;

public class DisablerModule extends Module {

    public final BooleanSetting cancelKeepAlive = new BooleanSetting(this, false, "Cancel Keep Alive");
    public final BooleanSetting cancelTransactions = new BooleanSetting(this, false, "Cancel Transactions");
    public final BooleanSetting vulcanLimit = new BooleanSetting(this, false, "Vulcan Scaffold Limit");
    public final BooleanSetting vulcanSprint = new BooleanSetting(this, false, "Vulcan Sprint");
    public final BooleanSetting hypixelSpeed = new BooleanSetting(this, false, "Hypixel Speed");

    /*public final BooleanSetting hover = new BooleanSetting("Hover", this, false);
    public final NumberSetting hoverAmount = new NumberSetting("Hover Amount", this, 0.15, 0, 1, 0.01);
    public final BooleanSetting hoverOnGround = new BooleanSetting("Hover Ground", this, true);*/
    public final BooleanSetting verusCombat = new BooleanSetting(this, false, "Verus Combat");
    public final BooleanSetting swingFix = new BooleanSetting(this, Description.of("Fixes the swing packet order for 1.8. Some awful anticheats flag 1.9+ behavior as illegitimate when on Viaversion."), false, "Swing fix");
    public final BooleanSetting verusSemi = new BooleanSetting(this, false, "Verus Semi");
    public final BooleanSetting cubecraft = new BooleanSetting(this, false, "Cubecraft");
    public final BooleanSetting funnyPacket = new BooleanSetting(this, false, "Funny Packets");
    public final BooleanSetting dumpTransactions = new BooleanSetting(this, false, "Dump Transactions");
    public final NumberSetting dumpTime = new NumberSetting(this, 20, 1, 300, 1, "Dump Time");

    private final ArrayList<Packet<?>> funnyPackets = new ArrayList<>();
    private final ArrayList<Packet<?>> queueBus = new ArrayList<>();
    private boolean groundThing = false;
    private int ticks = 0;
    private int grimTicks = 0;

    public DisablerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant()) {
            return;
        }
        if (!Template.isAdvanced()) {
            Template.notificationManager().addNotification(new Notification("Advanced module", 15000, "Incorrect configuration may flag ac's!"));
            return;
        }

        super.enable();
    }

    @Override
    public void onEnable() {
        ticks = 0;
        grimTicks = 0;
    }

    @Override
    public void onDisable() {
        funnyPackets.forEach(Template::sendNoEvent);
        funnyPackets.clear();
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (vulcanSprint.isEnabled()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }
        if (vulcanLimit.isEnabled()) {
            ticks++;
            if (ticks >= 10) {
                ticks = 0;
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }
        if (dumpTransactions.isEnabled()) {
            grimTicks++;
            if (grimTicks % dumpTime.getIValue() == 0) {
                funnyPackets.forEach(Template::sendNoEvent);
                funnyPackets.clear();
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (!nullCheck())
            return;

        if (event.packet instanceof KeepAliveC2SPacket && cancelKeepAlive.isEnabled()) event.cancel();
        if (event.packet instanceof CommonPongC2SPacket && cancelTransactions.isEnabled()) event.cancel();

        if (cubecraft.isEnabled()) {
            if (event.packet instanceof PlayerMoveC2SPacket wrapper && mc.currentScreen == null) {
                event.cancel();
                Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), wrapper.yaw, wrapper.pitch, true, mc.player.horizontalCollision));
                Template.sendNoEvent(
                        new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                                new BlockHitResult(new Vec3d(0.0d, 1.0d, 0.0d), Direction.UP, mc.player.getBlockPos().down(5), mc.player.horizontalCollision), 0)
                );
            } else if (event.packet instanceof ClientCommandC2SPacket wrapper) {
                if (wrapper.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                    event.cancel();
                    Template.sendNoEvent(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                }
            }
        }
        if (event.packet instanceof PlayerMoveC2SPacket && funnyPacket.isEnabled()) {
            event.cancel();
            mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand() == Hand.OFF_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
            Template.sendNoEvent(event.packet);
            Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
        }
        if (dumpTransactions.isEnabled()) {
            if (event.packet instanceof KeepAliveC2SPacket || event.packet instanceof CommonPongC2SPacket) {
                event.cancel();
                funnyPackets.add(event.packet);
            }
        }
        if (verusCombat.isEnabled()) {
            if (event.packet instanceof CommonPongC2SPacket || event.packet instanceof PlayerActionC2SPacket)
                event.cancel();
        }
        if (verusSemi.isEnabled()) {
            if (event.packet instanceof PlayerActionC2SPacket) {
                event.cancel();
            }

            if (event.packet instanceof KeepAliveC2SPacket || (event.packet instanceof CommonPongC2SPacket)) {
                queueBus.add(event.packet);
                event.cancel();

                if (queueBus.size() > 300) {
                    queueBus.forEach(Template::sendNoEvent);
                    queueBus.clear();
                }
            }

            if (event.packet instanceof PlayerMoveC2SPacket wrapper) {
                if (mc.player.age % 20 == 0) {
                    //Template.sendNoEvent(new PlayerInputC2SPacket(0.98f, 0.98f, false, false));
                }

                if (mc.player.age % 45 == 0) {
                    Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), -0.015625, mc.player.getZ(), wrapper.yaw, wrapper.pitch, false, false));
                }
            }
        }
        if (hypixelSpeed.isEnabled() && Template.moduleManager.isModuleEnabled(SpeedModule.class)) {
            if (event.packet instanceof PlayerMoveC2SPacket.Full wrapper) {
                if (!wrapper.isOnGround()) {
                    groundThing = false;
                    return;
                }
                if (!groundThing) {
                    groundThing = true;
                    event.setCancelled(true);
                    Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 0.01 * Math.random(), mc.player.getZ(), wrapper.yaw, wrapper.pitch, wrapper.isOnGround(), wrapper.horizontalCollision()));
                }
            } else if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround wrapper) {
                if (!wrapper.isOnGround()) {
                    groundThing = false;
                    return;
                }
                if (!groundThing) {
                    groundThing = true;
                    event.setCancelled(true);
                    Template.sendNoEvent(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.01 * Math.random(), mc.player.getZ(), wrapper.isOnGround(), wrapper.horizontalCollision()));
                }
            }
        }
    }
    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (verusSemi.isEnabled() && event.packet instanceof PlayerPositionLookS2CPacket wrapper && mc.player != null && mc.player.getPos().distanceTo(new Vec3d(wrapper.change().position().x, wrapper.change().position().y, wrapper.change().position().z)) < 8) {
            Template.sendNoEvent(new PlayerMoveC2SPacket.Full(wrapper.change().position().x, wrapper.change().position().y, wrapper.change().position().z, wrapper.change().yaw(), wrapper.change().pitch(), false, false));
            event.cancel();
        }
    }
    /*@EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (!nullCheck())
            return;

        if (hover.isEnabled() && event.getPacket() instanceof PlayerMoveC2SPacket wrapper) {
            event.cancel();
            Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                    mc.player.getY() + hoverAmount.getValue(),
                    mc.player.getZ(),
                    wrapper.yaw,
                    wrapper.pitch,
                    hoverOnGround.isEnabled() && mc.player.isOnGround())
            );
        }
    }*/
}
