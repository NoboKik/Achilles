package template.rip.module.modules.misc;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.screen.slot.Slot;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static template.rip.api.util.MathUtils.passedTime;

public class InventoryCleanerModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Automatically clicks slots\nLegit: Only clicks slots you hover over"), modeEnum.Normal, "Mode");

    public final MinMaxNumberSetting swapDelay = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting keepSame = new BooleanSetting(this, false, "Keep combinable items");
    public final BooleanSetting workOnKey = new BooleanSetting(this, false, "Work On Key");
    public final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    public final BooleanSetting autoClose = new BooleanSetting(this, false, "Auto close");
    private final NumberSetting minDura = new NumberSetting(this, 10d, 0d, 100d, 1d, "Minimum durability").setAdvanced();
    private final DividerSetting hotBarManager = new DividerSetting(this, false, "HotBar manager (adv)").setAdvanced();
    private final NumberSetting sword = new NumberSetting(this, 0, 0d, 9d, 1d, "Sword slot").setAdvanced();
    private final NumberSetting axe = new NumberSetting(this, 0, 0d, 9d, 1d, "Axe slot").setAdvanced();
    private final NumberSetting pickaxe = new NumberSetting(this, 0, 0d, 9d, 1d, "Pickaxe slot").setAdvanced();
    private final NumberSetting shovel = new NumberSetting(this, 0, 0d, 9d, 1d, "Shovel slot").setAdvanced();
    private final NumberSetting bow = new NumberSetting(this, 0, 0d, 9d, 1d, "Bow slot").setAdvanced();
    private final NumberSetting block = new NumberSetting(this, 0, 0d, 9d, 1d, "Block slot").setAdvanced();
    private final NumberSetting potion = new NumberSetting(this, 0, 0d, 9d, 1d, "Potion slot").setAdvanced();
    private final NumberSetting projectile = new NumberSetting(this, 0, 0d, 9d, 1d, "Projectile slot").setAdvanced();
    private final NumberSetting food = new NumberSetting(this, 0, 0d, 9d, 1d, "Food slot").setAdvanced();

    private long timer = System.currentTimeMillis();
    private boolean equipping = false;

    public InventoryCleanerModule(Category category, Description description, String name) {
        super(category, description, name);
        hotBarManager.addSetting(sword, axe, pickaxe, shovel, bow, block, potion, projectile, food);
    }

    @Override
    public void onEnable() {
        timer = System.currentTimeMillis();
    }

    @EventHandler
    private void onHUDRender(HudRenderEvent event) {
        if (mc.interactionManager == null || mc.player == null)
            return;

        if (!activateKey.isPressed() && workOnKey.isEnabled())
            return;

        Predicate<Item> blockPred = BlockUtils.placeableBlocks();

        if (mc.currentScreen instanceof InventoryScreen) {
                for (ItemStack stack : mc.player.getInventory().main) {
                    int slot = mc.player.getInventory().getSlotWithStack(stack);
                    Item item = stack.getItem();
                    if (currentItemSlot(slot) != -1) {
                        if (InvUtils.desirableItems().contains(stack.getItem().getClass()) || stack.getItem() instanceof ArmorItem || stack.get(DataComponentTypes.FOOD) != null) {

                            //ItemStack best = InvUtils.bestInventoryStack(keepSame.isEnabled(), stack.getItem().getClass(), minDura.getIValue(), stack.getItem() instanceof ArmorItem ? ((ArmorItem)stack.getItem()).getType() : null);
                            ItemStack best = InvUtils.bestInventoryStack(keepSame.isEnabled(), stack.getItem().getClass(), minDura.getIValue(), InvUtils.getSlot(stack));

                            if (best != null && best != stack) {
                                drop(slot, stack);
                            } else if (canSwap()) {
                                if (sword.getIValue() != 0 && item instanceof SwordItem && slot != sword.getIValue() -1)
                                    InvUtils.move().from(slot).to(sword.getIValue() - 1);
                                if (axe.getIValue() != 0 && item instanceof AxeItem && slot != axe.getIValue() -1)
                                    InvUtils.move().from(slot).to(axe.getIValue() - 1);
                                if (pickaxe.getIValue() != 0 && item instanceof PickaxeItem && slot != pickaxe.getIValue() -1)
                                    InvUtils.move().from(slot).to(pickaxe.getIValue() - 1);
                                if (shovel.getIValue() != 0 && item instanceof ShovelItem && slot != shovel.getIValue() -1)
                                    InvUtils.move().from(slot).to(shovel.getIValue() - 1);
                                if (bow.getIValue() != 0 && item instanceof BowItem && slot != bow.getIValue() -1)
                                    InvUtils.move().from(slot).to(bow.getIValue() - 1);
                                if (bow.getIValue() != 0 && item instanceof BowItem && slot != bow.getIValue() -1)
                                    InvUtils.move().from(slot).to(bow.getIValue() - 1);
                                if (block.getIValue() != 0 && blockPred.test(item) && slot != block.getIValue() -1 && (!(blockPred.test(mc.player.getInventory().getStack(block.getIValue() -1).getItem())) || mc.player.getInventory().getStack(block.getIValue() -1).isEmpty()))
                                    InvUtils.move().from(slot).to(block.getIValue() - 1);
                                if (potion.getIValue() != 0 && pot(item) && slot != potion.getIValue() -1 && (!pot(mc.player.getInventory().getStack(potion.getIValue() -1).getItem()) || mc.player.getInventory().getStack(potion.getIValue() -1).isEmpty()))
                                    InvUtils.move().from(slot).to(potion.getIValue() - 1);
                                if (projectile.getIValue() != 0 && proj().contains(item) && slot != projectile.getIValue() -1 && (!proj().contains(mc.player.getInventory().getStack(projectile.getIValue() -1).getItem()) || mc.player.getInventory().getStack(projectile.getIValue() -1).isEmpty()))
                                    InvUtils.move().from(slot).to(projectile.getIValue() - 1);
                                if (food.getIValue() != 0 && stack.get(DataComponentTypes.FOOD) != null && slot != food.getIValue() -1 && (mc.player.getInventory().getStack(food.getIValue() -1).get(DataComponentTypes.FOOD) == null || mc.player.getInventory().getStack(food.getIValue() -1).isEmpty()))
                                    InvUtils.move().from(slot).to(food.getIValue() - 1);
                            }
                        } else drop(slot, stack);
                    }
                }
            if (!equipping && autoClose.isEnabled() && mc.currentScreen instanceof HandledScreen<?>) {
                mc.currentScreen.close();
            }
            equipping = false;
        }
    }
    private void drop(int slot, ItemStack stack) {
        if (InvUtils.desirableStackClass().contains(stack.getItem().getClass()) || stack.get(DataComponentTypes.FOOD) != null || InvUtils.desirableStackItems().contains(stack.getItem()))
            return;
        if (canSwap())
            InvUtils.drop().from(slot).to(slot);
    }

    private List<Item> proj() {
        List<Item> proj = new ArrayList<>();
        proj.add(Items.SNOWBALL);
        proj.add(Items.EGG);
        proj.add(Items.FISHING_ROD);
        return proj;
    }

    private boolean pot(Item item) {
        return item instanceof PotionItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
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
        if (mc.currentScreen instanceof InventoryScreen screen) {
            if (mode.is(modeEnum.Normal))
                return sl;

            Slot slot = screen.focusedSlot;
            if (mode.is(modeEnum.Legit) && slot != null && sl == slot.getIndex())
                return sl;
        }
        return -1;
    }
}
