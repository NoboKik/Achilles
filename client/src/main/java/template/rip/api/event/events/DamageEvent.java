package template.rip.api.event.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import template.rip.api.event.Cancellable;

public class DamageEvent extends Cancellable {

    public LivingEntity damaged;
    public DamageSource source;

    public DamageEvent(LivingEntity damaged, DamageSource source) {
        this.damaged = damaged;
        this.source = source;
    }
}
