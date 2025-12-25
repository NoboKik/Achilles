package template.rip.module.modules.blatant;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

public class ElytraCircleModule extends Module {

    public final NumberSetting radius = new NumberSetting(this, 1, 0, 6, 0.1, "Radius");
    public final NumberSetting speed = new NumberSetting(this, 0.84, 0, 10, 0.1, "Glide Speed for Calc");

    public ElytraCircleModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;
        super.enable();
    }

    @EventHandler
    public void onMouseUpdate(MouseUpdateEvent.Post event) {
        if (!nullCheck()) {
            return;
        }
        ItemStack itemStack;
        if (mc.player.hasVehicle() && mc.player.isClimbing() && !(itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST)).contains(DataComponentTypes.GLIDER) && !isUsable(itemStack)) {
            return;
        }
        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null) {
            return;
        }
        Vec3d vec = calculateNextPosition(speed.value, mc.player.getPos(), target.getPos());
        float yaw;
        float pitch;
        if (mc.player.getAttackCooldownProgress(0.5f) > 0.9) {
            Rotation rot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox().getCenter());
            yaw = rot.fyaw();
            pitch = rot.fpitch();
        } else {
            yaw = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), vec).fyaw();
            pitch = 30;
        }
        Template.rotationManager().setRotation(yaw, pitch);
    }

    // from tarasande
    private Vec3d calculateNextPosition(double selfSpeed, Vec3d curPos, Vec3d center) {
        double angleOffset = selfSpeed / radius.value;
        double angle = Math.toRadians(RotationUtils.getYaw(curPos.subtract(center))) + angleOffset;

        return new Vec3d(center.x - radius.value * Math.sin(angle), center.y, center.z + radius.value * Math.cos(angle));
    }

    private boolean isUsable(ItemStack stack) {
        return stack.getDamage() < stack.getMaxDamage() - 1;
    }
}