package template.rip.api.event.events;

import net.minecraft.world.chunk.Chunk;

public class ChunkDataEvent {

    public Chunk chunk;

    public ChunkDataEvent(Chunk chunk) {
        this.chunk = chunk;
    }
}
