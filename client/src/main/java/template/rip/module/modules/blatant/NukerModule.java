package template.rip.module.modules.blatant;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.player.AutoToolModule;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;
import template.rip.module.setting.settings.RegistrySetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class NukerModule extends Module {

    public enum modeEnum {Legit, On, Off, Surroundings}
    public final NumberSetting reach = new NumberSetting(this, 4.5, 3, 6, 0.1, "Block reach");
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Legit: Breaks blocks like in normal minecraft\nOn: Breaks blocks through walls\nOff: Only breaks exposed blocks\nSurroundings: Breaks the neighboring block to the target block"), modeEnum.Legit, "Through Walls Mode");
    private final RegistrySetting<Block> targetBlocks = new RegistrySetting<>(Arrays.asList(Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.DRAGON_EGG, Blocks.CAKE), this, Registries.BLOCK, "Target Blocks");
    private BlockHitResult theHitResult;
    private static final ThreadPoolExecutor tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
    public static boolean isNuking;
    private BlockPos miningPos;
    private double blockDamage;

    public NukerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        theHitResult = null;
    }

    @EventHandler
    private void onMouse(HandleInputEvent.Post event) {
        if (mc.interactionManager == null || mc.player == null || mc.world == null)
            return;

        tpe.execute(() -> {
            List<BlockHitResult> bhrs = new ArrayList<>();

            int reachValue = (int) Math.ceil(reach.value);
            for (int x = -reachValue; x < reachValue; x++) {
                for (int y = -reachValue; y < reachValue; y++) {
                    for (int z = -reachValue; z < reachValue; z++) {
                        BlockPos pos = mc.player.getBlockPos().up().add(new BlockPos(x, y, z));
                        BlockState blockState = mc.world.getBlockState(pos);
                        Block block = blockState.getBlock();

                        boolean isValidBlock = targetBlocks.selected.contains(block);

                        if (!isValidBlock)
                            continue;

                        VoxelShape shape = blockState.getCollisionShape(mc.world, pos);

                        Box box = shape.getBoundingBox();

                        if (box == null)
                            continue;

                        box = box.offset(pos);

                        BlockHitResult hr = PlayerUtils.rayCast(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box.getCenter(), mc.player);
                        BlockHitResult thru = new BlockHitResult(box.getCenter(), PlayerUtils.rayCast(MathUtils.closestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box), box.getCenter(), mc.player).getSide(), pos, false);

                        if (MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box) > reach.value)
                            continue;

                        if (hr.getType() == HitResult.Type.MISS)
                            continue;

                        BlockHitResult blockHitResult = null;
                        // don't question the method tyvm
                        if (mode.is(modeEnum.Off) && hr.getBlockPos().getManhattanDistance(pos) == 0) {
                            blockHitResult = hr;
                        }
                        if (mode.is(modeEnum.Legit)) {
                            blockHitResult = hr;
                        }
                        if (mode.is(modeEnum.On)) {
                            blockHitResult = thru;
                        }
                        if (mode.is(modeEnum.Surroundings)) {
                            Box aboveBox = box.offset(0,1,0);
                            BlockPos abovePos = pos.offset(Direction.UP, 1);
                            blockHitResult = new BlockHitResult(aboveBox.getCenter(), PlayerUtils.rayCast(MathUtils.closestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), aboveBox), aboveBox.getCenter(), mc.player).getSide(), abovePos, false);
                            if (isNukable(thru)) blockHitResult = thru;

                            BlockState bs = mc.world.getBlockState(blockHitResult.getBlockPos());
                            int best = AutoToolModule.getbestSlot(bs);
                            if (best != mc.player.getInventory().selectedSlot && blockHitResult.getType() != HitResult.Type.MISS) {
                                mc.player.getInventory().selectedSlot = best;
                            }
                        }
                        if (blockHitResult != null) {
                            bhrs.add(blockHitResult);
                        }
                    }
                }
            }

            bhrs.sort(Comparator.comparing(bhr -> mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(bhr.getPos())));

            if (bhrs.isEmpty()) {
                theHitResult = null;
            } else {
                theHitResult = bhrs.get(0);
            }
        });

        if (theHitResult != null) {
            BlockHitResult bhrCopy = new BlockHitResult(theHitResult.getPos(), theHitResult.getSide(), theHitResult.getBlockPos(), theHitResult.isInsideBlock());
            Template.rotationManager().setRotation(RotationUtils.correctSensitivity(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), bhrCopy.getPos())));
            if (mode.is(modeEnum.Surroundings)) {
                mineBlock(bhrCopy.getBlockPos());
            } else {
                mc.interactionManager.updateBlockBreakingProgress(bhrCopy.getBlockPos(), bhrCopy.getSide());
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
        isNuking = theHitResult != null;
    }

    public float getPlayerRelativeBlockHardness(BlockState block, BlockPos pos) {
        float f = block.getHardness(mc.world, pos);
        return f < 0.0f ? 0.0f : (!mc.player.canHarvest(block) ? AutoToolModule.getBreakSpeed(block, mc.player.getInventory().selectedSlot) / f / 100.0f : AutoToolModule.getBreakSpeed(block, mc.player.getInventory().selectedSlot) / f / 30.0f);
    }

    public boolean isNukable(BlockHitResult bhr) {
        BlockPos pos;
        pos = bhr.getBlockPos().add(1,0,0);
        if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return true;

        pos = bhr.getBlockPos().add(-1,0,0);
        if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return true;

        pos = bhr.getBlockPos().add(0,1,0);
        if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return true;

        pos = bhr.getBlockPos().add(0,0,1);
        if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return true;

        pos = bhr.getBlockPos().add(0,0,-1);
        return mc.world.getBlockState(pos).getBlock() instanceof AirBlock;
    }

    private void mineBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (block instanceof AirBlock && this.miningPos != null) {
            this.stopMining(this.miningPos);
            return;
        }
        if (this.miningPos == null || !this.miningPos.equals(pos)) {
            this.blockDamage = 0.0;
        }
        this.blockDamage += getPlayerRelativeBlockHardness(mc.world.getBlockState(pos), pos);
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        if (this.miningPos == null || !this.miningPos.equals(pos)) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
            if (this.blockDamage >= 1.0) {
                mc.world.removeBlock(pos, false);
            }
        } else if (this.blockDamage >= 1.0) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
            mc.world.removeBlock(pos, false);
        }
        this.miningPos = pos;
    }

    private void stopMining(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP));
        this.miningPos = null;
    }
}
