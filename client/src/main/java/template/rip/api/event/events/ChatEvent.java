package template.rip.api.event.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import template.rip.api.event.Cancellable;

import java.time.Instant;

public class ChatEvent extends Cancellable {

    public Text message;

    public static class Chat extends ChatEvent {

        public SignedMessage signedMessage;
        public GameProfile sender;
        public MessageType.Parameters params;
        public Instant receptionTimestamp;

        public Chat(Text message, SignedMessage signedMessage, GameProfile sender, MessageType.Parameters params, Instant receptionTimestamp) {
            this.message = message;
            this.signedMessage = signedMessage;
            this.sender = sender;
            this.params = params;
            this.receptionTimestamp = receptionTimestamp;
        }
    }

    public static class Game extends ChatEvent {

        public boolean overlay;

        public Game(Text message, boolean overlay) {
            this.message = message;
            this.overlay = overlay;
        }
    }
}
