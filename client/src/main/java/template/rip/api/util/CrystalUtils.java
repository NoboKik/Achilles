package template.rip.api.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Rectangle;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static template.rip.Template.mc;

public class CrystalUtils {

    private static final HashMap<Entity, Boolean> brokenCrystals = new HashMap<>();
    private static final HashMap<LivingEntity, Integer> totemPops = new HashMap<>();
    public static final ConcurrentHashMap<Entity, Pair<Rectangle, Boolean>> positions = new ConcurrentHashMap<>();
    private static Matrix4f[] posAndProj;

    public static boolean isCrystalBroken(Entity crystal) {
        return brokenCrystals.containsKey(crystal);
    }

    public static boolean canPlaceCrystalServer(BlockPos pos, boolean crystalCheck) {
        if (mc.world == null) return false;

        BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) {
            return false;
        }

        BlockPos crystalPos = pos.up();
        BlockState crystalState = mc.world.getBlockState(crystalPos);
        if (!mc.world.isAir(crystalPos) || crystalState.getBlock() == Blocks.OBSIDIAN || crystalState.getBlock() == Blocks.BEDROCK) {
            return false;
        }

        if (crystalCheck) {
            Box box = new Box(crystalPos).offset(0.5, 0.0, 0.5).stretch(0.0, 2.0, 0.0);
            List<Entity> entities = mc.world.getEntitiesByClass(Entity.class, box, e -> !(e instanceof ClientPlayerEntity));
            for (Entity entity : entities) {
                if (entity instanceof EndCrystalEntity) {
                    return false;
                }
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent.Pre event) {
        if (event.target instanceof EndCrystalEntity) {
            brokenCrystals.put(event.target, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRemove(EntityRemoveEvent event) {
        if (event.entity instanceof EndCrystalEntity) {
            if (isCrystalBroken(event.entity))
                brokenCrystals.remove(event.entity);
        }
    }
    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet && packet.getStatus() == 35 && packet.getEntity(mc.world) instanceof LivingEntity entity) {
            totemPops.put(entity, totemPops.getOrDefault(entity, 0) + 1);
        }
    }

    // make stuff work in screens
    @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (!mc.mouse.isCursorLocked() || !mc.isWindowFocused()) {
            AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
            if (asm != null && asm.pauseMenuTick.isEnabled()) {
                Template.EVENTBUS.post(new MouseUpdateEvent.Post());
            }
        }
        try {
            posAndProj = new Matrix4f[]{(Matrix4f) event.context.positionMatrix().clone(), (Matrix4f) event.context.projectionMatrix().clone()};
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onTwoDeeTick(TwoDeePosTicker event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        Matrix4f[] pap = posAndProj;
        if (pap == null) {
            return;
        }
        Matrix4f lastOurPos = pap[0];
        Matrix4f lastOurProj = pap[1];
        if (lastOurPos == null || lastOurProj == null) {
            return;
        }
        ArrayList<Entity> ents = PlayerUtils.findTargets(true);
        for (Entity ent : ents) {
            positions.put(ent, RenderUtils.Render3D.twoDeePosition(ent, lastOurPos, lastOurProj));
        }
    }

    public static Set<Map.Entry<Entity, Pair<Rectangle, Boolean>>> getEntrySet() {
        return new HashSet<>(positions.entrySet());
    }

    @EventHandler
    public void onTick(PlayerTickEvent.Pre event) {
        if (mc.getOverlay() != null || mc.currentScreen != null) {
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
            if (asm != null && asm.pauseMenuTick.isEnabled()) {
                mc.handleInputEvents();
                if (mc.attackCooldown > 0) {
                    --mc.attackCooldown;
                }
            }
        }
    }

    public static int totemPops(LivingEntity livingEntity) {
        if (isDead(livingEntity)) {
            totemPops.put(livingEntity, 0);
        }
        return totemPops.getOrDefault(livingEntity, 0);
    }

    private static boolean isDead(LivingEntity entity) {
        return entity.isDead() || entity.getHealth() <= 0.0 || !entity.isPartOfGame() || entity.isInvulnerable() || !(mc.world.getEntityById(entity.getId()) instanceof LivingEntity);
    }
}
