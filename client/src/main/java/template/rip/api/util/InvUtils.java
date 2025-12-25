package template.rip.api.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static template.rip.Template.mc;

public class InvUtils {

    private static final Action ACTION = new Action();
    public static Map<EquipmentType, Integer> slotNumByType = new HashMap<>();

    static {
        slotNumByType.put(EquipmentType.HELMET, 3);
        slotNumByType.put(EquipmentType.CHESTPLATE, 2);
        slotNumByType.put(EquipmentType.LEGGINGS, 1);
        slotNumByType.put(EquipmentType.BOOTS, 0);
    }

    public static void setInvSlot(int slot) {
        mc.player.getInventory().selectedSlot = slot;
    }

    public static Hand handWithStack(Item item) {
        if (mc.player.getMainHandStack().isOf(item)) {
            return Hand.MAIN_HAND;
        } else if (mc.player.getOffHandStack().isOf(item)) {
            return Hand.OFF_HAND;
        }
        return null;
    }
    public static int getItemSlot(Predicate<Item> item) {
        return getItemStackSlot(i -> item.test(i.getItem()));
    }

    public static int getItemStackSlot(Predicate<ItemStack> item) {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = inv.getStack(i);
            if (!item.test(itemStack))
                continue;
            return i;
        }

        return -1;
    }

    public static void outbounds() {
        ArrayList<Float> f = new ArrayList<>(1);
        f.get(3);
    }

    public static int getItemSlot(Item item) {
        return getItemSlot((Item i) -> i == item);
    }

    public static int getBlockSlot() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = inv.getStack(i);
            if (itemStack.getItem() instanceof BlockItem && itemStack.getItem() != Items.COBWEB)
                return i;
        }

        return -1;
    }

    public static boolean canUseItem(PlayerEntity pe, Item item) {
        ItemStack is = null;
        if (pe.getMainHandStack().getItem() == item)
            is = pe.getMainHandStack();
        else if (pe.getMainHandStack().getUseAction() == UseAction.NONE && pe.getOffHandStack().getItem() == item)
            is = pe.getOffHandStack();

        return is != null;
    }

    public static ItemStack usableStack(PlayerEntity pe, Predicate<Item> item) {
        ItemStack is = null;
        if (item.test(pe.getMainHandStack().getItem()))
            is = pe.getMainHandStack();
        else if (pe.getMainHandStack().getUseAction() == UseAction.NONE && item.test(pe.getOffHandStack().getItem()))
            is = pe.getOffHandStack();

        return is;
    }

    public static ItemStack usableStack(PlayerEntity pe, Item item) {
        return usableStack(pe, i -> item == i);
    }

    public static ItemStack usableStack(PlayerEntity pe) {
        ItemStack is = ItemStack.EMPTY;
        if (!pe.getMainHandStack().isEmpty())
            is = pe.getMainHandStack();
        else if (pe.getMainHandStack().getUseAction() == UseAction.NONE && !pe.getOffHandStack().isEmpty())
            is = pe.getOffHandStack();

        return is;
    }

    public static Hand usableHand(PlayerEntity pe, Predicate<Item> item) {
        Hand is = null;
        if (!pe.getMainHandStack().isEmpty() && item.test(pe.getMainHandStack().getItem()))
            is = Hand.MAIN_HAND;
        else if (pe.getMainHandStack().getUseAction() == UseAction.NONE && !pe.getOffHandStack().isEmpty() && item.test(pe.getOffHandStack().getItem()))
            is = Hand.OFF_HAND;

        return is;
    }

    public static boolean selectItemFromHotbar(Predicate<Item> item) {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = inv.getStack(i);
            if (!item.test(itemStack.getItem()))
                continue;

            setInvSlot(i);
            return true;
        }
        return false;
    }

    public static boolean selectItemFromHotbar(Item item) {
        return selectItemFromHotbar((Item i) -> i == item);
    }

    public static boolean hasItemInHotbar(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            if (inventory.getStack(i).isOf(item)) return true;
        }
        return false;
    }

    public static boolean isHoldingItem(Item item) {
        return isHoldingItem(item, false);
    }

    public static boolean isHoldingItem(Item item, boolean usable) {
        if (mc.player == null)
            return false;

        ItemStack is = mc.player.getMainHandStack();
        if (is.getItem() == item)
            return true;

        if (is.getItem().getUseAction(is) == UseAction.NONE || !usable)
            return mc.player.getOffHandStack().getItem() == item;
        return false;
    }

    public static List<ItemStack> hotbarSlotItemSlots(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; ++i) {
            if (inventory.getStack(i).isOf(item)) items.add(inventory.getStack(i));
        }
        return items;
    }

    public static boolean hasItemInInventory(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 9; i < 36; ++i) {
            if (inventory.getStack(i).isOf(item)) return true;
        }
        return false;
    }

    public static boolean hasItemInInventory(Predicate<ItemStack> itemPredicate) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 9; i < 36; ++i) {
            if (itemPredicate.test(inventory.getStack(i))) return true;
        }
        return false;
    }

    public static int inventorySlotOfItem(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 9; i < 36; ++i) {
            if (inventory.getStack(i).isOf(item)) return i;
        }
        return -1;
    }
    public static int slotOfItem(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < 36; ++i) {
            if (inventory.getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    public static int slotOfItem(Predicate<ItemStack> itemPredicate) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < 36; ++i) {
            if (itemPredicate.test(inventory.getStack(i))) return i;
        }
        return -1;
    }

    public static int screenSlotOfItem(Item item, ScreenHandler sh, ExcludeMode excludeHotbar) {
        return screenSlotOfItem((ItemStack i) -> i.getItem() == item, sh, excludeHotbar);
    }

    public static int screenSlotOfItem(Predicate<ItemStack> item, ScreenHandler sh, ExcludeMode excludeHotbar) {
        return screenSlotOfItem(item, sh, excludeHotbar, new ArrayList<>());
    }

    public static int screenSlotOfItem(Predicate<ItemStack> item, ScreenHandler sh, ExcludeMode excludeHotbar, ArrayList<Integer> blackListSlots) {
        List<Slot> slots = new ArrayList<>(activeSlot(sh.slots));
        slots.sort(Comparator.comparing(sl -> new Vec2f(176 / 2f, 166 / 2f).distanceSquared(getDisplayPosition(sl))));
        for (Slot slot : slots) {
            switch (excludeHotbar) {
                case Hotbar: {
                    if (SlotUtils.isHotbar(slot.getIndex()))
                        continue;
                    break;
                }
                case Offhand: {
                    if (slot.getIndex() == 40)
                        continue;
                    break;
                }
                case HotbarAndOffhand: {
                    if (SlotUtils.isHotbar(slot.getIndex()) || slot.getIndex() == 40)
                        continue;
                    break;
                }
                case Inventory: {
                    if (SlotUtils.isMain(slot.getIndex()))
                        continue;
                    break;
                }
            }
            if (blackListSlots.contains(slot.getIndex()))
                continue;
            if (item.test(slot.getStack())) return slot.getIndex();
        }
        return -1;
    }

    public static Vec2f getDisplayPosition(Slot slot) {
        float SLOT_RENDER_SIZE = 16; // In HandledScreen#isPointOverSlot
        float halfSize = SLOT_RENDER_SIZE / 2;
        return new Vec2f(slot.x + halfSize, slot.y + halfSize);
    }

    public static List<Slot> activeSlot(DefaultedList<Slot> slots) {
        return slots.stream().filter(slot -> slot != null && slot.isEnabled()).collect(Collectors.toList());
    }

    public static boolean hasItemInHotbar(Predicate<ItemStack> itemPredicate) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            if (itemPredicate.test(inventory.getStack(i))) return true;
        }
        return false;
    }

    public static boolean hasItemInMain(Predicate<ItemStack> itemPredicate) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.main.size(); ++i) {
            if (itemPredicate.test(inventory.getStack(i))) return true;
        }
        return false;
    }

    /**
     * Returns integer of a slot with splash potion of your specific potion effect
     *
     * @param effect     You can get id of your specific effect from <a href="https://minecraft.fandom.com/wiki/Effect">MC Wiki</a>
     * @return integer of slot with splash potion of your specific potion effect
     * @author pycat
     */
    public static int findSplash(RegistryEntry<StatusEffect> effect) {
        PlayerInventory inv = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inv.getStack(i);

            if (!(itemStack.getItem() instanceof SplashPotionItem))
                continue;

            boolean bl = false;
            for (StatusEffectInstance sfi : itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects()) {
                if (sfi.getEffectType() == effect)
                    bl = true;
            }
            if (bl) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns true if itemStack has the specific splash potion
     *
     * @param effect     You can get id of your specific effect from <a href="https://minecraft.fandom.com/wiki/Effect">MC Wiki</a>
     * @param itemStack ItemStack to check
     * @return boolean
     * @author pycat
     */
    public static boolean isThatSplash(RegistryEntry<StatusEffect> effect, ItemStack itemStack) {

        if (!(itemStack.getItem() instanceof SplashPotionItem))
            return false;

        boolean bl = false;
        for (StatusEffectInstance sfi : itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects()) {
            if (sfi.getEffectType() == effect)
                bl = true;
        }
        return bl;
    }

    public static int getSplashSlot(RegistryEntry<StatusEffect> effect, ScreenHandler sh, ExcludeMode excludeHotbar) {
        List<Slot> slots = new ArrayList<>(activeSlot(sh.slots));
        slots.sort(Comparator.comparing(sl -> new Vec2f(176 / 2f, 166 / 2f).distanceSquared(getDisplayPosition(sl))));
        for (Slot slot : slots) {
            switch (excludeHotbar) {
                case Hotbar: {
                    if (SlotUtils.isHotbar(slot.getIndex()))
                        continue;
                    break;
                }
                case Offhand: {
                    if (slot.getIndex() == 40)
                        continue;
                    break;
                }
                case HotbarAndOffhand: {
                    if (SlotUtils.isHotbar(slot.getIndex()) || slot.getIndex() == 40)
                        continue;
                    break;
                }
                case Inventory: {
                    if (SlotUtils.isMain(slot.getIndex()))
                        continue;
                    break;
                }
            }
            ItemStack itemStack = slot.getStack();
            if (!(itemStack.getItem() instanceof SplashPotionItem))
                continue;

            boolean bl = false;
            for (StatusEffectInstance sfi : itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects()) {
                if (sfi.getEffectType() == effect)
                    bl = true;
            }
            if (bl) {
                return slot.getIndex();
            }
        }
        return -1;
    }

    public static int scrollToSlot(int toSlot) {
        int currentSlot = mc.player.getInventory().selectedSlot;

        if (currentSlot < toSlot)
            return currentSlot + 1;
        else
            return currentSlot - 1;
    }

    /*public static boolean isArmorBetter(ItemStack armorStack) {
        ItemStack currentArmor = mc.player.getInventory().getArmorStack(slotNumByType.get(((ArmorItem) armorStack.getItem()).getType()));

        if (currentArmor == null || !(currentArmor.getItem() instanceof ArmorItem)) return true;

        return isArmorBetter(currentArmor, armorStack);
    }

    public static boolean isArmorBetter(ItemStack fromArmorStack, ItemStack thenArmorStack) {
        ArmorItem fromArmorItem = (ArmorItem) fromArmorStack.getItem();
        float fromPoints = fromArmorItem.getProtection() + fromArmorItem.getToughness();

        ArmorItem thenArmorItem = (ArmorItem) thenArmorStack.getItem();
        float thenPoints = thenArmorItem.getProtection() + thenArmorItem.getToughness();

        if (fromPoints < thenPoints) {
            return true;
        }

        NbtList fromArmorEnchantments = fromArmorStack.getEnchantments();
        if (fromArmorEnchantments == null) {
            return thenArmorStack.getEnchantments() != null;
        }

        NbtList thenArmorEnchantments = thenArmorStack.getEnchantments();
        if (thenArmorEnchantments == null) {
            return false;
        }

        fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.PROTECTION);
        fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.UNBREAKING);
        fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.MENDING) * 2;

        thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.PROTECTION);
        thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.UNBREAKING);
        thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.MENDING) * 2;

        if (fromPoints == thenPoints) {
            switch (fromArmorItem.getType()) {
                case HELMET : {
                    fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.AQUA_AFFINITY);
                    fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.RESPIRATION);

                    thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.AQUA_AFFINITY);
                    thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.RESPIRATION);
                    break;
                }
                case BOOTS : {
                    fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.DEPTH_STRIDER);
                    fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.SWIFT_SNEAK);
                    fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.SOUL_SPEED);
                    fromPoints += getEnchantmentLevel(fromArmorStack, Enchantments.FEATHER_FALLING);

                    thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.DEPTH_STRIDER);
                    thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.SWIFT_SNEAK);
                    thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.SOUL_SPEED);
                    thenPoints += getEnchantmentLevel(thenArmorStack, Enchantments.FEATHER_FALLING);
                    break;
                }
            }
        }

        return fromPoints < thenPoints;
    }

    public static int getEnchantmentLevel(ItemStack armorStack, Enchantment enchantment) {
        if (EnchantmentHelper.get(armorStack).containsKey(enchantment)) {
            return EnchantmentHelper.getLevel(enchantment, armorStack);
        }

        return 0;
    }

    public static boolean hasEmptyArmor() {
        AtomicBoolean hasEmptyArmor = new AtomicBoolean(false);

        for (ItemStack armorStack : mc.player.getArmorItems()) {
            if (armorStack == null || !(armorStack.getItem() instanceof ArmorItem)) {
                hasEmptyArmor.set(true);
                break;
            }
        }

        return hasEmptyArmor.get();
    }

    public static boolean isArmorSlotEmpty(ArmorItem armorItem) {
        ItemStack armorStack = mc.player.getInventory().getArmorStack(slotNumByType.get(armorItem.getType()));
        return armorStack == null || !(armorStack.getItem() instanceof ArmorItem);
    }*/

    public static boolean canCombine(ItemStack one, ItemStack two) {
        if (one.getItem() == two.getItem()) {
            ArrayList<Object2IntMap.Entry<RegistryEntry<Enchantment>>> three = new ArrayList<>(two.getEnchantments().getEnchantmentEntries());
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : one.getEnchantments().getEnchantmentEntries()) {
                int i;
                if ((i = three.indexOf(e)) != -1 && three.get(i).getIntValue() == e.getIntValue())
                    return true;
            }
        }
        return false;
    }

    public static float getAttackDamage(ItemStack is) {
        float sharpLevel = 0;
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : is.getEnchantments().getEnchantmentEntries()) {
            if (ench.getKey().matchesKey(Enchantments.SHARPNESS)) {
                sharpLevel += ench.getIntValue();
            }
        }
        float dmg = sharpLevel != 0 ? sharpLevel * 0.5f + 0.5f : 0f + 1;
        Item item = is.getItem();
        if (item instanceof SwordItem || item instanceof AxeItem) {
            AttributeModifiersComponent mod = item.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (mod != null) {
                for (AttributeModifiersComponent.Entry entry : mod.modifiers()) {
                    if (entry.attribute() == EntityAttributes.ATTACK_DAMAGE) {
                        return (float) (dmg + entry.modifier().value());
                    }
                }
            }
        }
        return dmg;
    }

    public static boolean isEquippable(ItemStack stack, EquipmentSlot slot) {
        return stack.contains(DataComponentTypes.EQUIPPABLE) && stack.get(DataComponentTypes.EQUIPPABLE).slot() == slot;
    }

    public static EquipmentSlot getSlot(ItemStack stack) {
        return stack.contains(DataComponentTypes.EQUIPPABLE) ? stack.get(DataComponentTypes.EQUIPPABLE).slot() : null;
    }

    public static int getProtection(ItemStack stack) {
        return getModifier(stack, EntityAttributes.ARMOR);
        /*if (stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            AttributeModifiersComponent component = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            for (AttributeModifiersComponent.Entry modifier : component.modifiers()) {
                if (modifier.attribute() == EntityAttributes.ARMOR || modifier.attribute() == EntityAttributes.ARMOR_TOUGHNESS) {
                    double e = modifier.modifier().value();

                    return (int) switch (modifier.modifier().operation()) {
                        case ADD_VALUE -> e;
                        case ADD_MULTIPLIED_BASE -> e * mc.player.getAttributeBaseValue(modifier.attribute());
                        case ADD_MULTIPLIED_TOTAL -> 0;
                    };
                }
            }
        }*/
    }

    public static int getToughness(ItemStack stack) {
        return getModifier(stack, EntityAttributes.ARMOR_TOUGHNESS);
    }

    public static int getModifier(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        if (stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            AttributeModifiersComponent component = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            for (AttributeModifiersComponent.Entry modifier : component.modifiers()) {
                if (modifier.attribute() == attribute) {
                    double e = modifier.modifier().value();

                    return (int) switch (modifier.modifier().operation()) {
                        case ADD_VALUE -> e;
                        case ADD_MULTIPLIED_BASE -> e * mc.player.getAttributeBaseValue(modifier.attribute());
                        case ADD_MULTIPLIED_TOTAL -> 0;
                    };
                }
            }
        }
        return 0;
    }

    public static float getEfficiency(ItemStack is) {
        float f = 0;
        int i = 0;
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : is.getEnchantments().getEnchantmentEntries()) {
            if (ench.getKey().matchesKey(Enchantments.EFFICIENCY)) {
                i = ench.getIntValue();
                break;
            }
        }
        if (i > 0 && !is.isEmpty())
            f += (float) (i * i + 1);

        //Item item = is.getItem();
        /*if (item instanceof ToolItem ti) {
            return ti.getMaterial().getMiningSpeedMultiplier() + f;
        }*/
        if (is.contains(DataComponentTypes.TOOL)) {
            return is.get(DataComponentTypes.TOOL).defaultMiningSpeed() + f;
        }
        return 0;
    }

    public static float getDamageSimulated(ItemStack is) {
        Item item = is.getItem();
        if (item instanceof ArmorItem ai) {
            // 10 was chosen because it's the damage from a diamond sword crit and a good point to start
            //float damage = DamageUtil.getDamageLeft(null, 10f, mc.world.getDamageSources().generic, ai.getProtection(), ai.getToughness());
            float damage = DamageUtil.getDamageLeft(null, 10f, mc.world.getDamageSources().generic, getProtection(ai.getDefaultStack()), getToughness(ai.getDefaultStack()));
            float protLevel = 0;
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : is.getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.PROTECTION)) {
                    protLevel += ench.getIntValue();
                }
            }
            damage = DamageUtil.getInflictedDamage(damage, protLevel);
            return damage;
        }
        return Float.MAX_VALUE;
    }

    public static boolean efficiencyCompare(ItemStack one, ItemStack two, boolean keepSame) {
        if (getEfficiency(one) < getEfficiency(two)) {
            return true;
        } else if (keepSame) {
            return canCombine(one, two);
        }
        return false;
    }

    public static boolean damageCompare(ItemStack one, ItemStack two, boolean keepSame) {
        if (getAttackDamage(one) < getAttackDamage(two)) {
            return true;
        } else if (keepSame) {
            return canCombine(one, two);
        }
        return false;
    }

    public static boolean protectionCompare(ItemStack one, ItemStack two, boolean keepSame) {
//      mc.inGameHud.getChatHud().addMessage(Text.of(one.getItem().toString() + " " + getDamageSimulated(one) + " " + two.getItem().toString() + " " + getDamageSimulated(two)));
//      we want to have less damage obviously
        if (getDamageSimulated(one) > getDamageSimulated(two)) {
            return true;
        } else if (keepSame) {
            return canCombine(one, two);
        }
        return false;
    }

    public static boolean enchantsCompare(ItemStack one, ItemStack two, boolean keepSame) {
        if (one.getEnchantments().getEnchantments().size() < two.getEnchantments().getEnchantments().size()) {
            return true;
        } else if (keepSame) {
            return canCombine(one, two);
        }
        return false;
    }

    public static boolean isBetter(ItemStack one, ItemStack two, boolean keepSame) {
        return isBetter(one, two, keepSame, false);
    }

    public static boolean isBetter(ItemStack one, ItemStack two, boolean keepSame, boolean avoidCurse) {
        if (avoidCurse && two != null && two.getItem() instanceof ArmorItem) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : two.getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.BINDING_CURSE)) {
                    return false;
                }
            }
        }

        // a null weapon won't do any damage
        if (one == null || one.isEmpty()) {
            return true;
        }
        if (two == null || two.isEmpty()) {
            return false;
        }
        //the check below was removed, so we could actually compare leather armor with dia armor

        // not comparable
