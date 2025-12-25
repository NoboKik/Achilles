package template.rip.module.modules.player;

import com.google.common.collect.Streams;
import net.minecraft.item.BlockItem;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.SafeWalkEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InputUtil;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class BridgeAssistModule extends Module {

    public enum modeEnum{Sneak, Stop_Move, GodBridge, Breezily, SafeWalk}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Sneak, "Mode");

    public final NumberSetting minAngle = new NumberSetting(this, 50, 45, 75, 1, "Min Angle");
//  public final NumberSetting checkHeight = new NumberSetting("Min Height", this, 5, 0, 10, 1);
    public final BooleanSetting onlyWhenHolding = new BooleanSetting(this, true, "Only when holding block");
    public final BooleanSetting onlyWhenLookingAtBlock = new BooleanSetting(this, false, "Only when looking at block");
    public final BooleanSetting onlyWhenBack = new BooleanSetting(this, true, "Only when moving backwards");
    public final MinMaxNumberSetting unSneak = new MinMaxNumberSetting(this, 75, 150, 0, 300, 1, "UnSneak delays");
    public final NumberSetting gbPredict = new NumberSetting(this, 0.55, 0.1, 2, 0.1, "GodBridge predict").setAdvanced();
    public final NumberSetting edgeDistance = new NumberSetting(this, 0.2, 0.1, 0.5, 0.01, "Edge Distance");
    private long timer = 0;
    private boolean right = false;

    public BridgeAssistModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        right = false;
        timer = 0;
    }

    public boolean checkHands() {
        if (mc.player == null) return true;
        return mc.player.getMainHandStack().getItem() instanceof BlockItem || mc.player.getOffHandStack().getItem() instanceof BlockItem;
    }

    private Box setMinY(Box box, double minY) {
        return new Box(box.minX, box.minY - minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @EventHandler
    private void onSafeWalk(SafeWalkEvent event) {
        if (!pre())
            return;

        if (!mode.is(modeEnum.SafeWalk))
            return;

        event.safe = true;
    }

    private boolean pre() {
        if (mc.currentScreen != null || !checkHands() && onlyWhenHolding.isEnabled()) return false;
        if (onlyWhenLookingAtBlock.isEnabled() && (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK)) return false;
        if (onlyWhenBack.isEnabled() && mc.player.input.movementForward > 0) return false;
        return MathHelper.wrapDegrees(mc.player.getPitch()) > minAngle.getFValue();
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (mc.player == null || mc.world == null || !event.check || !pre()) return;
        if (!mc.player.isOnGround() && mode.is(modeEnum.Sneak)) {
            timer = System.currentTimeMillis() + unSneak.getRandomInt();
        } else {

            if (mode.is(modeEnum.GodBridge)) {
                // 0.55 works mostly
                Box adjustedBox = mc.player.getBoundingBox().offset(0, -0.5, 0).offset(mc.player.getVelocity().multiply(gbPredict.value));
                if (Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox)).findAny().isEmpty()) {
                    event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
                }
            }

            if (PlayerUtils.blockEdgeDist() != 0 && PlayerUtils.blockEdgeDist() <= edgeDistance.value) {
                if (mode.is(modeEnum.Sneak)) {
                    timer = System.currentTimeMillis() + unSneak.getRandomInt();
                }
                if (mode.is(modeEnum.Stop_Move) && !event.input.playerInput.sneak()) {
                    event.input.movementSideways = 0;
                    event.input.movementForward = Math.max(event.input.movementForward, 0f);
                }
            }
            if (mode.is(modeEnum.Breezily) && event.input.movementForward < 0) {
                Direction moveDir = Direction.fromHorizontalDegrees(MathHelper.wrapDegrees(Template.rotationManager().yaw() + 180));
                Vec3d vec = mc.player.getBlockPos().toCenterPos().subtract(mc.player.getPos());

                switch (moveDir) {
                    case EAST: {
                        if (vec.z >= 0.1)
                            right = true;
                        if (vec.z <= -0.1)
                            right = false;
                        break;
                    }
                    case WEST: {
                        if (vec.z >= 0.1)
                            right = false;
                        if (vec.z <= -0.1)
                            right = true;
                        break;
                    }
                    case SOUTH: {
                        if (vec.x >= 0.1)
                            right = false;
                        if (vec.x <= -0.1)
                            right = true;
                        break;
                    }
                    case NORTH: {
                        if (vec.x >= 0.1)
                            right = true;
                        if (vec.x <= -0.1)
                            right = false;
                        break;
                    }
                }
                event.input.movementSideways = (right ? 1 : -1);
            }
        }
        if (System.currentTimeMillis() < timer) event.input.playerInput = InputUtil.setSneaking(event.input.playerInput, true);
    }
}
