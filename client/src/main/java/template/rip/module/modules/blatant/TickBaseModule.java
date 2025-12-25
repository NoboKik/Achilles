package template.rip.module.modules.blatant;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.RenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

public class TickBaseModule extends Module {
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Manual Activate Key");
    public final NumberSetting distanceTravel = new NumberSetting(this, 3d, 0.1d, 6d, 0.1d, "Distance to travel to");
    public final NumberSetting distanceReset = new NumberSetting(this, 4d, 0.1d, 6d, 0.1d, "Distance to reset from");
    public final AnyNumberSetting ticks = new AnyNumberSetting(this, 5d, false, "Ticks to charge");
    public final AnyNumberSetting releaseTicks = new AnyNumberSetting(this, 5d, false, "Ticks to release");

    public enum tickChargeModeEnum{Pre, Post, Stand_Still};
    public final ModeSetting<tickChargeModeEnum> tickChargeMode = new ModeSetting<>(this, tickChargeModeEnum.Pre, "Charge Ticks Mode");
    private final BooleanSetting holdSAfterHit = new BooleanSetting(this, false, "Hold S after hit").setAdvanced();

//    public final ModeSetting unChargeMode = new ModeSetting("Charge Release Mode", this, "Instant", "Instant", "Timer");
//    public final NumberSetting timerSpeed = new NumberSetting("TPS Speed", this, 20d, 20d, 100d, 1d);
//    private final AdvBooleanSetting doubleDecay = new AdvBooleanSetting("Double Decay on timer", this, true);

    private int savedTicks = 0;
    private boolean wasPressed = false;
    private boolean ticking = false;
    private boolean reset = true;
    private boolean toDisable = false;
    private boolean hit = false;

    public TickBaseModule(Category category, Description description, String name) {
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
        savedTicks = 0;
        ticking = false;
        reset = true;
        wasPressed = false;
        toDisable = false;
        hit = false;
    }


    @Override
    public void onDisable() {
        if (savedTicks > 0) {
            if (!tickChargeMode.is(tickChargeModeEnum.Stand_Still)) {
                savedTicks = Math.max(releaseTicks.getIValue(), savedTicks);
            }
            ticking = true;
//            if (unChargeMode.is("Instant")) {
            while (savedTicks > 0 && mc.player != null) {
//                    mc.player.tick();
                mc.tick();
                if (hit && holdSAfterHit.isEnabled()) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(true);
                }
            }
            ticking = false;
//            }
            hit = false;
            wasPressed = false;
            toDisable = false;
            mc.options.forwardKey.setPressed(KeyUtils.isKeyPressed(mc.options.forwardKey.boundKey.getCode()));
            mc.options.backKey.setPressed(KeyUtils.isKeyPressed(mc.options.backKey.boundKey.getCode()));
        }
    }

    @EventHandler
    private void onAttack(AttackEntityEvent.Pre event) {
        hit = true;
    }

    @EventHandler
    private void onInput(KeyPressEvent event) {
        if (activateKey.isPressed())
            wasPressed = true;
    }

    /*@EventHandler
    private void onRenderTick(RenderTickEvent event) {
        if (unChargeMode.is("Timer") && ticking) {
            if (savedTicks <= 0) event.TPS = 20;
            else event.TPS = timerSpeed.getIValue();
        }
    }*/
    @EventHandler
    private void onTick2(RenderEvent event) {
        if (toDisable) {
            onDisable();
        }
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player == null)
            return;

        if (ticking) {
            savedTicks--;
            /*if (unChargeMode.is("Timer") && doubleDecay.isEnabled())
                savedTicks--;*/
            if (savedTicks <= 0)
                ticking = false;
            return;
        }

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target instanceof PlayerEntity) {
            if (MathUtils.closestPosBoxDistance(target.getBoundingBox()) >= distanceReset.value && !ticking && !wasPressed) reset = true;
            if (reset && MathUtils.closestPosBoxDistance(PlayerUtils.predictState(ticks.getIValue(), mc.player).getLeft().getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), PlayerUtils.predictState(ticks.getIValue(), ((PlayerEntity) target)).getLeft().getBoundingBox()) <= distanceTravel.value && PlayerUtils.isPressingMoveInput()) {
                reset = false;
                wasPressed = true;
            }
        }

        switch (tickChargeMode.getMode()) {
            case Post: {
                if (savedTicks < ticks.getIValue()) {
                    savedTicks++;
                    event.cancel();
                }

                if (wasPressed) {
                    toDisable = true;
                }
                break;
            }

            case Pre: {
                if (wasPressed) {
                    if (savedTicks < ticks.getIValue()) {
                        savedTicks++;
                        event.cancel();
                    } else toDisable = true;
                }
                break;
            }

            case Stand_Still: {
                if (!PlayerUtils.isPressingMoveInput()) {
                    savedTicks++;
                    event.cancel();
                } else toDisable = true;
                break;
            }
        }
        savedTicks = Math.min(ticks.getIValue(), savedTicks);
    }
}