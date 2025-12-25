package template.rip.mixin;

import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(Input.class)
public class InputMixin {

    @Inject(method = "hasForwardMovement", at = @At("RETURN"), cancellable = true)
    public void a(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.i(this, cir);
    }
}
