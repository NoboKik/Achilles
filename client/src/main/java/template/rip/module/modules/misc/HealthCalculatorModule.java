package template.rip.module.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.world.GameMode;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.DamageUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HealthCalculatorModule extends Module {

    public enum modeEnum {Calculate, Scoreboard, Hoplite, BlocksMC}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Calculate, "Mode");

    public ConcurrentHashMap<LivingEntity, Float> calculatedDamage = new ConcurrentHashMap<>();
    public ConcurrentHashMap<LivingEntity, Float> serverCalcHealth = new ConcurrentHashMap<>();

    public HealthCalculatorModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        calculatedDamage.clear();
        serverCalcHealth.clear();
    }

    @EventHandler
    private void onAttack(AttackEntityEvent.Pre event) {
        if (!nullCheck() || mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
            return;

        Entity ent = event.target;
        if (ent instanceof LivingEntity entity && PlayerUtils.findTargets(true).contains(entity)) {
            if (PlayerUtils.isBlockedByShield(mc.player, entity, false))
                return;

            if (!serverCalcHealth.containsKey(entity))
                serverCalcHealth.put(entity, entity.getMaxHealth());

            calculatedDamage.put(entity, (float) DamageUtils.getSwordDamage(mc.player, entity));
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null)
            return;

        for (Entity e : PlayerUtils.findTargets()) {
            if (e instanceof LivingEntity le) {
                switch (mode.getMode()) {
                    case Calculate: {
                        // prevent server from overriding client health somehow
                        if (serverCalcHealth.containsKey(le)) {
                            if (serverCalcHealth.get(le) <= 0.0) {
                                serverCalcHealth.put(le, le.getMaxHealth());
                                continue;
                            }

                            le.setHealth(serverCalcHealth.get(le));
                        }
                        break;
                    }
                    case Scoreboard: {
                        Collection<ScoreboardObjective> sb = mc.world.getScoreboard().getObjectives();
                        for (ScoreboardObjective so : sb) {
                            Object2IntMap<ScoreboardObjective> map = so.getScoreboard().getScoreHolderObjectives(le);
                            for (Map.Entry<ScoreboardObjective, Integer> set : map.entrySet()) {
                                if (set.getKey().getDisplayName().getString().equals("❤"))
                                    le.setHealth(set.getValue().floatValue());
                            }
                        }
                        break;
                    }
                    case Hoplite: {
                        Collection<ScoreboardObjective> sb = mc.world.getScoreboard().getObjectives();
                        for (ScoreboardObjective so : sb) {
                            Object2IntMap<ScoreboardObjective> map = so.getScoreboard().getScoreHolderObjectives(le);
                            for (Map.Entry<ScoreboardObjective, Integer> set : map.entrySet()) {
                                if (set.getKey().getDisplayName().getString().equals("HEALTH_MODULE_LIST"))
                                    le.setHealth(set.getValue().floatValue());
                            }
                        }
                        break;
                    }
                    case BlocksMC: {
                        Collection<ScoreboardObjective> sb = mc.world.getScoreboard().getObjectives();
                        for (ScoreboardObjective so : sb) {
                            Object2IntMap<ScoreboardObjective> map = so.getScoreboard().getScoreHolderObjectives(le);
                            for (Map.Entry<ScoreboardObjective, Integer> set : map.entrySet()) {
                                if (set.getKey().getDisplayName().getString().equals("HealthTab"))
                                    le.setHealth(set.getValue().floatValue());
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        LivingEntity le = event.damaged;
        if (!serverCalcHealth.containsKey(le))
            serverCalcHealth.put(le, le.getMaxHealth());

        if (calculatedDamage.containsKey(le)) {
            Template.EVENTBUS.post(new EntityHealthEvent(le, serverCalcHealth.get(le), serverCalcHealth.get(le) - calculatedDamage.get(le), true));
            serverCalcHealth.put(le, serverCalcHealth.get(le) - calculatedDamage.get(le));
            calculatedDamage.remove(le);
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (mc.world == null)
            return;

        if (event.packet instanceof EntitySpawnS2CPacket) {
            Entity e = mc.world.getEntityById(((EntitySpawnS2CPacket) event.packet).getEntityId());
            LivingEntity le;
            if (e instanceof LivingEntity && serverCalcHealth.containsKey((le = ((LivingEntity) e))))
                serverCalcHealth.put(le, le.getMaxHealth());
        }
        if (event.packet instanceof EntitiesDestroyS2CPacket) {
            for (int id : ((EntitiesDestroyS2CPacket) event.packet).getEntityIds()) {
                Entity e = mc.world.getEntityById(id);
                if (e instanceof LivingEntity) {
                    serverCalcHealth.remove((LivingEntity) e);
                }
            }
        }
    }
}