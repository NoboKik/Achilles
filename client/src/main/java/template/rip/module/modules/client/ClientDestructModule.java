package template.rip.module.modules.client;

import template.rip.api.object.Description;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.StringSetting;

public class ClientDestructModule extends Module {

    public enum modeEnum {Delete, Replace}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Delete, "Mode");
    public final StringSetting downloadURL = new StringSetting("https://github.com/sootysplash/sootysplash/raw/main/optimalAim-1.0.0.jar", this, Description.of("The URL to fetch the replacement mod from"), "File URL");

    public ClientDestructModule(Category category, Description description, String name) {
        super(category, description, name);

        downloadURL.addConditionMode(mode, modeEnum.Replace);
    }
    
    @Override
    protected void enable() {
        AchillesMenu.stopClient();
    }
}
