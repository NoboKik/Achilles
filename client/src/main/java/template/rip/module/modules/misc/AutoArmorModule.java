package template.rip.module.modules.misc;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.screen.slot.Slot;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;

import static template.rip.api.util.MathUtils.passedTime;

public class AutoArmorModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Automatically clicks slots\nLegit: Only clicks slots you hover over"), modeEnum.Normal, "Mode");
    public final MinMaxNumberSetting swapDelay = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting workOnKey = new BooleanSetting(this, false, "Work On Key");
    public final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    public final BooleanSetting avoidBinding = new BooleanSetting(this, false, "Avoid Curse Of Binding");
    public final BooleanSetting autoClose = new BooleanSetting(this, false, "Auto close");

    private long timer = System.currentTimeMillis();
    private boolean equipping = false;

    public AutoArmorModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    @Override
    public void onEnable() {
        timer = System.currentTimeMillis();
        equipping = false;
    }

    @EventHandler
    private void onHUDRender(HudRenderEvent event) {
        if (mc.interactionManager == null || mc.player == null || !activateKey.isPressed() && workOnKey.isEnabled())
            return;

        if (mc.currentScreen instanceof InventoryScreen) {
            boolean anythingToEquip = false;

            //for (EquipmentType type : EquipmentType.values()) {
            for (EquipmentSlot type : EquipmentSlot.values()) {
                if (type == EquipmentSlot.BODY) {
                //if (type == EquipmentType.BODY) {
                    continue;
                }

                /*int armorSlot = switch (type) {
                    case HELMET -> 3;
                    case CHESTPLATE -> 2;
                    case LEGGINGS -> 1;
                    case BOOTS -> 0;
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };*/

                int armorSlot = switch (type) {
                    case HEAD -> 3;
                    case CHEST -> 2;
                    case LEGS -> 1;
                    case FEET -> 0;
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };

                ItemStack armorPiece = mc.player.getInventory().armor.get(armorSlot);
                Integer newArmorSlot = null;

                for (ItemStack stack : mc.player.getInventory().main) {
                    //if (stack.getItem() instanceof ArmorItem && ((ArmorItem)stack.getItem()).getType() == type) {
                    if (InvUtils.isEquippable(stack, type)) {
                        if (InvUtils.isBetter(armorPiece, stack, false, avoidBinding.isEnabled())) {
                            armorPiece = stack;
                            newArmorSlot = mc.player.getInventory().getSlotWithStack(stack);
                        }
                    }
                }

                if (newArmorSlot != null) {
                    int currentArmorSlot = currentItemSlot(newArmorSlot);
                    if (currentArmorSlot != -1 && InvUtils.isBetter(mc.player.getInventory().armor.get(armorSlot), armorPiece, false, avoidBinding.isEnabled())) {
                        if (canSwap()) {
                            if (!mc.player.getInventory().armor.get(armorSlot).isEmpty()) {
                                InvUtils.drop().fromArmor(armorSlot).toArmor(armorSlot);
                                return;
                            }
                            InvUtils.shiftClick().from(currentArmorSlot).to(currentArmorSlot);
                        }
                        equipping = true;
                        anythingToEquip = true;
                    }
                }

            }
            if (equipping && !anythingToEquip && autoClose.isEnabled() && mc.currentScreen instanceof HandledScreen<?>) {
                mc.currentScreen.close();
                equipping = false;
            }
        }
    }

    private boolean canSwap() {
        if (swapDelay.getMinValue() == 0 && swapDelay.getMaxValue() == 0)
            return true;
        if (passedTime(swapDelay.getRandomDouble(), timer)) {
            timer = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    private int currentItemSlot(int sl) {
        if (mc.currentScreen instanceof InventoryScreen) {
            if (mode.is(modeEnum.Normal))
                return sl;

            Slot slot = ((InventoryScreen)mc.currentScreen).focusedSlot;
            if (mode.is(modeEnum.Legit) && slot != null)
                if (sl == slot.getIndex())
                    return sl;
        }
        return -1;
    }
}