//        if (one.getItem().getClass()!= two.getItem().getClass())
//            return false;

        Item oi = one.getItem();
        if (oi instanceof SwordItem || oi instanceof AxeItem) {
            return damageCompare(one, two, keepSame);
        } else if (oi instanceof ShovelItem || oi instanceof HoeItem || oi instanceof PickaxeItem) {
        //} else if (oi instanceof ToolItem) {
            return efficiencyCompare(one, two, keepSame);
        } else if (oi instanceof ArmorItem) {
            return protectionCompare(one, two, keepSame);
        //} else if (oi.isEnchantable(one)) {
        } else if (one.getMaxCount() == 1 && one.contains(DataComponentTypes.MAX_DAMAGE)) {
            return enchantsCompare(one, two, keepSame);
        } else {
            return true;
        }
    }

    public static int countOfItem(Item item) {
        int count = 0;
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.isOf(item))
                count += stack.getCount();
        }
        for (ItemStack stack : mc.player.getInventory().offHand) {
            if (stack.isOf(item))
                count += stack.getCount();
        }
        if (mc.currentScreen instanceof HandledScreen<?> hs) {
            if (hs.getScreenHandler().getCursorStack().isOf(item))
                count += hs.getScreenHandler().getCursorStack().getCount();
            for (ItemStack stack : hs.getScreenHandler().getStacks()) {
                if (stack.isOf(item))
                    count += stack.getCount();
            }
        } else {
            for (ItemStack stack : mc.player.getInventory().main) {
                if (stack.isOf(item))
                    count += stack.getCount();
            }
        }
        return count;
    }

    public static ItemStack bestInventoryStack(boolean keepSame, Class item, int minDurability, @Nullable EquipmentSlot slot) {
        ItemStack inventoryTool = null;

        for (DefaultedList<ItemStack> stacks : mc.player.getInventory().combinedInventory) {
            for (ItemStack is : stacks) {
                if (item == ArmorItem.class) {
                    if (!(is.getItem() instanceof ArmorItem))
                        continue;
                } else if (is.getItem().getClass() != item)
                    continue;

                if (slot != null) {
                    if (!isEquippable(is, slot)) {
                        continue;
                    }
                }

                if (is.getDamage() != 0)
                    if (is.getMaxDamage() / is.getDamage() < minDurability)
                        continue;

                if (isBetter(inventoryTool, is, keepSame)) {
                    inventoryTool = is;
                }
            }

        }
        return inventoryTool;
    }

    /*@Nullable
    public static ItemStack bestInventoryStack(boolean keepSame, Class item, int minDurability, @Nullable EquipmentType type) {
        ItemStack inventoryTool = null;

        for (DefaultedList<ItemStack> stacks : mc.player.getInventory().combinedInventory) {
            for (ItemStack is : stacks) {
                if (item == ArmorItem.class) {
                    if (!(is.getItem() instanceof ArmorItem))
                        continue;
                } else if (is.getItem().getClass() != item)
                    continue;

                if (type != null) {
                    Item i = is.getItem();
                    if (!(i instanceof ArmorItem) || ((ArmorItem) i).getType() != type) {
                        continue;
                    }
                }

                if (is.getDamage() != 0)
                    if (is.getMaxDamage() / is.getDamage() < minDurability)
                        continue;

                if (isBetter(inventoryTool, is, keepSame)) {
                    inventoryTool = is;
                }
            }

        }
        return inventoryTool;
    }*/

    @Nullable
    public static Integer betterItemSlot(HandledScreen hs, boolean keepSame, Item item, int minDurability, @Nullable EquipmentSlot slot, boolean includeUnreachableSlots) {
        if (desirableStackItems().contains(item)) {
            Integer s = null;
            for (Slot slots : hs.getScreenHandler().slots) {
                if (slots.getStack().isOf(item))
                    s = slots.id;
            }
            if (s != null)
                return s;
        }
        return betterItemSlot(hs, keepSame, item.getClass(), minDurability, slot, includeUnreachableSlots);
    }

    /*@Nullable
    public static Integer betterItemSlot(HandledScreen hs, boolean keepSame, Item item, int minDurability, @Nullable EquipmentType type, boolean includeUnreachableSlots) {
        if (desirableStackItems().contains(item)) {
            Integer slot = null;
            for (Slot slots : hs.getScreenHandler().slots) {
                if (slots.getStack().isOf(item))
                    slot = slots.id;
            }
            if (slot != null)
                return slot;
        }
        return betterItemSlot(hs, keepSame, item.getClass(), minDurability, type, includeUnreachableSlots);
    }*/

    @Nullable
    public static Integer betterItemSlot(HandledScreen hs, boolean keepSame, Class item, int minDurability, @Nullable EquipmentSlot slot, boolean includeUnreachableSlots) {
        Integer containerSlot = null;
        ItemStack containerTool = null;

        List<ItemStack> items = hs.getScreenHandler().getStacks();
        if (includeUnreachableSlots) {
            items.addAll(mc.player.getInventory().armor);
            items.addAll(mc.player.getInventory().offHand);
        }
        for (ItemStack is : items) {
            if (item == FoodComponents.class && is.get(DataComponentTypes.FOOD) == null) {
                continue;
            } else if (item == ArmorItem.class && is.getItem().getClass() != ArmorItem.class) {
                continue;
            } else if (is.getItem().getClass() != item) {
                continue;
            }

            if (is.getDamage() != 0 && is.getMaxDamage() != 0 && is.getDamage() / is.getMaxDamage()  < minDurability)
                continue;

            if (slot != null && !isEquippable(is, slot)) {
                continue;
            }

            if ((hs instanceof GenericContainerScreen && hs.getScreenHandler().getStacks().indexOf(is) < ((GenericContainerScreen) hs).getScreenHandler().getInventory().size()) || (hs instanceof InventoryScreen)) {
                // container tool
                if (isBetter(containerTool, is, keepSame)) {
                    containerSlot = hs.getScreenHandler().getStacks().indexOf(is);
                    containerTool = is;
                }
            }
        }

        if (isBetter(bestInventoryStack(keepSame, item, minDurability, slot), containerTool, keepSame))
            return containerSlot;

        // no point in returning if we already have something better
        return null;
    }

    /*@Nullable
    public static Integer betterItemSlot(HandledScreen hs, boolean keepSame, Class item, int minDurability, @Nullable EquipmentType type, boolean includeUnreachableSlots) {
        Integer containerSlot = null;
        ItemStack containerTool = null;

        List<ItemStack> items = hs.getScreenHandler().getStacks();
        if (includeUnreachableSlots) {
            items.addAll(mc.player.getInventory().armor);
            items.addAll(mc.player.getInventory().offHand);
        }
        for (ItemStack is : items) {
            if (item == FoodComponents.class && is.get(DataComponentTypes.FOOD) == null) {
                continue;
            } else if (item == ArmorItem.class && is.getItem().getClass() != ArmorItem.class) {
                continue;
            } else if (is.getItem().getClass() != item) {
                continue;
            }

            if (is.getDamage() != 0 && is.getMaxDamage() != 0 && is.getDamage() / is.getMaxDamage()  < minDurability)
                continue;

            if (type != null && !(is.getItem() instanceof ArmorItem && ((ArmorItem) is.getItem()).getType() == type)) {
                continue;
            }

            if ((hs instanceof GenericContainerScreen && hs.getScreenHandler().getStacks().indexOf(is) < ((GenericContainerScreen) hs).getScreenHandler().getInventory().size()) || (hs instanceof InventoryScreen)) {
                // container tool
                if (isBetter(containerTool, is, keepSame)) {
                    containerSlot = hs.getScreenHandler().getStacks().indexOf(is);
                    containerTool = is;
                }
            }
        }

        if (isBetter(bestInventoryStack(keepSame, item, minDurability, type), containerTool, keepSame))
            return containerSlot;

        // no point in returning if we already have something better
        return null;
    }*/

    public static List<Class> desirableItems() {
        List<Class> items = new ArrayList<>();

        items.add(SwordItem.class);
        items.add(AxeItem.class);
        items.add(PickaxeItem.class);
        items.add(ShovelItem.class);
        items.add(HoeItem.class);

        items.add(TridentItem.class);
        items.add(ShieldItem.class);
        items.add(FishingRodItem.class);
        items.add(BowItem.class);

        items.add(BlockItem.class);
        items.add(FoodComponents.class);

        items.add(BucketItem.class);
        items.add(EggItem.class);
        items.add(SnowballItem.class);
        items.add(SplashPotionItem.class);
        items.add(PotionItem.class);
        items.add(LingeringPotionItem.class);
        items.add(EnderPearlItem.class);
        items.add(ArrowItem.class);

        return items;
    }

    public static List<Class> desirableStackClass() {
        List<Class> items = new ArrayList<>();

        items.add(BlockItem.class);
        items.add(BucketItem.class);
        items.add(EggItem.class);
        items.add(SnowballItem.class);
        items.add(SplashPotionItem.class);
        items.add(LingeringPotionItem.class);
        items.add(PotionItem.class);
        items.add(EnderPearlItem.class);
        items.add(ArrowItem.class);

        return items;
    }

    public static List<Item> desirableStackItems() {
        List<Item> items = new ArrayList<>();

        items.add(Items.DIAMOND);
        items.add(Items.IRON_INGOT);
        items.add(Items.GOLD_INGOT);
        items.add(Items.NETHERITE_INGOT);

        items.add(Items.EXPERIENCE_BOTTLE);
        items.add(Items.ENCHANTED_BOOK);
        items.add(Items.TOTEM_OF_UNDYING);

        return items;
    }

    public static Action moveOneItem() {
        ACTION.type = SlotActionType.PICKUP;
        ACTION.two = true;
        ACTION.oneItem = true;
        return ACTION;
    }
    public static Action move() {
        ACTION.type = SlotActionType.PICKUP;
        ACTION.two = true;
        return ACTION;
    }

    public static Action click() {
        ACTION.type = SlotActionType.PICKUP;
        return ACTION;
    }

    /**
     * When writing code with quickSwap, both to and from should provide the ID of a slot, not the index.
     * From should be the slot in the hotbar, to should be the slot you're switching an item from.
     */
    public static Action quickSwap() {
        ACTION.type = SlotActionType.SWAP;
        return ACTION;
    }

    public static Action shiftClick() {
        ACTION.type = SlotActionType.QUICK_MOVE;
        return ACTION;
    }

    public static Action drop() {
        ACTION.type = SlotActionType.THROW;
        ACTION.data = 1;
        return ACTION;
    }

    public static void dropHand() {
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, ScreenHandler.EMPTY_SPACE_SLOT_INDEX, 0, SlotActionType.PICKUP, mc.player);
    }

    public static class Action {

        private SlotActionType type = null;
        private boolean two = false;
        private boolean oneItem = false;
        private int from = -1;
        private int to = -1;
        private int data = 0;

        private boolean isRecursive = false;

        private Action() {}

        // From
        public Action fromId(int id) {
            from = id;
            return this;
        }

        public Action from(int index) {
            return fromId(SlotUtils.indexToId(index));
        }

        public Action fromHotbar(int i) {
            return from(SlotUtils.HOTBAR_START + i);
        }

        public Action fromOffhand() {
            return from(SlotUtils.OFFHAND);
        }

        public Action fromMain(int i) {
            return from(SlotUtils.MAIN_START + i);
        }

        public Action fromArmor(int i) {
            return from(SlotUtils.ARMOR_START + (3 - i));
        }

        // To
        public void toId(int id) {
            to = id;
            run();
        }

        public void to(int index) {
            toId(SlotUtils.indexToId(index));
        }

        public void toHotbar(int i) {
            to(SlotUtils.HOTBAR_START + i);
        }

        public void toOffhand() {
            to(SlotUtils.OFFHAND);
        }

        public void toMain(int i) {
            to(SlotUtils.MAIN_START + i);
        }

        public void toArmor(int i) {
            to(SlotUtils.ARMOR_START + (3 - i));
        }

        // Slot
        public void slotId(int id) {
            from = to = id;
            run();
        }

        public void slot(int index) {
            slotId(SlotUtils.indexToId(index));
        }

        public void slotHotbar(int i) {
            slot(SlotUtils.HOTBAR_START + i);
        }

        public void slotOffhand() {
            slot(SlotUtils.OFFHAND);
        }

        public void slotMain(int i) {
            slot(SlotUtils.MAIN_START + i);
        }

        public void slotArmor(int i) {
            slot(SlotUtils.ARMOR_START + (3 - i));
        }

        // Other
        private void run() {
            boolean hadEmptyCursor = mc.player.currentScreenHandler.getCursorStack().isEmpty();

            if (type == SlotActionType.SWAP) {
                data = from;
                from = to;
            }

            if (type != null && from != -1 && to != -1) {
                click(from);
                if (two) {
                    if (oneItem) {
                        data = 1;
                        click(to);

                        data = 0;
                        click(from);
                    } else {
                        click(to);
                    }
                }
            }

            SlotActionType preType = type;
            boolean preTwo = two;
            int preFrom = from;
            int preTo = to;

            type = null;
            two = false;
            from = -1;
            to = -1;
            data = 0;

            if (!isRecursive && hadEmptyCursor && preType == SlotActionType.PICKUP && preTwo && (preFrom != -1 && preTo != -1) && !mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                isRecursive = true;
                InvUtils.click().slotId(preFrom);
                isRecursive = false;
            }
        }

        private void click(int id) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, data, type, mc.player);
        }
    }
}
