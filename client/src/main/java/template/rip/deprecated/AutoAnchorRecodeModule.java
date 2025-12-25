package template.rip.deprecated;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.blatant.MultiTaskModule;
import template.rip.module.setting.settings.*;

public class AutoAnchorRecodeModule extends Module {
    public final BooleanSetting charge = new BooleanSetting(this, true, "Charge anchors?");
    public final MinMaxNumberSetting chargeDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Charge delays");
    public final BooleanSetting detonate = new BooleanSetting(this, true, "Detonate anchors?");
    public final MinMaxNumberSetting detonationDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Detonation delays");
    public final MinMaxNumberSetting coolDown = new MinMaxNumberSetting(this, 5, 10, 0, 40, 1, "Per Anchor Delay");
    public final NumberSetting detonationSlot = new NumberSetting(this, 5, 1, 9, 1d, "Slot to detonate with");
    public final MinMaxNumberSetting slotDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Switch slot delays");
    public final BooleanSetting airPlaceDetonate = new BooleanSetting(this, false, "AirPlace Detonate");

    public final DividerSetting airPlace = new DividerSetting(this, false, "AirPlace");
    public final BooleanSetting onlyOnKey = new BooleanSetting(this, false, "Only AirPlace On Key");
    public final KeybindSetting onlyOnSaidKey = new KeybindSetting(this, -1, false, "Air Anchor Bind");
    public final NumberSetting airPlaceChance = new NumberSetting(this, 100, 0, 100, 1, "AirPlace Chance");

    public final BooleanSetting safeAnchorPlace = new BooleanSetting(this, false, "Safe Anchor Place");
    public final DividerSetting safeAnchor = new DividerSetting(this, false, "Safe Anchor");
    public final MinMaxNumberSetting safeAnchorDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Safe Anchor delays");
    public enum safeMode {GlowStone, Obsidian}
    public final ModeSetting<safeMode> safeAnchorBlockMode = new ModeSetting<>(this, safeMode.GlowStone, "Safe Anchor Block Mode");
    public final BooleanSetting safeAnchorOnKey = new BooleanSetting(this, false, "Only Safe Anchor on Key");
    public final KeybindSetting onlyOnSaidSafeAnchorKey = new KeybindSetting(this, -1, false, "Safe Anchor Bind");

    int placeClock, switchClock, chargeClock, detonateClock, count, safeClock, sneakingTicks;
    boolean hasAnchored, placed, charged, detonated, switched, doSneak, safePlaced;
    Boolean airAnchor;
    BlockPos currentBlockPos;

    public AutoAnchorRecodeModule() {
        super(Category.CRYSTAL, Description.of("Automatically sets your spawn point with respawn anchors\nOnly use this in the nether! (If it isn't obvious, this description is a joke)"), "AutoAnchor");
        airPlace.addSetting(onlyOnKey, onlyOnSaidKey, airPlaceChance);
        safeAnchor.addSetting(safeAnchorDelays, safeAnchorBlockMode, safeAnchorOnKey, onlyOnSaidSafeAnchorKey);
    }


    @Override
    public void onEnable() {
        placeClock = 0;
        hardReset();
        reset();
    }

    private void hardReset() {
        placed = false;
        charged = false;
        detonated = false;
        switched = false;
        hasAnchored = false;
        airAnchor = null;
        doSneak = false;
        safePlaced = false;
        sneakingTicks = 0;
    }

    private void reset() {
        chargeClock = chargeDelays.getRandomInt();
        detonateClock = detonationDelays.getRandomInt();
        safeClock = safeAnchorDelays.getRandomInt();
    }

    private boolean canSwitch() {
        if (switchClock > 0) {
            switchClock--;
            return false;
        }
        switchClock = slotDelays.getRandomInt();
        return true;
    }

    @EventHandler
    private void onPlayerInput(InputEvent event) {
        if (!nullCheck()) {
            return;
        }
        if (!event.check) {
            return;
        }
        if (safePlaced) {
            return;
        }
        if (!doSneak) {
            return;
        }
        if (sneakingTicks > 20) {// fallback
            safePlaced = true;
            return;
        }
        sneakingTicks++;
        InputUtil.setSneaking(event.input.playerInput, true);
    }

