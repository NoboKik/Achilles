package template.rip.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.BacktrackEvent;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.FakePlayerEntity;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BacktrackModule extends Module {

    public enum modeEnum {Latency, Pulse, Infinite, DynamicLatency}

    private final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Latency: Smoothly delays packets, like high ping\nPulse: Delays all packets till the delay is met, like bad wifi\nInfinite: Delays packets infinitely\nDynamicLatency: Latency mode but it dynamically changes the delay based off of target distance"), modeEnum.Latency, "Mode");
    private final MinMaxNumberSetting delays = new MinMaxNumberSetting(this, 200, 600, 0, 2500, 20, "Delays");
    private final MinMaxNumberSetting dynamicLatencyDistance = new MinMaxNumberSetting(this, 3.0, 5.0, 0, 6, 0.1, "DynLatency dist ping");
    private final ColorSetting lRColor = new ColorSetting(this, new JColor(0.0f, 0.0f, 1.0f, 0.3f), true, Description.of("Renders the real position of entities while using backtrack"), "Real position box Color");
    private final BooleanSetting onlyClientTargets = new BooleanSetting(this, true, "Only render for targets");
    private final BooleanSetting weaponOnly = new BooleanSetting(this, true, "Weapon Only");
    private final BooleanSetting auto = new BooleanSetting(this, Description.of("Only delays packets when near a target"), true, "Dynamic backtrack");
    private final NumberSetting dynaDistance = new NumberSetting(this, Description.of("The distance to enable backtrack when under, and disable backtrack when over"), 3.1, 1.0, 6.0, 0.1, "Backtrack distance");
    private final BooleanSetting realPos = new BooleanSetting(this, true, "Render real position");
    private final NumberSetting maxRealDistance = new NumberSetting(this, Description.of("Max real target position distance."), 9, 3, 30, 0.1, "Max real distance");
    private final DividerSetting packetWhiteListDiv = new DividerSetting(this, false, "Packet Whitelist");
    private final BooleanSetting updateHealth = new BooleanSetting(this, true, "Update Health");
    private final BooleanSetting realHurtTime = new BooleanSetting(this, false, "Update HurtTime").setAdvanced();
    private final BooleanSetting realAnimation = new BooleanSetting(this, false, "Update Animations").setAdvanced();
    private final BooleanSetting realData = new BooleanSetting(this, false, "Update DataTracker").setAdvanced();
    private final BooleanSetting realEquipment = new BooleanSetting(this, false, "Update Equipment").setAdvanced();
    private final BooleanSetting ignoreKeepAlive = new BooleanSetting(this, Description.of("Won't delay KeepAlive packets\nso you'll have normal ping in the playerlist"), false, "Instant KeepAlive");
    private final BooleanSetting velodisable = new BooleanSetting(this, Description.of("Pulses when you receive velocity"), false, "Disable on velocity");
    private final BooleanSetting delayUntilHit = new BooleanSetting(this, Description.of("Delays packets when you receive velocity until you sprint attack (to reduce knockback)!"), false, "Delay on velo till hit").setAdvanced();
    private final NumberSetting delayUntilHitTimeOut = new NumberSetting(this, Description.of("The maximum amount of time to delay packets for when waiting for a sprint attack"), 2500, 50, 100000, 1, "Delay on velo timer");
    private final BooleanSetting disableOnTp = new BooleanSetting(this, Description.of("Pulses when you get teleported or mitigated"), true, "Disable on teleport").setAdvanced();
    private final KeybindSetting pulseKey = new KeybindSetting(this, -1, "Pulse Key");
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    private final ConcurrentHashMap<Entity, Vec3d> realPositions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Entity, Vec3d> lastPositions = new ConcurrentHashMap<>();
    private World lastWorld = null;
    private long latencyTimer = 0;
    private long pulseTimer = 0;
    public static boolean hadVelo;
    private long veloTime;

    public BacktrackModule(Category category, Description description, String name) {
        super(category, description, name);
        packetWhiteListDiv.addSetting(updateHealth, realHurtTime, realAnimation, realData, ignoreKeepAlive);
        dynamicLatencyDistance.addConditionMode(mode, modeEnum.DynamicLatency);
    }

    @Override
    public String getSuffix() {
        String suffix = " ";
        switch (mode.getMode()) {
            case Latency:
                suffix += (Template.backtrackUtil().latency ? latencyTimer : 0) + " ms";
                break;
            case DynamicLatency:
                suffix += (Template.backtrackUtil().dynamicLatency ? latencyTimer : 0) + " ms";
                break;
            case Pulse:
                suffix += Math.max(Template.backtrackUtil().backtrack ? pulseTimer - System.currentTimeMillis() : 0, 0) + " ms";
                break;
            case Infinite:
                suffix += "Infinite";
                break;
        }
        return suffix;
    }

    @Override
    public void onEnable() {
        realPositions.clear();
        lastPositions.clear();
        latencyTimer = delays.getRandomInt();
        pulseTimer = System.currentTimeMillis() + delays.getRandomInt();
        veloTime = 0;
        hadVelo = false;
    }

    @Override
    public void onDisable() {
        executorService.submit(() -> Template.backtrackUtil().dumpPackets());
    }

    private boolean isHoldingWeapon() {
        if (mc.player == null)
            return false;

        ItemStack heldItem = mc.player.getMainHandStack();

        return heldItem.getItem() instanceof SwordItem || heldItem.getItem() instanceof AxeItem;
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!nullCheck())
            return;

        if (realPos.isEnabled()) {
            for (Map.Entry<Entity, Vec3d> info : realPositions.entrySet()) {
//                if (realPosMode.is("Box")) {
                Entity e = info.getKey();
                if (e == null)
                    continue;

                if (!PlayerUtils.findTargets().contains(e) && onlyClientTargets.isEnabled())
                    continue;

                Box box = e.getBoundingBox();
                Vec3d pos = info.getValue();

                if (pos == null)
                    continue;

                Vec3d last = pos;
                if (lastPositions.containsKey(e) && lastPositions.get(e) != null) {
                    last = lastPositions.get(e);
                }

                if (last == null)
                    continue;

                pos = MathUtils.smoothVec3d(last, pos, 10f / mc.getCurrentFps());

                box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());
                box = box.offset(pos.subtract(new Vec3d(box.maxX / 2, 0, box.maxZ / 2)));
                RenderUtils.Render3D.renderBox(box, lRColor.getColor().getRed(), lRColor.getColor().getGreen(), lRColor.getColor().getBlue(), lRColor.getColor().getAlpha(), event.context);

                lastPositions.put(e, pos);
//                }
            }
        }
    }

    private void putPos(Entity e, Vec3d v) {
        realPositions.put(e, v);
    }

    @EventHandler
    public void onInput(KeyPressEvent event) {
        if (!nullCheck()) return;
        if (pulseKey.isPressed()) {
            Template.backtrackUtil().dumpPackets();
            onEnable();
        }
    }

    @EventHandler
    public void onAttack(AttackEntityEvent.Pre event) {
        if (mc.player.isSprinting() && hadVelo) {
            Template.backtrackUtil().dumpPackets();
            onEnable();
        }
    }

    @EventHandler(priority = -1)
    private void onReceivePacket(BacktrackEvent event) {
        if (!nullCheck() || event.packet == null)
            return;

        event.backtrack = true;
        Packet<?> p = event.packet;

        if (weaponOnly.isEnabled() && !isHoldingWeapon()) {
            event.backtrack = false;
        }

        if (p instanceof PlayerPositionLookS2CPacket && disableOnTp.isEnabled()) {
            event.backtrack = false;
        }

        if ((p instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == mc.player.getId()) || p instanceof ExplosionS2CPacket) {
            if (velodisable.isEnabled()) {
                event.backtrack = false;
            } else if (delayUntilHit.isEnabled()) {
                veloTime = System.currentTimeMillis();
                hadVelo = true;
            }
        }

        if (p instanceof EntityS2CPacket packet && realPos.isEnabled() && packet.isPositionChanged() && packet.getEntity(mc.world) instanceof LivingEntity && !(packet.getEntity(mc.world) instanceof FakePlayerEntity)) {
            Entity ent = packet.getEntity(mc.world);
            if (ent != null) {
                TrackedPosition trackedPosition = new TrackedPosition();
                trackedPosition.setPos(realPositions.containsKey(ent) ? realPositions.get(ent) : ent.getPos());
                Vec3d pos = trackedPosition.withDelta(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());

                putPos(ent, pos);
            }
        }

        if (p instanceof EntityPositionS2CPacket packet && realPos.isEnabled() && mc.world.getEntityById(packet.entityId()) instanceof LivingEntity && !(mc.world.getEntityById(packet.entityId()) instanceof FakePlayerEntity)) {
            Entity ent = mc.world.getEntityById(packet.entityId());
            Vec3d pos = new Vec3d(packet.change().position().x, packet.change().position().y, packet.change().position().z);

            if (ent != null) {
                putPos(ent, pos);
            }
        }

        if (p instanceof EntitiesDestroyS2CPacket packet && realPos.isEnabled()) {
            List<Entity> ents = new ArrayList<>();
            packet.getEntityIds().forEach(integer -> ents.add((mc.world).getEntityById(integer)));
            for (Entity ent : ents) {
                if (ent != null) {
                    lastPositions.remove(ent);
                    realPositions.remove(ent);
                }
            }
        }

        if (lastWorld != mc.world) {
            event.backtrack = false;
            lastWorld = mc.world;
        }
        if (!delayPackets(p)) {
            event.skip = true;
        }
        lastWorld = mc.world;

        if (hadVelo && veloTime != 0 && System.currentTimeMillis() - veloTime > delayUntilHitTimeOut.getIValue()) {
            hadVelo = false;
            veloTime = 0;
            Template.backtrackUtil().dumpPackets();
            onEnable();
        }

        if (mode.is(modeEnum.DynamicLatency)) {
            if (!hadVelo) {
                event.dynamicLatency = true;
                event.backtrack = false;
                Entity entity = PlayerUtils.findFirstTarget();
                double delta = entity == null ? 1 : dynamicLatencyDistance.getLerpInMinAndMax((mc.player.getPos().distanceTo(realPositions.containsKey(entity) ? realPositions.get(entity) : entity.getPos())));
                delta *= -1;
                delta += 1;
                latencyTimer = (long) delays.getLerpedFromMinAndMax(delta);
                event.latencyTime = latencyTimer;
            }

            if (shouldStop() && auto.isEnabled()) {
                event.backtrack = false;
                event.dynamicLatency = false;
            }
        }

        if (mode.is(modeEnum.Latency)) {
            if (!hadVelo) {
                event.latency = true;
                event.backtrack = false;
                event.latencyTime = latencyTimer;
            }

            if (shouldStop() && auto.isEnabled()) {
                event.backtrack = false;
                event.latency = false;
            }
        }

        if (mode.is(modeEnum.Pulse) && (pulseTimer <= System.currentTimeMillis() && !hadVelo) || (shouldStop() && auto.isEnabled())) {
            event.backtrack = false;
        }

        if (mode.is(modeEnum.Infinite) && (shouldStop() && auto.isEnabled())) {
            event.backtrack = false;
        }
        if (!event.backtrack && !event.latency && !event.dynamicLatency) {
            Template.backtrackUtil().dumpPackets();
            onEnable();
        }
    }

    // from augustus 2.6 because I am so done with this
    public boolean delayPackets(Packet<?> packet) {
        if (packet instanceof WorldTimeUpdateS2CPacket) {
            return true;
        }
        if (packet instanceof KeepAliveS2CPacket && ignoreKeepAlive.isEnabled()) {
            return false;
        }
        if (packet instanceof EntityStatusS2CPacket) {
            return !(((EntityStatusS2CPacket) packet).getEntity(mc.world) instanceof LivingEntity);
        }
        if (packet instanceof EntityAnimationS2CPacket && realAnimation.isEnabled()) {
            return false;
        }
        if (packet instanceof EntityTrackerUpdateS2CPacket etu && realData.isEnabled() && etu.id() != mc.player.getId()) {
            return false;
        }
        if (packet instanceof EntityEquipmentUpdateS2CPacket equ && realEquipment.isEnabled() && equ.getEntityId() != mc.player.getId()) {
            return false;
        }
        if (packet instanceof EntityDamageS2CPacket && realHurtTime.isEnabled() && ((EntityDamageS2CPacket) packet).entityId() != mc.player.getId()) {
            return false;
        }
        if (packet instanceof HealthUpdateS2CPacket && updateHealth.isEnabled()) {
            return false;
        }
        return !(packet instanceof PlaySoundS2CPacket) && !(packet instanceof TeamS2CPacket);
    }

    private boolean shouldStop() {
        if (mc.world == null || mc.player == null)
            return true;

        if (hadVelo) {
            return false;
        }

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null)
            return true;

        if (MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox()) > dynaDistance.value)
            return true;

        if (realPositions.get(target) == null)
            return false;


        Box box = target.getBoundingBox();
        Vec3d position = realPositions.get(target);
        box = MathUtils.boxAtPos(box, position);

        double realDistance = MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box);
        double clientDistance = MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox());

        if (realDistance > maxRealDistance.value)
            return true;

        return clientDistance > realDistance;
    }
}
