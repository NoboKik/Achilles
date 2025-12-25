package template.rip.deprecated;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;

public class WTapModule extends Module {
    public enum modeEnum{W_Tap, Sprint_Tap, S_Tap, Packet_Pre, Packet_Post}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.W_Tap, "WTap mode");

    public final BooleanSetting onlyOnGround = new BooleanSetting(this, true, "Only when on ground");
    public final MinMaxNumberSetting delays = new MinMaxNumberSetting(this, 50, 100, 0, 250, 10, "UnSprint Delay");
    public final BooleanSetting smartHurtTime = new BooleanSetting(this, true, "Smart hurtTime");
    public final MinMaxNumberSetting minDelay = new MinMaxNumberSetting(this, 0, 0, 0, 500, 1, "Min Delay");

    public WTapModule() {
        super(Category.COMBAT, Description.of("Automatically resets your sprint after an attack"), "WTap");
    }

    boolean reset = false;
    long timer = System.currentTimeMillis();
    long timer2 = System.currentTimeMillis();
    boolean hasHurt = false;
    boolean lastPacketSprintState = false;
    boolean bl = false;
    @Override
    public void onEnable() {
        timer = System.currentTimeMillis() + delays.getRandomInt();
        timer2 = System.currentTimeMillis() + minDelay.getRandomInt();
        hasHurt = false;
        bl = false;
        if (mc.player != null)
            lastPacketSprintState = mc.player.isSprinting();
    }

    @Override
    public String getSuffix() {
        return " "+mode.getDisplayName();
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket packet) {
            switch (packet.getMode()) {
                case START_SPRINTING : lastPacketSprintState = true; break;
                case STOP_SPRINTING : lastPacketSprintState = false; break;
            }
        }
    }
    @EventHandler
    private void onAttack(AttackEntityEvent.Pre event) {
        if (!nullCheck())
            return;

        if (mc.player.isOnGround() || !onlyOnGround.isEnabled() && mode.is(modeEnum.Packet_Pre)) {
            boolean sprint = lastPacketSprintState;
            if (sprint) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
        }


    }
    @EventHandler
    private void onAttack(AttackEntityEvent.Post event) {
        if (!nullCheck())
            return;

        if (mc.player.isOnGround() || !onlyOnGround.isEnabled()) {
            reset = true;
            onEnable();
        }

        if (mode.is(modeEnum.Packet_Post) && reset) {
            boolean sprint = lastPacketSprintState;
            if (sprint) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (timer2 > System.currentTimeMillis())
            return;

        if (!event.check)
            return;

        if (!reset)
            return;

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();

        if ((target == null || target.hurtTime == target.maxHurtTime) || !smartHurtTime.isEnabled())
            hasHurt = true;

        if (!hasHurt)
            return;

        switch (mode.getMode()) {
            case W_Tap ://                event.input.movementSideways = 0F;
                    event.input.movementForward = 0F; break;

            case S_Tap ://                event.input.movementSideways = event.input.movementSideways * -1F;
                    event.input.movementForward = event.input.movementForward * -1F; break;
        }

        if (timer < System.currentTimeMillis()) {
            reset = false;
            onEnable();
        }
    }

}
