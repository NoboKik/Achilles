package template.rip.module.modules.misc;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.Text;
import template.rip.Template;
import template.rip.api.event.events.PlaySoundEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;

public class LightningDetectModule extends Module {

    public enum modeEnum{Notification, Chat}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Notification, "Mode");

    public LightningDetectModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if (mc.player == null)
            return;

        SoundInstance bolt = event.soundInstance;

        if (bolt.getId().getPath().equals("entity.lightning_bolt.thunder")) {
            String str = String.format("At X: "+ MathUtils.roundNumber(bolt.getX(), 100) + " Y: " + MathUtils.roundNumber(bolt.getY(), 100) + " Z: "+ MathUtils.roundNumber(bolt.getZ(), 100));
            if (mode.is(modeEnum.Notification))
                Template.notificationManager().addNotification(new Notification("Lightning detected", 30000, str));
            if (mode.is(modeEnum.Chat))
                mc.inGameHud.getChatHud().addMessage(Text.of("[§l§o§4Lightning§r] Lightning detected: "+str));
        }
    }
}