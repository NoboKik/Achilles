package template.rip.module.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.NumberSetting;

import static template.rip.api.util.MathUtils.passedTime;

public class WebMacroRecodeModule extends Module {

    public final MinMaxNumberSetting placeDelay = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting goToPrevSlot = new BooleanSetting(this, true, "Go To Prev Slot");
    public final NumberSetting extraChance = new NumberSetting(this, 100d, 0d, 100d, 1d, "Extra place chance");
    public final BooleanSetting placeTNT = new BooleanSetting(this, false, "Place explosive on Web");
    public final BooleanSetting placeLava = new BooleanSetting(this, false, "Place Lava on Web");
    public final BooleanSetting smartLava = new BooleanSetting(this, Description.of("Only allows lava placement when the target is in a cobweb"), false, "Smart Lava Placement");
    public final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    private boolean placedTnt = false;
    private boolean placedWeb = false;
    private boolean placedLava = false;
    private int prevSlot = -1;
    private boolean pressed = false;
    private long timer = System.currentTimeMillis();

    public WebMacroRecodeModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        resetWeb();
    }

    private void resetWeb() {
        prevSlot = -1;
        pressed = false;
        placedWeb = false;
        placedTnt = false;
        placedLava = false;
        timer = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        if (mc.player != null)
            updateSlot(mc.player.getInventory().selectedSlot);
    }

    public void updateSlot(int slot) {
        if (mc.getNetworkHandler() == null || mc.player == null)
            return;

        InvUtils.setInvSlot(slot);
    }

    private int getTNTSlot() {
        int tntSlot = InvUtils.getItemSlot(Items.TNT);
        if (tntSlot != -1) return tntSlot;

        return InvUtils.getItemSlot(Items.CREEPER_SPAWN_EGG);
    }

    private boolean checkStack(ItemStack handStack) {
        return handStack.isOf(Items.TNT) || handStack.isOf(Items.CREEPER_SPAWN_EGG);
    }

    private boolean canPlace() {
        if (placeDelay.getMinValue() == 0 && placeDelay.getMaxValue() == 0)
            return true;
        if (passedTime(placeDelay.getRandomDouble(), timer)) {
            timer = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    private boolean canSwitch() {
        if (placeDelay.getMinValue() == 0 && placeDelay.getMaxValue() == 0)
            return true;
        if (passedTime(placeDelay.getRandomInt(), timer)) {
            timer = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    public boolean isDone() {
        if (placedWeb) {
            if ((placeLava.isEnabled() && InvUtils.hasItemInHotbar(Items.LAVA_BUCKET)) || (placeTNT.isEnabled() & getTNTSlot() != -1)) {
                return placedLava || placedTnt;
            }
            return true;
        }
        return false;
    }

    private void place(Hand hand) {
        if (mc.player == null || mc.interactionManager == null || mc.world == null)
            return;

        if (!(mc.crosshairTarget instanceof BlockHitResult) || mc.crosshairTarget.getType() == HitResult.Type.MISS)
            return;

        if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, hand, (BlockHitResult) mc.crosshairTarget);
        if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
            mc.player.swingHand(hand);
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (activateKey.isPressed()) {
            if (prevSlot == -1 && mc.player != null) {
                prevSlot = mc.player.getInventory().selectedSlot;
            }
            pressed = true;
        } else {
            pressed = false;
        }
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (mc.currentScreen != null || mc.player == null) return;

        if (!pressed) {
            if (prevSlot != -1 && (goToPrevSlot.isEnabled())) {
                if (canSwitch()) {
                    updateSlot(prevSlot);
                    prevSlot = -1;
                }
            } else {
                resetWeb();
            }
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || mc.crosshairTarget.getType() == HitResult.Type.MISS) {
            return;
        }

        if (!BlockUtils.isBlock(Blocks.COBWEB, bhr.getBlockPos()) && !placedWeb) {
            if (InvUtils.hasItemInHotbar(Items.COBWEB) || InvUtils.isHoldingItem(Items.COBWEB, true)) {
                if (mc.player.getMainHandStack().getItem() != Items.COBWEB && mc.player.getOffHandStack().getItem() != Items.COBWEB) {
                    if (canSwitch())
                        updateSlot(InvUtils.getItemSlot(Items.COBWEB));
                }
                if (canPlace()) {
                    if (mc.player.getMainHandStack().getItem() == Items.COBWEB) {
                        place(Hand.MAIN_HAND);
                        placedWeb = true;
                    } else if (mc.player.getOffHandStack().getItem() == Items.COBWEB) {
                        place(Hand.OFF_HAND);
                        placedWeb = true;
                    }
                }
                if (!placedWeb)
                    return;
            }
        }

        if (MathUtils.getRandomInt(0, 100) <= extraChance.getIValue()) {
            if (BlockUtils.isBlock(Blocks.COBWEB, bhr.getBlockPos()) && placedWeb && placeTNT.isEnabled() && !placedTnt && getTNTSlot() != -1) {
                if (!(checkStack(mc.player.getMainHandStack())) && canSwitch()) {
                    updateSlot(getTNTSlot());
                }
                if (checkStack(mc.player.getMainHandStack()) && canPlace()) {
                    place(Hand.MAIN_HAND);
                    placedTnt = true;
                }
                if (!placedTnt)
                    return;
            }

            if (BlockUtils.isBlock(Blocks.COBWEB, bhr.getBlockPos()) && placedWeb && !placedTnt && placeLava.isEnabled() && !placedLava) {
                if (InvUtils.hasItemInHotbar(Items.LAVA_BUCKET)) {
                    if (!(mc.player.getMainHandStack().getItem() == Items.LAVA_BUCKET) && canSwitch()) {
                        updateSlot(InvUtils.getItemSlot(Items.LAVA_BUCKET));
                    }
                    if (mc.player.getMainHandStack().getItem() == Items.LAVA_BUCKET) {
                        boolean playerIn = !smartLava.isEnabled();

                        if (!playerIn) {
                            LivingEntity le = PlayerUtils.findFirstLivingTargetOrNull();

                            if (le != null) {
                                if (mc.world.getBlockState(le.getBlockPos()).getBlock() instanceof CobwebBlock) {
                                    int dist = bhr.getBlockPos().getManhattanDistance(le.getBlockPos());
                                    if (dist == 0) {
                                        playerIn = true;
                                    }
                                }
                            }
                        }

                        if (bhr.getSide() == Direction.UP && playerIn && canPlace()) {
                            placedLava = true;

                            if (Template.isClickSim())
                                MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                            ActionResult interactionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
                                mc.player.swingHand(Hand.MAIN_HAND);
                            }
                        }
                    }
                    if (!placedLava)
                        return;
                }
            }

        }
//        if (silent.isEnabled() && disableTimer + 500 + MathUtils.getRandomInt(placeDelayMin.getIValue(), placeDelayMax.getIValue()) > System.currentTimeMillis())
//            return;
        // when using silent we won't get client side blocks which means we wait on the server, I cannot think of a better implementation that allows for secondary places
        // this is no longer an issue since we change the block client side

        if (isDone())
            pressed = false;
    }
}
