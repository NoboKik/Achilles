package template.rip.module.modules.player;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

public class KeepSprintModule extends Module {

    public final NumberSetting motion = new NumberSetting(this, 1, 0.6, 1, 0.1, "Motion").setAdvanced();

    public KeepSprintModule(Category category, Description description, String name) {
        super(category, description, name);
    }
}