    @EventHandler
    private void onHandleInputs(HandleInputEvent.Pre event) {
        if (!canDo(true)) {
            return;
        }
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || mc.crosshairTarget.getType() == HitResult.Type.MISS) {
            return;
        }
        BlockPos pos = bhr.getBlockPos();
        if (!mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR) && !placed) {
//            print("place");
            if (mc.player.isHolding(Items.RESPAWN_ANCHOR)) {
//                print("place s");
                place(bhr);
                placed = true;

                placeClock = coolDown.getRandomInt();
                reset();

                mc.options.useKey.timesPressed = 0;//prevent double anchor placements
            }
            if (!placed || !mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
                return;
            }
        }

        if (charge.isEnabled()
                && BlockUtils.isAnchorUncharged(pos)
                && InvUtils.hasItemInHotbar(Items.GLOWSTONE)) {
//            print("charge");
            if (!mc.player.isHolding(Items.GLOWSTONE)) {
                if (canSwitch())
                    InvUtils.setInvSlot(InvUtils.getItemSlot(Items.GLOWSTONE));
            }
            if (mc.player.isHolding(Items.GLOWSTONE)) {
                if (chargeClock > 0) {
                    chargeClock--;
                    return;
                }
//                print("charge s");
                place(bhr);
                charged = true;
                reset();
            }
            if (!charged) {
//                print("charge r");
                return;
            }
        }

        if (BlockUtils.isAnchorCharged(pos)) {
            Item safeBlock = switch (safeAnchorBlockMode.getMode()) {
                case Obsidian -> Items.OBSIDIAN;
                case GlowStone -> Items.GLOWSTONE;
            };
            int i;
            if (safeAnchorPlace.isEnabled() && (!safeAnchorOnKey.isEnabled() || onlyOnSaidSafeAnchorKey.isPressed()) && !safePlaced && SlotUtils.isHotbar((i = InvUtils.getItemSlot(safeBlock)))) {
                doSneak = true;

                if (!mc.player.isHolding(safeBlock)) {
                    if (canSwitch())
                        InvUtils.setInvSlot(i);
                }
                if (mc.player.isHolding(safeBlock) && mc.player.isSneaking()) {
                    if (safeClock > 0) {
                        safeClock--;
                        return;
                    }

                    if (Template.isClickSim())
                        MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                    ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }

                    safePlaced = true;
                }

            } else {
                doSneak = false;
                int anchor = InvUtils.getItemSlot(Items.RESPAWN_ANCHOR);
                if (airAnchor == null) {
                    airAnchor = airPlaceDetonate.isEnabled()
                            && MathUtils.getRandomInt(0, 100) <= airPlaceChance.value
                            && anchor != -1
                            && (!onlyOnKey.isEnabled() || onlyOnSaidKey.isPressed());

                }
                int detSlot = airAnchor ? anchor : detonationSlot.getIValue() - 1;
                if (canSwitch()) {
                    if (detSlot != -1 && mc.player.getInventory().selectedSlot != detSlot) {
                        InvUtils.setInvSlot(detSlot);
                        switched = true;
                    }
                }
                if (!switched)
                    return;
                if (detonate.isEnabled()) {
//                print("detonate");
                    if (mc.player.getInventory().selectedSlot == detSlot) {
                        if (detonateClock > 0) {
                            detonateClock--;
                            return;
                        }
//                    print("detonate s");
                        place(bhr);
                        detonated = true;
                        reset();

                        if (!airAnchor || count >= 1) {
                            hasAnchored = true;
                        } else {
                            hasAnchored = true;
                            placeClock = 1;
                        }

                        if (count >= 1) {
                            count = 0;
                            airAnchor = null;
                            switched = false;
                            return;
                        }

                        if (airAnchor) {

                            do {
                                if (currentBlockPos != null && bhr.getBlockPos().getManhattanDistance(currentBlockPos) == 0) {
                                    if (count >= 1) break;
                                } else {
                                    currentBlockPos = bhr.getBlockPos();
                                    count = 0;
                                }

                                place(bhr);
                                count++;
                            } while (false);

                        }
                        airAnchor = null;
                        switched = false;
                    }
                }
            }
        }
    }

    private void print(Object o) {
        mc.inGameHud.getChatHud().addMessage(Text.of(String.valueOf(o)));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!canDo(false)) {
            return;
        }
        ItemStack is = mc.player.getMainHandStack();
        if (is.isEmpty()) {
            return;
        }
        boolean hasAnchor = is.isOf(Items.RESPAWN_ANCHOR) || is.isOf(Items.GLOWSTONE) || mc.player.getInventory().selectedSlot == detonationSlot.getIValue() - 1;
        if (!hasAnchor) {
            return;
        }
        mc.itemUseCooldown = 2;
    }

    private boolean canDo(boolean hasAnchoredTick) {
        if (!nullCheck()) {
            return false;
        }

        if (hasAnchoredTick && hasAnchored) {
            if (placeClock > 0) {
                placeClock--;
                return false;
            }
            hardReset();
            reset();
        }

        if (!KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode())) {
            reset();
            return false;
        }

        if (mc.player.isUsingItem()) {
            MultiTaskModule mtm = Template.moduleManager.getModule(MultiTaskModule.class);
            if (mtm == null || !mtm.isEnabled() || !mtm.item.isEnabled()) {
                reset();
                return false;
            }
        }

        return true;
    }

    private void place(BlockHitResult bhr) {
        if (!nullCheck())
            return;

        if (Template.isClickSim())
            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

}
