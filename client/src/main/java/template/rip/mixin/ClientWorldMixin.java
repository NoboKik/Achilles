package template.rip.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Shadow @Final private ClientWorld.Properties clientWorldProperties;

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void a(int entityId, Entity.RemovalReason removalReason, CallbackInfo info) {
        MixinMethods.cw2(entityId);
    }

    @Inject(method = "setTime", at = @At("HEAD"), cancellable = true)
    private void b(long time, long timeOfDay, boolean shouldTickTimeOfDay, CallbackInfo ci) {
        MixinMethods.cw4(ci, clientWorldProperties);
    }
}