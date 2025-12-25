package template.rip.deprecated;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class AutoDrainRewriteModule extends Module {
    public AutoDrainRewriteModule() {
        super(Category.COMBAT, Description.of("drain"), "AutoDrainRewrite");
    }

    public enum modeEnum{Switch, Manual}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Switch, "Bucket Mode");
    public final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch Back");
    public final NumberSetting rangeSetting = new NumberSetting(this, 6, 1, 6, 0.1, "Range");
    public final NumberSetting maximumWaters = new NumberSetting(this, 5, 1, 20, 1, "Max Waters");
    public final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 4, 6, 0, 10, 1, "Switch Delay");
    public final MinMaxNumberSetting stealDelay = new MinMaxNumberSetting(this, 2, 3, 0, 10, 1, "Steal Delay");
    public enum liqEnum{Lava, Water, WaterOnWeb, LavaOnWeb, All}
    public final ModeSetting<liqEnum> liqModes = new ModeSetting<>(this, liqEnum.WaterOnWeb, "Liquid Modes");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();


    int stealTimer, switchTimer, prevSlot;
    boolean did;
    int collectedWaters = 0;
    List<BlockPos> waterSources = new ArrayList<>();

    @Override
    public void onEnable() {
        stealTimer = stealDelay.getRandomInt();
        switchTimer = switchDelay.getRandomInt();
        did = false;
        prevSlot = -1;
        collectedWaters = 0;
    }

    @Override
    public void onDisable() {
        if (switchBack.isEnabled() && prevSlot != -1) {
            InvUtils.setInvSlot(prevSlot);
        }
        collectedWaters = 0;
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck() || (disableInScreens.isEnabled()) && mc.currentScreen != null) {
            return;
        }

        waterSources.clear();
        waterSources = findWater(mc.player);
        if (waterSources.isEmpty()) return;

        for (BlockPos water : waterSources) {
            if (collectedWaters >= maximumWaters.getIValue()) {
                waterSources.clear();
                break;
            }

            pickUpWater(water);
            waterSources.remove(water);
        }
    }

    private boolean checkWeb(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if (mc.world.getBlockState(offset).isOf(Blocks.COBWEB)) {
                return true;
            }
        }
        return false;
    }

    private void pickUpWater(BlockPos blockPos) {
        if (mc.interactionManager == null) return;

        if (!mc.player.getMainHandStack().isOf(Items.BUCKET)) {
            int bucketSlot = InvUtils.getItemSlot(Items.BUCKET);
            if (bucketSlot < 0 || bucketSlot > 8) return;
            InvUtils.setInvSlot(bucketSlot);
        }

        if (Template.isClickSim()) {
            Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());
        }

        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(blockPos.toCenterPos(), Direction.DOWN, blockPos, false));
        if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        ++collectedWaters;
    }

    private List<BlockPos> findWater(PlayerEntity player) {
        if (!nullCheck()) {
            return waterSources;
        }
        // Get the player's position and the range to check
        int range = rangeSetting.getIValue();

        for (BlockPos posToCheck : BlockPos.iterateOutwards(player.getBlockPos(), range, range, range)) {
            // Check if the block at that position is a water source block
            if (!mc.world.getBlockState(posToCheck).isOf(Blocks.WATER)) {
                continue;
            }
            // If we are to only check for water on web, check if the block below the water we found is a web
            if (liqModes.getMode().equals(liqEnum.WaterOnWeb)) {
                if (checkWeb(posToCheck.offset(Direction.DOWN))) {
                    waterSources.add(posToCheck);
                }
            } else {
                // Add the position to the list if it's a water source block
                waterSources.add(posToCheck);
            }
        }
        return waterSources;
    }

}
