package template.rip.module.modules.blatant;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class FlightModule extends Module {

    public enum modeEnum{Vanilla, Motion}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Vanilla, "Mode");
    public final NumberSetting vanillaSpeed = new NumberSetting(this, 1, 0, 10, 0.1, "Vanilla Speed");
    public final NumberSetting vanillaKickBypass = new NumberSetting(this, -0.1, -0.25, 0, 0.01, "Vanilla Kick Bypass");
    public final NumberSetting motionSpeed = new NumberSetting(this, 1, 0, 20, 0.1, "Motion Speed");
    public final NumberSetting motionKickBypass = new NumberSetting(this, -0.1, -0.25, 0, 0.01, "Motion Kick Bypass");
    private boolean wasFlying;
    private int ticks;

    public FlightModule(Category category, Description description, String name) {
        super(category, description, name);
        vanillaSpeed.addConditionMode(mode, modeEnum.Vanilla);
        vanillaKickBypass.addConditionMode(mode, modeEnum.Vanilla);
        motionSpeed.addConditionMode(mode, modeEnum.Motion);
        motionKickBypass.addConditionMode(mode, modeEnum.Motion);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;
        super.enable();
    }

    @Override
    public void onEnable() {
        ticks = 0;
        wasFlying = mc.player.getAbilities().flying;
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            if (mode.is(modeEnum.Motion)) mc.player.setVelocity(0,0,0);

            if(mode.is(modeEnum.Vanilla) && !wasFlying && !mc.player.isSpectator()) {
                mc.player.getAbilities().flying = false;
            }

            mc.player.getAbilities().setFlySpeed(0.05f);
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (mode.is(modeEnum.Vanilla)) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed(vanillaSpeed.getFValue() / 20f);
            if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {
                ticks++;
            } else {
                ticks = 0;
            }
            if (ticks > 10) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + vanillaKickBypass.value, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, mc.player.horizontalCollision));
            }
        } else if (mode.is(modeEnum.Motion)) {
            if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {
                ticks++;
            } else {
                ticks = 0;
            }
            if (ticks > 10) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + vanillaKickBypass.value, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, mc.player.horizontalCollision));
            }

            if (PlayerUtils.isMoving()) {
                strafe(motionSpeed.getValue());
            } else {
                mc.player.setVelocity(0, 0, 0);
            }
        }
    }

    public void strafe(double moveSpeed) {
        double x = 0;
        double y = 0;
        double z = 0;

        // Vertical
        if (KeyUtils.isKeyPressed(mc.options.jumpKey.boundKey.getCode()) && KeyUtils.isKeyPressed(mc.options.sneakKey.boundKey.getCode())) y = 0;
        else if (KeyUtils.isKeyPressed(mc.options.jumpKey.boundKey.getCode())) y = moveSpeed;
        else if (KeyUtils.isKeyPressed(mc.options.sneakKey.boundKey.getCode())) y = -moveSpeed;

        // Horizontal
        if (PlayerUtils.isPressingMoveInput(false)) {
            double rad = Math.toRadians(PlayerUtils.getExactMoveDirection() + 90);
            x = Math.cos(rad) * moveSpeed;
            z = Math.sin(rad) * moveSpeed;
        }
        Vec3d vec = new Vec3d(x, y, z);

        mc.player.setVelocity(vec);
    }
}
