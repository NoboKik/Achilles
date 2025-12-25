package template.rip.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract BlockPos getLandingPos();

    @Shadow
    public abstract boolean isOnGround();

    @Shadow
    public abstract World getWorld();

    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    // Fuck you mojang
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isRemoved()Z"))
    public void a(MovementType type, Vec3d movement, CallbackInfo ci) {
        BlockPos blockPos = getLandingPos();
        BlockState blockState = getWorld().getBlockState(blockPos);
        fall(movement.y, isOnGround(), blockState, blockPos);
    }

    @Inject(method = "updateVelocity", at = @At(value = "HEAD"), cancellable = true)
    public void b(float speed, Vec3d movementInput, CallbackInfo ci) {
        MixinMethods.e2(this, ci, speed, movementInput);
    }

    @Inject(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"), cancellable = true)
    public void c(Entity entity, CallbackInfo ci) {
        MixinMethods.e3(this, entity, ci);
    }

    @Inject(method = "getRotationVec", at = @At("HEAD"), cancellable = true)
    public void d(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        MixinMethods.e5(this, cir);
    }

    @Inject(method = "isInvisibleTo", at = @At("RETURN"), cancellable = true)
    private void e(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.e6(player, this, cir);
    }

    @Inject(method = "shouldRender*", at = @At("RETURN"), cancellable = true)
    private void f(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.e7(this, cir);
    }
}
