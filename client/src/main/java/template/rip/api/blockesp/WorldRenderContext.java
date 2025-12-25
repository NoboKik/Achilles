package template.rip.api.blockesp;

import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;

public class WorldRenderContext {

    private WorldRenderer worldRenderer;
    private float tickDelta;
    private boolean blockOutlines;
    private Camera camera;
    private Frustum frustum;
    private GameRenderer gameRenderer;
    private VertexConsumerProvider consumers;
    private ClientWorld world;
    private Matrix4f projectionMatrix;
    private Matrix4f positionMatrix;

    public void prepare(
            WorldRenderer worldRenderer,
            float tickDelta,
            boolean blockOutlines,
            Camera camera,
            GameRenderer gameRenderer,
            VertexConsumerProvider consumers,
            ClientWorld world,
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix
    ) {
        this.worldRenderer = worldRenderer;
        this.tickDelta = tickDelta;
        this.blockOutlines = blockOutlines;
        this.camera = camera;
        this.gameRenderer = gameRenderer;
        this.consumers = consumers;
        this.world = world;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
    }

    public void setFrustum(Frustum frustum) {
        this.frustum = frustum;
    }

    
    public WorldRenderer worldRenderer() {
        return worldRenderer;
    }


    public Matrix4f positionMatrix() {
        return positionMatrix;
    }

    
    public float tickDelta() {
        return tickDelta;
    }

    
    public boolean blockOutlines() {
        return blockOutlines;
    }

    
    public Camera camera() {
        return camera;
    }

    
    public ClientWorld world() {
        return world;
    }

    
    public Frustum frustum() {
        return frustum;
    }

    
    public VertexConsumerProvider consumers() {
        return consumers;
    }

    
    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    
    public VertexConsumer vertexConsumer() {
        return consumers.getBuffer(RenderLayer.getLines());
    }


    public Matrix4f projectionMatrix() {
        return projectionMatrix;
    }
}