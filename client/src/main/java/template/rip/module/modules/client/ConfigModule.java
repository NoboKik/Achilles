package template.rip.module.modules.client;

import template.rip.api.object.Description;
import template.rip.gui.clickgui.ConfigParent;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

import static template.rip.Template.moduleManager;

public class ConfigModule extends Module {

//    public final KeybindSetting configToggler = new KeybindSetting("Toggle configs key", -1, this);
    public final BooleanSetting visuals = new BooleanSetting(this, true, "Visuals");
    public final BooleanSetting modules = new BooleanSetting(this, true, "Modules");

    public ConfigModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (moduleManager != null && moduleManager.getModule(AchillesSettingsModule.class) != null && !moduleManager.isModuleEnabled(AchillesSettingsModule.class))
            moduleManager.getModule(AchillesSettingsModule.class).setEnabled(true);
        ConfigParent.getInstance().isOn = true;
    }

    @Override
    protected void disable() {
        ConfigParent.getInstance().isOn = false;
    }

    @Override
    public void toggleShowOptions() {
        setEnabled(true);
    }

    @Override
    public void renderSettings() {
        //configManager().renderGui();
        //configToggler.render();
    }
}
