package template.rip.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.gui.ImguiLoader;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(at = @At("HEAD"), method = "flipFrame", remap = false)
    private static void a(CallbackInfo ci) {
        Profilers.get().push("ImGui Render");
        ImguiLoader.onFrameRender();
        Profilers.get().pop();
    }
}
