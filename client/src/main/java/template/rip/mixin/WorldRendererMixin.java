package template.rip.mixin;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;
import template.rip.Template;
import template.rip.api.blockesp.WorldRenderContext;
import template.rip.api.event.events.WorldRenderEvent;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Unique
    WorldRenderContext context = new WorldRenderContext();
    @Final @Shadow private BufferBuilderStorage bufferBuilders;
    @Shadow private ClientWorld world;

    @Inject(at = @At(value = "HEAD"), method = "renderEntity", cancellable = true)
    private void a(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        MixinMethods.wr(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, ci);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void b(ObjectAllocator objectAllocator, RenderTickCounter renderTickCounter, boolean bl, Camera camera, GameRenderer gameRenderer, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        context = new WorldRenderContext();
        context.prepare((WorldRenderer) (Object) this, renderTickCounter.getTickDelta(false), bl, camera, gameRenderer, bufferBuilders.getEntityVertexConsumers(), world, matrix4f, matrix4f2);
    }

    @Inject(method = "setupTerrain", at = @At("RETURN"))
    private void c(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        context.setFrustum(frustum);
    }

    @Inject(at = @At(value = "RETURN"), method = "render")
    private void e(ObjectAllocator objectAllocator, RenderTickCounter renderTickCounter, boolean bl, Camera camera, GameRenderer gameRenderer, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        Template.EVENTBUS.post(new WorldRenderEvent(context));
    }
}
