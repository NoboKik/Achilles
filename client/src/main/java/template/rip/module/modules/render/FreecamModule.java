package template.rip.module.modules.render;

import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;
import template.rip.api.event.events.CameraEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.MouseDeltaEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InputUtil;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class FreecamModule extends Module {

    public final BooleanSetting preserveMovement = new BooleanSetting(this, true, "Preserve movement");
    public final NumberSetting speedeee = new NumberSetting(this, 1, 0.1, 10, 0.1, "Speed");
    public final BooleanSetting showCoords = new BooleanSetting(this, true, "Freecam Coords in F3");

    private float sideways = 0;
    private float forward = 0;
    private boolean jumping = false;
    private boolean sneaking = false;
    private Perspective perspective = Perspective.FIRST_PERSON;
    private Rotation rotation = new Rotation(0.0, 0.0);
    public Vec3d position = Vec3d.ZERO;
    private Vec3d prevCameraPos = Vec3d.ZERO;

    public FreecamModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!nullCheck())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.cameraEntity != null) {
            sideways = mc.player.input.movementSideways;
            forward = mc.player.input.movementForward;
            jumping = mc.player.input.playerInput.jump();
            sneaking = mc.player.input.playerInput.sneak();
            perspective = mc.options.getPerspective();
            rotation = new Rotation(mc.gameRenderer.getCamera().getYaw(), mc.gameRenderer.getCamera().getPitch());
            position = mc.gameRenderer.getCamera().getPos();
        }
    }

    @Override
    public void onDisable() {
        mc.options.setPerspective(perspective);
        sideways = 0;
        forward = 0;
        jumping = false;
        sneaking = false;
        rotation = new Rotation(0.0, 0.0);
        position = Vec3d.ZERO;
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        double yMotion = 0.0;
        if (mc.options.jumpKey.isPressed())
            yMotion += speedeee.value;
        if (mc.options.sneakKey.isPressed())
            yMotion -= speedeee.value;
        Vec3d velocity = Entity.movementInputToVelocity(new Vec3d(PlayerUtils.computeMovementInput().y, 0.0, PlayerUtils.computeMovementInput().x), speedeee.value.floatValue(), (float) rotation.yaw());
        velocity = new Vec3d(velocity.x, yMotion, velocity.z);
        prevCameraPos = position;
        position = position.add(velocity);
    }

    @EventHandler
    private void onCamera(CameraEvent event) {
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        event.camera.setRotation((float) rotation.yaw(), (float) rotation.pitch());
        event.camera.setPos(prevCameraPos.lerp(position, mc.getRenderTickCounter().getTickDelta(false)));
    }

    @EventHandler
    private void mouseDelta(MouseDeltaEvent event) {
        if (!nullCheck()) {
            setEnabled(false);
            return;
        }
        rotation = RotationUtils.correctSensitivity(RotationUtils.calculateNewRotation(rotation, new Pair<>(event.deltaX, event.deltaY)));
        event.deltaY = 0;
        event.deltaX = 0;
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (preserveMovement.isEnabled()) {
            event.input.movementSideways = sideways;
            event.input.movementForward = forward;
            event.input.playerInput = InputUtil.setValues(event.input.playerInput, jumping, sneaking);
        } else {
            event.input.movementSideways = 0;
            event.input.movementForward = 0;
            event.input.playerInput = InputUtil.setValues(event.input.playerInput, false, false);
        }
    }
}
