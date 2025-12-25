package template.rip.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.consume.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;
import template.rip.Template;
import template.rip.module.modules.blatant.MultiTaskModule;

import static template.rip.Template.mc;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void a(CallbackInfo ci) {
        MixinMethods.mc1(ci);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void b(CallbackInfo ci) {
        MixinMethods.mc2();
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void c(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.mc3(cir);
    }

    @Inject(method = "doAttack", at = @At("TAIL"), cancellable = true)
    private void d(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.mc4(cir);
    }

    @Inject(method = "doAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;attackCooldown:I"), cancellable = true)
    private void e(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.mc20(cir);
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void f(CallbackInfo ci) {
        MixinMethods.mc5(ci);
    }

    @Inject(method = "doItemUse", at = @At("TAIL"), cancellable = true)
    private void g(CallbackInfo ci) {
        MixinMethods.mc6(ci);
    }

    @Inject(method = "doItemUse", at = @At(value = "RETURN"), cancellable = true)
    private void h(CallbackInfo ci) {
        MixinMethods.mc7(ci);
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"), cancellable = true)
    private void i(CallbackInfo ci) {
        MixinMethods.mc21(ci);
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void j(boolean breaking, CallbackInfo ci) {
        MixinMethods.mc8(ci);
    }

    @Inject(method = "handleBlockBreaking", at = @At("TAIL"), cancellable = true)
    private void k(boolean breaking, CallbackInfo ci) {
        MixinMethods.mc9(ci);
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void l(CallbackInfo ci) {
        MixinMethods.mc10(ci);
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void m(Screen theScreen, CallbackInfo ci) {
       MixinMethods.mc11(theScreen, ci);
    }

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void n(CallbackInfo ci) {
        MixinMethods.mc12();
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void o(CallbackInfo ci) {
        MixinMethods.mc13();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void p(CallbackInfo ci) {
        MixinMethods.mc14();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void q(CallbackInfo ci) {
        MixinMethods.mc17();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;tick()V", shift = At.Shift.AFTER))
    private void r(CallbackInfo ci) {
        MixinMethods.mc18();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;updateCrosshairTarget(F)V", shift = At.Shift.AFTER))
    private void s(CallbackInfo ci) {
        MixinMethods.gr3(1f);
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 2))
    private boolean t(KeyBinding instance) {
        MultiTaskModule mtm = Template.moduleManager.getModule(MultiTaskModule.class);
        if (mtm != null && mtm.isEnabled() && mtm.item.isEnabled() && mc.player != null && mc.player.getItemUseTimeLeft() >= 0 && mc.player.getActiveItem().getUseAction() == UseAction.EAT) {
            return true;
        }
        return mc.options.useKey.isPressed();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void u(RunArgs args, CallbackInfo ci) {
        MixinMethods.mc15();
    }

    @Inject(method = "hasOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isGlowing()Z"), cancellable = true)
    public void glowEffect(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.mc16(entity, cir);
    }
}
