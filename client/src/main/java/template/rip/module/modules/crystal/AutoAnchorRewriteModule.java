package template.rip.module.modules.crystal;

import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.Collections;

public class AutoAnchorRewriteModule extends Module {

    public enum anchorStage implements EnumIncr<anchorStage> {Initial, SwitchAnchor, PlaceAnchor, SwitchGlowstone, PlaceGlowstone, SwitchSafe, PlaceSafe, SwitchDetonate, DetonateAnchor, SwitchAirGlowstone, PlaceAirGlowstone, SwitchAirDetonate, DetonateAirAnchor, None}
    public final MinMaxNumberSetting chargeDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Charge delays");
    public final MinMaxNumberSetting detonationDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Detonation delays");
    public final MinMaxNumberSetting slotDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Switch slot delays");
    public final NumberSetting detonationSlot = new NumberSetting(this, 5, 1, 9, 1d, "Slot to detonate with");
    public final KeybindSetting activateKey = new KeybindSetting(this, GLFW.GLFW_MOUSE_BUTTON_2, "Activate Key");
    public final RegistrySetting<Item> itemWhitelist = new RegistrySetting<>(Collections.singletonList(Items.RESPAWN_ANCHOR), this, Registries.ITEM, "Activation item whitelist").setAdvanced();
    public final DividerSetting safeAnchorDivider = new DividerSetting(this, false, "Safe Anchor");
    public enum safeAnchorModeEnum {NeighborBlock, Sneak, Off}
    public final ModeSetting<safeAnchorModeEnum> safeAnchorMode = new ModeSetting<>(this, safeAnchorModeEnum.Off, "Safe Anchor Place Mode");
    public final BooleanSetting onlyOnKey = new BooleanSetting(this, false, "Only Safe Anchor On Key");
    public final KeybindSetting safeAnchorBind = new KeybindSetting(this, -1, false, "Safe Anchor Bind");
    public final MinMaxNumberSetting safePlaceDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Safe Place delays");
    public enum safeBlocksMode {GlowStone, Obsidian}
    public final ModeSetting<safeBlocksMode> safeAnchorBlockMode = new ModeSetting<>(this, safeBlocksMode.GlowStone, "Safe Anchor Block Mode");
    private final EnumHolder<anchorStage> currentStage = EnumHolder.get(anchorStage.None);
    private int chargeCooldown, detonationCooldown, slotCooldown, safeCooldown, sneakTicks, lastSneakTick;
    private boolean hadUnchargedAnchor;
    private AirAnchorModule cache;
    private Boolean airPlacing;
    private BlockPos anchorPos;
    private int slotCache = -1;

    public AutoAnchorRewriteModule(Category category, Description description, String name) {
        super(category, description, name);
        safeAnchorDivider.addSetting(safeAnchorMode, onlyOnKey, safeAnchorBind, safePlaceDelays, safeAnchorBlockMode);
    }

    @Override
    public void onEnable() {
        internalEnable();
        currentStage.setValue(anchorStage.None);
    }

    private void internalEnable() {
        if (!nullCheck() || InvUtils.getItemSlot(Items.RESPAWN_ANCHOR) == -1 || InvUtils.getItemSlot(Items.GLOWSTONE) == -1) {
            currentStage.setValue(anchorStage.None);
            return;
        }
        currentStage.setValue(anchorStage.Initial.increment());
        chargeCooldown = chargeDelays.getRandomInt();
        detonationCooldown = detonationDelays.getRandomInt();
        slotCooldown = slotDelays.getRandomInt();
        safeCooldown = safePlaceDelays.getRandomInt();
        hadUnchargedAnchor = false;
        sneakTicks = 0;
        lastSneakTick = -1;
        airPlacing = null;
        anchorPos = null;
        KeyBinding use = mc.options.useKey;
        use.setPressed(false);
    }

    @EventHandler
    private void onMousePress(MousePressEvent event) {
        onKeyPress(new KeyPressEvent(event.button, 0, event.action, 0));
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        if (event.key != activateKey.getCode()) {
            return;
        }
        switch (event.action) {
            case GLFW.GLFW_PRESS -> {
                if (mc.player != null && itemWhitelist.selected.contains(mc.player.getMainHandStack().getItem())) {
                    internalEnable();
                }
            }
            case GLFW.GLFW_RELEASE -> currentStage.setValue(anchorStage.None);
        }
    }

