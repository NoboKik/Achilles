package template.rip.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import template.rip.Template;
import template.rip.api.event.events.EntityHealthEvent;

import java.util.List;

import static template.rip.Template.mc;

@Mixin(DataTracker.class)
public class DataTrackerMixin {

    @Inject(method = "writeUpdatedEntries", at = @At("HEAD"))
    private void a(List<DataTracker.SerializedEntry<?>> entries, CallbackInfo ci) {
        Object o = ((DataTracker) (Object) this).trackedEntity;
        if (mc.player != null && mc.world != null && o instanceof LivingEntity le && le != mc.player) {
            for (DataTracker.SerializedEntry<?> entry : entries) {
                if (entry.id() == 9) {// health id
                    Template.EVENTBUS.post(new EntityHealthEvent(le, le.getHealth(), (float) entry.value(), false));
                }
            }
        }
    }
}
