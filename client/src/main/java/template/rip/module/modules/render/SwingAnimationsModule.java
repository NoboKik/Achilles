package template.rip.module.modules.render;

import net.minecraft.item.ShieldItem;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import template.rip.api.event.events.HeldItemRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class SwingAnimationsModule extends Module {

    public enum modeEnum{Normal, Spin, К1$8, К1$7}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Normal, "Mode");

    public final BooleanSetting constantSwing = new BooleanSetting(this, false, "Constant animation");
    public final BooleanSetting hideShield = new BooleanSetting(this, true, "Hide shield");
    public final BooleanSetting shieldOnly = new BooleanSetting(this, true, "Shield only");
    public final BooleanSetting blockingOnly = new BooleanSetting(this, true, "Blocking only");
    public final BooleanSetting targetOnly = new BooleanSetting(this, true, "Target only");
    public final NumberSetting targetDistance = new NumberSetting(this, 3, 1, 12, 0.1, "Target Distance");
    public final NumberSetting swingDuration = new NumberSetting(this, 1, 0, 5, 0.01, "Swing Duration").setAdvanced();
    public final NumberSetting xScale = new NumberSetting(this, 1, -10d, 10d, 0.1, "X Scale").setAdvanced();
    public final NumberSetting yScale = new NumberSetting(this, 1, -10d, 10d, 0.1, "Y Scale").setAdvanced();
    public final NumberSetting zScale = new NumberSetting(this, 1, -10d, 10d, 0.1, "Z Scale").setAdvanced();
    public final NumberSetting xRotation = new NumberSetting(this, 0, -180d, 180d, 1, "X Rotation").setAdvanced();
    public final NumberSetting yRotation = new NumberSetting(this, 0, -180d, 180d, 1, "Y Rotation").setAdvanced();
    public final NumberSetting zRotation = new NumberSetting(this, 0, -180d, 180d, 1, "Z Rotation").setAdvanced();

    private float j = 0;

    public SwingAnimationsModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    public boolean isAllowed() {
        if (mc.player == null) return false;
        boolean isAllowed = true;
        if (targetOnly.isEnabled()) {
            if (PlayerUtils.findFirstTarget() != null) {
                if (PlayerUtils.findFirstTarget().distanceTo(mc.player) > targetDistance.getFValue()) {
                    isAllowed = false;
                }
            } else {
                isAllowed = false;
            }
        }
        if (shieldOnly.isEnabled() && mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
            if (blockingOnly.isEnabled() && !mc.player.isBlocking()) {
                isAllowed = false;
            }
        } else if (shieldOnly.isEnabled() && !(mc.player.getOffHandStack().getItem() instanceof ShieldItem)) {
            isAllowed = false;
        }

        return isAllowed;
    }

    @EventHandler
    private void onSwing(HeldItemRenderEvent.Swing event) {
        switch (mode.getMode()) {
            case Spin: {
                event.cancel();
                int i = event.arm == Arm.RIGHT ? 1 : -1;
                if (constantSwing.isEnabled()) {
                    j += 0.05f;
                } else {
                    j = event.progress;
                }
                float f = MathHelper.sin(event.progress * event.progress * (float) Math.PI);
                float g = MathHelper.sin(MathHelper.sqrt(event.progress) * (float) Math.PI);

                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0f + f * -20.0f)));

                event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0f));
                event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * -360));
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0f));
                break;
            }
            case Normal:
            case К1$8: {
                event.cancel();
                int i = event.arm == Arm.RIGHT ? 1 : -1;
                float f = MathHelper.sin(event.progress * event.progress * (float) Math.PI);
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0f + f * -20.0f)));
                float g = MathHelper.sin(MathHelper.sqrt(event.progress) * (float) Math.PI);
                event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0f));
                event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f));
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0f));
                break;
            }
        }
    }

    @EventHandler
    private void onEquip(HeldItemRenderEvent.Equip event) {
        switch (mode.getMode()) {
            case Normal: {
                event.cancel();
                event.matrices.scale(xScale.getFValue(), yScale.getFValue(), zScale.getFValue());

                event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation.getFValue()));
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation.getFValue()));
                event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(zRotation.getFValue()));
                int i = event.arm == Arm.RIGHT ? 1 : -1;
                event.matrices.translate((float) i * 0.56f, -0.52f + event.progress * -0.6f, -0.72f);
                break;
            }
            // https://github.com/ViaVersion/ViaFabricPlus/blob/9eb2adf6265cf0ac9d2a17921791642f2b0cdd2c/src/main/java/de/florianmichael/viafabricplus/injection/mixin/fixes/minecraft/item/MixinHeldItemRenderer.java#L50-L60
            case К1$8: {
                event.cancel();
                event.matrices.scale(xScale.getFValue(), yScale.getFValue(), zScale.getFValue());

                event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation.getFValue()));
                event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation.getFValue()));
                event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(zRotation.getFValue()));
                int i = event.arm == Arm.RIGHT ? 1 : -1;
                event.matrices.translate((float) i * 0.56f, -0.52f + event.progress * -0.3f, -0.72f);

                if (i == 1) {
                    event.matrices.translate(event.arm == Arm.RIGHT ? -0.1F : 0.1F, 0.05F, 0.0F);

                    event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
                    event.matrices.multiply((event.arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
                    event.matrices.multiply((event.arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
                }
                break;
            }
            //default -> {
            //    int i = event.arm == Arm.RIGHT ? 1 : -1;
            //    event.matrices.translate((float) i * 0.56f, -0.52f /*+ event.progress * -0.6f*/, -0.72f);
            //}
        }
    }
}
