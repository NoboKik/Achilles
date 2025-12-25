package template.rip.module.modules.legit;

import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import template.rip.api.event.events.CameraEvent;
import template.rip.api.event.events.MouseDeltaEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.module.Module;

public class FreelookModule extends Module {

    Perspective perspective = Perspective.FIRST_PERSON;
    Rotation rotation = new Rotation(0.0, 0.0);

    public FreelookModule() {
        super(Category.LEGIT, Description.of("Allows you to look around you in 3rd person"), "Freelook");
    }

    @Override
    protected void enable() {
        if (!nullCheck())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            perspective = mc.options.getPerspective();
            rotation = new Rotation(mc.gameRenderer.getCamera().getYaw(), mc.gameRenderer.getCamera().getPitch());
        }
    }

    @Override
    public void onDisable() {
        mc.options.setPerspective(perspective);
        rotation = new Rotation(0.0, 0.0);
    }

    @EventHandler
    private void onCamera(CameraEvent event) {
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        Camera cam = event.camera;
        Entity focusedEntity = cam.getFocusedEntity();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        cam.setRotation((float) rotation.yaw(), (float) rotation.pitch());
        cam.setPos((MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX())), MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY()) + (double)MathHelper.lerp(tickDelta, cam.lastCameraY, cam.cameraY), MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ()));
        cam.moveBy(-event.camera.clipToSpace(4.0f), 0, 0);
    }

    @EventHandler
    private void mouseDelta(MouseDeltaEvent event) {
        rotation = RotationUtils.correctSensitivity(RotationUtils.calculateNewRotation(rotation, new Pair<>(event.deltaX, event.deltaY)));
        event.deltaY = 0;
        event.deltaX = 0;
    }
}
