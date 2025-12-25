package template.rip.api.util;

import net.minecraft.entity.Entity;

/**
 * Addition to {@link net.minecraft.client.render.entity.state.EntityRenderState}
 */
public interface EntityRenderStateAddition {

    void achilles$setEntity(Entity entity);

    Entity achilles$getEntity();

}