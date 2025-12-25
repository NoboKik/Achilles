package template.rip.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.Template;
import template.rip.module.modules.legit.FullBrightModule;

@Mixin(LightmapTextureManager.class)
public class LightMapMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"), method = "getBrightness(FI)F", cancellable = true)
    private static void light(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        float f = (float)lightLevel / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        cir.setReturnValue(Template.moduleManager.isModuleEnabled(FullBrightModule.class) ? 16f : MathHelper.lerp(ambientLight, g, 1.0f));
    }
}
