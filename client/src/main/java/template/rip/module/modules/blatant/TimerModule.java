package template.rip.module.modules.blatant;

import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import template.rip.Template;
import template.rip.api.event.events.BacktrackEvent;
import template.rip.api.event.events.BlinkEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.RenderTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class TimerModule extends Module {

    public final NumberSetting speed = new NumberSetting(this, 1d, 0.1d, 10d, 0.01d, "Speed");
    public final NumberSetting balanceDecay = new NumberSetting(this, 5, 0.1, 25, 0.1d, "Balance decay");
    public final BooleanSetting balanceAbuse = new BooleanSetting(this, false, "Balance Abuse");
    public final BooleanSetting balanceIndicator = new BooleanSetting(this, false, "Balance indicator");

    private double packetsSaved;
    private boolean timering;
    private int pongsSinceMove;

    public TimerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        packetsSaved = 0;
        timering = false;
        pongsSinceMove = 0;
    }

    @EventHandler
    public void onTick(PlayerTickEvent.Pre event) {
        if (!nullCheck()) return;
        if (balanceAbuse.isEnabled() && balanceIndicator.isEnabled()) {
            mc.getMessageHandler().onGameMessage(Text.of("PS: " + packetsSaved), true);
        }
    }
    @EventHandler
    public void onTPS(RenderTickEvent event) {
        if ((!balanceAbuse.isEnabled() || (packetsSaved > 0 && PlayerUtils.isPressingMoveInput(false)))) {
            event.TPS = (int) Math.round(speed.value * 20.0);
            timering = true;
        } else {
            timering = false;
        }
    }
    
    @EventHandler
    public void onBacktrack(BacktrackEvent event) {
        if (!nullCheck()) return;
        if (balanceAbuse.isEnabled()) {
            if (timering) {
                event.backtrack = true;
            }
        }
    }
    
    @EventHandler
    public void onBlink(BlinkEvent event) {
        if (!nullCheck()) return;
        if (balanceAbuse.isEnabled()) {
            if (event.packet instanceof PlayerMoveC2SPacket packet) {
//                mc.inGameHud.getChatHud().addMessage(Text.of("Move: " + pongsSinceMove));
                pongsSinceMove = 0;
                if (PlayerUtils.entityStill(mc.player) && !packet.changesLook()) {
                    if (!timering) {
                        packetsSaved += 100;
                    }
                } else if (packetsSaved > 0) {
                    timering = true;
                    packetsSaved -= balanceDecay.value;
                }
                packetsSaved = (int) MathUtils.coerceIn(packetsSaved, 0, 100);
            }
            if (event.packet instanceof CommonPongC2SPacket) {
                pongsSinceMove++;
            }
            event.blink = timering;
        }
    }
}
