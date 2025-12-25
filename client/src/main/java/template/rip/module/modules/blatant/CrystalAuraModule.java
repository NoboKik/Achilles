package template.rip.module.modules.blatant;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.ArrayList;

public class CrystalAuraModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Normal, "Mode");
    public final BooleanSetting booleanSetting = new BooleanSetting(this, true, "Boolean Setting");
    public final NumberSetting numberSetting = new NumberSetting(this, 1, 0, 5, 1, "Number Setting");
    public final MinMaxNumberSetting minMaxNumberSetting = new MinMaxNumberSetting(this, 1, 2, 0, 5, 1, "Pair number Setting");
    public final KeybindSetting keySetting = new KeybindSetting(this, -1, "Key Setting");
    public final StringSetting stringSetting = new StringSetting("Content", this, "String Setting");
    public final ColorSetting colorSetting = new ColorSetting(this, new JColor(JColor.PINK), true, "Color setting");
    public final RegistrySetting<Item> registrySetting = new RegistrySetting<>(new ArrayList<>(), this, Registries.ITEM, "Registry Setting");

    public boolean pressed = false;

    public CrystalAuraModule() {
        super(Category.MISC, Description.of("o35's Crystal Aura ported to 1.20, Code provided by: accq (lagoon) on discord, Originally made by: Charlie353535"), "PlannedModule");
    }

    @Override
    protected void enable() {
        super.enable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (mc.targetedEntity instanceof LivingEntity) {
            RenderUtils.Render3D.renderBox(mc.targetedEntity.getBoundingBox(), colorSetting.getColor(), colorSetting.getValue().getAlpha(), event.context);
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
       pressed = keySetting.isPressed();
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (booleanSetting.isEnabled() && numberSetting.getIValue() > 0) {
            mc.options.attackKey.timesPressed += minMaxNumberSetting.getRandomInt();
            mc.inGameHud.getChatHud().addMessage(Text.of(stringSetting.getContent() + " " + mode.getMode()));
        }
    }
}
