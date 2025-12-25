package template.rip.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    public void c(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MixinMethods.gr4(ci);
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    public void d(ItemStack floatingItem, CallbackInfo ci) {
        MixinMethods.gr5(floatingItem, ci);
    }
}
