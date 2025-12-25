package template.rip.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.Template;
import template.rip.api.event.events.KeyBindingEvent;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow public int timesPressed;
    @Shadow public boolean pressed;

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void a(CallbackInfoReturnable<Boolean> cir) {
        KeyBindingEvent keybindEvent = new KeyBindingEvent(((KeyBinding) (Object) this), this.pressed);
        Template.EVENTBUS.post(keybindEvent);

        if (keybindEvent.isChanged()) {
            if (keybindEvent.isPressed()) this.timesPressed++;
            cir.setReturnValue(keybindEvent.isPressed());
        }
    }
}
