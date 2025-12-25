package template.rip.module.modules.crystal;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.misc.AutoArmorModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.ModeSetting;

import java.util.function.Predicate;

public class ElytraMacroModule extends Module {

    public enum modeEnum {Macro, Swap, InventorySwap}
    public final ModeSetting<modeEnum> modeSetting = new ModeSetting<>(this, modeEnum.Swap, "Mode");
    public final BooleanSetting inventoryFirework = new BooleanSetting(this, false, "Inventory Firework");
    public final KeybindSetting enableKey = new KeybindSetting(this, -1, "Enable Key");

    private boolean pressed, fireWorked, elytra;
    private int lastElySlot, lastSlot, offGroundTicks, lastFireworkSlot;
    private final Predicate<Item> pred = i -> InvUtils.isEquippable(i.getDefaultStack(), EquipmentSlot.CHEST);
    private final Predicate<ItemStack> stackPred = i -> pred.test(i.getItem());

    public ElytraMacroModule(Category category, Description description, String name) {
        super(category, description, name);
        inventoryFirework.addConditionMode(modeSetting, modeEnum.Macro);
    }

    @Override
    public void onEnable() {
        pressed = false;
        elytra = false;
        fireWorked = false;
        lastSlot = -1;
        lastElySlot = -1;
        lastFireworkSlot = -1;
        offGroundTicks = 0;
    }

