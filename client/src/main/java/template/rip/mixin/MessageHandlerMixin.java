package template.rip.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

import java.time.Instant;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {

    @Inject(method = "processChatMessageInternal", at = @At(value = "HEAD"), cancellable = true)
    public void a(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.mg(params, message, decorated, sender, receptionTimestamp, cir);
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void b(Text message, boolean overlay, CallbackInfo ci) {
        MixinMethods.cpn3(message, overlay, ci);
    }
}
