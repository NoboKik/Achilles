package template.rip.sodium;

import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static template.rip.MixinMethods.*;

/*
  to lagoon who will probably change this to the class,
  I don't want to add sodium extra, sodium and whatever else zkm will cry about to my libraries
  so please just leave it as is by targeting the class via string
  - sooty
 */
@Mixin(targets = "me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraHud")
public class SEH {
    @ModifyVariable(method = "onStartTick", at = @At(value = "STORE"))
    private Vec3d onPlayerPos(Vec3d pos) {
        return pos.add(xoffset, yoffset, zoffset);
    }
}
