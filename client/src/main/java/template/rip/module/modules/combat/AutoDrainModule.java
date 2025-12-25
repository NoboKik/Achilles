package template.rip.module.modules.combat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.blatant.MultiTaskModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;

import java.util.function.BiPredicate;

public class AutoDrainModule extends Module {

    public enum rotationMode {Silent, Off}

    public final ModeSetting<rotationMode> rotMode = new ModeSetting<>(this, rotationMode.Silent, "Rotation Mode");

    public enum modeEnum {Switch, Manual}

    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Switch, "Bucket Mode");
    //    public enum bucketFilter {Water, Lava, WaterThenLava, LavaThenWater, Empty}
//    public final ModeSetting<bucketFilter> bucketFilterMode = new ModeSetting<>(this, Description.of("Empty buckets will always take priority over other buckets regardless of filter mode"), bucketFilter.WaterThenLava, "Bucket filter mode");
    public final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch Back");
    public final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 2, 3, 0, 10, 1, "Switch Delay");
    public final MinMaxNumberSetting stealDelay = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Steal Delay");

    public enum liqEnum {Lava, Water, WaterOnWeb, LavaOnWeb, All}

    public final ModeSetting<liqEnum> liqModes = new ModeSetting<>(this, liqEnum.WaterOnWeb, "Liquid Modes");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();

    private int stealTimer, switchTimer, prevSlot;
    private boolean did;

    BiPredicate<Block, BlockPos> liqPred = (block, bpos) -> {
        switch (liqModes.getMode()) {
            case All: {
                if (block != Blocks.WATER && block != Blocks.LAVA)
                    return false;
                break;
            }
            case Water: {
                if (block != Blocks.WATER)
                    return false;
                break;
            }
            case Lava: {
                if (block != Blocks.LAVA)
                    return false;
                break;
            }
            case LavaOnWeb: {
                if (block != Blocks.LAVA || !checkWeb(bpos))
                    return false;
                break;
            }
            case WaterOnWeb: {
                if (block != Blocks.WATER || !checkWeb(bpos))
                    return false;
                break;
            }
        }
        return true;
    };

    public AutoDrainModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        stealTimer = stealDelay.getRandomInt();
        switchTimer = switchDelay.getRandomInt();
        did = false;
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (switchBack.isEnabled() && prevSlot != -1) {
            InvUtils.setInvSlot(prevSlot);
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck() || (disableInScreens.isEnabled() && mc.currentScreen != null)) {
            return;
        }

        MultiTaskModule mtm = Template.moduleManager.getModule(MultiTaskModule.class);
        if (mc.player.isUsingItem() && (mtm == null || !mtm.isEnabled() || !mtm.item.isEnabled())) {
            return;
        }

        Boolean cd = canDo();
        if (Boolean.FALSE.equals(cd)) {
            if (switchBack.isEnabled() && prevSlot != -1) {
                if (switchTimer > 0) {
                    switchTimer--;
                    return;
                }
                InvUtils.setInvSlot(prevSlot);
                prevSlot = -1;
            } else {
                onEnable();
            }
            return;
        }

        if (stealTimer > 0) {
            stealTimer--;
            return;
        }

        if (Template.isClickSim())
            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        did = true;

    }

    private boolean checkWeb(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (mc.world.getBlockState(pos.add(dir.getVector())).getBlock() == Blocks.COBWEB)
                return true;
        }
        return false;
    }

    private Boolean canDo() {
        if (did) {
            return false;
        }

        Vec3d eye = mc.player.getEyePos();
        BlockHitResult hitResult = mc.world.raycast(new RaycastContext(eye, eye.add(RotationUtils.forwardVector(Template.rotationManager().rotation()).multiply(5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.SOURCE_ONLY, mc.player));

        if (rotMode.is(rotationMode.Silent)) {
            // check hit result then rotate so server gets the last tick rotation (buckets are server sided)
            for (BlockPos blockPos : BlockPos.iterateOutwards(mc.player.getBlockPos().up(), 5, 5, 5)) {
                if (!liqPred.test(mc.world.getBlockState(blockPos).getBlock(), blockPos)) {
                    continue;
                }

                Box blockBox = new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
                Vec3d closest = MathUtils.closestPointToBox(eye, blockBox);
                if (eye.distanceTo(closest) > 5) {
                    continue;
                }

                BlockHitResult hitResultRaycast = mc.world.raycast(new RaycastContext(eye, blockBox.getCenter(), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.SOURCE_ONLY, mc.player));

                BlockPos raycastedBlockPos = hitResultRaycast.getBlockPos();
                if (hitResultRaycast.getType() == HitResult.Type.MISS || raycastedBlockPos.getManhattanDistance(blockPos) != 0) {
                    continue;
                }

                Template.rotationManager().setRotation(RotationUtils.correctSensitivity(RotationUtils.getRotations(eye, hitResultRaycast.getPos())));
                break;
            }
        }

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockState bs = mc.world.getBlockState(hitResult.getBlockPos());
        if (!liqPred.test(bs.getBlock(), hitResult.getBlockPos())) {
            return false;
        }

        switch (mode.getMode()) {
            case Switch -> {
                if (!mc.player.getMainHandStack().isOf(Items.BUCKET)) {
                    int slot = InvUtils.getItemSlot(Items.BUCKET);
                    if (slot == -1) {
                        return false;
                    } else {
                        if (prevSlot == -1) {
                            prevSlot = mc.player.getInventory().selectedSlot;
                        }
                        InvUtils.setInvSlot(slot);
                    }
                }
            }
            case Manual -> {
                if (!mc.player.getMainHandStack().isOf(Items.BUCKET)) {
                    return false;
                }
            }
        }

        return true;
    }
}