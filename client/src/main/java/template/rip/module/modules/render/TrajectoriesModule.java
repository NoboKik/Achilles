package template.rip.module.modules.render;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.ProjectileUtilities;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TrajectoriesModule extends Module {

    public final NumberSetting maxPrediction = new NumberSetting(this, 100, 0, 1000, 1, "Max prediction");
    public final BooleanSetting relativeVelocity = new BooleanSetting(this, true, "Relative velocity");
    private final NumberSetting alphaVal = new NumberSetting(this, 100d, 0d, 255d, 1d, "Alpha");
    private static final ThreadPoolExecutor tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);

    public enum TrajType{Bow, Trident, Egg, Snowball, Ender_Pearl, Crossbow1, Crossbow2, Crossbow3, Projectile}
    public ConcurrentHashMap<Pair<Integer, TrajType>, net.minecraft.util.Pair<List<Vec3d>, HitResult>> projections = new ConcurrentHashMap<>();

    public TrajectoriesModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
         projections.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (!nullCheck()) {
            return;
        }

        tpe.execute(() -> {
            if (mc.world == null) {
                return;
            }

            int maxPredict = maxPrediction.getIValue();
            for (Entity e : mc.world.getEntities()) {
                if (e instanceof PlayerEntity pe) {
                    if (pe.getActiveItem().getItem() instanceof BowItem) {
                        projections.put(Pair.of(pe.getId(), TrajType.Bow), ProjectileUtilities.predictBow(mc.world, pe, maxPredict, relativeVelocity.isEnabled()));
                    }

                    if (pe.getActiveItem().getItem() instanceof TridentItem && !hasRiptide(pe.getActiveItem())) {
                        projections.put(Pair.of(pe.getId(), TrajType.Trident), ProjectileUtilities.predtictTrident(mc.world, pe, pe.getActiveItem(), maxPredict));
                    }

                    if (InvUtils.canUseItem(pe, Items.EGG)) {
                        ItemStack is = InvUtils.usableStack(pe, Items.EGG);
                        projections.put(Pair.of(pe.getId(), TrajType.Egg), ProjectileUtilities.throwableItem(mc.world, pe, is, maxPredict, relativeVelocity.isEnabled()));
                    }

                    if (InvUtils.canUseItem(pe, Items.SNOWBALL)) {
                        ItemStack is = InvUtils.usableStack(pe, Items.SNOWBALL);
                        projections.put(Pair.of(pe.getId(), TrajType.Snowball), ProjectileUtilities.throwableItem(mc.world, pe, is, maxPredict, relativeVelocity.isEnabled()));
                    }

                    if (InvUtils.canUseItem(pe, Items.ENDER_PEARL)) {
                        ItemStack is = InvUtils.usableStack(pe, Items.ENDER_PEARL);
                        projections.put(Pair.of(pe.getId(), TrajType.Ender_Pearl), ProjectileUtilities.throwableItem(mc.world, pe, is, maxPredict, relativeVelocity.isEnabled()));
                    }

                    if (InvUtils.canUseItem(pe, Items.CROSSBOW)) {
                        ItemStack is = InvUtils.usableStack(pe, Items.CROSSBOW);
                        List<TrajType> traj = Arrays.asList(TrajType.Crossbow1, TrajType.Crossbow2, TrajType.Crossbow3);
                        if (is != null && is.getItem() instanceof CrossbowItem) {
                            List<net.minecraft.util.Pair<List<Vec3d>, HitResult>> list = ProjectileUtilities.predictCrossbowArrows(mc.world, pe, is, maxPredict);
                            for (int i = 0; i < list.size(); i++) {
                                projections.put(Pair.of(pe.getId(), traj.get(i)), list.get(i));
                            }
                        }
                    }
                }

                if (e instanceof ArrowEntity && ((ArrowEntity) e).isInGround()) {
                    continue;
                }

                if (e instanceof TridentEntity && ((TridentEntity) e).isInGround()) {
                    continue;
                }

                if (e instanceof ProjectileEntity) {
                    projections.put(Pair.of(e.getId(), TrajType.Projectile), ProjectileUtilities.projectilePredict(e, maxPredict));
                }
            }
        });
    }

    private static boolean hasRiptide(ItemStack stack) {
        for (RegistryEntry<Enchantment> entry : stack.getEnchantments().getEnchantments()) {
            if (entry.matchesKey(Enchantments.RIPTIDE)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (mc.world == null)
            return;

        int alpha = alphaVal.getIValue();
        for (Map.Entry<Pair<Integer, TrajType>, net.minecraft.util.Pair<List<Vec3d>, HitResult>> entry : projections.entrySet()) {
            Entity e = mc.world.getEntityById(entry.getKey().left());
            if (e instanceof PlayerEntity pe) {
                int col = pe.getTeamColorValue();
                Color color = new Color((col >> 16) & 0xFF, (col >> 8) & 0xFF, col & 0xFF);
                if (pe.getActiveItem().getItem() instanceof BowItem && entry.getKey().right() == TrajType.Bow) {
                    RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
                }

                if (pe.getActiveItem().getItem() instanceof TridentItem && !hasRiptide(pe.getActiveItem()) && entry.getKey().right() == TrajType.Trident) {
                    RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
                }

                if (InvUtils.canUseItem(pe, Items.EGG) && entry.getKey().right() == TrajType.Egg) {
                    RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
                }

                if (InvUtils.canUseItem(pe, Items.SNOWBALL) && entry.getKey().right() == TrajType.Snowball) {
                    RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
                }

                if (InvUtils.canUseItem(pe, Items.ENDER_PEARL) && entry.getKey().right() == TrajType.Ender_Pearl) {
                    RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
                }

                if (InvUtils.canUseItem(pe, Items.CROSSBOW)) {
                    ItemStack is = InvUtils.usableStack(pe, Items.CROSSBOW);
                    if (is != null && is.getItem() instanceof CrossbowItem) {
                        if (Arrays.asList(TrajType.Crossbow1, TrajType.Crossbow2, TrajType.Crossbow3).contains(entry.getKey().right())) {
                            RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
                        }
                    }
                }
            }

            if (e instanceof ArrowEntity && ((ArrowEntity) e).isInGround()) {
                continue;
            }

            if (e instanceof TridentEntity && ((TridentEntity) e).isInGround()) {
                continue;
            }

            if (e instanceof ProjectileEntity && entry.getKey().right() == TrajType.Projectile) {
                ProjectileEntity pe = (ProjectileEntity) e;
                Color color = Color.WHITE;
                if (pe.getOwner() != null) {
                    int col = pe.getOwner().getTeamColorValue();
                    color = new Color((col >> 16) & 0xFF, (col >> 8) & 0xFF, col & 0xFF);
                }
                RenderUtils.Render3D.renderPrediction(entry.getValue(), true, color, alpha, event.context);
            }
        }
    }
}
