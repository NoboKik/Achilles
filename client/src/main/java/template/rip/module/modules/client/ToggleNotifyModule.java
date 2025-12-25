package template.rip.module.modules.client;

import template.rip.Template;
import template.rip.api.event.events.FastTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.module.Module;

import java.util.concurrent.CopyOnWriteArrayList;

public class ToggleNotifyModule extends Module {

    private final CopyOnWriteArrayList<Module> enabledModules = new CopyOnWriteArrayList<>();

    public ToggleNotifyModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        for (Module m : Template.moduleManager.getModules()) {
            if (m.isEnabled()) {
                enabledModules.add(m);
            }
        }
    }

    @Override
    public void onDisable() {
        enabledModules.clear();
    }

    @EventHandler
    private void onFastTick(FastTickEvent event) {
        if (Template.moduleManager.isModuleEnabled(ClientDestructModule.class)) {
            this.setEnabled(false);
            return;
        }
        for (int i = 0; i < enabledModules.size(); i++) {
            Module m = enabledModules.get(i);
            if (!m.isEnabled()) {
                if (!(m instanceof AchillesSettingsModule))
                    Template.notificationManager().addNotification(new Notification("Module Disabled", 1000, "has been disabled", m.getFullName()));

                enabledModules.remove(m);
                i--;
            }
        }

        for (Module m : Template.moduleManager.getModules()) {
            if (m.isEnabled() && !enabledModules.contains(m)) {
                if (!(m instanceof AchillesSettingsModule))
                    Template.notificationManager().addNotification(new Notification("Module Enabled", 1000, "has been enabled", m.getFullName()));

                enabledModules.add(m);
            }
        }
    }
}
