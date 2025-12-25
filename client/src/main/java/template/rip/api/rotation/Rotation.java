package template.rip.api.rotation;

public class Rotation {

    private final double yaw, pitch;

    public Rotation(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation withfPitch(float pitch) {
        return new Rotation(this.yaw, pitch);
    }

    public Rotation withfYaw(float yaw) {
        return new Rotation(yaw, this.pitch);
    }

    public Rotation withPitch(double pitch) {
        return new Rotation(this.yaw, pitch);
    }

    public Rotation withYaw(double yaw) {
        return new Rotation(yaw, this.pitch);
    }

    public float fyaw() {
        return (float) yaw;
    }

    public float fpitch() {
        return (float) pitch;
    }

    public double yaw() {
        return yaw;
    }

    public double pitch() {
        return pitch;
    }

    public Rotation copy() {
        return new Rotation(yaw, pitch);
    }

    @Override
    public String toString() {
        return "Yaw: " + yaw + " Pitch: " + pitch;
    }
}
