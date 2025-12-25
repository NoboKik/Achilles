package template.rip.api.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import template.rip.api.event.events.GameJoinedEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.ArmorDamage;
import template.rip.api.object.ExplosionImpl;
import template.rip.api.object.FakePlayerEntity;

import java.util.Objects;

import static template.rip.Template.mc;

// stolen from meteor
public class DamageUtils {

    private static Vec3d vec3d = new Vec3d(0, 0, 0);
    private static Explosion explosion;
    private static RaycastContext raycastContext;

    public static void formatExc() {
        int i = Integer.valueOf("e");
    }

    @EventHandler
    private static void onGameJoined(GameJoinedEvent event) {
        explosion = new ExplosionImpl(mc.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);
        raycastContext = new RaycastContext(null, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player);
    }

    // Crystal damage
    public static double crystalDamage(LivingEntity player, Vec3d crystal, boolean predictMovement, BlockPos obsidianPos, boolean ignoreTerrain) {
        if (player == null || player instanceof PlayerEntity && PlayerUtils.getGameMode(((PlayerEntity)player)) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) return 0;

        vec3d = new Vec3d(player.getPos().x, player.getPos().y, player.getPos().z);
        if (predictMovement) vec3d = new Vec3d(vec3d.x + player.getVelocity().x, vec3d.y + player.getVelocity().y, vec3d.z + player.getVelocity().z);

        double modDistance = Math.sqrt(vec3d.squaredDistanceTo(crystal));
        if (modDistance > 12) return 0;

        double exposure = getExposure(crystal, player, predictMovement, raycastContext, obsidianPos, ignoreTerrain);
        double impact = (1 - (modDistance / 12)) * exposure;
        double damage = ((impact * impact + impact) / 2 * 7 * (6 * 2) + 1);

        damage = getDamageForDifficulty(damage);
        damage = DamageUtil.getDamageLeft(player, (float) damage, mc.world.getDamageSources().generic, (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).getValue());
        damage = resistanceReduction(player, damage);

        explosion = set(new EndCrystalEntity(mc.world, crystal.x, crystal.y, crystal.z), crystal, 6, false);
        damage = protectionReduction(player, (float) damage, mc.world.getDamageSources().explosion(explosion));

        return damage < 0 ? 0 : damage;
    }

    public static double crystalDamage(PlayerEntity player, Vec3d crystal) {
        return crystalDamage(player, crystal, false, null, false);
    }

    public static double nonReducedDamage(double damage, LivingEntity defender) {
        float tough = 0;
        float armor = 0;
        for (int i = 0; i < 4; i++) {
            Item stack = defender.getEquippedStack(ArmorDamage.armorSlots[i]).getItem();
            if (stack instanceof ArmorItem ai) {
                armor += InvUtils.getProtection(ai.getDefaultStack());
                tough += InvUtils.getToughness(ai.getDefaultStack());
                //armor += ai.getProtection();
                //tough += ai.getToughness();
            }
        }

        damage = normalProtIncrease(defender, damage);

        damage = getFullDamage((float) damage, armor, tough);

        return Math.max(damage, 0);
    }

    public static float getFullDamage(float damage, float armor, float armorToughness) {
        float f = 2.0f + armorToughness / 4.0f;
        float g = MathHelper.clamp(armor - damage / f, armor * 0.2f, 20.0f);
        return damage / (1.0f - g / 25.0f);
    }

