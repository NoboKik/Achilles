package template.rip.mixin.accessors;

import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkManager.class)
public interface IClientChunkManager {
    @Accessor("chunks")
    ClientChunkManager.ClientChunkMap getChunks();
}