package template.rip.module.modules.player;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class BabyPlayerModule extends Module {

    public final BooleanSetting onlyWhileSneaking = new BooleanSetting(this, false, "Only when sneak");

    public BabyPlayerModule(Category category, Description description, String name) {
        super(category, description, name);
    }
}
