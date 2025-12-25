package template.rip.deprecated;

import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.InvUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.NumberSetting;

import static template.rip.api.util.MathUtils.passedTime;

public class AutoAnchorOldModule extends Module {
    public final BooleanSetting charge = new BooleanSetting(this, true, "Charge anchors?");
    public final MinMaxNumberSetting chargeDelays = new MinMaxNumberSetting(this, 20, 60, 0, 500, 1, "Charge delays");
    public final BooleanSetting detonate = new BooleanSetting(this, true, "Detonate anchors?");
    public final MinMaxNumberSetting detonationDelays = new MinMaxNumberSetting(this, 60, 70, 0, 500, 1, "Detonation delays");
    public final MinMaxNumberSetting coolDown = new MinMaxNumberSetting(this, 200, 250, 0, 500, 1, "Per Anchor Delay");
    public final NumberSetting detonationSlot = new NumberSetting(this, 5, 1, 9, 1d, "Slot to detonate with");
    public final MinMaxNumberSetting slotDelays = new MinMaxNumberSetting(this, 15, 45, 0, 500, 1, "Switch slot delays");
//    public final BooleanSetting airPlaceDetonate = new BooleanSetting("AirPlace Detonate", this, false);
//    public final NumberSetting airPlaceChance = new NumberSetting("Air place Chance", this, 100, 0, 100, 1);
//    public final NumberSetting maxAirPlaces = new NumberSetting("Max Air Places", this, 2, 0, 10, 1);



    boolean hasAnchored = false;
    long chargeTimer = System.currentTimeMillis();
    long detonationTimer = System.currentTimeMillis();
    long switchTimer = System.currentTimeMillis();
    long cooldownTimer = System.currentTimeMillis();
    boolean charged = false;
    boolean detonated = false;
    boolean placed = false;
    boolean switched = false;

    public AutoAnchorOldModule() {
        super(Category.CRYSTAL, Description.of("Automatically charges and/or detonates respawn anchors\nKept in for fans of legacy auto anchor"), "AutoAnchorOld");
    }


    @Override
    public void onEnable() {
        hasAnchored = false;
        chargeTimer = System.currentTimeMillis();
        detonationTimer = System.currentTimeMillis();
        switchTimer = System.currentTimeMillis();
        cooldownTimer = System.currentTimeMillis();
        charged = false;
        detonated = false;
        switched = false;
        placed = false;
        mc.options.useKey.setPressed(KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()));
    }

    private void place() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null)
            return;

        if (!(mc.crosshairTarget instanceof BlockHitResult) || mc.crosshairTarget.getType() == HitResult.Type.MISS)
            return;


        if (Template.isClickSim())
            Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget);
        if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean canDo(MinMaxNumberSetting mmns, long timer) {
        if (mmns.getMinValue() == 0 && mmns.getMaxValue() == 0)
            return true;
        return passedTime(mmns.getRandomInt(), timer);
    }

    private boolean canSwitch() {
        if (canDo(slotDelays, switchTimer)) {
            switchTimer = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private boolean cooldownDone() {
        if (canDo(coolDown, cooldownTimer)) {
            cooldownTimer = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private boolean canDetonate() {
        if (canDo(detonationDelays, detonationTimer)) {
            detonationTimer = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private boolean canCharge() {
        if (canDo(chargeDelays, chargeTimer)) {
            chargeTimer = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.world == null || mc.player == null || mc.currentScreen != null)
            return;

        if (hasAnchored) {
            if (!cooldownDone()) {
                return;
            }
            onEnable();
        }

        if (!KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode())) {
            onEnable();
            return;
        }

        if (mc.player.isUsingItem())
            return;

        if (!(mc.crosshairTarget instanceof BlockHitResult) || mc.crosshairTarget.getType() == HitResult.Type.MISS)
            return;

        if (mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
            mc.options.useKey.setPressed(false);
        }

        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

        /*if (!mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
            if (mc.player.isHolding(Items.RESPAWN_ANCHOR) && cooldownDone()) {
                place();
                placed = true;
            }
            if (!placed)
                return;

        }*/

        if (charge.isEnabled()) {
            if (BlockUtils.isAnchorUncharged(pos)) {
                if (InvUtils.hasItemInHotbar(Items.GLOWSTONE)) {
                    if (!mc.player.isHolding(Items.GLOWSTONE)) {
                        if (canSwitch())
                            InvUtils.setInvSlot(InvUtils.getItemSlot(Items.GLOWSTONE));
                    }
                    if (mc.player.isHolding(Items.GLOWSTONE)) {
                        if (canCharge()) {
                            place();
                            charged = true;
                        }
                    }
                    if (!charged)
                        return;
                }
            } else {
                chargeTimer = System.currentTimeMillis();
            }
        }


        if (BlockUtils.isAnchorCharged(pos)) {
            int anchor = InvUtils.getItemSlot(Items.RESPAWN_ANCHOR);
            int detSlot = /*airPlaceDetonate.isEnabled() && anchor != -1 ? anchor : */detonationSlot.getIValue() - 1;
            if (mc.player.getInventory().selectedSlot != detSlot) {
                if (canSwitch() && !switched) {
                    InvUtils.setInvSlot(detSlot);
                    switched = true;
                }
            }
            if (!switched)
                return;
            if (detonate.isEnabled()) {
                if (mc.player.getInventory().selectedSlot == detSlot) {
                    if (canDetonate()) {
                        place();
                        detonated = true;

                        /*if (airPlaceDetonate.isEnabled() && MathUtils.getRandomInt(0, 100) <= airPlaceChance.value && mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR) && mc.crosshairTarget instanceof BlockHitResult blockHitResult && BlockUtils.isAnchorCharged(blockHitResult.getBlockPos())) {

                            if (currentBlockPos != null && blockHitResult.getBlockPos().getManhattanDistance(currentBlockPos) == 0) {
                                if (count >= maxAirPlaces.getIValue()) return;
                            } else {
                                currentBlockPos = blockHitResult.getBlockPos();
                                count = 0;
                            }

                            if (Template.isClickSim())
                                Template.mouseSimulation().mouseClick(mc.options.useKey.boundKey.getCode());
                            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, 0));
                            mc.player.swingHand(Hand.MAIN_HAND);
                            count++;

                        }*/

                    }
                }
                if (!detonated)
                    return;
            }
        } else {
            detonationTimer = System.currentTimeMillis();
        }

        hasAnchored = true;
    }
}