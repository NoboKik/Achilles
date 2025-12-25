package template.rip.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;
import template.rip.Template;
import template.rip.module.modules.player.SprintModule;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), cancellable = true)
    public void a(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.cpe1(cir, this);
    }

    @Inject(method = "isWalking", at = @At(value = "RETURN"), cancellable = true)
    public void b(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.cpe2(cir, this);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/network/ClientPlayerEntity;canStartSprinting()Z"))
    public void c(CallbackInfo ci) {
        MixinMethods.cpe3(this);
    }

    @Inject(at = @At("HEAD"), method = "tickMovement", cancellable = true)
    private void a(CallbackInfo ci) {
        MixinMethods.mc19(this, ci);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"), cancellable = true)
    public void d(CallbackInfo ci) {
        MixinMethods.cpe4(this, ci);
        MixinMethods.cpe7(this);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 1))
    public boolean j(KeyBinding instance) {
        return (instance.isPressed() || Template.moduleManager.isModuleEnabled(SprintModule.class)) && !SprintModule.isStopSprint();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.AFTER))
    public void e(CallbackInfo ci) {
        MixinMethods.cpe5(this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void f(CallbackInfo ci) {
        MixinMethods.cpe6(this);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void h(CallbackInfo info) {
        MixinMethods.cpe8(this);
    }

    @Override
    protected boolean clipAtLedge() {
        if (MixinMethods.cpe9(this))
            return true;
        return super.clipAtLedge();
    }

    @Override
    public boolean isBlocking() {
        if (MixinMethods.cpe10(this))
            return true;
        return super.isBlocking();
    }
}