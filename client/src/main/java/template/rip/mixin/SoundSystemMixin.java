package template.rip.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "playNextTick", at = @At("HEAD"), cancellable = true)
    public void a(TickableSoundInstance sound, CallbackInfo ci) {
        MixinMethods.ss1(ci);
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    public void b(SoundInstance sound, int delay, CallbackInfo ci) {
        MixinMethods.ss2(sound, ci);
    }
}
