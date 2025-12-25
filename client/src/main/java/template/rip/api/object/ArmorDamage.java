package template.rip.api.object;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableFloat;

import static template.rip.Template.mc;

import java.util.List;
import java.util.function.Consumer;

public class ArmorDamage {

    public static final EquipmentSlot[] armorSlots = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    public LivingEntity entity;
    public int[] slots;
    public int[] damage;

    public ArmorDamage(LivingEntity entity) {
        this.entity = entity;
        slots = new int[]{0, 1, 2, 3};
        damage = new int[]{0, 0, 0, 0};
    }

    public void setDamage() {
        for (int index : slots) {
            ItemStack itemStack = entity.getEquippedStack(armorSlots[index]);
            itemStack.setDamage(damage[index]);
        }
    }

    public void damageArmor(float amount) {
        if (amount <= 0.0f) {
            return;
        }
        if ((amount /= 4.0f) < 1.0f) {
            amount = 1.0f;
        }
        for (int index : slots) {
            ItemStack itemStack = entity.getEquippedStack(armorSlots[index]);
            if (!(itemStack.getItem() instanceof ArmorItem)) continue;
            damage(itemStack, (int)amount, entity, index);
        }
    }
    public <T extends LivingEntity> void damage(ItemStack stack, int amount, T entity, int index) {
        if (entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode) {
            return;
        }
        if (!stack.isDamageable()) {
            return;
        }
//      if (damage(stack, amount, entity.getRandom(), index)) {
//          uhhh
//      }
    }
    public boolean damage(ItemStack stack, int amount, Random random, int index) {
        int i;
        if (!stack.isDamageable()) {
            return false;
        }
        if (amount > 0 && (amount = getItemDamage(stack, random, amount)) <= 0) {
            return false;
        }

//      mc.inGameHud.getChatHud().addMessage(Text.of(" " + stack.getItem().getName().getString() +" " + amount));
        i = damage[index] + amount;
        damage[index] = i;
        return i >= stack.getMaxDamage();
    }

    private static int getItemDamage(ItemStack itemStack, Random random, int i2) {
        MutableFloat mutableFloat = new MutableFloat(i2);
        ItemEnchantmentsComponent itemEnchantmentsComponent = itemStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
            modifyItemDamage(entry.getKey().value(), entry.getIntValue(), random, itemStack, mutableFloat);
        }
        return mutableFloat.intValue();
    }

    private static void modifyItemDamage(Enchantment ench, int i, Random random, ItemStack itemStack, MutableFloat mutableFloat) {
        modifyValue(ench, random, i, itemStack, mutableFloat);
    }

    private static void modifyValue(Enchantment ench, Random random, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        applyEffects(ench.getEffect(EnchantmentEffectComponentTypes.ITEM_DAMAGE), enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.apply(i, random, mutableFloat.getValue())));
    }

    private static <T> void applyEffects(List<EnchantmentEffectEntry<T>> list, Consumer<T> consumer) {
        for (EnchantmentEffectEntry<T> enchantmentEffectEntry : list) {
//          if (!enchantmentEffectEntry.test(lootContext)) continue;
            consumer.accept(enchantmentEffectEntry.effect());
        }
    }
}
