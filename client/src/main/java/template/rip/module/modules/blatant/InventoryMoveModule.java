package template.rip.module.modules.blatant;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;

import java.util.ArrayList;

public class InventoryMoveModule extends Module {

    public enum modeEnum{None, Hypixel, HypixelTest, Blink}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.None, "Mode");
    private final ArrayList<Packet<?>> movePackets = new ArrayList<>();
    private final ArrayList<Packet<?>> otherPackets = new ArrayList<>();
    private boolean isBlinking = true;

    public InventoryMoveModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return " " + mode.getMode().name();
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    protected void disable() {
        super.disable();

        if (!movePackets.isEmpty()) movePackets.forEach(Template::sendNoEvent);
        movePackets.clear();
        if (!otherPackets.isEmpty()) otherPackets.forEach(Template::sendNoEvent);
        otherPackets.clear();
    }

    public double sqrtSpeed() {
        return Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    public int getSpeedEffect() {
        return mc.player.getStatusEffect(StatusEffects.SPEED) != null ? mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() : 0;
    }
    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player == null) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
            mc.options.forwardKey.setPressed(KeyUtils.isKeyPressed(mc.options.forwardKey.boundKey.getCode()));
            mc.options.backKey.setPressed(KeyUtils.isKeyPressed(mc.options.backKey.boundKey.getCode()));
            mc.options.rightKey.setPressed(KeyUtils.isKeyPressed(mc.options.rightKey.boundKey.getCode()));
            mc.options.leftKey.setPressed(KeyUtils.isKeyPressed(mc.options.leftKey.boundKey.getCode()));
            mc.options.jumpKey.setPressed(KeyUtils.isKeyPressed(mc.options.jumpKey.boundKey.getCode()));
            mc.options.sneakKey.setPressed(KeyUtils.isKeyPressed(mc.options.sneakKey.boundKey.getCode()));
            if (mode.getMode() == modeEnum.Hypixel && mc.currentScreen instanceof InventoryScreen) {
                Template.sendNoEvent(new CloseHandledScreenC2SPacket(0));
            }
        }

        if (mode.is(modeEnum.HypixelTest) && mc.currentScreen instanceof InventoryScreen) {
            double x = mc.player.getVelocity().x;
            double y = mc.player.getVelocity().y;
            double z = mc.player.getVelocity().z;

            if (mc.player.getMovementSpeed() >= 0.12) {
                x = x * 0.6;
                z = z * 0.6;
            }

            if (getSpeedEffect() == 0) {
                x = x * 0.74;
                z = z * 0.74;
            }

            int amplifier = getSpeedEffect();

            switch(amplifier) {
                case 1:
                    x = x * 0.52;
                    z = z * 0.52;
                case 2:
                    x = x * 0.31;
                    z = z * 0.31;
            }
            Vec3d vec = new Vec3d(x, y, z);
            mc.player.setVelocity(vec);
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mode.getMode() == modeEnum.Blink && mc.currentScreen instanceof HandledScreen<?>) {
            isBlinking = true;
            if (event.packet instanceof PlayerMoveC2SPacket) movePackets.add(event.packet);
            else otherPackets.add(event.packet);
            event.setCancelled(true);
        } else if (isBlinking) {
            isBlinking = false;
            if (!movePackets.isEmpty()) movePackets.forEach(Template::sendNoEvent);
            movePackets.clear();
            if (!otherPackets.isEmpty()) otherPackets.forEach(Template::sendNoEvent);
            otherPackets.clear();
        }
    }
}
