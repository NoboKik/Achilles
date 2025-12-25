package template.rip.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(at = @At("HEAD"), method = "attackEntity")
    private void b(PlayerEntity player, Entity target, CallbackInfo ci) {
        MixinMethods.cpi2(target);
    }

    @Inject(at = @At("TAIL"), method = "attackEntity")
    private void c(PlayerEntity player, Entity target, CallbackInfo ci) {
        MixinMethods.cpi3(target);
    }

    @Inject(method = "interactBlockInternal", at = @At(value = "HEAD"))
    private void d(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        MixinMethods.cpi4(player, hand, hitResult);
    }

    @Inject(method = "interactBlockInternal", at = @At(value = "RETURN"))
    private void e(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        MixinMethods.cpi5(player, hand, hitResult, cir);
    }

    @Inject(method = "interactItem", at = @At(value = "HEAD"))
    private void f(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        MixinMethods.cpi6();
    }

    @Inject(method = "interactItem", at = @At(value = "RETURN"))
    private void g(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        MixinMethods.cpi7();
    }

    @Inject(at = @At("HEAD"), method = "clickSlot")
    private void h(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
//        mc.inGameHud.getChatHud().addMessage(Text.of(slotId + " " + button + " " + actionType.name()));
    }
}