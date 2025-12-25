package template.rip.module.modules.misc;

import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.tuple.Triple;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;

import java.util.*;

import static net.minecraft.item.equipment.EquipmentType.*;

public class AutoCraftModule extends Module {

    public final MinMaxNumberSetting delays = new MinMaxNumberSetting(this, 50, 100, 0, 500, 1, "Delays");
    public final BooleanSetting armor = new BooleanSetting(this, true, "Armor");
    public final BooleanSetting tntCarts = new BooleanSetting(this, false, "TNT carts");

    private long timer = System.currentTimeMillis() + delays.getRandomInt();
    private HashMap<Integer, Boolean> bools = new HashMap<>();
    private int lastSlot = -1;

    public AutoCraftModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return String.format(" %s %s", delays.getIMinValue(), delays.getIMaxValue());
    }

    private boolean canDo() {
        if (delays.getRandomInt() == 0)
            return true;
        if (timer <= System.currentTimeMillis()) {
            timer = System.currentTimeMillis() + delays.getRandomInt();
            return true;
        }
        return false;
    }

    @Override
    public void onEnable() {
        bools.clear();
        timer = System.currentTimeMillis() + delays.getRandomInt();

        if (lastSlot != -1) {
            if (mc.currentScreen instanceof HandledScreen<?> screen && nullCheck()) {
                ItemStack cursor = screen.getScreenHandler().getCursorStack();
                if (!cursor.isEmpty()) {
                    mc.interactionManager.clickSlot(((HandledScreen<?>)mc.currentScreen).getScreenHandler().syncId, SlotUtils.indexToId(lastSlot), 0, SlotActionType.PICKUP, mc.player);
                }
            }
            lastSlot = -1;
        }
    }

    @EventHandler
    private void onRender(TickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (mc.currentScreen instanceof CraftingScreen cs && armor.isEnabled()) {
            List<Triple<Item, ArmorItem, Float>> itemList = betterCraftableItems();

            if (itemList.isEmpty())
                return;

            Pair<Item, ArmorItem> item = new Pair<>(itemList.get(0).getLeft(), itemList.get(0).getMiddle());
            ArmorItem armor = item.getRight();
            Item mat = item.getLeft();

            int slot = InvUtils.inventorySlotOfItem(mat);
            if (slot == -1)
                slot = InvUtils.getItemSlot(mat);

            if (lastSlot == -1) {
                mc.interactionManager.clickSlot(cs.getScreenHandler().syncId, SlotUtils.indexToId(slot), 0, SlotActionType.PICKUP, mc.player);
                lastSlot = slot;
            }

            if (slot == -1 && !cs.getScreenHandler().getCursorStack().isOf(mat))
                return;

            oneToSlot(cs, Arrays.asList(1, 3, 4, 6));

            switch (armor.getComponents().get(DataComponentTypes.EQUIPPABLE).slot()) {
                case HEAD : oneToSlot(cs, List.of(2)); break;
                case CHEST : oneToSlot(cs, Arrays.asList(5, 7, 8, 9)); break;
                case LEGS : oneToSlot(cs, Arrays.asList(2, 7, 9)); break;
            }
            ItemStack is = cs.getScreenHandler().getSlot(0).getStack();
            if (!is.isEmpty() && is.getItem() == armor && canDo()) {
                InvUtils.shiftClick().fromId(0).toId(0);
                bools.clear();
            }
        } else if (mc.currentScreen instanceof InventoryScreen is && tntCarts.isEnabled()) {
            int cart = InvUtils.slotOfItem(Items.MINECART);
            int tnt = InvUtils.slotOfItem(Items.TNT);
            if (is.getScreenHandler().getStacks().get(0).isOf(Items.TNT_MINECART) && canDo())
                mc.interactionManager.clickSlot(is.getScreenHandler().syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);

            if (cart != -1 && !is.getScreenHandler().getStacks().get(1).isOf(Items.MINECART) && is.getScreenHandler().getStacks().get(3).isOf(Items.TNT) && canDo()) {
                InvUtils.move().from(cart).toId(1);
            }
            if (tnt != -1 && !is.getScreenHandler().getStacks().get(3).isOf(Items.TNT) && canDo()) {
                InvUtils.move().from(tnt).toId(3);
            }
        } else onEnable();
    }

    private void oneToSlot(CraftingScreen cs, List<Integer> slot) {
        for (int sl : slot) {
            if (!bools.containsKey(slot) && cs.getScreenHandler().getSlot(sl).getStack().isEmpty() && !cs.getScreenHandler().getCursorStack().isEmpty() && canDo()) {
                mc.interactionManager.clickSlot(cs.getScreenHandler().syncId, sl, 1, SlotActionType.PICKUP, mc.player);
//              InvUtils.moveOneItem().from(from).toId(sl);
                bools.put(sl, true);
            }
        }
    }

    private List<Triple<Item, Integer, Item>> desiredItems() {
        List<Triple<Item, Integer, Item>> items = new ArrayList<>();

        items.add(Triple.of(Items.DIAMOND, 8, Items.DIAMOND_CHESTPLATE));
        items.add(Triple.of(Items.DIAMOND, 7, Items.DIAMOND_LEGGINGS));
        items.add(Triple.of(Items.DIAMOND, 5, Items.DIAMOND_HELMET));
        items.add(Triple.of(Items.DIAMOND, 4, Items.DIAMOND_BOOTS));

        items.add(Triple.of(Items.IRON_INGOT, 8, Items.IRON_CHESTPLATE));
        items.add(Triple.of(Items.IRON_INGOT, 7, Items.IRON_LEGGINGS));
        items.add(Triple.of(Items.IRON_INGOT, 5, Items.IRON_HELMET));
        items.add(Triple.of(Items.IRON_INGOT, 4, Items.IRON_BOOTS));

        items.add(Triple.of(Items.GOLD_INGOT, 8, Items.GOLDEN_CHESTPLATE));
        items.add(Triple.of(Items.GOLD_INGOT, 7, Items.GOLDEN_LEGGINGS));
        items.add(Triple.of(Items.GOLD_INGOT, 5, Items.GOLDEN_HELMET));
        items.add(Triple.of(Items.GOLD_INGOT, 4, Items.GOLDEN_BOOTS));

        items.add(Triple.of(Items.LEATHER, 8, Items.LEATHER_CHESTPLATE));
        items.add(Triple.of(Items.LEATHER, 7, Items.LEATHER_LEGGINGS));
        items.add(Triple.of(Items.LEATHER, 5, Items.LEATHER_HELMET));
        items.add(Triple.of(Items.LEATHER, 4, Items.LEATHER_BOOTS));

        return items;
    }

    private List<Pair<Item, ArmorItem>> craftableItems() {
        List<Pair<Item, ArmorItem>> items = new ArrayList<>();

        for (Triple<Item, Integer, Item> itemsToDo : desiredItems()) {
            if (InvUtils.countOfItem(itemsToDo.getLeft()) >= itemsToDo.getMiddle() && itemsToDo.getRight() instanceof ArmorItem)
                items.add(new Pair<>(itemsToDo.getLeft(), ((ArmorItem) itemsToDo.getRight())));
        }

        return items;
    }

    private List<Triple<Item, ArmorItem, Float>> betterCraftableItems() {
        Triple<Item, ArmorItem, Float> bestChest = Triple.of(null, null, 0f);
        Triple<Item, ArmorItem, Float> bestLegs = Triple.of(null, null, 0f);
        Triple<Item, ArmorItem, Float> bestBoots = Triple.of(null, null, 0f);
        Triple<Item, ArmorItem, Float> bestHelmet = Triple.of(null, null, 0f);

        for (Pair<Item, ArmorItem> craftableItem : craftableItems()) {
            EquipmentSlot slot = InvUtils.getSlot(craftableItem.getRight().getDefaultStack());
            ItemStack ourStack = InvUtils.bestInventoryStack(false, ArmorItem.class, 10, slot);
            ItemStack defaultSt = craftableItem.getRight().getDefaultStack();

            ArmorItem armorItem = switch (slot) {
                case CHEST -> bestChest.getMiddle();
                case LEGS -> bestLegs.getMiddle();
                case HEAD -> bestHelmet.getMiddle();
                case FEET -> bestBoots.getMiddle();
                default -> null;
            };

            /*ItemStack ourStack = InvUtils.bestInventoryStack(false, ArmorItem.class, 10, craftableItem.getRight().getType());
            ItemStack defaultSt = craftableItem.getRight().getDefaultStack();

            ArmorItem armorItem = switch (craftableItem.getRight().getType()) {
                case CHESTPLATE -> bestChest.getMiddle();
                case LEGGINGS -> bestLegs.getMiddle();
                case HELMET -> bestHelmet.getMiddle();
                case BOOTS -> bestBoots.getMiddle();
                default -> null;
            };*/

            boolean our = InvUtils.isBetter(ourStack, defaultSt, false);
            boolean armor = armorItem == null || InvUtils.isBetter(armorItem.getDefaultStack(), defaultSt, false);
            float fl = our && ourStack != null ? InvUtils.getDamageSimulated(ourStack) - InvUtils.getDamageSimulated(defaultSt) : Float.MAX_VALUE;

            if (our && armor) {
                /*switch (craftableItem.getRight().getType()) {
                    case CHESTPLATE : bestChest = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                    case LEGGINGS : bestLegs = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                    case HELMET : bestHelmet = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                    case BOOTS : bestBoots = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                }*/
                switch (slot) {
                    case CHEST : bestChest = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                    case LEGS : bestLegs = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                    case HEAD : bestHelmet = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                    case FEET : bestBoots = Triple.of(craftableItem.getLeft(), craftableItem.getRight(), fl);
                }
            }
        }

        List<Triple<Item, ArmorItem, Float>> items = new ArrayList<>();
        if (bestChest.getMiddle() != null)
            items.add(bestChest);
        if (bestLegs.getMiddle() != null)
            items.add(bestLegs);
        if (bestHelmet.getMiddle() != null)
            items.add(bestHelmet);
        if (bestBoots.getMiddle() != null)
            items.add(bestBoots);
        items.sort(Comparator.comparing(t -> -t.getRight()));
        return items;
    }
}
