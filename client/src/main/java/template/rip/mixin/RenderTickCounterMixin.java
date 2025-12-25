package template.rip.mixin;

import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.Template;
import template.rip.api.event.events.RenderTickEvent;

@Mixin(RenderTickCounter.Dynamic.class)
public class RenderTickCounterMixin {

    @Mutable
    @Final
    @Shadow
    private float tickTime;

    @Inject(at = @At(value = "HEAD"), method = "beginRenderTick(J)I")
    private void a(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        tickTime = 50;
        RenderTickEvent rte = new RenderTickEvent(20);
        Template.EVENTBUS.post(rte);
        tickTime = 1000f / rte.TPS;
    }
}