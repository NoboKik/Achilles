package template.rip.api.object;

import me.sootysplash.bite.BiteMap;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InputUtil;

import static template.rip.Template.mc;

public class InputRecord {

    private boolean jumping, sneaking, sprinting, lmb, rmb;
    private float forward, sideways, yaw, pitch;
    private double x, y, z;

    public InputRecord(Input toCopy, Rotation rot, Vec3d pos, boolean lmb, boolean rmb, boolean sprinting) {
        sneaking = toCopy.playerInput.sneak();
        jumping = toCopy.playerInput.jump();
        forward = toCopy.movementForward;
        sideways = toCopy.movementSideways;
        yaw = rot.fyaw();
        pitch = rot.fpitch();
        x = pos.x;
        y = pos.y;
        z = pos.z;
        this.lmb = lmb;
        this.rmb = rmb;
        this.sprinting = sprinting;
    }

    public InputRecord() {}

    public void copyTo(Input toApply, Entity rotOnto) {
        InputUtil.setJumping(toApply.playerInput, jumping);
        InputUtil.setSneaking(toApply.playerInput, sneaking);
        toApply.movementForward = forward;
        toApply.movementSideways = sideways;
        RotationUtils.setEntityRotation(rotOnto, RotationUtils.correctSensitivity(new Rotation(yaw, pitch)));
        rotOnto.setSprinting(sprinting);
    }

    public void applyMouseButtons() {
        KeyBinding use = mc.options.useKey;
        KeyBinding attack = mc.options.attackKey;
        if (use.isPressed() != rmb) {
            if (rmb) {
                use.setPressed(true);
                use.timesPressed++;
            } else {
                use.setPressed(false);
            }
        }
        if (attack.isPressed() != lmb) {
            if (lmb) {
                attack.setPressed(true);
                attack.timesPressed++;
            } else {
                attack.setPressed(false);
            }
        }
    }

    public Rotation getRot() {
        return new Rotation(yaw, pitch);
    }

    public Vec3d getVec() {
        return new Vec3d(x, y, z);
    }

    public BiteMap toJSON() {
        BiteMap jo = BiteMap.newInstance();
        jo.add("jumping", jumping);
        jo.add("sneaking", sneaking);
        jo.add("forward", forward);
        jo.add("sideways", sideways);
        jo.add("yaw", yaw);
        jo.add("pitch", pitch);
        jo.add("x", x);
        jo.add("y", y);
        jo.add("z", z);
        jo.add("lmb", lmb);
        jo.add("rmb", rmb);
        return jo;
    }

    public static InputRecord load(BiteMap jsonObject) {
        try {
            InputRecord ir = new InputRecord();
            ir.jumping = jsonObject.get("jumping").getBoolean();
            ir.sneaking = jsonObject.get("sneaking").getBoolean();
            ir.forward = jsonObject.get("forward").getFloat();
            ir.sideways = jsonObject.get("sideways").getFloat();
            ir.yaw = jsonObject.get("yaw").getFloat();
            ir.pitch = jsonObject.get("pitch").getFloat();
            ir.x = jsonObject.get("x").getFloat();
            ir.y = jsonObject.get("y").getFloat();
            ir.z = jsonObject.get("z").getFloat();
            ir.lmb = jsonObject.get("lmb").getBoolean();
            ir.rmb = jsonObject.get("rmb").getBoolean();
            return ir;
        } catch (Exception e) {
//          e.printStackTrace();
            return null;
        }
    }
}
