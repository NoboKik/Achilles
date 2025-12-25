package template.rip.module.modules.crystal;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.tuple.Triple;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.ExcludeMode;
import template.rip.api.util.InvUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class OffhandModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Automatically clicks slots\nLegit: Only clicks slots you hover over"), modeEnum.Normal, "Mode");

    public final BooleanSetting workOnKey = new BooleanSetting(this, false, "Work On Key");
    public final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");

    public final BooleanSetting autoInv = new BooleanSetting(this, true, "Auto open inventory");
    public final MinMaxNumberSetting swapDelay = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting shield = new BooleanSetting(this, false, "Offhand shields").setAdvanced();
    public final NumberSetting shieldHealth = new NumberSetting(this, 0.8, 0.0, 1.0, 0.01, "Health % for shield").setAdvanced();
    public final BooleanSetting totem = new BooleanSetting(this, true, "Offhand totems").setAdvanced();
    public final NumberSetting totemHealth = new NumberSetting(this, 1.0, 0.0, 1.0, 0.01, "Health % for totem").setAdvanced();
    public final BooleanSetting gapples = new BooleanSetting(this, false, "Offhand gapples").setAdvanced();
    public final NumberSetting gappleHealth = new NumberSetting(this, 0.6, 0.0, 1.0, 0.01, "Health % for gapples").setAdvanced();
    public final BooleanSetting tntcart = new BooleanSetting(this, false, "Offhand TNT carts").setAdvanced();
    public final NumberSetting tntcartHealth = new NumberSetting(this, 0.6, 0.0, 1.0, 0.01, "Health % for TNT carts").setAdvanced();
    private static final ThreadPoolExecutor tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
    private boolean opened = false;
    private long timer = System.currentTimeMillis();
    private Item lastItem = null;
    private Item currentItem = null;

    public OffhandModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        opened = false;
        reset();
        lastItem = null;
        currentItem = null;
    }

    private boolean canSwap() {
        if (swapDelay.getMinValue() == 0 && swapDelay.getMaxValue() == 0)
            return true;
        if (timer < System.currentTimeMillis()) {
            reset();
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        timer = System.currentTimeMillis() + swapDelay.getRandomInt();
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!activateKey.isPressed() && workOnKey.isEnabled()) {
            return;
        }

        tpe.execute(() -> currentItem = processItem());

        if (mc.player.getInventory().getStack(40).isOf(currentItem())) {
            reset();
        }

        if (mc.player != null && autoInv.isEnabled()) {
            Item i = currentItem();
            if (mc.player.getOffHandStack().getItem() == currentItem()) {
                HotbarRefillModule hrm = Template.moduleManager.getModule(HotbarRefillModule.class);
                if (opened && mc.currentScreen instanceof InventoryScreen && !(hrm != null && hrm.isEnabled() && hrm.totems.isEnabled() && !mc.player.getInventory().getStack(hrm.totemSlot.getIValue() - 1).isOf(Items.TOTEM_OF_UNDYING) && InvUtils.hasItemInInventory(Items.TOTEM_OF_UNDYING))) {
                    mc.currentScreen.close();
                    opened = false;
                }
            } else if (!opened && i != null && InvUtils.slotOfItem(i) != -1) {
                mc.setScreen(new InventoryScreen(mc.player));
                opened = true;
            }
        }

        if (mc.currentScreen instanceof InventoryScreen inventoryScreen) {
            PlayerInventory inventory = mc.player.getInventory();

            if (!inventory.getStack(40).isOf(currentItem())) {
                Integer slot = currentItemSlot(inventoryScreen.getScreenHandler(), inventoryScreen.focusedSlot);
                if (slot != null && canSwap()) {
                    if (slot < 9)
                        slot += 36;
                    mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, slot, 40, SlotActionType.SWAP, mc.player);
                    HotbarRefillModule hr = Template.moduleManager.getModule(HotbarRefillModule.class);
                    if (hr != null && hr.isEnabled()) {
                        hr.reset();
                    }
                }
            }
        }
    }

    private Integer currentItemSlot(ScreenHandler where, Slot focusedSlot) {
        if (mode.is(modeEnum.Normal)) {
            int ss = InvUtils.screenSlotOfItem(currentItem(), where, ExcludeMode.Offhand);
            if (ss != -1)
                return ss;
        }

        if (mode.is(modeEnum.Legit) && focusedSlot != null && focusedSlot.getStack().isOf(currentItem())) {
            return focusedSlot.getIndex();
        }
        return null;
    }

    private float healthPercent() {
        return Math.min((mc.player.getHealth() + mc.player.getAbsorptionAmount()) / mc.player.getMaxHealth(), 1.0f);
    }

    private Item processItem() {
        List<Triple<Item, Boolean, Double>> items = new ArrayList<>();
        items.add(Triple.of(Items.SHIELD, shield.isEnabled() && healthPercent() <= shieldHealth.value && (mc.player.getInventory().getStack(40).getItem() == Items.SHIELD || InvUtils.slotOfItem(Items.SHIELD) != -1) && !mc.player.getItemCooldownManager().isCoolingDown(Items.SHIELD.getDefaultStack()) && (PlayerUtils.findFirstLivingTargetOrNull() == null || !(PlayerUtils.findFirstLivingTargetOrNull().disablesShield())), shieldHealth.value));
        items.add(Triple.of(Items.TOTEM_OF_UNDYING, totem.isEnabled() && healthPercent() <= totemHealth.value && (mc.player.getInventory().getStack(40).getItem() == Items.TOTEM_OF_UNDYING || InvUtils.slotOfItem(Items.TOTEM_OF_UNDYING) != -1), totemHealth.value));
        items.add(Triple.of(Items.ENCHANTED_GOLDEN_APPLE, gapples.isEnabled() && healthPercent() <= gappleHealth.value && (mc.player.getInventory().getStack(40).getItem() == Items.ENCHANTED_GOLDEN_APPLE || InvUtils.slotOfItem(Items.ENCHANTED_GOLDEN_APPLE) != -1), gappleHealth.value + 0.1));
        items.add(Triple.of(Items.GOLDEN_APPLE, gapples.isEnabled() && healthPercent() <= gappleHealth.value && (mc.player.getInventory().getStack(40).getItem() == Items.GOLDEN_APPLE || InvUtils.slotOfItem(Items.GOLDEN_APPLE) != -1), gappleHealth.value));
        items.add(Triple.of(Items.TNT_MINECART, tntcart.isEnabled() && healthPercent() <= tntcartHealth.value && (mc.player.getInventory().getStack(40).getItem() == Items.TNT_MINECART || InvUtils.slotOfItem(Items.TNT_MINECART) != -1), tntcartHealth.value));

        items.sort(Comparator.comparing(t -> -t.getRight()));

        lastItem = null;
        for (Triple<Item, Boolean, Double> triple : items) {
            if (triple.getMiddle()) {
                lastItem = triple.getLeft();
            }
        }

        if (lastItem != null)
            return lastItem;

        items = new ArrayList<>();
        items.add(Triple.of(Items.SHIELD, shield.isEnabled() && (mc.player.getInventory().getStack(40).getItem() == Items.SHIELD || InvUtils.slotOfItem(Items.SHIELD) != -1) && !mc.player.getItemCooldownManager().isCoolingDown(Items.SHIELD.getDefaultStack()) && (PlayerUtils.findFirstLivingTargetOrNull() == null || !(PlayerUtils.findFirstLivingTargetOrNull().disablesShield())), shieldHealth.value));
        items.add(Triple.of(Items.TOTEM_OF_UNDYING, totem.isEnabled() && (mc.player.getInventory().getStack(40).getItem() == Items.TOTEM_OF_UNDYING || InvUtils.slotOfItem(Items.TOTEM_OF_UNDYING) != -1), totemHealth.value));
        items.add(Triple.of(Items.ENCHANTED_GOLDEN_APPLE, gapples.isEnabled() && (mc.player.getInventory().getStack(40).getItem() == Items.ENCHANTED_GOLDEN_APPLE || InvUtils.slotOfItem(Items.ENCHANTED_GOLDEN_APPLE) != -1), gappleHealth.value + 0.1));
        items.add(Triple.of(Items.GOLDEN_APPLE, gapples.isEnabled() && (mc.player.getInventory().getStack(40).getItem() == Items.GOLDEN_APPLE || InvUtils.slotOfItem(Items.GOLDEN_APPLE) != -1), gappleHealth.value));
        items.add(Triple.of(Items.TNT_MINECART, tntcart.isEnabled() && (mc.player.getInventory().getStack(40).getItem() == Items.TNT_MINECART || InvUtils.slotOfItem(Items.TNT_MINECART) != -1), tntcartHealth.value));

        items.sort(Comparator.comparing(t -> -t.getRight()));

        for (Triple<Item, Boolean, Double> triple : items) {
            if (triple.getMiddle()) {
                return triple.getLeft();
            }
        }

        return null;
    }

    private Item currentItem() {
        return currentItem;
    }
}