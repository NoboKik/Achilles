package template.rip.module.modules.misc;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.events.SetScreenEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.Objects;

import static template.rip.api.util.MathUtils.passedTime;

public class ChestStealerModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Automatically clicks slots\nLegit: Only clicks slots you hover over"), modeEnum.Normal, "Mode");

    public final MinMaxNumberSetting startDelay = new MinMaxNumberSetting(this, 0, 0, 0, 500, 1, "Start Delays");
    public final MinMaxNumberSetting swapDelay = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting keepSame = new BooleanSetting(this, false, "Keep combinable items").setAdvanced();
    public final BooleanSetting workOnKey = new BooleanSetting(this, false, "Work On Key");
    public final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    public final BooleanSetting autoClose = new BooleanSetting(this, false, "Auto close");
    private final NumberSetting minDura = new NumberSetting(this, 10d, 0d, 100d, 1d, "Minimum durability").setAdvanced();
    public enum checkTitleEnum{Off, Contains, Equals}
    public final ModeSetting<checkTitleEnum> checkTitle = new ModeSetting<>(this, checkTitleEnum.Off, "Check Storage Title");

    public final StringSetting titleString = new StringSetting("Chest", this, "Title String");
    private long timer = System.currentTimeMillis();
    private long timer2 = System.currentTimeMillis();
    private boolean stealing = false;

    public ChestStealerModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        timer = System.currentTimeMillis();
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    public boolean hasEmptySlots() {
        return mc.player.getInventory().getEmptySlot() != -1;
    }

    private boolean canSwap() {
        if (!passedTime(startDelay.getRandomDouble(), timer2))
            return false;
        if (swapDelay.getMinValue() == 0 && swapDelay.getMaxValue() == 0)
            return true;
        if (passedTime(swapDelay.getRandomDouble(), timer)) {
            timer = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    private void onScreenSet(SetScreenEvent event) {
        if (!(event.screen instanceof GenericContainerScreen)) return;
        timer2 = System.currentTimeMillis();
    }

    // we can click whenever we want
    @EventHandler
    private void onRender(HudRenderEvent event) {
        if (mc.interactionManager == null || !activateKey.isPressed() && workOnKey.isEnabled())
            return;

        if (mc.currentScreen instanceof GenericContainerScreen cs) {
            if (!cs.getTitle().getString().contains(titleString.getContent()) && checkTitle.is(checkTitleEnum.Contains))
                return;

            if (!cs.getTitle().getString().equals(titleString.getContent()) && checkTitle.is(checkTitleEnum.Equals))
                return;

            for (int i = 0; i < cs.getScreenHandler().slots.size(); i++) {
                for (Item item : InvUtils.desirableStackItems()) {
                    Integer sl = InvUtils.betterItemSlot(cs, keepSame.isEnabled(), item, minDura.getIValue(), null, true);
                    if (hasEmptySlots() && sl != null) {
                        stealing = true;
                        if (currentItemSlot(sl) != -1 && canSwap()) {
                            move(cs, sl);
                        }
                    }
                }

                for (Class clas : InvUtils.desirableItems()) {
                    Integer sl = InvUtils.betterItemSlot(cs, keepSame.isEnabled(), clas, minDura.getIValue(), null, true);
                    if (hasEmptySlots() && sl != null) {
                        stealing = true;
                        if (currentItemSlot(sl) != -1 && canSwap()) {
                            move(cs, sl);
                        }
                    }
                }

                for (EquipmentSlot type : EquipmentSlot.values()) {
                //for (EquipmentType type : EquipmentType.values()) {
                    Integer sl = InvUtils.betterItemSlot(cs, keepSame.isEnabled(), ArmorItem.class, minDura.getIValue(), type, true);
                    if (hasEmptySlots() && sl != null) {
                        stealing = true;
                        if (currentItemSlot(sl) != -1 && canSwap()) {
                            move(cs, sl);
                        }
                    }
                }

            }
            if (autoClose.isEnabled() && mc.currentScreen instanceof HandledScreen<?> && !stealing) {
                mc.currentScreen.close();
            }
            stealing = false;
        }
    }

    private void move(HandledScreen<?> sh, int slot) {
        mc.interactionManager.clickSlot(sh.getScreenHandler().syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    private int currentItemSlot(Integer sl) {
        if (mc.currentScreen instanceof GenericContainerScreen cs) {
            if (mode.is(modeEnum.Normal) && sl != null && sl < cs.getScreenHandler().getInventory().size())
                return sl;

            Slot slot = cs.focusedSlot;
            if (mode.is(modeEnum.Legit) && slot != null && Objects.equals(sl, slot.getIndex()))
                return slot.getIndex();
        }
        return -1;
    }
}
