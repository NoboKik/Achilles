package template.rip.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;
import template.rip.Template;
import template.rip.module.modules.render.NoRenderModule;

import static template.rip.Template.mc;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "jump", at = @At("HEAD"))
    public void a(CallbackInfo ci) {
        MixinMethods.le1(this);
    }

    @Inject(method = "jump", at = @At("TAIL"))
    public void b(CallbackInfo ci) {
        MixinMethods.le2(this);
    }

    @Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
    public void c(CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.le3(this, cir);
    }

    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    public void d(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == mc.player) {
            MixinMethods.le4(cir);
        }
    }

    @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
    public void e(RegistryEntry<StatusEffect> registryEntry, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if ((Object) this == mc.player) {
            NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
            if (noRender != null && noRender.isEnabled()) {
                if (noRender.blindness.isEnabled() && registryEntry.value() == StatusEffects.BLINDNESS.value()) {
                    cir.setReturnValue(null);
                }
                if (noRender.nausea.isEnabled() && registryEntry.value() == StatusEffects.NAUSEA.value()) {
                    cir.setReturnValue(null);
                }
            }
        }
    }

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    public void f(RegistryEntry<StatusEffect> registryEntry, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == mc.player) {
            NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
            if (noRender != null && noRender.isEnabled()) {
                if (noRender.blindness.isEnabled() && registryEntry.value() == StatusEffects.BLINDNESS.value()) {
                    cir.setReturnValue(false);
                }
                if (noRender.nausea.isEnabled() && registryEntry.value() == StatusEffects.NAUSEA.value()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "damage", at = @At(value = "HEAD"))
    public void g(ServerWorld serverWorld, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
//        mc.inGameHud.getChatHud().addMessage(Text.of(this.getName().getString() +" "+ source.getName() + " " + amount));
    }

    @Inject(method = "onDamaged", at = @At("HEAD"), cancellable = true)
    private void h(DamageSource damageSource, CallbackInfo ci) {
        if (MixinMethods.cpn13(((LivingEntity) (Object) this), damageSource)) {
            ci.cancel();
        }
    }
}