    private static double normalProtIncrease(LivingEntity player, double damage) {// todo fix this
        int protLevel = 0;
        for (ItemStack stack : player.getArmorItems()) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : stack.getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.PROTECTION)) {
                    protLevel += ench.getIntValue();
                }
            }
        }
        if (protLevel > 20) protLevel = 20;

        if (protLevel <= 8) {//eugh
            return damage;
        }

        damage *= (protLevel * .24) - 1;
        return damage < 0 ? 0 : damage;
    }

    // Sword damage
    public static double getSwordDamage(PlayerEntity attacker, LivingEntity defender) {
        double damage = getSwordDamage(attacker);

        // Reduce by resistance
        damage = resistanceReduction(defender, damage);

        // Reduce by armour
        damage = DamageUtil.getDamageLeft(defender, (float) damage, mc.world.getDamageSources().generic, (float) defender.getArmor(), (float) defender.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        damage = normalProtReduction(defender, damage);

        return Math.max(damage, 0);
    }

    public static double getNonReducedDamage(PlayerEntity attacker, LivingEntity defender) {
        double damage = getSwordDamage(attacker);

        // Reduce by resistance
        damage = resistanceReduction(defender, damage);

        return Math.max(damage, 0);
    }

    public static double getSwordDamage(PlayerEntity attacker) {
        //tools add onto the players fist damage (1)
        double damage = 1;

        //bad solution for now but whatever
        if (attacker.getAttackCooldownProgress(0.5f) > 0.7f) {
            Item mh = attacker.getMainHandStack().getItem();
            if (mh instanceof SwordItem) {
                AttributeModifiersComponent mod = mh.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                if (mod != null) {
                    for (AttributeModifiersComponent.Entry entry : mod.modifiers()) {
                        if (entry.attribute() == EntityAttributes.ATTACK_DAMAGE) {
                            damage += (float) (entry.modifier().value());
                        }
                    }
                }
            }
            if (PlayerUtils.canCrit(attacker, false))
                damage *= 1.5;
        }

        if (attacker.getActiveItem().getEnchantments() != null) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : attacker.getMainHandStack().getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.SHARPNESS)) {
                    damage += (0.5 * ench.getIntValue()) + 0.5;
                    break;
                }
            }
        }

        if (attacker.getActiveStatusEffects().containsKey(StatusEffects.STRENGTH)) {
            int strength = Objects.requireNonNull(attacker.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            damage += 3 * strength;
        }

        if (attacker.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)) {
            int weakness = Objects.requireNonNull(attacker.getStatusEffect(StatusEffects.WEAKNESS)).getAmplifier() + 1;
            damage -= 4 * weakness;
        }

        return Math.max(damage, 0);
    }

    // Bed damage
    public static double bedDamage(LivingEntity player, Vec3d bed) {
        if (player instanceof PlayerEntity && ((PlayerEntity) player).getAbilities().creativeMode) return 0;

        double modDistance = Math.sqrt(player.squaredDistanceTo(bed));
        if (modDistance > 10) return 0;

        double exposure = ExplosionImpl.getExposure(bed, player);
        double impact = (1.0 - (modDistance / 10.0)) * exposure;
        double damage = (impact * impact + impact) / 2 * 7 * (5 * 2) + 1;

        // Multiply damage by difficulty
        damage = getDamageForDifficulty(damage);

        // Reduce by resistance
        damage = resistanceReduction(player, damage);

        // Reduce by armour
        damage = DamageUtil.getDamageLeft(player, (float) damage, mc.world.getDamageSources().generic, (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        explosion = set(bed, 5, true);
        damage = protectionReduction(player, (float) damage, mc.world.getDamageSources().explosion(explosion));

        if (damage < 0) damage = 0;
        return damage;
    }

    public static Explosion set(Explosion explosion, Vec3d explosionPos, float power, boolean createFire) {
        return new ExplosionImpl(mc.world, explosion.getEntity(), explosionPos.x, explosionPos.y, explosionPos.z, power, createFire, Explosion.DestructionType.DESTROY);
    }

    public static Explosion set(Entity entity, Vec3d explosionPos, float power, boolean createFire) {
        return new ExplosionImpl(mc.world, entity, explosionPos.x, explosionPos.y, explosionPos.z, power, createFire, Explosion.DestructionType.DESTROY);
    }

    public static Explosion set(Vec3d explosionPos, float power, boolean createFire) {
        return new ExplosionImpl(mc.world, null, explosionPos.x, explosionPos.y, explosionPos.z, power, createFire, Explosion.DestructionType.DESTROY);
    }

    // Anchor damage
    public static double anchorDamage(LivingEntity player, Vec3d anchor) {
        BlockState bl = mc.world.getBlockState(BlockPos.ofFloored(anchor));
        mc.world.removeBlock(BlockPos.ofFloored(anchor), false);
        double damage = bedDamage(player, anchor);
        mc.world.setBlockState(BlockPos.ofFloored(anchor), bl);
        return damage;
    }

    // Utils
    private static double getDamageForDifficulty(double damage) {
        /*return switch (mc.world.getDifficulty()) {
//            case PEACEFUL -> 0;
//            case EASY     -> Math.min(damage / 2 + 1, damage);
//            case HARD     -> damage * 3 / 2;
            default       -> damage * 3 / 2;
        };*/
        return damage * 3 / 2;
    }

    private static double normalProtReduction(LivingEntity player, double damage) {
        int protLevel = 0;
        for (ItemStack stack : player.getArmorItems()) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : stack.getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.PROTECTION)) {
                    protLevel += ench.getIntValue();
                }
            }
        }
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);
        return damage < 0 ? 0 : damage;
    }

    private static double blastProtReduction(LivingEntity player, double damage, Explosion explosion) {
        int protLevel = 0;
        for (ItemStack stack : player.getArmorItems()) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> ench : stack.getEnchantments().getEnchantmentEntries()) {
                if (ench.getKey().matchesKey(Enchantments.BLAST_PROTECTION)) {
                    protLevel += ench.getIntValue();
                }
            }
        }
        if (protLevel > 20) protLevel = 20;

        damage *= (1 - (protLevel / 25.0));
        return damage < 0 ? 0 : damage;
    }

    private static double resistanceReduction(LivingEntity player, double damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
            damage *= (1 - (lvl * 0.2));
        }

        return damage < 0 ? 0 : damage;
    }

    private static double getExposure(Vec3d source, Entity entity, boolean predictMovement, RaycastContext raycastContext, BlockPos obsidianPos, boolean ignoreTerrain) {
        Box box = entity.getBoundingBox();
        if (predictMovement) {
            Vec3d v = entity.getVelocity();
            box = box.offset(v.x, v.y, v.z);
        }

        double d = 1 / ((box.maxX - box.minX) * 2 + 1);
        double e = 1 / ((box.maxY - box.minY) * 2 + 1);
        double f = 1 / ((box.maxZ - box.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) / 2;
        double h = (1 - Math.floor(1 / f) * f) / 2;

        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for (double k = 0; k <= 1; k += d) {
                for (double l = 0; l <= 1; l += e) {
                    for (double m = 0; m <= 1; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                        vec3d = new Vec3d(n + g, o, p + h);
                        raycastContext = new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);

                        if (raycast(raycastContext, obsidianPos, ignoreTerrain).getType() == HitResult.Type.MISS) i++;

                        j++;
                    }
                }
            }

            return (double) i / j;
        }

        return 0;
    }

    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return damage;

        int damageProtection = 0;

        for (ItemStack itemStack : player.getAllArmorItems()) {
            if (!itemStack.isEmpty()) {
                Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemStack.getEnchantments().getEnchantmentEntries()) {
                    enchantments.put(entry.getKey(), entry.getIntValue());
                }

                int protection = getEnchantmentLevel(enchantments, Enchantments.PROTECTION);
                if (protection > 0) {
                    damageProtection += protection;
                }

                int fireProtection = getEnchantmentLevel(enchantments, Enchantments.FIRE_PROTECTION);
                if (fireProtection > 0 && source.isIn(DamageTypeTags.IS_FIRE)) {
                    damageProtection += 2 * fireProtection;
                }

                int blastProtection = getEnchantmentLevel(enchantments, Enchantments.BLAST_PROTECTION);
                if (blastProtection > 0 && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                    damageProtection += 2 * blastProtection;
                }

                int projectileProtection = getEnchantmentLevel(enchantments, Enchantments.PROJECTILE_PROTECTION);
                if (projectileProtection > 0 && source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                    damageProtection += 2 * projectileProtection;
                }

                int featherFalling = getEnchantmentLevel(enchantments, Enchantments.FEATHER_FALLING);
                if (featherFalling > 0 && source.isIn(DamageTypeTags.IS_FALL)) {
                    damageProtection += 3 * featherFalling;
                }
            }
        }

        return DamageUtil.getInflictedDamage(damage, damageProtection);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(itemEnchantments)) {
            if (entry.getKey().matchesKey(enchantment)) return entry.getIntValue();
        }
        return 0;
    }

    private static BlockHitResult raycast(RaycastContext context, BlockPos obsidianPos, boolean ignoreTerrain) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals(obsidianPos)) blockState = Blocks.OBSIDIAN.getDefaultState();
            else {
                blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600 && ignoreTerrain) blockState = Blocks.AIR.getDefaultState();
            }

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();

            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
        });
    }
}
