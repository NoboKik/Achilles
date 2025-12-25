package template.rip.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import template.rip.api.util.EntityRenderStateAddition;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements EntityRenderStateAddition {

    @Unique
    private Entity achilles$entity;

    @Unique
    @Override
    @SuppressWarnings("unused")
    public void achilles$setEntity(Entity entity) {
        this.achilles$entity = entity;
    }

    @Unique
    @Override
    @SuppressWarnings("unused")
    public Entity achilles$getEntity() {
        return achilles$entity;
    }

}