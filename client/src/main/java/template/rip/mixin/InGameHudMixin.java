package template.rip.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void a(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo ci) {
        MixinMethods.ig1(drawContext, renderTickCounter.getTickDelta(false));
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void b(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        MixinMethods.ig2(texture, ci);
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void c(DrawContext drawContext, ScoreboardObjective scoreboardObjective, CallbackInfo ci) {
        MixinMethods.ig3(drawContext, scoreboardObjective, ci);
    }
}
