package template.rip.api.util;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import template.rip.Template;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static template.rip.Template.mc;

public class ProjectileUtilities {

    private static final List<ItemStack> oneArrow = List.of(new ItemStack(Items.ARROW));
    private static final List<ItemStack> threeArrow = List.of(new ItemStack(Items.ARROW), new ItemStack(Items.ARROW), new ItemStack(Items.ARROW));

    public static List<Pair<List<Vec3d>, HitResult>> predictCrossbowArrows(World world, LivingEntity shooter, ItemStack crossbow, int maxPredict) {
        List<ItemStack> list = ProjectileUtilities.getProjectiles(crossbow);
        List<Pair<List<Vec3d>, HitResult>> crossbowArrow = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemStack = list.get(i);
            if (i == 0) {
                crossbowArrow.add(ProjectileUtilities.predictCrossbow(world, shooter, crossbow, itemStack, 3.15f, 0, maxPredict));
                continue;
            }
            if (i == 1) {
                crossbowArrow.add(ProjectileUtilities.predictCrossbow(world, shooter, crossbow, itemStack, 3.15f, -10f, maxPredict));
                continue;
            }
            if (i != 2) continue;
            crossbowArrow.add(ProjectileUtilities.predictCrossbow(world, shooter, crossbow, itemStack, 3.15f, 10f, maxPredict));
        }
        return crossbowArrow;
    }

    public static Pair<List<Vec3d>, HitResult> predictCrossbow(World world, LivingEntity shooter, ItemStack crossbow, ItemStack projectile, float speed, float simulated, int maxPredict) {
        // no need for a no relative velocity setting, because crossbows are 1.14+

        ProjectileEntity projectileEntity;
        if (projectile.isOf(Items.FIREWORK_ROCKET)) {
            projectileEntity = new FireworkRocketEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - (double) 0.15f, shooter.getZ(), true);
        } else {
            projectileEntity = createArrowEntity(world, shooter, crossbow, projectile);
        }

        Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(simulated * ((float) Math.PI / 180), vec3d.x, vec3d.y, vec3d.z);
        Vec3d vec3d2 = shooter.getRotationVec(1.0f);
        Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);


        projectileEntity.setPosition(shooter.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)));
        // adding divergence will just make it more innacurate compared to what actually might happen when we shoot it, it'll all be fine when the arrow is actually spawned into the world though
        projectileEntity.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, 0);

        return projectilePredict(projectileEntity, maxPredict);
    }

    public static List<ItemStack> getProjectiles(ItemStack crossbow) {
        if (CrossbowItem.isCharged(crossbow)) {
            for (RegistryEntry<Enchantment> es : crossbow.getEnchantments().getEnchantments()) {
                if (es.matchesKey(Enchantments.MULTISHOT)) {
                    return threeArrow;
                }
            }
            return oneArrow;
        }
        return new ArrayList<>(0);
    }

    public static Pair<List<Vec3d>, HitResult> predtictTrident(World world, PlayerEntity playerEntity, ItemStack itemStack, int maxPredict) {
        TridentEntity te = new TridentEntity(world, playerEntity, itemStack);
        te.setPosition(playerEntity.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)));
        Rotation rot = Template.rotationManager().isEnabled() && playerEntity == mc.player ? Template.rotationManager().getClientRotation() : RotationUtils.entityRotation(playerEntity);
        // adding divergence will just make it more innacurate compared to what actually might happen when we shoot it, it'll all be fine when the arrow is actually spawned into the world though
        te.setVelocity(playerEntity, rot.fpitch(), rot.fyaw(), 0.0f, 2.5f, 0f);
        return projectilePredict(te, maxPredict);
    }

    public static Pair<List<Vec3d>, HitResult> predictRod(World world, PlayerEntity playerEntity, int maxPredict) {
        return projectilePredict(new FishingBobberEntity(playerEntity, world, 0, 0), maxPredict);
    }

    public static Pair<List<Vec3d>, HitResult> predictBow(World world, PlayerEntity playerEntity, int maxPredict, boolean relativeVelocity) {
        return predictBow(world, playerEntity, maxPredict, 0, relativeVelocity);
    }

    public static Pair<List<Vec3d>, HitResult> predictBow(World world, PlayerEntity playerEntity, int maxPredict, int minusUseTime, boolean relativeVelocity) {
        float f = BowItem.getPullProgress(playerEntity.getItemUseTime() - minusUseTime);

        ArrowEntity ae = new ArrowEntity(EntityType.ARROW, world);
        Vec3d vec3d = Vec3d.ZERO;
        if (!relativeVelocity) {
            vec3d = playerEntity.getVelocity();
            playerEntity.setVelocity(Vec3d.ZERO);
        }
        ae.setPosition(playerEntity.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)));
        Rotation rot = Template.rotationManager().isEnabled() && playerEntity == mc.player ? Template.rotationManager().getClientRotation() : RotationUtils.entityRotation(playerEntity);
        // adding divergence will just make it more innacurate compared to what actually might happen when we shoot it, it'll all be fine when the arrow is actually spawned into the world though
        ae.setVelocity(playerEntity, rot.fpitch(), rot.fyaw(), 0.0f, f * 3.0f, /*1.*/0f);
        if (!relativeVelocity) {
            playerEntity.setVelocity(vec3d);
        }
        return projectilePredict(ae, maxPredict);
    }

    public static Pair<List<Vec3d>, HitResult> throwableItem(World world, PlayerEntity pe, ItemStack is, int maxPredict, boolean relativeVelocity) {
        // snowballs and eggs are identical except for sound types
        EggEntity eggEntity = new EggEntity(world, pe, is);
        eggEntity.setItem(is);
        eggEntity.setPosition(pe.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)));
        Vec3d vec3d = Vec3d.ZERO;
        if (!relativeVelocity) {
            vec3d = pe.getVelocity();
            pe.setVelocity(Vec3d.ZERO);
        }
        Rotation rot = Template.rotationManager().isEnabled() && pe == mc.player ? Template.rotationManager().getClientRotation() : RotationUtils.entityRotation(pe);
        eggEntity.setVelocity(pe, rot.fpitch(), rot.fyaw(), 0.0f, 1.5f, 0f);
        if (!relativeVelocity) {
            pe.setVelocity(vec3d);
        }

        return projectilePredict(eggEntity, maxPredict);
    }

    public static Pair<List<Vec3d>, HitResult> projectilePredict(Entity toPredict, int maxPredict) {
        Vec3d velocity = toPredict.getVelocity();
        Vec3d pos = toPredict.getPos();
        if (toPredict instanceof ThrownEntity) {
            return projectilePredict(pos, velocity, toPredict, 0.03, maxPredict);
        }
        return projectilePredict(pos, velocity, toPredict, 0.05f, maxPredict);
    }


    public static Pair<List<Vec3d>, HitResult> projectilePredict(Vec3d pos, Vec3d velocity, Entity entity, double gravity, int maxPredict) {
        List<Vec3d> path = new ArrayList<>();
        HitResult hitResult = null;
        Vec3d lastPos = entity.getPos();
        for (int i = 0; i < maxPredict; i++) {
            path.add(pos);
            entity.setPosition(pos);
            hitResult = getCollision(pos, entity, Entity::canBeHitByProjectile, velocity, mc.world);
            if (hitResult.getType() == HitResult.Type.MISS) {
                Vec3d vec3d = velocity;
                double d = pos.getX() + vec3d.x;
                double e = pos.getY() + vec3d.y;
                double f = pos.getZ() + vec3d.z;
                BlockPos bpos = BlockPos.ofFloored(pos);
                double h = mc.world.getBlockState(bpos).isOf(Blocks.WATER) ? 0.8 : 0.99;
                velocity = vec3d.multiply(h);
                // we will probably always have gravity
                Vec3d vec3d2 = velocity;
                velocity = new Vec3d(vec3d2.x, vec3d2.y - gravity, vec3d2.z);
                pos = new Vec3d(d, e, f);
            } else break;
        }
        entity.setPosition(lastPos);
        if (mc.world.getEntityById(entity.getId()) == null) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
        return new Pair<>(path, hitResult);
    }

    private static HitResult getCollision(Vec3d pos, Entity entity, Predicate<Entity> predicate, Vec3d velocity, World world) {
        EntityHitResult hitResult2;
        Vec3d vec3d = pos.add(velocity);
        HitResult hitResult = world.raycast(new RaycastContext(pos, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
        if ((hitResult).getType() != HitResult.Type.MISS) {
            vec3d = hitResult.getPos();
        }
        if ((hitResult2 = getEntityCollision(world, entity, pos, vec3d, entity.getBoundingBox().stretch(velocity).expand(1.0), predicate, 0.3f)) != null) {
            hitResult = hitResult2;
        }
        return hitResult;
    }

    public static EntityHitResult getEntityCollision(World world, Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, float margin) {
        double d = Double.MAX_VALUE;
        Entity entity2 = null;
        for (Entity entity3 : world.getOtherEntities(entity, box, predicate)) {
            double e;
            Box box2 = entity3.getBoundingBox().expand(margin);
            Optional<Vec3d> optional = box2.raycast(min, max);
            if (optional.isEmpty() || !((e = min.squaredDistanceTo(optional.get())) < d)) continue;
            entity2 = entity3;
            d = e;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2);
    }

    private static ProjectileEntity createArrowEntity(World world, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
        return (itemStack2.getItem() instanceof ArrowItem arrowItem ? arrowItem : (ArrowItem)Items.ARROW).createArrow(world, itemStack2, livingEntity, itemStack);
    }
}
