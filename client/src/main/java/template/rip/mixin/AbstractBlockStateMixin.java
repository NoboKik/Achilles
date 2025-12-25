package template.rip.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    
    @Shadow
    protected abstract BlockState asBlockState();

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void a(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        MixinMethods.abs(this.asBlockState(), pos, entity, ci);
    }
}
