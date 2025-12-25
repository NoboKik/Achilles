package template.rip.api.rotation;

import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.util.PlayerUtils;
import template.rip.module.modules.client.AchillesSettingsModule;

import static java.lang.Math.abs;
import static template.rip.Template.mc;
import static template.rip.module.modules.blatant.ScaffoldModule.allowSprint;

public class RotationManager {

    public float yyyaw = 0f;
    public float pppitch = 0f;
    public float lastForward;
    private static long lastModificationTime = 0;
    float prevYaw, prevPitch;
    private boolean enabled, disabled, rotateBack;
    private float clientYaw, clientPitch;
    private float realYaw, realPitch;

    public RotationManager() {
        enabled = true;
        rotateBack = false;

        this.clientYaw = 0;
        this.clientPitch = 0;

        this.realYaw = 0;
        this.realPitch = 0;
    }

    public Rotation getClientRotation() {
        return new Rotation(clientYaw, clientPitch);
    }

    public Rotation getRealRotation() {
        return new Rotation(realYaw, realPitch);
    }

    public Rotation getPrevRotation() {
        return new Rotation(prevYaw, prevPitch);
    }

    private void enable() {
        enabled = true;
        disabled = false;
        rotateBack = false;
    }

    private void disable() {
        if (!disabled) {
            disabled = true;
            if (!rotateBack) {
                rotateBack = true;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setRotation(Rotation rotation) {
        lastModificationTime = System.currentTimeMillis();
        this.clientYaw = rotation.fyaw();
        this.clientPitch = rotation.fpitch();
    }

    public void setRotation(double yaw, double pitch) {
        setRotation(new Rotation(yaw, pitch));
    }

    public void setYaw(double yaw) {
        lastModificationTime = System.currentTimeMillis();
        clientYaw = (float) yaw;
    }

    public void setPitch(double pitch) {
        lastModificationTime = System.currentTimeMillis();
        clientPitch = (float) pitch;
    }

    public void tick() {
        if (System.currentTimeMillis() - lastModificationTime > 500) {
            disable();
        } else {
            enable();
        }
    }

    public float yaw() {
        return (isEnabled() ? getClientRotation().fyaw() : (mc.player != null ? mc.player.getYaw() : 0));
    }

    public float pitch() {
        return (isEnabled() ? getClientRotation().fpitch() : (mc.player != null ? mc.player.getPitch() : 0));
    }

    public Rotation rotation() {
        return new Rotation(yaw(), pitch());
    }

    @EventHandler(priority = EventPriority.HIGH)
    void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesLook()) {
                clientYaw = packet.getYaw(clientYaw);
                clientPitch = packet.getPitch(clientPitch);

                realYaw = clientYaw;
                realPitch = clientPitch;
            }
        }
    }

    @EventHandler
    void onYaw(WorldRenderEvent event) {
        yyyaw = event.context.camera().getYaw();
        pppitch = event.context.camera().getPitch();
        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            yyyaw = MathHelper.wrapDegrees(yyyaw + 180);
        }
    }

    @EventHandler
    void onWalk(WalkingForwardEvent event) {
        AchillesSettingsModule acm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (acm != null && (acm.moveFixMode.is(AchillesSettingsModule.moveFixModeEnum.Backwards_Sprint) || !allowSprint) && isEnabled() && PlayerUtils.isMoving()) {
            double yaw = abs(MathHelper.wrapDegrees(Template.rotationManager().getClientRotation().fyaw() - PlayerUtils.getMoveDirection()));
            event.forward = yaw <= 45F;
        }
    }

    // from tarasande (nobody else has a better movefix)
    @EventHandler(priority = 9999)
    void onInput(InputEvent event) {
        if (!event.check || !isEnabled() || event.input.movementForward == 0 && event.input.movementSideways == 0)
            return;

        AchillesSettingsModule acm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (acm != null && !acm.moveFixMode.is(AchillesSettingsModule.moveFixModeEnum.Silent))
            return;

        float realYaw = yyyaw;
        float fakeYaw = clientYaw;

        double moveX = event.input.movementSideways * Math.cos(Math.toRadians(realYaw)) - event.input.movementForward * Math.sin(Math.toRadians(realYaw));
        double moveZ = event.input.movementForward * Math.cos(Math.toRadians(realYaw)) + event.input.movementSideways * Math.sin(Math.toRadians(realYaw));

        double minDist = Double.MAX_VALUE;
        double bestForward = 0;
        double bestStrafe = 0;

        for (double forward = -1; forward <= 1; forward++) {
            for (double strafe = -1; strafe <= 1; strafe++) {
                double newMoveX = strafe * Math.cos(Math.toRadians(fakeYaw)) - forward * Math.sin(Math.toRadians(fakeYaw));
                double newMoveZ = forward * Math.cos(Math.toRadians(fakeYaw)) + strafe * Math.sin(Math.toRadians(fakeYaw));

                double deltaX = newMoveX - moveX;
                double deltaZ = newMoveZ - moveZ;

                double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                if (minDist > dist) {
                    minDist = dist;
                    bestForward = forward;
                    bestStrafe = strafe;
                }
            }
        }
        event.input.movementForward = (Math.round(bestForward));
        event.input.movementSideways = (Math.round(bestStrafe));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onLastInput(InputEvent event) {
        lastForward = event.input.movementForward;
    }

    @EventHandler
    void preMovementPacket(SendMovementPacketEvent.Pre event) {
        if (!rotateBack) {
            if (isEnabled()) {
                prevYaw = realYaw = mc.player.getYaw();
                prevPitch = realPitch = mc.player.getPitch();

                mc.player.setYaw(clientYaw);
                mc.player.setPitch(clientPitch);
            }
        } else {
            Rotation serverRot = new Rotation(clientYaw, clientPitch);
            Rotation clientRot = new Rotation(mc.player.getYaw(), mc.player.getPitch());

            Rotation diff = RotationUtils.getDiff(serverRot, clientRot);

            float pitch = Math.abs(MathHelper.wrapDegrees(diff.fpitch()));
            float yaw = Math.abs(MathHelper.wrapDegrees(diff.fyaw()));
            if (pitch < 1 && yaw < 1) {
                rotateBack = false;
                enabled = false;
            } else {
                Rotation smoothRotation = RotationUtils.getSmoothRotation(serverRot, clientRot, 0.4);
                smoothRotation = RotationUtils.correctSensitivity(smoothRotation);

                clientYaw = smoothRotation.fyaw();
                clientPitch = smoothRotation.fpitch();

                prevYaw = mc.player.getYaw();
                prevPitch = mc.player.getPitch();

                mc.player.setYaw(clientYaw);
                mc.player.setPitch(clientPitch);
            }
        }
    }

    @EventHandler
    void postMovementPacket(SendMovementPacketEvent.Post event) {
        if (prevYaw != 0 && prevPitch != 0) {
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);

            prevYaw = 0;
            prevPitch = 0;
        }
    }
}
