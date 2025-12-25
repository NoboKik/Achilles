package template.rip.api.event.events;

import net.minecraft.entity.LivingEntity;

public class EntityHealthEvent {

    public LivingEntity entity;
    public float fromHP, toHP;
    public Type type;
    public boolean calculator;

    public EntityHealthEvent(LivingEntity entity, float fromHP, float toHP, boolean calculator) {
        this.entity = entity;
        this.fromHP = fromHP;
        this.toHP = toHP;
        this.type = fromHP > toHP ? Type.Damage : (toHP > fromHP ? Type.Heal : Type.Update);
        this.calculator = calculator;
    }

    public enum Type {
        Heal,
        Damage,
        Update
    }
}
