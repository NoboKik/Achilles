package template.rip.module.modules.client;

import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.UI;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class InterfaceModule extends Module {

    public enum fontEnum{Poppins, Montserrat, Outfit, Product, Iosevka}
    public final ModeSetting<fontEnum> font = new ModeSetting<>(this, fontEnum.Poppins, "Font");
    public final ColorSetting color1 = new ColorSetting(this, new JColor(0.79f, 0.24f, 0.32f, 1f), false, "Color 1");
    public final ColorSetting color2 = new ColorSetting(this, new JColor(0.79f, 0.24f, 0.32f, 1f).jDarker(), false, "Color 2");
    public final BooleanSetting rainbowEnabled = new BooleanSetting(this, false, "Rainbow Enabled");
    public final NumberSetting rainbowSaturation = new NumberSetting(this, 0.8, 0, 1, 0.01, "Rainbow Saturation");
    public final NumberSetting rainbowBrightness = new NumberSetting(this, 1, 0, 1, 0.01, "Rainbow Brightness");

    public static InterfaceModule instance = null;

    public InterfaceModule(Category category, Description description, String name) {
        super(category, description, name);
        instance = this;
    }

    @Override
    protected void enable() {
        setEnabled(false);
    }

    @Override
    public void renderSettings() {
        settings.forEach(Setting::renderIfDoDisplay);
        UI.color1 = color1.getColor();
        UI.color2 = color2.getColor();
    }
}
