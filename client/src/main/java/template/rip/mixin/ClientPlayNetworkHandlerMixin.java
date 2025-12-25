package template.rip.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import template.rip.MixinMethods;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocityClient(DDD)V"), cancellable = true)
    public void a(EntityVelocityUpdateS2CPacket p, CallbackInfo ci) {
        MixinMethods.cpn1(p, ci);
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At(value = "TAIL"))
    public void b(EntityVelocityUpdateS2CPacket p, CallbackInfo ci) {
        MixinMethods.cpn2(p);
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void d(GameJoinS2CPacket packet, CallbackInfo ci) {
        MixinMethods.cpn4();
    }

    @Inject(method = "onPlayerPositionLook", at = @At(value = "HEAD"))
    private void e(CallbackInfo info) {
        MixinMethods.cpn5();
    }

    @Inject(method = "onTitle", at = @At(value = "HEAD"), cancellable = true)
    private void f(TitleS2CPacket packet, CallbackInfo info) {
        MixinMethods.cpn6(packet, info);
    }

    @Inject(method = "onSubtitle", at = @At(value = "HEAD"), cancellable = true)
    private void g(SubtitleS2CPacket packet, CallbackInfo info) {
        MixinMethods.cpn7(packet, info);
    }

    @Inject(method = "onPlayerPositionLook", at = @At(value = "TAIL"))
    private void h(CallbackInfo info) {
        MixinMethods.cpn8();
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void i(ChunkDataS2CPacket packet, CallbackInfo info) {
        MixinMethods.cpn9(packet);
    }

    /*@Inject(method = "sendChatMessage", at = @At("TAIL"))
    private void onChat(String content, CallbackInfo ci) {
        if (AchillesMenu.isClientEnabled()) {
            Pattern pattern = Pattern.compile("\\bu[wv]u", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    AudioInputStream stream = AudioSystem.getAudioInputStream(SoundUtils.class.getClassLoader().getResourceAsStream("haveyou.wav"));
                    Clip clip = AudioSystem.getClip();
                    clip.open(stream);
                    clip.start();
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException | NullPointerException ignored) {
                }
            }
        }
    }*/

    /*@Inject(method = "sendChatCommand", at = @At("TAIL"))
    private void onCommand(String content, CallbackInfo ci) {
        if (AchillesMenu.isClientEnabled()) {
            Pattern pattern = Pattern.compile("\\bu[wv]u", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    AudioInputStream stream = AudioSystem.getAudioInputStream(SoundUtils.class.getClassLoader().getResourceAsStream("haveyou.wav"));
                    Clip clip = AudioSystem.getClip();
                    clip.open(stream);
                    clip.start();
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException | NullPointerException ignored) {
                }
            }
        }
    }*/

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void k(PlayerPositionLookS2CPacket packet, CallbackInfo ci, @Local PlayerEntity playerEntity) {
        MixinMethods.cpn10(packet, ci, playerEntity);
    }

    @Inject(method = "onLookAt", at = @At("HEAD"), cancellable = true)
    private void l(LookAtS2CPacket packet, CallbackInfo ci) {
        MixinMethods.cpn11(ci);
    }
}
