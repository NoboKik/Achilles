package template.rip.module.setting.settings;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.Setting;

public class PosSetting extends Setting {

    public float x, y;

    public PosSetting(String name, Module parent, float x, float y) {
        super(Description.of(), name);
        this.parent = parent;
        this.x = x;
        this.y = y;

        parent.addSettings(this);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}
