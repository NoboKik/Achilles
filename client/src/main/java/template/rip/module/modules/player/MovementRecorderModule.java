package template.rip.module.modules.player;

import net.minecraft.util.Pair;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.config.MoveRecordConfig;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.font.JColor;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.object.InputRecord;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InputUtil;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ButtonSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.ModeSetting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MovementRecorderModule extends Module {

    private HashMap<Integer, InputRecord> inputs = new HashMap<>();
    private final ButtonSetting play = new ButtonSetting(this::startPlayback, this, "Start Playback");
    private final BooleanSetting loop = new BooleanSetting(this, false, "Loop Playback");
    private final BooleanSetting renderPath = new BooleanSetting(this, true, "Render path");
    private final BooleanSetting recordMB = new BooleanSetting(this, true, "Record Mouse Buttons");
    private final BooleanSetting record = new BooleanSetting(this, true, "Record") {
        @Override
        public void setEnabled(boolean enabled) {
            if (enabled) {
                onEnable();
            }
            super.setEnabled(enabled);
        }
        @Override
        public void toggle() {
            if (!isEnabled()) {
                onEnable();
            }
            super.toggle();
        }
    };
    private final ButtonSetting navToStart = new ButtonSetting(this::navToStart, this, "Nav to start");
    private final ButtonSetting count = new ButtonSetting(() -> {}, this, "Steps") {
        @Override
        public String getButtonName() {
            return "Steps: " + inputs.size();
        }
    };
    private final ButtonSetting clear = new ButtonSetting(() -> {
        onEnable();
        inputs.clear();
    }, this, "Clear");
    private final ButtonSetting save = new ButtonSetting(this::save, this, "Save");
    private final ButtonSetting load = new ButtonSetting(this::load, this, "Load");

    public enum renderMode{Line, Square, Cube}
    private final ModeSetting<renderMode> pathMode = new ModeSetting<>(this, renderMode.Line, "Path Render Mode");
    private final ColorSetting passedColor = new ColorSetting(this, new JColor(JColor.GREEN, 100), true, "Passed Color");
    private final ColorSetting todoColor = new ColorSetting(this, new JColor(JColor.WHITE, 100), true, "Pending Color");

    private boolean finished, playingBack, navigating, cancelInputs;
    private int startAge;

    public MovementRecorderModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        startAge = 0;
        finished = false;
        playingBack = false;
        cancelInputs = false;
    }

    public void startPlayback() {
        playingBack = true;
        startAge = 0;
        finished = false;
    }

    public void save() {
        new MoveRecordConfig().saveProfile(inputs);
    }

    public void load() {
        inputs = new MoveRecordConfig().loadProfile();
    }

    public void navToStart() {
        navigating = true;
    }

    @EventHandler
    private void onHandle(HandleInputEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }
        if (playingBack && cancelInputs && recordMB.isEnabled()) {
            event.cancel();
        }
    }

    @EventHandler
    private void onMouse(MouseUpdateEvent.Post event) {
        if (!nullCheck()) {
            return;
        }
        InputRecord input1 = inputs.get(startAge - 1);
        InputRecord input2 = inputs.get(startAge);
        if (input1 != null && input2 != null) {
            RotationUtils.setEntityRotation(mc.player, RotationUtils.getSmoothRotation(input1.getRot(), input2.getRot(), mc.getRenderTickCounter().getTickDelta(false)));
        }
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!nullCheck()) {
            return;
        }
        if (renderPath.isEnabled()) {
            Color p = passedColor.getColor();
            Vec3d last = null;
            for (Map.Entry<Integer, InputRecord> ir : inputs.entrySet()) {
                if (ir.getKey() == startAge) {
                    p = todoColor.getColor();
                }
                Vec3d vec = ir.getValue().getVec();
                switch (pathMode.getMode()) {
                    case Line: {
                        if (last != null)
                            RenderUtils.Render3D.renderLineTo(last, vec, p, 1f, event.context);
                        last = vec;
                        break;
                    }
                    case Cube: {
                        Box box = new Box(vec.subtract(0.05, 0.05, 0.05), vec.add(0.05, 0.05, 0.05));
                        RenderUtils.Render3D.renderBox(box, p.getRed(), p.getGreen(), p.getBlue(), p.getAlpha(), event.context);
                        break;
                    }
                    case Square: {
                        Box box = new Box(vec.subtract(0.05, 0.05, 0.05), vec.add(0.05, 0.05, 0.05));
                        RenderUtils.Render3D.renderBox(box, p.getRed(), p.getGreen(), p.getBlue(), p.getAlpha(), false, event.context);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onInput(InputEvent event) {
        if (!event.check || mc.player == null)
            return;

        if (!playingBack) {
            if (navigating) {
                InputRecord ir = inputs.get(0);
                if (ir != null) {
                    Pair<Double, Double> dl = PlayerUtils.correctedInputForPos(ir.getVec());
                    boolean done = true;
                    if (dl.getRight() != 0) {
                        event.input.movementSideways = (float) (double) dl.getRight();
                        done = false;
                    }
                    if (dl.getLeft() != 0) {
                        event.input.movementForward = (float) (double) dl.getLeft();
                        done = false;
                    }
                    if (done)
                        navigating = false;
                    else if (mc.player.getPos().distanceTo(ir.getVec()) < 2)
                        event.input.playerInput = InputUtil.setSneaking(event.input.playerInput, true);
                }
            }
            if (record.isEnabled()) {
                inputs.put(startAge++, new InputRecord(event.input, RotationUtils.entityRotation(mc.player), mc.player.getPos(), KeyUtils.isKeyPressed(mc.options.attackKey.boundKey.getCode()), KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()), mc.player.isSprinting()));
            }
        } else {
            record.setEnabled(false);
            InputRecord pr = inputs.get(startAge++);
            if (pr != null) {
                pr.copyTo(event.input, mc.player);
                if (recordMB.isEnabled()) {
                    pr.applyMouseButtons();
                    cancelInputs = false;
                    mc.handleInputEvents();
                    cancelInputs = true;
                }
            } else if (!finished) {
                Template.notificationManager().addNotification(new Notification("Playback finished", 5000, ""));
                finished = true;
                playingBack = false;
                if (loop.isEnabled()) {
                    startPlayback();
                }
            }
        }
    }
}
