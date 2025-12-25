package template.rip.mixin;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void a(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        MixinMethods.k1(window, key, scancode, action);
    }

    @Inject(method = "onChar", at = @At("HEAD"))
    private void b(long window, int codePoint, int modifiers, CallbackInfo ci) {
        MixinMethods.k2(window, codePoint, modifiers);
    }
}
