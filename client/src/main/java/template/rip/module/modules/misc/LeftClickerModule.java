package template.rip.module.modules.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.HashMap;

public class LeftClickerModule extends Module {

    public enum modeEnum{Normal, Reduce, Always}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Clicks the cps set constantly\nReduce: Only clicks the set cps when on hurt tick,\nelse only attacks when target has 0 hurttime (+ pre hit ticks)\nAlways: Perfect 20 CPS"), modeEnum.Normal, "Click mode");
    public final NumberSetting reducePreHit = new NumberSetting(this, Description.of("How many ticks to attack before the target has 0 hurttime, recommended: for every 50ms of ping, +1 pre hit tick"), 2d, 0d, 6d, 1d, "Reduce pre-hit ticks").setAdvanced();
    public final MinMaxNumberSetting cps = new MinMaxNumberSetting(this, 6d, 12d, 1d, 20d, 1d, "CPS");
    public final NumberSetting cpsDropNoTarget = new NumberSetting(this, 5, 0, 20, 1, "CPS drop without target");
    private final BooleanSetting breakBlocks = new BooleanSetting(this, true, "Break blocks");
    private final BooleanSetting friendSpare = new BooleanSetting(this, true, "Don't attack friends");
    private final BooleanSetting holdLMB = new BooleanSetting(this, true, "Hold Attack Key");
    private final BooleanSetting viaFabricPlus = new BooleanSetting(this, Description.of("Does not click when blocking with sword in 1.8."), true, "ViaFabricPlus");
    private final BooleanSetting perfectBlocking = new BooleanSetting(this, Description.of("Right-clicks after you stop blocking."), true, "Perfect Blocking");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();

    private final HashMap<Entity, Boolean> critEnts = new HashMap<>();
    private long timer = System.currentTimeMillis();
    private boolean wasUsingItem = false;
    private int tick = 0;

    public LeftClickerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return String.format(" %s %s", cps.getIMinValue(), cps.getIMaxValue());
    }

    @Override
    public void onEnable() {
        tick = 0;
        timer = System.currentTimeMillis();
        critEnts.clear();
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Post event) {
        tick++;
    }

    @EventHandler
    private void onUpdate(PlayerTickEvent.Pre event) {
        if ((mc.currentScreen != null && disableInScreens.isEnabled()) || !nullCheck())
            return;

        if (!KeyUtils.isKeyPressed(mc.options.attackKey.boundKey.getCode()) && holdLMB.isEnabled())
            return;

        if (breakBlocks.isEnabled() && (mc.crosshairTarget == null || mc.crosshairTarget.getType() == HitResult.Type.BLOCK))
            return;

        if (friendSpare.isEnabled() && mc.crosshairTarget instanceof EntityHitResult result && result.getEntity() instanceof PlayerEntity entity && PlayerUtils.isFriend(entity))
            return;

        if (click()) {
            if (Template.isClickSim() && mc.currentScreen == null)
                MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());

            if (!nullCheck())
                return;

            mc.doAttack();
        }
        wasUsingItem = mc.player.isUsingItem();
    }

    private boolean click() {
        if ((mc.player.isUsingItem() || mc.options.useKey.isPressed()) && viaFabricPlus.isEnabled())
            return false;

        if (!KeyUtils.isKeyPressed(mc.options.rightKey.boundKey.getCode()) && wasUsingItem && !mc.player.isUsingItem() && perfectBlocking.isEnabled()) {
            return true;
        }

        double CPS = cps.getRandomDouble();
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY)
            CPS = Math.max(CPS - cpsDropNoTarget.value, 1);

        double delay = 950 / CPS;
        switch (mode.getMode()) {
            case Normal: {
                return normal(delay);
            }
            case Reduce: {
                if (!(mc.targetedEntity instanceof LivingEntity target))
                    return normal(delay);

                if ((((target.hurtTime <= 1 + reducePreHit.getIValue() && target.hurtTime >= reducePreHit.getIValue() - 1) || target.hurtTime == 0) && 2 < tick) || (critEnts.get(target) == Boolean.FALSE && PlayerUtils.canCrit(mc.player, true))) {
                    tick = 0;
                    critEnts.put(target, PlayerUtils.canCrit(mc.player, true));
                    return true;
                } else if ((mc.player != null ? mc.player.hurtTime : 0) > 0 && System.currentTimeMillis() >= timer + delay) {
                    timer = System.currentTimeMillis();
                    return true;
                }
                break;
            }
            case Always: {
                return true;
            }
        }
        return false;
    }

    private boolean normal(double delay) {
        if (System.currentTimeMillis() >= timer + delay) {
            timer = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
