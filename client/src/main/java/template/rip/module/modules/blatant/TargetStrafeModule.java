package template.rip.module.modules.blatant;

import net.minecraft.entity.LivingEntity;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class TargetStrafeModule extends Module {
    public TargetStrafeModule() {
        super(Category.BLATANT, Description.of("Strafes around your target."), "TargetStrafe");
    }

    //public enum modeEnum{Grim, Hoplite, Blink, Hypixel, Motion}
    //public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Grim, "Mode");
    public final NumberSetting strafeDist = new NumberSetting(this, 0.15, 1, 10, 0.1, "Strafe Dist");
    public final NumberSetting minDist = new NumberSetting(this, 2, 1, 20, 0.01, "Min Dist");
    public final NumberSetting strafeDegree = new NumberSetting(this, 30, -90, 90, 0.01, "Strafe Degree");
    public final BooleanSetting spaceBar = new BooleanSetting(this, false, "Space Bar Only");

    public double getDirection() {
        if (!nullCheck()) return PlayerUtils.getMoveDirection();
        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null) return PlayerUtils.getMoveDirection();

        double extra = MathUtils.closestPosBoxDistance(mc.player.getPos(), target.getBoundingBox()) < strafeDist.getValue() ?
                strafeDegree.getValue() : 0;

        Rotation targetRot = RotationUtils.getRotations(mc.player.getPos(), target.getBoundingBox().getCenter());
        return targetRot.fyaw()+extra;
    }
}
