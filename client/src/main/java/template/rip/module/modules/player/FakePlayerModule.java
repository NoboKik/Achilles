package template.rip.module.modules.player;

import template.rip.api.event.events.InputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.object.FakePlayerEntity;
import template.rip.module.Module;
import template.rip.module.setting.settings.KeybindSetting;

import java.util.ArrayDeque;

public class FakePlayerModule extends Module {

    private final KeybindSetting extraSpawn = new KeybindSetting(this, -1, "Extra player spawn key");

    private final ArrayDeque<FakePlayerEntity> fakePlayers = new ArrayDeque<>();
    private boolean pressed = false;

    public FakePlayerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!nullCheck())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            fakePlayers.clear();
            FakePlayerEntity fakePlayer = new FakePlayerEntity(mc.player, "Testificate_", 20f, true, false);
            fakePlayer.spawn();
            fakePlayers.add(fakePlayer);
        }
    }

    @Override
    public void onDisable() {
        fakePlayers.forEach(fp -> {
            if (fp != null) {
                fp.despawn();
            }
        });
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (extraSpawn.isPressed()) {
            if (!pressed) {
                pressed = true;
                FakePlayerEntity fakePlayer = new FakePlayerEntity(mc.player, "Testificate_" + fakePlayers.size(), 20f, true, false);
                fakePlayer.spawn();
                fakePlayers.add(fakePlayer);
            }
        } else {
            pressed = false;
        }
    }
}
