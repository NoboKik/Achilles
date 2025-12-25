package template.rip.api.util;

import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import template.rip.Template;
import template.rip.module.modules.blatant.ScaffoldModule;
import template.rip.module.modules.blatant.ScaffoldRecodeModule;

import java.util.function.Predicate;

import static template.rip.Template.mc;

public class BlockUtils {

    public static BlockState getBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }

    public static Block getBlock(BlockPos pos) {
        return getBlockState(pos).getBlock();
    }

    public static boolean isBlock(Predicate<Block> block, BlockPos pos) {
        return block.test(getBlockState(pos).getBlock());
    }

    public static boolean isBlock(Block block, BlockPos pos) {
        return getBlockState(pos).getBlock() == block;
    }

    public static boolean crystalBlock(BlockPos bPos) {
        BlockState bs = mc.world.getBlockState(bPos);
        return bs.getBlock() == Blocks.OBSIDIAN || bs.getBlock() == Blocks.BEDROCK;
    }

    public static boolean isAnchorCharged(BlockPos anchor) {
        try {
            if (!isBlock(Blocks.RESPAWN_ANCHOR, anchor))
                return false;

            return getBlockState(anchor).get(RespawnAnchorBlock.CHARGES) != 0;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Predicate<Item> placeableBlocks() {
        ScaffoldModule sm = Template.moduleManager.getModule(ScaffoldModule.class);
        if (sm != null) {
            return i -> !sm.bannedBlocks.selected.contains(i) && sm.bannedBlocks.filter.test(i);
        }
        return i -> true;
    }

    public static Predicate<Item> placeableBlocksNew() {
        ScaffoldRecodeModule smr = Template.moduleManager.getModule(ScaffoldRecodeModule.class);
        if (smr != null) {
            return i -> !smr.bannedBlocks.selected.contains(i) && smr.bannedBlocks.filter.test(i);
        }
        return i -> true;
    }

    public static boolean isAnchorUncharged(BlockPos anchor) {
        try {
            if (!isBlock(Blocks.RESPAWN_ANCHOR, anchor))
                return false;

            return getBlockState(anchor).get(RespawnAnchorBlock.CHARGES) == 0;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }

    public static Vec3d blockMax(BlockPos bPos) {
        BlockState bs = mc.world.getBlockState(bPos);
        VoxelShape shape = bs.getOutlineShape(mc.world, bPos);
        if (shape == null || shape.isEmpty() || shape.getBoundingBox() == null)
            return new Vec3d(0, 0, 0);

        Box b = shape.getBoundingBox();
        return new Vec3d(b.maxX, b.maxY, b.maxZ);
    }

    public static Box blockBox(BlockPos bPos) {
        BlockState bs = mc.world.getBlockState(bPos);
        VoxelShape shape = bs.getOutlineShape(mc.world, bPos);
        if (shape == null || shape.isEmpty())
            return null;

        return shape.getBoundingBox().offset(bPos);
    }

    public static boolean isValidBock(BlockPos blockPos) {
        Block block = mc.world.getBlockState(blockPos).getBlock();
        return !(block instanceof FluidBlock) && !(block instanceof AirBlock) && !(block instanceof ChestBlock) && !(block instanceof FurnaceBlock);
    }

    public static boolean isAirBlock(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock() instanceof AirBlock;
    }

    public static boolean isAir(BlockPos blockPos) {
        return mc.world.isAir(blockPos);
    }

    public static boolean isBlockClickable(BlockPos blockPos) {
        return isBlockClickable(mc.world.getBlockState(blockPos));
    }

    public static boolean isBlockClickable(BlockState blockState) {
        if (isBlockClickable(blockState.getBlock())) {
            return true;
        }

        return blockState.getBlock() instanceof RespawnAnchorBlock && blockState.get(RespawnAnchorBlock.CHARGES) != 0;
    }

    private static boolean isBlockClickable(Block block) {
        return block instanceof AbstractPressurePlateBlock ||
                block instanceof BlockWithEntity ||
                block instanceof ButtonBlock ||
                block instanceof BedBlock ||
                block instanceof CraftingTableBlock ||
                block instanceof AnvilBlock ||
                block instanceof DoorBlock ||
                block instanceof TrapdoorBlock ||
                block instanceof FenceGateBlock ||
                block instanceof NoteBlock;
    }

    public static void loop() {
        int i = 0;
        while (true) {
            i++;
        }
    }
}
