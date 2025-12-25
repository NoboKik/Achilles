package template.rip.module.modules.client;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.StringSetting;

public class ClientSpooferModule extends Module {

    public final StringSetting brand = new StringSetting("vanilla", this, "Client brand");

    public ClientSpooferModule(Category category, Description description, String name) {
        super(category, description, name);
    }
}
