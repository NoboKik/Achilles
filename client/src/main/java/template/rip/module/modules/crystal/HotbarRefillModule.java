package template.rip.module.modules.crystal;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.SetScreenEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.util.ExcludeMode;
import template.rip.api.util.InvUtils;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.Arrays;
import java.util.function.Predicate;

public class HotbarRefillModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Automatically clicks slots\nLegit: Only clicks slots you hover over"), modeEnum.Normal, "Mode");
    public final BooleanSetting pots = new BooleanSetting(this, false, "Health Pots");
    public final BooleanSetting utilPots = new BooleanSetting(this, false, "Utility Pots");
    public final MinMaxNumberSetting swapDelay = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting totems = new BooleanSetting(this, false, "Totems");
    public final NumberSetting totemSlot = new NumberSetting(this, 1, 1, 9, 1, "Totem Slot");
    public final BooleanSetting soups = new BooleanSetting(this, false, "Soups");
    public final BooleanSetting carts = new BooleanSetting(this, false, "TNT Carts");
    public final BooleanSetting anySlot = new BooleanSetting(this, false, "Any Empty Cart Slot");
    public final NumberSetting cartSlot = new NumberSetting(this, 1, 1, 9, 1, "TNT Cart slot");
    public final BooleanSetting workOnKey = new BooleanSetting(this, false, "Work On Key");
    public final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    public enum ItemEnum{Totem, Pot, Soup, Cart}
    private long timer = System.currentTimeMillis();

    public HotbarRefillModule(Category category, Description description, String name) {
        super(category, description, name);
        totemSlot.addConditionBoolean(totems, true);

        anySlot.addConditionBoolean(carts, true);
        cartSlot.addConditionBoolean(anySlot, false);
        cartSlot.addConditionBoolean(carts, true);
    }

    @Override
    public void onEnable() {
        reset();
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

    @EventHandler(priority = EventPriority.LOWEST)
    private void onSetScreen(SetScreenEvent event) {
        if (!event.isCancelled() && event.screen instanceof InventoryScreen) {
            reset();
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (activateKey.isPressed() && workOnKey.isEnabled())
            return;

        if (mc.currentScreen instanceof InventoryScreen inventoryScreen) {
            PlayerInventory inventory = mc.player.getInventory();
            Integer theSlot;

            if (totems.isEnabled() && !inventory.getStack(totemSlot.getIValue() - 1).isOf(Items.TOTEM_OF_UNDYING) && (theSlot = getSlot(ItemEnum.Totem)) != null && canSwap()) {
                mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, theSlot, totemSlot.getIValue() - 1, SlotActionType.SWAP, mc.player);
                OffhandModule o = Template.moduleManager.getModule(OffhandModule.class);
                if (o != null && o.isEnabled()) {
                    o.reset();
                }
            }

            if (pots.isEnabled()) {
                for (int i = 0; i <= 8; i++) {
                    if (inventory.getStack(i).isEmpty() && (theSlot = getSlot(ItemEnum.Pot)) != null && canSwap()) {
                        mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, theSlot, i, SlotActionType.SWAP, mc.player);
                    }
                }
            }
            if (soups.isEnabled()) {
                for (int i = 0; i <= 8; i++) {
                    if ((inventory.getStack(i).isEmpty() || inventory.getStack(i).isOf(Items.BOWL)) && (theSlot = getSlot(ItemEnum.Soup)) != null && canSwap()) {
                        mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, theSlot, i, SlotActionType.SWAP, mc.player);
                    }
                }
            }
            if (carts.isEnabled() && (theSlot = getSlot(ItemEnum.Cart)) != null) {
                if (anySlot.isEnabled()) {
                    for (int i = 0; i <= 8; i++) {
                        if ((inventory.getStack(i).isEmpty()) && canSwap()) {
                            mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, theSlot, i, SlotActionType.SWAP, mc.player);
                        }
                    }
                } else if (!inventory.getStack(cartSlot.getIValue() - 1).isOf(Items.TNT_MINECART) && canSwap()) {
                    mc.interactionManager.clickSlot(inventoryScreen.getScreenHandler().syncId, theSlot, cartSlot.getIValue() - 1, SlotActionType.SWAP, mc.player);
                }
            }
        }
    }

    private Integer getSlot(ItemEnum item) {
        if (mc.currentScreen instanceof InventoryScreen is) {
            Predicate<ItemStack> soupPred = theItem -> theItem.getItem() == Items.MUSHROOM_STEW || theItem.getItem() == Items.RABBIT_STEW || theItem.getItem() == Items.SUSPICIOUS_STEW || theItem.getItem() == Items.BEETROOT_SOUP;
            if (mode.is(modeEnum.Normal)) {
                int totsl = InvUtils.screenSlotOfItem(Items.TOTEM_OF_UNDYING, is.getScreenHandler(), ExcludeMode.HotbarAndOffhand);
                if (item.equals(ItemEnum.Totem) && totsl != -1 && !SlotUtils.isHotbar(totsl))
                    return totsl;

                int splashsl = -1;
                if (utilPots.isEnabled()) {
                    for (RegistryEntry<StatusEffect> se : Arrays.asList(StatusEffects.SPEED, StatusEffects.STRENGTH, StatusEffects.REGENERATION)) {
                        if (splashsl == -1 && InvUtils.getSplashSlot(se, is.getScreenHandler(), ExcludeMode.Inventory) == -1) {
                            splashsl = InvUtils.getSplashSlot(se, is.getScreenHandler(), ExcludeMode.HotbarAndOffhand);
                        }
                    }
                }
                if (splashsl == -1)
                    splashsl = InvUtils.getSplashSlot(StatusEffects.INSTANT_HEALTH, is.getScreenHandler(), ExcludeMode.HotbarAndOffhand);
                if (item.equals(ItemEnum.Pot) && splashsl != -1 && !SlotUtils.isHotbar(splashsl))
                    return splashsl;

                int soupSL = InvUtils.screenSlotOfItem(soupPred, is.getScreenHandler(), ExcludeMode.HotbarAndOffhand);
                if (item.equals(ItemEnum.Soup) && soupSL != -1 && !SlotUtils.isHotbar(soupSL))
                    return soupSL;

                int cartsl = InvUtils.screenSlotOfItem(Items.TNT_MINECART, is.getScreenHandler(), ExcludeMode.HotbarAndOffhand);
                if (item.equals(ItemEnum.Cart) && cartsl != -1 && !SlotUtils.isHotbar(cartsl))
                    return cartsl;
            }

            Slot slot = is.focusedSlot;
            if (mode.is(modeEnum.Legit) && slot != null && !SlotUtils.isHotbar(slot.getIndex()) && !SlotUtils.isOffhand(slot.getIndex())) {
                if (item.equals(ItemEnum.Totem) && slot.getStack().isOf(Items.TOTEM_OF_UNDYING))
                    return slot.getIndex();

                if (item.equals(ItemEnum.Pot) && InvUtils.isThatSplash(StatusEffects.INSTANT_HEALTH, slot.getStack()))
                    return slot.getIndex();

                if (item.equals(ItemEnum.Soup) && soupPred.test(slot.getStack()))
                    return slot.getIndex();

                if (item.equals(ItemEnum.Cart) && slot.getStack().isOf(Items.TNT_MINECART))
                    return slot.getIndex();
            }
        }
        return null;
    }
}
