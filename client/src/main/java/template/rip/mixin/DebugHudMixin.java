package template.rip.mixin;

import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import template.rip.MixinMethods;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Redirect(method = "getLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getX()D"))
    public double a(Entity instance) {
        return instance.getX() + MixinMethods.xoffset;
    }

    @Redirect(method = "getLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getY()D"))
    public double b(Entity instance) {
        return instance.getY() + MixinMethods.yoffset;
    }

    @Redirect(method = "getLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getZ()D"))
    public double c(Entity instance) {
        return instance.getZ() + MixinMethods.zoffset;
    }
}
