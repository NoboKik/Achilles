/*package template.rip.module.modules.blatant;

import net.minecraft.client.util.GlfwUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;
import template.rip.module.setting.settings.StringSetting;

import java.nio.file.Paths;

import static imgui.ImGui.isKeyDown;

public class LongJumpModule extends Module {
    public enum modeEnum{Damage}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>("Mode", this, modeEnum.Damage, modeEnum.values());
    private final BooleanSetting stopMovement = new BooleanSetting("Stop movement", this, false);
    private final NumberSetting waitingTicks = new NumberSetting("Waiting ticks", this, 10, 4, 20, 1);
    private final NumberSetting horizontalBoostAmount = new NumberSetting("Horizontal boost amount", this, 0.2, 0.02, 1.0, 0.02);
    private final NumberSetting afterVelocityYBoost = new NumberSetting("After velocity Y boost", this, 0.0, 0.0, 0.08, 0.002);
    private boolean started;
    private int counter;
    private int ticks;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public LongJumpModule() {
        super("LongJump", "Makes you jump far asf", Category.BLATANT);
    }



    @Override
    public void onEnable() {
        this.ticks = 0;
        this.counter = 0;
        this.started = false;
        this.velocityY = -1.0;
        switch (this.mode.getMode()) {
        }
    }


    @EventHandler
    public void onUpdate(TickEvent.Pre event) {
        switch (this.mode.getMode()) {
            case Damage: {
                if (this.started) {
                    ++this.ticks;
                    if (this.ticks == waitingTicks.getIValue() && this.velocityY != -1.0) {
                        double velocityX = mc.player.getVelocity().x;
                        double velocityY = mc.player.getVelocity().y;
                        double velocityZ = mc.player.getVelocity().z;

                        velocityX -= (double)MathHelper.sin((float)Math.toRadians(mc.player.getYaw())) * this.horizontalBoostAmount.getValue();
                        velocityY = this.velocityY;
                        velocityZ += (double)MathHelper.cos((float)Math.toRadians(mc.player.getYaw())) * this.horizontalBoostAmount.getValue();

                        mc.player.setVelocity(velocityX, velocityY, velocityZ);
                    }
                    if (this.ticks > this.waitingTicks.getValue() && this.velocityY != -1.0) {
                        double velocityX = mc.player.getVelocity().x;
                        double velocityY = mc.player.getVelocity().y;
                        double velocityZ = mc.player.getVelocity().z;

                        velocityY = this.afterVelocityYBoost.getValue();

                        mc.player.setVelocity(velocityX, velocityY, velocityZ);
                    }
                    if (!mc.player.isOnGround()) break;
                    this.setEnabled(false);
                    break;
                }
                if (!mc.player.isOnGround()) break;
                mc.player.jump();
                ++this.counter;
                if (this.counter <= 3) break;
                this.started = true;
                mc.options.forwardKey.setPressed(isKeyDown(mc.options.forwardKey.boundKey.getCode()));
            }
        }
    }

    @EventHandler
    public void onEntityAction(HandleInputEvent event) {
        if (mc.player == null) return;
        if (this.mode.is(modeEnum.Damage) && this.stopMovement.isEnabled() && this.counter < 4) {
            event.setCancelled(true);
        }
    }


    public void strafe(double moveSpeed) {
        if (!PlayerUtils.isPressingMoveInput(false))
            return;

        double rad = Math.toRadians(PlayerUtils.getExactMoveDirection() + 90);
        //double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.cos(rad) * moveSpeed;
        double z = Math.sin(rad) * moveSpeed;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }
    public int getSpeedEffect() {
        return mc.player.getStatusEffect(StatusEffects.SPEED) != null ?
                mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() : 0;
    }

    @EventHandler
    public void onMove(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!PlayerUtils.isMoving()) return;
        if (this.mode.getMode() == modeEnum.Damage) {
            if (this.stopMovement.isEnabled()) {
                if (this.counter <= 3) {
                    strafe(0.0);
                } else if (this.counter == 4 && mc.player.isOnGround() && mc.player.getVelocity().y > 0.4) {
                    if (getSpeedEffect() != 0) {
                        strafe(0.6 + (double) getSpeedEffect() * 0.07);
                    } else {
                        strafe(0.6);
                    }
                }
            }
            if (this.ticks >= this.waitingTicks.getValue() + 14) {
                strafe(0.28);
                return;
            }
            if (this.ticks < this.waitingTicks.getValue())
                strafe(2.0);
            if (this.started || this.counter >= 3) return;
            mc.player.setOnGround(false);
        }
    }


    @EventHandler
    public void onReceive(PacketEvent.Receive event) {
        switch (this.mode.getMode()) {
            case Damage: {
                EntityVelocityUpdateS2CPacket packet;
                if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket) || (packet = (EntityVelocityUpdateS2CPacket)event.getPacket()).getId() != mc.player.getId()) break;
                event.setCancelled(true);
                this.velocityX = (double)packet.getVelocityX() / 8000.0;
                this.velocityY = (double)packet.getVelocityY() / 8000.0;
                this.velocityZ = (double)packet.getVelocityZ() / 8000.0;
            }
        }
    }
}


 */