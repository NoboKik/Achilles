package template.rip.module.modules.client;

import template.rip.api.object.Description;
import template.rip.gui.clickgui.LegitMenu;
import template.rip.module.Module;

import static template.rip.Template.moduleManager;

public class ModMenuModule extends Module {

    public ModMenuModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (moduleManager != null && moduleManager.getModule(AchillesSettingsModule.class) != null && !moduleManager.isModuleEnabled(AchillesSettingsModule.class))
            moduleManager.getModule(AchillesSettingsModule.class).setEnabled(false);
        LegitMenu.getInstance().isOn = true;
    }

    @Override
    public void toggleShowOptions() {
        setEnabled(true);
    }

    @Override
    public void disable() {
        LegitMenu.getInstance().isOn = false;
    }
}
