package template.rip.module.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import template.rip.Template;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InvUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.function.Predicate;

public class AutoHealModule extends Module {

    private final NumberSetting minHealth = new NumberSetting(this, 0.6, 0, 1, 0.01, "Min Health %");
    private final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Switch Delay");
    private final MinMaxNumberSetting useDelay = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Use Delay");

    public enum modeEnum{Auto, Manual}
    private final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Manual, "Mode");
    private final BooleanSetting healthPots = new BooleanSetting(this, true, "Throw Health Pots");
    private final BooleanSetting positivePots = new BooleanSetting(this, true, "Throw Other Util Pots");
    private final BooleanSetting heads = new BooleanSetting(this, false, "Use Heads");
    private final BooleanSetting soups = new BooleanSetting(this, false, "Eat Soups");
    private final BooleanSetting dropSoup = new BooleanSetting(this, true, "Drop soups");
    private final BooleanSetting scroll = new BooleanSetting(this, false, "Scroll");
    private final BooleanSetting disableAA = new BooleanSetting(this, true, "Disable Aim Assist");
    private final BooleanSetting onlyThrowPitch = new BooleanSetting(this, true, "Pitch Req for Throw");
    private final NumberSetting minPitch = new NumberSetting(this, 45, 0, 90, 1, "Min Pitch");

    private final DividerSetting rotSettings = new DividerSetting(this, false, "Auto Rotation Settings");
    private final BooleanSetting autoRot = new BooleanSetting(this, false, "Auto Rotation Enabled");
    public enum rotEnum {Normal, Silent}
    public final ModeSetting<rotEnum> rotationMode = new ModeSetting<>(this, rotEnum.Normal, "Rotation Mode");
    private final NumberSetting targetPitch = new NumberSetting(this, 75, 0, 90, 1, "Target Pitch");
    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 5d, 7d, 0.1, 10d, 0.1, "Pitch Speeds");
    public final MinMaxNumberSetting yawNoise = new MinMaxNumberSetting(this, 0.5, 1, 0, 5, 0.1, "Yaw noise");

    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    private int switchClock, useClock, prevSlot;
    private boolean pressed;

    public AutoHealModule(Category category, Description description, String name) {
        super(category, description, name);
        dropSoup.addConditionBoolean(soups, true);
        minPitch.addConditionBoolean(onlyThrowPitch, true);
        rotSettings.addSetting(autoRot, rotationMode, targetPitch, pitchSpeed, yawNoise);
    }

    @Override
    public void onEnable() {
        switchClock = switchDelay.getRandomInt();
        useClock = useDelay.getRandomInt();
        prevSlot = -1;
        pressed = false;
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    @EventHandler
    private void onMouse(MouseUpdateEvent.Post event) {
        if (!nullCheck() || !autoRot.isEnabled())
            return;

        if ((pressed|| mode.is(modeEnum.Auto)) && stackPred.test(mc.player.getMainHandStack())) {
            double pitchStrength = pitchSpeed.getRandomDouble() / 25.0;
            float pitch = MathHelper.lerpAngleDegrees((float) pitchStrength, rotationMode.is(rotEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().fpitch(), targetPitch.getFValue());
            Rotation finalRot = RotationUtils.correctSensitivity(RotationUtils.addNoise(Template.rotationManager().rotation().withfPitch(pitch), yawNoise.getRandomDouble(), 0));

            switch (rotationMode.getMode()) {
                case Silent: Template.rotationManager().setRotation(finalRot); break;
                case Normal: {
                    Pair<Double, Double> pair = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(finalRot, RotationUtils.entityRotation(mc.player)));
                    mc.player.changeLookDirection(pair.getLeft(), pair.getRight());
                    break;
                }
            }
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.currentScreen != null || !nullCheck())
            return;

        if (!pressed && KeyUtils.isKeyPressed(activateKey.getCode())) {
            pressed = true;
        }
        
        if (pressed || mode.is(modeEnum.Auto)) {
            Predicate<ItemStack> itemPred = stackPred;
            if (!itemPred.test(mc.player.getMainHandStack())) {
                if (switchClock > 0) {
                    switchClock--;
                    return;
                }

                if (prevSlot == -1) prevSlot = mc.player.getInventory().selectedSlot;

                int healSlot = InvUtils.getItemStackSlot(itemPred);
                if (healSlot != -1) {
                    if (scroll.isEnabled()) {
                        InvUtils.setInvSlot(InvUtils.scrollToSlot(healSlot));
                    } else {
                        InvUtils.setInvSlot(healSlot);
                    }

                    switchClock = switchDelay.getRandomInt();
                } else {
                    InvUtils.setInvSlot(prevSlot);
                    onEnable();
                }
            }

            if (itemPred.test(mc.player.getMainHandStack())) {
                if (useClock > 0) {
                    useClock--;
                    return;
                }

                if (onlyThrowPitch.isEnabled()) {
                    if (Template.rotationManager().pitch() < minPitch.value)
                        return;
                }

                pressed = false;

                if (Template.isClickSim())
                    MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                ActionResult actionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (PlayerUtils.shouldSwingHand(actionResult))
                    mc.player.swingHand(Hand.MAIN_HAND);

                useClock = useDelay.getRandomInt();
            } else if (prevSlot != -1) {
                if (mode.is(modeEnum.Auto)) {
                    InvUtils.setInvSlot(prevSlot);
                }
                onEnable();
            }
        } else if (InvUtils.getItemSlot(Items.BOWL) != -1 && dropSoup.isEnabled() && soups.isEnabled()) {
            if (mc.player.getMainHandStack().getItem() != Items.BOWL) {
                if (switchClock > 0) {
                    switchClock--;
                    return;
                }

                if (prevSlot == -1) prevSlot = mc.player.getInventory().selectedSlot;

                int soupSlot = InvUtils.getItemSlot(Items.BOWL);

                if (soupSlot != -1) {
                    if (scroll.isEnabled()) {
                        InvUtils.setInvSlot(InvUtils.scrollToSlot(soupSlot));
                    } else {
                        InvUtils.setInvSlot(soupSlot);
                    }

                    switchClock = switchDelay.getRandomInt();
                }
            }

            if (mc.player.getMainHandStack().getItem() == Items.BOWL) {
                if (useClock > 0) {
                    useClock--;
                    return;
                }

                mc.player.dropSelectedItem(false);

                useClock = useDelay.getRandomInt();
            }
        } else if (prevSlot != -1) {
            InvUtils.setInvSlot(prevSlot);
            onEnable();
        }
    }

    private final Predicate<ItemStack> stackPred = item -> {
        if (positivePots.isEnabled() && ((!mc.player.hasStatusEffect(StatusEffects.SPEED) && InvUtils.isThatSplash(StatusEffects.SPEED, item)) || (!mc.player.hasStatusEffect(StatusEffects.STRENGTH) && InvUtils.isThatSplash(StatusEffects.STRENGTH, item)) || (!mc.player.hasStatusEffect(StatusEffects.REGENERATION) && InvUtils.isThatSplash(StatusEffects.REGENERATION, item)))) {
            return true;
        }

        if (Math.min((mc.player.getHealth() + mc.player.getAbsorptionAmount()) / mc.player.getMaxHealth(), 1.0f) <= minHealth.getFValue()) {
            if (healthPots.isEnabled() && InvUtils.isThatSplash(StatusEffects.INSTANT_HEALTH, item)) {
                return true;
            }
            Item itemAsItem = item.getItem();
            if (heads.isEnabled() && itemAsItem == Items.PLAYER_HEAD) {
                return true;
            }
            return soups.isEnabled() && (itemAsItem == Items.MUSHROOM_STEW || itemAsItem == Items.RABBIT_STEW || itemAsItem == Items.SUSPICIOUS_STEW || itemAsItem == Items.BEETROOT_SOUP);
        }
        
        return false;
    };

    public static boolean stopAA() {
        if (MinecraftClient.getInstance().player != null) {
            AutoHealModule ahm = Template.moduleManager.getModule(AutoHealModule.class);
            if (ahm != null && ahm.isEnabled() && ahm.disableAA.isEnabled()) {
                return ahm.stackPred.test(MinecraftClient.getInstance().player.getMainHandStack());
            }
        }
        return false;
    }
}
