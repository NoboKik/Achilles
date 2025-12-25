package template.rip.module.modules.player;

import template.rip.Template;
import template.rip.api.event.events.WalkingForwardEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.BooleanSetting;

import static template.rip.module.modules.blatant.ScaffoldModule.allowSprint;

public class SprintModule extends Module {

    private static boolean stopSprint = false;
    public final BooleanSetting omniSprint = new BooleanSetting(this, false, "Omni sprint");

    public SprintModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    public static void setStopSprint(boolean stop) {
        stopSprint = stop;
    }

    public static boolean isStopSprint() {
        return stopSprint;
    }

    @Override
    public String getSuffix() {
        if (omniSprint.isEnabled()) return " Omni";
        return "";
    }

    @EventHandler
    void onWalk(WalkingForwardEvent event) {
        if (!/*Scaffold.*/allowSprint) event.forward = false;
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (asm != null && !(asm.moveFixMode.is(AchillesSettingsModule.moveFixModeEnum.Backwards_Sprint))) {
            if (omniSprint.isEnabled() && PlayerUtils.isMoving())
                event.forward = true;
        }
        else omniSprint.setEnabled(false);
    }
}
