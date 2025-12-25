package template.rip.api.object;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FakeCrystalEntity extends EndCrystalEntity {

    public FakeCrystalEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public void tick() {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    public boolean canHit() {
        return false;
    }

    /*@Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }*/

    @Override
    public void kill(ServerWorld world) {
        super.kill(world);
    }

    @Override
    public void setBeamTarget(@Nullable BlockPos beamTarget) {}

    @Nullable
    public BlockPos getBeamTarget() {
        return null;
    }

    @Override
    public void setShowBottom(boolean showBottom) {}

    @Override
    public boolean shouldShowBottom() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return false;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return null;
    }
}
