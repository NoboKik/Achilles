package template.rip.mixin;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(InputUtil.class)
public class InputUtilMixin {

    @Inject(at = @At(value = "HEAD"), method = "setKeyboardCallbacks", cancellable = true)
    private static void a(long handle, GLFWKeyCallbackI keyCallback, GLFWCharModsCallbackI charModsCallback, CallbackInfo ci) {
        MixinMethods.it(handle, keyCallback, charModsCallback, ci);
    }
}
