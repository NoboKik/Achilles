package template.rip.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void a(CallbackInfo callbackInfo) {
        MixinMethods.pe1();
    }

    @Inject(method = "getDamageTiltYaw", at = @At(value = "TAIL"), cancellable = true)
    public void b(CallbackInfoReturnable<Float> cir) {
        MixinMethods.pe2(cir);
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    public void c(CallbackInfoReturnable<Double> cir) {
        MixinMethods.pe4(cir);
    }

    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    public void d(CallbackInfoReturnable<Double> cir) {
        MixinMethods.pe3(cir);
    }
}
