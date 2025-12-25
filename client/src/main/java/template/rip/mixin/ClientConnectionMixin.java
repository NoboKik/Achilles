package template.rip.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.MixinMethods;

@Mixin(value = ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Shadow @Final private NetworkSide side;

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void a(Packet<T> packet, PacketListener listener, CallbackInfo callback) {
        MixinMethods.cc1(packet, listener, callback);
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void b(Packet<?> packet, PacketCallbacks packetCallback, CallbackInfo callback) {
        MixinMethods.cc2(packet, side, callback);
    }

    @Inject(method = "exceptionCaught", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"))
    private void c(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        MixinMethods.cc3(ex);
    }
}