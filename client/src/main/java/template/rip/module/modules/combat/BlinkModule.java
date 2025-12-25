package template.rip.module.modules.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.math.Box;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.BlinkEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlinkModule extends Module {

    public enum modeEnum{Latency, Pulse, Infinite}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Latency: Smoothly delays packets, like high ping\nPulse: Delays all packets till the delay is met, like bad wifi\nInfinite: Delays packets infinitely"), modeEnum.Latency, "Mode");

    public final MinMaxNumberSetting delays = new MinMaxNumberSetting(this, 200, 600, 0, 2500, 20, "Delays");
    public final ColorSetting lRColor = new ColorSetting(this, new JColor(1.0f, 1.0f, 1.0f, 0.3f), true, "Real Position Box Color");
    private final BooleanSetting renderRealPosition = new BooleanSetting(this, Description.of("Render the position of where you started blinking"), true, "Render Real Box Position");
    private final BooleanSetting lagRange = new BooleanSetting(this, Description.of("Only delays packets when near a target"), true, "Lag range");
    public final NumberSetting dynaDistance = new NumberSetting(this, Description.of("The distance to enable blink when under, and disable blink when over"), 3.6, 1.0, 6.0, 0.1, "lag range distance");
    private final BooleanSetting nonObtrusiveBlink = new BooleanSetting(this, Description.of("Pulses when you send important packets"), false, "Prevents interference");

    private Box realBox = null;
    private long latencyTimer = 0;
    private long pulseTimer = 0;
    private boolean attacked = false;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public BlinkModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        realBox = null;
        attacked = false;
        latencyTimer = delays.getRandomInt();
        pulseTimer = System.currentTimeMillis() + delays.getRandomInt();
    }

    @Override
    public void onDisable() {
        executorService.submit(() -> Template.blinkUtil().dumpPackets(false));
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!nullCheck())
            return;
        if (realBox != null && renderRealPosition.isEnabled()) {
            RenderUtils.Render3D.renderBox(realBox, lRColor.getColor().getRed(), lRColor.getColor().getGreen(), lRColor.getColor().getBlue(), lRColor.getColor().getAlpha(), event.context);
        }
    }

    @EventHandler
    private void attackEvent(AttackEntityEvent.Post event) {
        if (!nullCheck())
            return;
        if (lagRange.isEnabled() && event.target != null) {
            attacked = true;
            Template.blinkUtil().dumpPackets(false);
        }
    }

    @EventHandler
    private void onBlink(BlinkEvent event) {
        if (!nullCheck())
            return;

        Packet<?> p = event.packet;
        event.blink = true;
        if (nonObtrusiveBlink.isEnabled()) {
            if (isBl(p)) {
                event.blink = false;
                realBox = null;
                return;
            }
        }

        if (lagRange.isEnabled()) {
            if (shouldStop()) {
                event.blink = false;
                onEnable();
                return;
            }
        }

        if (realBox == null) {
            realBox = mc.player.getBoundingBox();
        }

        if (mode.is(modeEnum.Latency)) {
            event.latency = true;
            event.blink = false;
            event.latencyTime = latencyTimer;
            return;
        }

        if (mode.is(modeEnum.Pulse) && pulseTimer <= System.currentTimeMillis()) {
            event.blink = false;
            onEnable();
        }
    }

    private boolean shouldStop() {
        if (mc.world == null || mc.player == null)
            return true;

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null)
            return true;

        if (MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox()) > dynaDistance.value) {
            attacked = false;
            return true;
        }

        if (attacked && MathUtils.closestPosBoxDistance(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox()) < dynaDistance.value)
            return true;

        if (realBox == null)
            return false;

        return MathUtils.closestPosBoxDistance(realBox.getCenter(), target.getBoundingBox()) < MathUtils.closestPosBoxDistance(mc.player.getBoundingBox().getCenter(), target.getBoundingBox());
    }

    private static boolean isBl(Packet<?> p) {
        boolean bl = p instanceof ClickSlotC2SPacket;

        if (p instanceof PlayerInteractBlockC2SPacket)
            bl = true;
        if (p instanceof PlayerInteractEntityC2SPacket)
            bl = true;
        if (p instanceof PlayerInteractItemC2SPacket)
            bl = true;
        if (p instanceof PlayerActionC2SPacket)
            bl = true;
        /*if (p instanceof PickFromInventoryC2SPacket)
            bl = true;*/
        if (p instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) p).getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING)
            bl = true;
        return bl;
    }
}
