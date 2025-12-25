package template.rip.api.event.events;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import template.rip.api.event.Cancellable;

public class BlockCollisionEvent extends Cancellable {

    public BlockPos pos;
    public Entity ent;
    public BlockState blockState;

    public BlockCollisionEvent(BlockState blockState, BlockPos pos, Entity ent) {
        this.blockState = blockState;
        this.pos = pos;
        this.ent = ent;
    }
}
