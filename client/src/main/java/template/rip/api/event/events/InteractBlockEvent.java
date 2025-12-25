package template.rip.api.event.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import static template.rip.Template.mc;

public class InteractBlockEvent {

    public PlayerEntity player;
    public Hand hand;
    public boolean check;
    public BlockHitResult blockHitResult;

    public static class Pre extends InteractBlockEvent {

        public Pre(PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
            this.player = player;
            this.hand = hand;
            this.blockHitResult = blockHitResult;
            this.check = mc.player == player;
        }
    }

    public static class Post extends InteractBlockEvent {

        public ActionResult actionResult;

        public Post(PlayerEntity player, Hand hand, BlockHitResult blockHitResult, ActionResult actionResult) {
            this.player = player;
            this.hand = hand;
            this.blockHitResult = blockHitResult;
            this.actionResult = actionResult;
            this.check = mc.player == player;
        }
    }
}
