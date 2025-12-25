package template.rip.module.modules.crystal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.UpdateCrosshairEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.modules.blatant.MultiTaskModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;

import java.util.HashMap;
import java.util.Objects;

public class AutoDisableModule extends Module {

    public enum modeEnum {Auto, Manual}
    public enum disableEnum {Stun, Spam}
    public final ModeSetting<modeEnum> switchMode = new ModeSetting<>(this, modeEnum.Auto, "Switch Mode");
    public final MinMaxNumberSetting autoSwitchDelay = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Auto Switch Delay");
    public final MinMaxNumberSetting range = new MinMaxNumberSetting(this, 2.4d, 2.9, 0d, 6d, 0.1d, "Range");
    public final ModeSetting<disableEnum> disableMode = new ModeSetting<>(this, disableEnum.Stun, "Disable Mode");
    public final MinMaxNumberSetting spamCps = new MinMaxNumberSetting(this, 4d, 6d, 1d, 20d, 1d, "Spam CPS");
    public final BooleanSetting predictionDisable = new BooleanSetting(this, Description.of("Tries to disable before the target is actually blocking"), true, "Prediction Disable");
    public final BooleanSetting throughWalls = new BooleanSetting(this, Description.of("Allows attacking through walls"), false, "Through Walls");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();
    private final HashMap<LivingEntity, Integer> shieldAttacks = new HashMap<>();
    private int lastSlot, switchDelay;
    private double currentRange;
    private long timer;
    private boolean isBreaking;
    private HitResult hr;

    public AutoDisableModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        isBreaking = false;
        lastSlot = -1;
        shieldAttacks.clear();
        hr = null;
        switchDelay = autoSwitchDelay.getRandomInt();
        currentRange = range.getRandomDouble();
        timer = System.currentTimeMillis();
    }

    @EventHandler
    private void updateCrosshair(UpdateCrosshairEvent event) {
        if (mc.player == null) {
            hr = null;
            return;
        }
        LivingEntity le = PlayerUtils.findFirstLivingTargetOrNull();
        if (le == null) {
            hr = null;
            return;
        }
        Rotation rot = Template.rotationManager().rotation();
        hr = PlayerUtils.getHitResult(mc.player, e -> e == le, rot.fyaw(), rot.fpitch(), currentRange, throughWalls.isEnabled(), 0);
    }

    @EventHandler
    public void onHandle(HandleInputEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }
        if (disableInScreens.isEnabled() && mc.currentScreen != null) {
            return;
        }
        isBreaking = dO();
        if (!isBreaking) {
            if (lastSlot != -1 && switchMode.is(modeEnum.Auto)) {
                if (switchDelay > 0) {
                    switchDelay--;
                    return;
                } else switchDelay = autoSwitchDelay.getRandomInt();
                InvUtils.setInvSlot(lastSlot);
                lastSlot = -1;
            } else switchDelay = autoSwitchDelay.getRandomInt();
        }
    }

    private boolean dO() {
        if (!(hr instanceof EntityHitResult && ((EntityHitResult) hr).getEntity() instanceof LivingEntity target)) {
            shieldAttacks.clear(); // we missed the stun window
            return false;
        }
        if (mc.player.isUsingItem()) {
            MultiTaskModule mtm = Template.moduleManager.getModule(MultiTaskModule.class);
            if (mtm == null || !mtm.isEnabled() || !mtm.attack.isEnabled()) {
                shieldAttacks.clear(); // we missed the stun window
                return false;
            }
        }
        switch (disableMode.getMode()) {
            case Stun: {
                if (!PlayerUtils.isBlockedByShield(mc.player, target, predictionDisable.isEnabled()) && !Objects.equals(shieldAttacks.get(target), 1)) {//on low ping we might skip the hit after disabling the shield
                    shieldAttacks.clear();
                    return false;
                }
                int attacks = shieldAttacks.getOrDefault(target, 0);
                if (attacks <= 1) {
                    if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem) && switchMode.is(modeEnum.Auto)) {
                        int slot = InvUtils.getItemSlot(i -> i instanceof AxeItem);
                        if (slot != -1) {
                            if (switchDelay > 0) {
                                switchDelay--;
                                return true;
                            }
                            lastSlot = mc.player.getInventory().selectedSlot;
                            InvUtils.setInvSlot(slot);
                        }
                    }
                    if (mc.player.getMainHandStack().getItem() instanceof AxeItem) {
                        if (Template.isClickSim())
                            MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(Hand.MAIN_HAND);

                        shieldAttacks.put(target, attacks + 1);
                        currentRange = range.getRandomDouble();
                        switchDelay = autoSwitchDelay.getRandomInt();
                    }
                    return true;
                }
                break;
            }
            case Spam: {
                if (!PlayerUtils.isBlockedByShield(mc.player, target, predictionDisable.isEnabled())) {
                    return false;
                }
                if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem) && switchMode.is(modeEnum.Auto)) {
                    int slot = InvUtils.getItemSlot(i -> i instanceof AxeItem);
                    if (slot != -1) {
                        if (switchDelay > 0) {
                            switchDelay--;
                            return true;
                        }
                        lastSlot = mc.player.getInventory().selectedSlot;
                        InvUtils.setInvSlot(slot);
                    }
                }
                if (mc.player.getMainHandStack().getItem() instanceof AxeItem) {
                    if (normal()) {
                        if (Template.isClickSim())
                            MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(Hand.MAIN_HAND);

                        currentRange = range.getRandomDouble();
                        switchDelay = autoSwitchDelay.getRandomInt();
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean normal() {
        double delay = 950 / spamCps.getRandomDouble();
        if (System.currentTimeMillis() >= timer + delay) {
            timer = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
