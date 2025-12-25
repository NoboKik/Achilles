package template.rip.api.event.events;

import net.minecraft.client.sound.SoundInstance;
import template.rip.api.event.Cancellable;

public class PlaySoundEvent extends Cancellable {

    public SoundInstance soundInstance;

    public PlaySoundEvent(SoundInstance soundInstance) {
        this.soundInstance = soundInstance;
    }
}
