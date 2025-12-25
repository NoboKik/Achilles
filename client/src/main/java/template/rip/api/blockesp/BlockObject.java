package template.rip.api.blockesp;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class BlockObject {
    public Color color;
    public BlockPos pos;
    public BlockState state;
    public Block block;
    public int x, y, z, red, green, blue;

    public BlockObject(BlockPos pos) {
        this.pos = new BlockPos((x = pos.getX()), (y = pos.getY()), (z = pos.getZ()));
        refresh();
    }

    public void refresh() {
        if (MinecraftClient.getInstance().world != null) {
            block = (state = MinecraftClient.getInstance().world.getBlockState(pos)).getBlock();
        }
        refreshColor();
    }

    public void refreshColor() {
        if (BlockUtilities.coal().contains(block)) {
            color = Color.BLACK;
        } else if (BlockUtilities.iron().contains(block)) {
            color = Color.WHITE;
        } else if (BlockUtilities.redstone().contains(block)) {
            color = Color.RED;
        } else if (BlockUtilities.lapis().contains(block)) {
            color = Color.BLUE;
        } else if (BlockUtilities.gold().contains(block)) {
            color = Color.YELLOW;
        } else if (BlockUtilities.emerald().contains(block)) {
            color = Color.GREEN;
        } else if (BlockUtilities.diamond().contains(block)) {
            color = Color.CYAN;
        } else if (BlockUtilities.netherite().contains(block)) {
            color = new Color(1, 50, 32);
        } else if (BlockUtilities.head().contains(block)) {
            color = Color.GRAY;
        } else if (block.equals(Blocks.ENDER_CHEST)) {
            color = new Color(115, 0, 255);
        } else if (block.equals(Blocks.CHEST)) {
            color = new Color(255, 123, 0);
        } else if (block.equals(Blocks.BARREL)) {
            color = new Color(255, 123, 0);
        } else if (BlockUtilities.shulker().contains(block)) {
            color = new Color(255, 0, 234);
        } else if (block.equals(Blocks.TRAPPED_CHEST)) {
            color = new Color(255, 0, 0);
        } else if (block.equals(Blocks.FURNACE)) {
            color = new Color(85, 85, 85);
        } else if (block.equals(Blocks.DISPENSER)) {
            color = new Color(85, 85, 85);
        } else if (block.equals(Blocks.DROPPER)) {
            color = new Color(85, 85, 85);
        } else if (block.equals(Blocks.HOPPER)) {
            color = new Color(85, 85, 85);
        } else if (block.equals(Blocks.SPAWNER)) {
            color = new Color(255, 255, 0);
        } else {
            color = BlockUtilities.fallBackColor;
        }

        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlockObject e) {
            return e.hashCode() == this.hashCode();
        }
        return false;
    }
}
