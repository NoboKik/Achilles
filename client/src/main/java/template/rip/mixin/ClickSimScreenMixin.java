package template.rip.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(value = Screen.class, priority = Integer.MAX_VALUE)
public class ClickSimScreenMixin {

    @Inject(method = "addCrashReportSection", cancellable = true, at = @At("HEAD"))
    private void c(CrashReport report, CallbackInfo ci) {
        MixinMethods.ClickSim.osp("", ci);
    }
}
