package template.rip.api.object;

import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class Path {

    public ArrayList<Pair<Vec3d, Vec3d>> lines;
    public ArrayList<Vec3d> corners;
    public BlockPos currentPos;
    public BlockPos target;
    public int cost;

    public Path(BlockPos currentPos, BlockPos target, ArrayList<Pair<Vec3d, Vec3d>> lines, ArrayList<Vec3d> corners) {
        this.lines = lines;
        this.corners = corners;
        this.currentPos = currentPos;
        this.target = target;
        this.cost = 0;
    }

    public boolean reached() {
        return currentPos.getManhattanDistance(target) == 0;
    }

    public int distance() {
        return currentPos.getManhattanDistance(target);
    }

    public Path withCost(int cost) {
        this.cost = cost;
        return this;
    }
}
