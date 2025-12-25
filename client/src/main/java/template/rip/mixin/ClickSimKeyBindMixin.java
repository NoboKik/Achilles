package template.rip.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(value = KeyBinding.class, priority = Integer.MAX_VALUE)
public class ClickSimKeyBindMixin {

    @Inject(at = @At("RETURN"), method = "setKeyPressed")
    private static void o(InputUtil.Key key, boolean bl, CallbackInfo ci) {
        MixinMethods.ClickSim.skb(key, bl);
    }

    @Inject(at = @At("HEAD"), method = "isPressed", cancellable = true)
    private void p(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.ClickSim.ikb(this, cir);
    }

    @Inject(at = @At("RETURN"), method = "onKeyPressed")
    private static void t(InputUtil.Key key, CallbackInfo ci) {
        MixinMethods.ClickSim.okb(key);
    }
}
