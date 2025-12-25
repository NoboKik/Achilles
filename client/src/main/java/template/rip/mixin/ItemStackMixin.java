package template.rip.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getBobbingAnimationTime", at = @At("HEAD"), cancellable = true)
    private void a(CallbackInfoReturnable<Integer> cir) {
        MixinMethods.is(this, cir);
    }
}
