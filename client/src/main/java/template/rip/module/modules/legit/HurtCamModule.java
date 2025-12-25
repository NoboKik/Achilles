package template.rip.module.modules.legit;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class HurtCamModule extends Module {

    public final BooleanSetting disableHurtcam = new BooleanSetting(this, false, "Disable hurtcam");
    public final BooleanSetting oldHurtcam = new BooleanSetting(this, false, "Old Hurtcam");
    public final NumberSetting scale = new NumberSetting(this, 1, 0, 2, 0.1, "Scale");

    public HurtCamModule(Category category, Description description, String name) {
        super(category, description, name);
    }
}
