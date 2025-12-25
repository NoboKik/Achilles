package template.rip.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;
import template.rip.api.util.EntityRenderStateAddition;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void a(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MixinMethods.er(((EntityRenderStateAddition) state).achilles$getEntity(), ((EntityRenderStateAddition) state).achilles$getEntity().getYaw(), 0, matrices, vertexConsumers, light, ci);
    }
    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void hookInjectEntityIntoState(T entity, S state, float tickDelta, CallbackInfo ci) {
        ((EntityRenderStateAddition) state).achilles$setEntity(entity);
    }
}
