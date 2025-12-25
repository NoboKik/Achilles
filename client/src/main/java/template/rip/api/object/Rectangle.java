package template.rip.api.object;

import static template.rip.Template.mc;

public class Rectangle {

    public double x, y, z, w;

    public Rectangle(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Rectangle toMc() {
        return new Rectangle(x, mc.getWindow().getHeight() - y, z, mc.getWindow().getHeight() - w);
    }

    public boolean safe() {
        return x > 0 && y > 0 && x <= z && y <= w && z < mc.getWindow().getWidth() && w < mc.getWindow().getHeight();
    }
}
