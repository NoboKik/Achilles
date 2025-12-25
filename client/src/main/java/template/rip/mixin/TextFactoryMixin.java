package template.rip.mixin;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.MixinMethods;

@Mixin(TextVisitFactory.class)
public class TextFactoryMixin {

    @Inject(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void a(String text, int startIndex, Style style, CharacterVisitor visitor, CallbackInfoReturnable<Boolean> cir) {
        MixinMethods.tf(text, startIndex, style, visitor, cir);
    }
}
