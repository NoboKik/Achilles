package template.rip.module.modules.client;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;

public class ClickSimulationModule extends Module {

    public final BooleanSetting clickEnabled = new BooleanSetting(this, Description.of("Simulate Mouse clicks?"), true, "Click Simulation");
    public final MinMaxNumberSetting lmbDuration = new MinMaxNumberSetting(this, Description.of("How long LMB will be pressed for when simulating clicks"), 3, 4, 0, 20, 1, "LMB Click Duration");
    public final MinMaxNumberSetting rmbDuration = new MinMaxNumberSetting(this, Description.of("How long RMB will be pressed for when simulating clicks"), 3, 4, 0, 20, 1, "RMB Click Duration");
    public final BooleanSetting experimentalClick = new BooleanSetting(this, Description.of("Enable this if clicks are not simulated in your KeyBind/CPS hud"), false, "Experimental Simulation").setAdvanced();

    public ClickSimulationModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {}

    @Override
    protected void disable() {}

    @Override
    public void renderSettings() {
        clickEnabled.render();
        lmbDuration.render();
        rmbDuration.render();
        experimentalClick.render();
    }
}
