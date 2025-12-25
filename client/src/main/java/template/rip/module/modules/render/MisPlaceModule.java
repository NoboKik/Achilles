package template.rip.module.modules.render;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

public class MisPlaceModule extends Module {

    public final NumberSetting blocksCloser = new NumberSetting(this, 1.5, -4.5, 4.5, 0.1, "Blocks closer");

    public MisPlaceModule(Category category, Description description, String name) {
        super(category, description, name);
    }
}
