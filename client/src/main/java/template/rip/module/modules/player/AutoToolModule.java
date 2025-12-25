package template.rip.module.modules.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.modules.blatant.NukerModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;

public class AutoToolModule extends Module {

    private final BooleanSetting switchTools = new BooleanSetting(this, true, "Switch Tools");
    private final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch back");
    private final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 0, 1, 0, 10, 1, "Switch delay");
    private final BooleanSetting onlyWhenSneak = new BooleanSetting(this, false, "Only when sneak");
    private final BooleanSetting switchWeapons = new BooleanSetting(this, false, "Switch Weapons");
    private final BooleanSetting axeWeapons = new BooleanSetting(this, false, "Axes are weapons");

    private int tick = 0;
    private int switchTick = 0;
    private int lastSlot = -1;

    public AutoToolModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onAttack(AttackEntityEvent.Pre event) {
        if (!nullCheck() || !switchWeapons.isEnabled()) return;

        if (event.target instanceof PlayerEntity) {
            InvUtils.setInvSlot(getbestWeapon());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || !switchTools.isEnabled()) return;

        if (!mc.options.attackKey.isPressed() && !NukerModule.isNuking) {
            if (lastSlot != -1 && switchBack.isEnabled()) {
                if (switchTick > 0) {
                    switchTick--;
                    return;
                } else {
                    switchTick = switchDelay.getRandomInt();
                }
                mc.player.getInventory().selectedSlot = lastSlot;
                lastSlot = -1;
            }
            return;
        } else {
            switchTick = switchDelay.getRandomInt();
        }

        if (onlyWhenSneak.isEnabled() && !mc.player.isSneaking()) {
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult bhr))
            return;

        BlockState bs = mc.world.getBlockState(bhr.getBlockPos());
        if (getbestSlot(bs) != mc.player.getInventory().selectedSlot && bhr.getType() != HitResult.Type.MISS) {
            if (tick > 0) {
                tick--;
                return;
            } else {
                tick = switchDelay.getRandomInt();
            }
            if (lastSlot == -1) {
                lastSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = getbestSlot(bs);
        } else {
            tick = switchDelay.getRandomInt();
        }
    }

    public static int getbestSlot(BlockState bs) {
        int best = 0;
        float bestSpeed = 0;
        for (int slot = 0; slot < 9; slot++) {
            float  f = getBreakSpeed(bs, slot);
            if (f > bestSpeed) {
                bestSpeed = f;
                best = slot;
            }
        }
        if (bestSpeed == getBreakSpeed(bs, mc.player.getInventory().selectedSlot))
            return mc.player.getInventory().selectedSlot;

        return best;
    }

    public int getbestWeapon() {
        int best = 0;
        float bestDmg = 0;
        for (int slot = 0; slot < 9; slot++) {
            float f = dmg(mc.player.getInventory().getStack(slot));
            if (f > bestDmg) {
                bestDmg = f;
                best = slot;
            }
        }

        if (bestDmg == dmg(mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot)))
            return mc.player.getInventory().selectedSlot;

        return best;
    }

    public float dmg(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem)
            return InvUtils.getAttackDamage(stack);

        if (stack.getItem() instanceof AxeItem && axeWeapons.isEnabled())
            return InvUtils.getAttackDamage(stack);

        return 0f;
    }

    public static float getBreakSpeed(BlockState block, int slot) {
        // copy and pasted code from minecraft itself!!!
        float f = mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(block);
        if (f > 1.0f) {
            ItemStack itemStack = mc.player.getInventory().getStack(slot);
            int effLevel = 0;
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : itemStack.getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.EFFICIENCY)) {
                    effLevel = ench.getIntValue();
                    break;
                }
            }
            if (effLevel > 0 && !itemStack.isEmpty()) {
                f += (float) (effLevel * effLevel + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 : f *=  0.3f; break;
                case 1 : f *=  0.09f; break;
                case 2 : f *=  0.0027f; break;
                default : f *=  8.1E-4f; break;
            }
        }
        if (mc.player.isSubmergedInWater()) {
            f /= 5.0f;
        }
        if (!mc.player.isOnGround()) {
            f /= 5.0f;
        }
        return f;
    }
}