    private boolean canEnable() {
        return switch (modeSetting.getMode()) {
            case Macro -> InvUtils.slotOfItem(Items.ELYTRA) != -1 && (InvUtils.getItemSlot(Items.FIREWORK_ROCKET) != -1 || (inventoryFirework.isEnabled() && InvUtils.slotOfItem(Items.FIREWORK_ROCKET) != -1)) && mc.player.currentScreenHandler.getCursorStack().isEmpty();
            case Swap, InventorySwap -> -1 != (mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) ? InvUtils.slotOfItem(stackPred) : InvUtils.slotOfItem(Items.ELYTRA));
        };
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!nullCheck() || event.check || !pressed)
            return;

        switch (modeSetting.getMode()) {
            case Macro -> {
                if (!mc.player.getInventory().armor.get(2).isOf(Items.ELYTRA)) {
                    if (!(mc.currentScreen instanceof InventoryScreen is)) {
                        mc.setScreen(new InventoryScreen(mc.player));
                    } else {
                        if (is.getScreenHandler().getCursorStack().isEmpty()) {
                            lastElySlot = InvUtils.slotOfItem(Items.ELYTRA);
                            InvUtils.click().from(lastElySlot).to(lastElySlot);
                            InvUtils.click().fromArmor(2).toArmor(2);
                            InvUtils.click().from(lastElySlot).to(lastElySlot);
                            is.close();
                        }
                    }
                } else if (!mc.player.canGlide()) {
                    event.input.movementForward = 1f;
                    event.input.playerInput = InputUtil.setJumping(event.input.playerInput, offGroundTicks == 0 || offGroundTicks > 3);
                } else if (!mc.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET) && !fireWorked) {
                    if (inventoryFirework.isEnabled()) {
                        if (!(mc.currentScreen instanceof InventoryScreen is)) {
                            mc.setScreen(new InventoryScreen(mc.player));
                        } else {
                            if (is.getScreenHandler().getCursorStack().isEmpty()) {
                                lastFireworkSlot = InvUtils.slotOfItem(Items.FIREWORK_ROCKET);
                                InvUtils.click().from(lastFireworkSlot).to(lastFireworkSlot);
                                InvUtils.click().from(mc.player.getInventory().selectedSlot).to(mc.player.getInventory().selectedSlot);
                                InvUtils.click().from(lastFireworkSlot).to(lastFireworkSlot);
                                is.close();
                            }
                        }
                    } else if (SlotUtils.isHotbar(InvUtils.getItemSlot(Items.FIREWORK_ROCKET))) {
                        lastSlot = mc.player.getInventory().selectedSlot;
                        InvUtils.setInvSlot(InvUtils.getItemSlot(Items.FIREWORK_ROCKET));
                    }
                }
            }
            case Swap -> {
                if (lastSlot == -1) {
                    elytra = !mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
                    lastSlot = mc.player.getInventory().selectedSlot;
                } else {
                    boolean bl = switch (Boolean.toString(elytra)) {
                        case "true" -> {
                            if (mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                                yield true;
                            } else if (!mc.player.getMainHandStack().isOf(Items.ELYTRA)) {
                                if (InvUtils.hasItemInHotbar(Items.ELYTRA)) {
                                    InvUtils.setInvSlot(InvUtils.getItemSlot(Items.ELYTRA));
                                } else {
                                    yield true;
                                }
                            } else {
                                if (Template.isClickSim())
                                    MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                                ActionResult ar = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                if (ar.isAccepted() || PlayerUtils.shouldSwingHand(ar)) {
                                    mc.player.swingHand(Hand.MAIN_HAND);
                                }
                            }
                            yield false;
                        }
                        case "false" -> {
                            if (pred.test(mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem())) {
                                yield true;
                            } else if (!pred.test(mc.player.getMainHandStack().getItem())) {
                                if (InvUtils.getItemSlot(pred) != -1) {
                                    InvUtils.setInvSlot(InvUtils.getItemSlot(pred));
                                } else {
                                    yield true;
                                }
                            } else {
                                if (Template.isClickSim())
                                    MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                                ActionResult ar = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                if (ar.isAccepted() || PlayerUtils.shouldSwingHand(ar)) {
                                    mc.player.swingHand(Hand.MAIN_HAND);
                                }
                            }
                            yield false;
                        }
                        default -> false;
                    };
                    if (bl) {
                        InvUtils.setInvSlot(lastSlot);
                        onEnable();
                    }
                }
            }
            case InventorySwap -> {
                if (lastSlot == -1) {
                    elytra = !mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
                    lastSlot = mc.player.getInventory().selectedSlot;
                } else {
                    boolean bl = switch (Boolean.toString(elytra)) {
                        case "true" -> {
                            if (!(mc.currentScreen instanceof InventoryScreen)) {
                                mc.setScreen(new InventoryScreen(mc.player));
                            } else {
                                if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
                                    InvUtils.shiftClick().fromArmor(2).toArmor(2);
                                } else {
                                    int i = InvUtils.slotOfItem(Items.ELYTRA);
                                    InvUtils.shiftClick().from(i).to(i);
                                    yield true;
                                }
                            }
                            yield false;
                        }
                        case "false" -> {
                            if (!(mc.currentScreen instanceof InventoryScreen)) {
                                mc.setScreen(new InventoryScreen(mc.player));
                            } else {
                                if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
                                    InvUtils.shiftClick().fromArmor(2).toArmor(2);
                                } else {
                                    int i = InvUtils.slotOfItem(stackPred);
                                    InvUtils.shiftClick().from(i).to(i);
                                    yield true;
                                }
                            }
                            yield false;
                        }
                        default -> false;
                    };
                    if (bl) {
                        onEnable();
                        mc.setScreen(null);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onInput(KeyPressEvent event) {
       if (nullCheck() && enableKey.isPressed() && canEnable()) {
           AutoArmorModule aam = Template.moduleManager.getModule(AutoArmorModule.class);
           if (aam != null && aam.isEnabled()) {
               return;
           }
           pressed = true;
       }
    }

    @EventHandler
    private void onPreTick(PlayerTickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (modeSetting.is(modeEnum.Macro)) {
            if (mc.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET) && !fireWorked) {
                if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                ActionResult ar = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (ar.isAccepted() || PlayerUtils.shouldSwingHand(ar)) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }

                fireWorked = true;

                if (SlotUtils.isHotbar(lastSlot) && !inventoryFirework.isEnabled()) {
                    InvUtils.setInvSlot(lastSlot);
                    lastSlot = -1;
                }
            }

            if (!mc.player.isOnGround()) {
                offGroundTicks++;
            } else {
                offGroundTicks = 0;
                if (fireWorked) {
                    pressed = false;
                    if (lastElySlot != -1) {
                        if (!(mc.currentScreen instanceof InventoryScreen is)) {
                            mc.setScreen(new InventoryScreen(mc.player));
                        } else {
                            if (is.getScreenHandler().getCursorStack().isEmpty()) {
                                InvUtils.click().from(lastElySlot).to(lastElySlot);
                                InvUtils.click().fromArmor(2).toArmor(2);
                                InvUtils.click().from(lastElySlot).to(lastElySlot);

                                is.close();
                                lastElySlot = -1;
                            }
                        }
                    } else if (inventoryFirework.isEnabled() && lastFireworkSlot != -1) {
                        if (!(mc.currentScreen instanceof InventoryScreen is)) {
                            mc.setScreen(new InventoryScreen(mc.player));
                        } else {
                            if (is.getScreenHandler().getCursorStack().isEmpty()) {
                                if (inventoryFirework.isEnabled()) {
                                    InvUtils.click().from(lastFireworkSlot).to(lastFireworkSlot);
                                    InvUtils.click().from(mc.player.getInventory().selectedSlot).to(mc.player.getInventory().selectedSlot);
                                    InvUtils.click().from(lastFireworkSlot).to(lastFireworkSlot);
                                }
                                is.close();
                                lastFireworkSlot = -1;
                            }
                        }

                    } else {
                        onEnable();
                    }
                }
            }
        }
    }
}
