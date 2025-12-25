package template.rip.module.modules.blatant;

import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class FireballJumpModule extends Module {

    public enum modeEnum{Basic}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Basic, "Mode");
    public final NumberSetting strength = new NumberSetting(this, 3, 0, 4, 0.01, "Strength");
    public final BooleanSetting auto = new BooleanSetting(this, false, "Auto");
    public static int ticksSinceExplosion = Integer.MAX_VALUE;
    int ticksSinceFireball = Integer.MAX_VALUE;

    public FireballJumpModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;
        super.enable();
        if (!nullCheck()) return;

        if (auto.isEnabled()) {
            if (mc.player.getMainHandStack().getItem() != Items.FIRE_CHARGE) {
                int slot = InvUtils.getItemSlot(Items.FIRE_CHARGE);
                if (slot == -1) {
                    this.disable();
                    return;
                }
                InvUtils.setInvSlot(slot);
            }
            Template.rotationManager().setRotation(RotationUtils.correctSensitivity(new Rotation(mc.player.getYaw(), 87f)));
            ticksSinceFireball = 0;
        } else ticksSinceFireball = Integer.MAX_VALUE;
    }

    @Override
    public void onEnable() {
        ticksSinceExplosion = Integer.MAX_VALUE;
        ticksSinceFireball = Integer.MAX_VALUE;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!nullCheck()) {
            return;
        }
        if (event.packet instanceof ExplosionS2CPacket wrapper) {
            if (new Vec3d(wrapper.center().x, wrapper.center().y, wrapper.center().z).distanceTo(mc.player.getPos()) < 5)
                ticksSinceExplosion = 0;
        }
        //if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket wrapper && ticksSincePacket<3)
        //    ticksSinceExplosion = 0;
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }
        if (ticksSinceFireball>0 && auto.isEnabled()) {
            ticksSinceFireball=Integer.MAX_VALUE;
            mc.doItemUse();
        }
        ticksSinceFireball++;
        if (ticksSinceExplosion > 3 && mc.player.isOnGround()) {
            ticksSinceExplosion = Integer.MAX_VALUE;
        }
        if (ticksSinceExplosion <= 80 && ticksSinceExplosion > 1)
            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y + 0.028f, mc.player.getVelocity().z);
        if (ticksSinceExplosion != 1) {
            ticksSinceExplosion++;
            return;
        }
        ticksSinceExplosion++;
        strafe(strength.getFValue());
    }

    public void strafe(double moveSpeed) {
        double rad = Math.toRadians(mc.player.getYaw() + 90);
        //double moveSpeed = 0.15321679421194379; // Sprint walk speed
        double x = Math.cos(rad) * moveSpeed;
        double z = Math.sin(rad) * moveSpeed;
        Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
        mc.player.setVelocity(vec);
    }
}
