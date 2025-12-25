package template.rip.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    public void a(CallbackInfo ci) {
        Pair<Double, Double> pr = MixinMethods.m2(cursorDeltaX, cursorDeltaY);
        cursorDeltaY = pr.getRight();
        cursorDeltaX = pr.getLeft();
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void b(CallbackInfo ci) {
        MixinMethods.m3(ci);
    }

    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void c(CallbackInfo ci) {
        MixinMethods.m4();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void d(long window, double horizontal, double vertical, CallbackInfo ci) {
        MixinMethods.m5(vertical, ci);
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void e(long window, int button, int action, int mods, CallbackInfo ci) {
        MixinMethods.m6(button, action, ci);
    }

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void f(CallbackInfo ci) {
        MixinMethods.m7(ci);
    }
}
