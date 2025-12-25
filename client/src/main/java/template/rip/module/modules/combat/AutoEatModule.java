package template.rip.module.modules.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import template.rip.Template;
import template.rip.api.event.events.KeyBindingEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.module.Module;
import template.rip.module.modules.player.CombatBotModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class AutoEatModule extends Module {

    private final BooleanSetting health = new BooleanSetting(this, true, "Health");
    public final NumberSetting healthToEat = new NumberSetting(this, 0.6, 0.0, 1.0, 0.01, "Health % to eat at");
    private final BooleanSetting saturation = new BooleanSetting(this, true, "Saturation");
    public final NumberSetting saturationToEat = new NumberSetting(this, 0.4, 0.0, 1.0, 0.01, "Saturation % to eat at");
    private final BooleanSetting food = new BooleanSetting(this, true, "Food");
    public final NumberSetting foodToEat = new NumberSetting(this, 0.8, 0.0, 1.0, 0.01, "Food % to eat at");
    private final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();
    public boolean eating = false;
    private float sat = 20f;

    public AutoEatModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        eating = false;
        sat = 20f;
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()));
    }

    @EventHandler
    private void onSaturation(PacketEvent.Receive event) {
        if (event.packet instanceof HealthUpdateS2CPacket packet)
            sat = packet.getSaturation();
    }

    @EventHandler
     private void onKeyBind(KeyBindingEvent event) {
        if (event.key == mc.options.useKey && eating) {
            CombatBotModule cbm = Template.moduleManager.getModule(CombatBotModule.class);
            event.setPressed(event.isPressed() || cbm == null || !cbm.isEnabled() || !cbm.runAndEat.isEnabled() || ((cbm.canEat() && cbm.isMinDistance()) || mc.player.isUsingItem()));
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.player == null || (mc.currentScreen != null && disableInScreens.isEnabled()))
            return;

        if (food.isEnabled() && mc.player.getHungerManager().getFoodLevel() / 20.0 <= foodToEat.value) {
            startEating();
        } else if (health.isEnabled() && (mc.player.getHealth() + mc.player.getAbsorptionAmount()) / mc.player.getMaxHealth() <= healthToEat.value) {
            startEating();
        } else if (saturation.isEnabled() && sat / 20.0 <= saturationToEat.value) {
            startEating();
        } else {
            eating = false;
        }
    }

    private void startEating() {
        ItemStack is = mc.player.getMainHandStack();
        if (is.get(DataComponentTypes.FOOD) != null) {
            is = mc.player.getOffHandStack();
            if (is.get(DataComponentTypes.FOOD) != null) {
                eating = false;
                return;
            }
        }
        FoodComponent food = is.get(DataComponentTypes.FOOD);
        boolean hungry = mc.player.getHungerManager().getFoodLevel() < 20;

        if ((food != null && food.canAlwaysEat()) || hungry) {
            eating = true;
        }
    }
}