    @EventHandler
    private void onSwitchSlot(SwitchSlotEvent event) {
        if (!nullCheck()) {
            return;
        }

        if (!currentStage.is(anchorStage.None) && !currentStage.is(anchorStage.DetonateAnchor) && !currentStage.is(anchorStage.DetonateAirAnchor)) {
            event.switchSlot(slotCache);
        } else {
            slotCache = -1;
        }

        if (slotCooldown > 0) {
            slotCooldown--;
            return;
        }

        slotCooldown = slotDelays.getRandomInt();
        int slotTo = -999;
        Boolean tryingToAirPlace = null;

        if (cache == null) {
            cache = Template.moduleManager.getModule(AirAnchorModule.class);
        }

        if (currentStage.is(anchorStage.SwitchSafe)) {
            int safeSlot = InvUtils.getItemSlot(switch (safeAnchorBlockMode.getMode()) {
                case Obsidian -> Items.OBSIDIAN;
                case GlowStone -> Items.GLOWSTONE;
            });
            boolean isSafe = !safeAnchorMode.is(safeAnchorModeEnum.Off) && safeSlot != -1 && (!onlyOnKey.isEnabled() || safeAnchorBind.isPressed());
            if (isSafe) {
                slotTo = safeSlot;
            } else {
                currentStage.increment();
                currentStage.increment();
            }
        }

        if (slotTo == -999) {
            slotTo = switch (currentStage.getValue()) {
                case SwitchAnchor -> InvUtils.getItemSlot(Items.RESPAWN_ANCHOR);
                case SwitchGlowstone, SwitchAirGlowstone -> InvUtils.getItemSlot(Items.GLOWSTONE);
                case SwitchDetonate -> {
                    tryingToAirPlace = (airPlacing == null && cache != null && cache.isEnabled() && InvUtils.getItemSlot(Items.RESPAWN_ANCHOR) != -1 && MathUtils.getRandomInt(0, 100) <= cache.airPlaceChance.getIValue());
                    yield tryingToAirPlace ? InvUtils.getItemSlot(Items.RESPAWN_ANCHOR) : detonationSlot.getIValue() - 1;
                }
                case SwitchAirDetonate -> detonationSlot.getIValue() - 1;
                default -> -1;
            };
        }

        if (event.switchSlot(slotTo)) {
            slotCache = slotTo;
            if (tryingToAirPlace != null) {
                airPlacing = tryingToAirPlace;
            }
            currentStage.increment();
        }
    }

    @EventHandler
    private void onPlayerInput(InputEvent event) {
        if (!nullCheck()) {
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult hr && hr.getType() == HitResult.Type.BLOCK)) {
            return;
        }

        if (currentStage.is(anchorStage.PlaceSafe) && safeAnchorMode.is(safeAnchorModeEnum.Sneak) && (BlockUtils.isAnchorCharged(hr.getBlockPos()) || mc.player.age - lastSneakTick < 10)) {
            if (sneakTicks > 20) {
                currentStage.increment();
            }
            event.input.playerInput = InputUtil.setSneaking(event.input.playerInput, true);
            lastSneakTick = mc.player.age;
            sneakTicks++;
        }
    }

    @EventHandler
    private void onHandleInputs(HandleInputEvent.Pre event) {
        BlockHitResult bhr = mc.crosshairTarget instanceof BlockHitResult hr && hr.getType() == HitResult.Type.BLOCK ? hr : null;
        if (!nullCheck() || bhr == null) {
            return;
        }

        BlockPos bpos = bhr.getBlockPos();
        hadUnchargedAnchor = BlockUtils.isAnchorUncharged(bpos);
        if (currentStage.is(anchorStage.PlaceSafe)) {
            if (safeAnchorMode.is(safeAnchorModeEnum.Off)) {
                currentStage.increment();
                return;
            } else {
                if (safeCooldown > 0) {
                    safeCooldown--;
                    return;
                }
                switch (safeAnchorMode.getMode()) {
                    case Sneak -> {
                        if (mc.player.isSneaking() && BlockUtils.isAnchorCharged(bpos)) {
                            if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                            ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                            if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                                mc.player.swingHand(Hand.MAIN_HAND);
                            }
                            currentStage.increment();
                            return;
                        }
                    }
                    case NeighborBlock -> {
                        if (anchorPos != null && bhr.getBlockPos().getManhattanDistance(anchorPos) != 0) {
                            if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                            ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                            if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                                mc.player.swingHand(Hand.MAIN_HAND);
                            }
                            currentStage.increment();
                            return;
                        }
                    }
                }
            }
        }

        switch (currentStage.getValue()) {
            case PlaceAnchor -> {
                if (mc.world.getBlockState(bpos).getBlock() != Blocks.RESPAWN_ANCHOR) {
                    if (mc.world.getBlockState(bpos).isReplaceable() && mc.world.getBlockState(bpos).getBlock() != Blocks.FIRE) {
                        return;
                    }

                    if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                    ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    if ((ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) || mc.world.getBlockState(bpos).getBlock() == Blocks.FIRE) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        currentStage.increment();
                    }
                } else {
                    currentStage.increment();
                }
            }
            case PlaceGlowstone, PlaceAirGlowstone -> {
                if (!hadUnchargedAnchor && currentStage.is(anchorStage.PlaceAirGlowstone)) {
                    return;
                }

                if (chargeCooldown > 0) {
                    chargeCooldown--;
                    return;
                }
                chargeCooldown = chargeDelays.getRandomInt();

                if (BlockUtils.isAnchorUncharged((anchorPos = bpos))) {
                    if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                    ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        currentStage.increment();
                    }
                } else {
                    currentStage.increment();
                }
            }
            case DetonateAnchor -> {
                if (detonationCooldown > 0) {
                    detonationCooldown--;
                    return;
                }
                detonationCooldown = detonationDelays.getRandomInt();

                if (BlockUtils.isAnchorCharged(bpos)) {
                    if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                    ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if (airPlacing) {
                        if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                        ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                        if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }
                        currentStage.increment();
                    }
                }
            }
            case DetonateAirAnchor -> {
                if (detonationCooldown > 0) {
                    detonationCooldown--;
                    return;
                }
                detonationCooldown = detonationDelays.getRandomInt();

                if (BlockUtils.isAnchorCharged(bhr.getBlockPos())) {
                    if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                    ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        currentStage.increment();
                    }
                }
            }
        }
    }
}
