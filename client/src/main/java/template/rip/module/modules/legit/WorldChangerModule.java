package template.rip.module.modules.legit;

import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.AnyNumberSetting;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;

public class WorldChangerModule extends Module {

    public enum weatherEnum{None, Rain, Thunder}
    public final ModeSetting<weatherEnum> weather = new ModeSetting<>(this, weatherEnum.None, "Weather");

    public final BooleanSetting modifyTime = new BooleanSetting(this, true, "Modify Time");
    public final AnyNumberSetting time = new AnyNumberSetting(this, 0, false, "Time");

    public WorldChangerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onDisable() {
        if (mc.world != null) {
            mc.world.setRainGradient(0f);
            mc.world.setThunderGradient(0f);
        }
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (mc.world == null)
            return;

        switch (weather.getMode()) {
            case Rain: mc.world.setRainGradient(1.0f); break;
            case Thunder: {
                mc.world.setThunderGradient(1.0f);
                mc.world.setRainGradient(1.0f);
                break;
            }
            case None: {
                mc.world.setThunderGradient(0f);
                mc.world.setRainGradient(0f);
                break;
            }
        }
    }
}
