package template.rip.module.modules.misc;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.AnyNumberSetting;

public class CoordSpooferModule extends Module {

    public final AnyNumberSetting xPosOffset = new AnyNumberSetting(this, -4500, true, "X Offset");
    public final AnyNumberSetting yPosOffset = new AnyNumberSetting(this, 0, true, "Y Offset");
    public final AnyNumberSetting zPosOffset = new AnyNumberSetting(this, 7200, true, "Z Offset");

    public CoordSpooferModule(Category category, Description description, String name) {
        super(category, description, name);
    }
}
