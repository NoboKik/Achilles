package template.rip.module.modules.combat;

import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InputUtil;
import template.rip.module.Module;
import template.rip.module.modules.player.SprintModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class WTapRecodeModule extends Module {

    public enum modeEnum{W_Tap, S_Tap, Sneak_Tap, Sprint_Tap}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.W_Tap, "Tap mode");
    public final BooleanSetting onlyOnGround = new BooleanSetting(this, true, "Only when on ground");
    public final MinMaxNumberSetting wTapStart = new MinMaxNumberSetting(this, 100, 200, 0, 1000, 1, "Tap start time");
    public final MinMaxNumberSetting wTapStop = new MinMaxNumberSetting(this, 300, 400, 0, 1000, 1, "Tap stop time");
    public final NumberSetting minDelay = new NumberSetting(this, 500, 0, 2500, 1, "Delay Per Tap");

    private long startTime, stopTime, lastTap;
    private boolean stopped, started;

    public WTapRecodeModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        stopped = true;
        started = true;
    }

    @Override
    public void onDisable() {
        SprintModule.setStopSprint(false);
    }

    @Override
    public String getSuffix() {
        return " "+mode.getDisplayName();
    }

    @EventHandler
    private void onAttack(AttackEntityEvent.Post event) {
        if (lastTap < System.currentTimeMillis() && stopped && started) {
            if (!onlyOnGround.isEnabled() || mc.player.isOnGround()) {
                startTime = System.currentTimeMillis() + wTapStart.getRandomInt();
                stopTime = startTime + wTapStop.getRandomInt();
                lastTap = System.currentTimeMillis() + minDelay.getIValue();
                if (mode.is(modeEnum.Sprint_Tap)) {
                    mc.player.setSprinting(true);
                    SprintModule.setStopSprint(false);
                }
                stopped = false;
                started = false;
                Template.moduleManager.getModule(SprintModule.class).setEnabled(true);
            }
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!event.check) {
            return;
        }

        if (!stopSprint()) {
            if (!started && !stopped) {
                started = true;
                stopped = true;
                if (mode.is(modeEnum.Sprint_Tap)) {
                    mc.player.setSprinting(true);
                    SprintModule.setStopSprint(false);
                }
            }
        } else {
            switch (mode.getMode()) {
                case W_Tap -> event.input.movementForward = 0;
                case S_Tap -> event.input.movementForward *= -1;
                case Sneak_Tap -> event.input.playerInput = InputUtil.setSneaking(event.input.playerInput, true);
                case Sprint_Tap -> {
                    if (!started) {
                        started = true;
                        if (mode.is(modeEnum.Sprint_Tap)) {
                            mc.player.setSprinting(false);
                            SprintModule.setStopSprint(true);
                        }
                    }
                }
            }
        }
    }

    private boolean stopSprint() {
        long now = System.currentTimeMillis();
        return now >= startTime && now <= stopTime;
    }
}
