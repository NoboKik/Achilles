package template.rip.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.KeyBindingEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.screens.LoginScreen;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.util.HashMap;
import java.util.Optional;
import java.util.TreeSet;

public class ModuleManager {

    private final HashMap<Class<? extends Module>, Module> class2module = new HashMap<>();
    private final HashMap<Module.Category, TreeSet<Module>> category2Module = new HashMap<>();
    private final TreeSet<Module> modules = new TreeSet<>(Module::compare);

    public boolean binding = false;
    public boolean typing = false;

    public ModuleManager() {
        for (Module.Category category : Module.Category.values()) {
            category2Module.put(category, new TreeSet<>(Module::compare));
        }
    }

    private boolean isModuleEnabled(@Nullable Module m) {
        return m != null && m.isEnabled();
    }

    public boolean isModuleEnabled(Class<? extends Module> clazz) {
        return isModuleEnabled(class2module.get(clazz));
    }

    public boolean isModuleDisabled(Class<? extends Module> clazz) {
        return !isModuleEnabled(clazz);
    }

    @Nullable
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return moduleClass.cast(class2module.get(moduleClass));
    }

    @NotNull
    public <T extends Module> Optional<T> getOptModule(Class<T> moduleClass) {
        return Optional.ofNullable(moduleClass.cast(class2module.get(moduleClass)));
    }

    @SuppressWarnings("unchecked cast")
    public TreeSet<Module> getModules() {
        synchronized (modules) {
            return (TreeSet<Module>) modules.clone();
        }
    }

    @SuppressWarnings("unchecked cast")
    public TreeSet<Module> getModulesByCategory(@Nullable Module.Category c) {
        synchronized (category2Module) {
            return (TreeSet<Module>) category2Module.getOrDefault(c, new TreeSet<>(Module::compare)).clone();
        }
    }

    public void addModule(Module module) {
        synchronized (category2Module) {
            synchronized (class2module) {
                synchronized (modules) {
                    category2Module.get(module.getCategory()).add(module);
                    class2module.put(module.getClass(), module);
                    modules.add(module);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onGameInput(HandleInputEvent.Pre event) {
        if (!AchillesMenu.isClientEnabled())
            return;

        if (typing && Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class) || LoginScreen.getInstance().isRendered()) {
            event.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onGameKey(KeyBindingEvent event) {
        if (!AchillesMenu.isClientEnabled())
            return;

        if (typing && Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class) || LoginScreen.getInstance().isRendered()) {
            event.setPressed(false);
            event.key.wasPressed();
        }
    }
}
