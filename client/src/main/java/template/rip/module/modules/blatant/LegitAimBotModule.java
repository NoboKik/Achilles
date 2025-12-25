package template.rip.module.modules.blatant;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.modules.combat.AutoEatModule;
import template.rip.module.modules.combat.AutoHealModule;
import template.rip.module.modules.player.CombatBotModule;
import template.rip.module.setting.settings.*;

import java.awt.*;

public class LegitAimBotModule extends Module {

    public final BooleanSetting throughWalls = new BooleanSetting(this, true, "Through walls");
    public final NumberSetting aimDist = new NumberSetting(this, 6, 0, 12, 0.1, "Aim Distance");
    public final NumberSetting playerReach = new NumberSetting(this, 3, 0, 6, 0.1, "Reach");
    public final NumberSetting boxShrinkForTrack = new NumberSetting(this, 0.25, 0, 1, 0.01, "Box Shrink For Track");
    public final MinMaxNumberSetting yawSpeed = new MinMaxNumberSetting(this, 45, 90, 0, 180, 0.1, "Min Max Yaw");
    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 15, 45, 0, 90, 0.1, "Min Max Pitch");
    public final MinMaxNumberSetting yawNoise = new MinMaxNumberSetting(this, 2, 5, 0, 10, 0.1, "Yaw noise");
    public final MinMaxNumberSetting pitchNoise = new MinMaxNumberSetting(this, 1, 3, 0, 10, 0.1, "Pitch noise");
    public final MinMaxNumberSetting reactionTime = new MinMaxNumberSetting(this, 200, 300, 0, 500, 1, "Reaction Time");

    public enum modeEnum{Normal, Silent}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("Normal: Change player's rotation\nSilent: Change client's fake rotation"), modeEnum.Silent, "Rotation Mode");
    public final BooleanSetting renderAimPoint = new BooleanSetting(this, true, "Render Aim Point");
    public final ColorSetting aimPointLenienceColor = new ColorSetting(this, new JColor(JColor.GREEN), true, "Aim Point Lenience Color");
    public final ColorSetting aimPointTrackColor = new ColorSetting(this, new JColor(JColor.YELLOW), true, "Aim Point Track Color");
    public final ColorSetting aimPointFlickColor = new ColorSetting(this, new JColor(JColor.RED), true, "Aim Point Flick Color");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();

    private Vec3d targetVecOffset;
    public Box lastbox;
    private boolean doingFlick, doingTrack;
    private Rotation rot;
    private long timer;

    public LegitAimBotModule(Category category, Description description, String name) {
        super(category, description, name);
        aimPointLenienceColor.addConditionBoolean(renderAimPoint, true);
        aimPointTrackColor.addConditionBoolean(renderAimPoint, true);
        aimPointFlickColor.addConditionBoolean(renderAimPoint, true);
    }

    @Override
    public void onEnable() {
        doingFlick = false;
        rot = null;
        timer = System.currentTimeMillis() + reactionTime.getRandomInt();
        lastbox = null;
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        Entity target;
        if (targetVecOffset == null || (target = PlayerUtils.findFirstTarget()) == null)
            return;

        Color color;
        if (doingFlick) {
            color = aimPointFlickColor.getColor();
        } else if (doingTrack) {
            color = aimPointTrackColor.getColor();
        } else {
            color = aimPointLenienceColor.getColor();
        }

        if (renderAimPoint.isEnabled()) {
            Vec3d renderVec = PlayerUtils.minBox(PlayerUtils.renderBox(target)).add(targetVecOffset);
            Box render = new Box(renderVec.add(0.1, 0.1, 0.1), renderVec.subtract(0.1, 0.1, 0.1));
            RenderUtils.Render3D.renderBox(render, color, color.getAlpha(), event.context);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (disableInScreens.isEnabled() && mc.currentScreen != null)
            return;

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();

        if (target == null) {
            return;
        }

        Box targetBox = target.getBoundingBox();
        Box lenience = targetBox.expand(-boxShrinkForTrack.value);

        if (MathUtils.closestPosBoxDistance(targetBox) > aimDist.value || MathUtils.closestPosBoxDistance(lenience) > aimDist.value) {
            return;
        }

        if (AutoHealModule.stopAA()) {
            return;
        }

        Rotation start = mode.is(modeEnum.Normal) ? RotationUtils.entityRotation(mc.player) : Template.rotationManager().getClientRotation();
        boolean normalHR = validHit(PlayerUtils.getHitResult(mc.player, e -> e == target, start.fyaw(), start.fpitch(), aimDist.value, throughWalls.isEnabled(), 0));
        boolean lenienceHR = validHit(PlayerUtils.getHitResult(mc.player, e -> e == target, start.fyaw(), start.fpitch(), aimDist.value, throughWalls.isEnabled(), -boxShrinkForTrack.value));
        Vec3d targetVec;
        if (normalHR && !lenienceHR) {
            // tracking
            if (timer < System.currentTimeMillis()) {
                doingFlick = false;
                doingTrack = true;
                targetVec = getTrackVec(lenience);
                Rotation end = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), targetVec);
                Rotation turn = RotationUtils.getLimitedRotation(start, end, yawSpeed.getRandomDouble() / 5.0, pitchSpeed.getRandomDouble() / 5.0);
                turn = RotationUtils.addNoise(turn, yawNoise.getRandomDouble() * 2, pitchNoise.getRandomDouble() * 2);

                turn = RotationUtils.correctSensitivity(turn);

                rot = turn;
            }
        } else if (!normalHR && !lenienceHR) {
            // flick
            if (timer < System.currentTimeMillis()) {
                doingTrack = false;
                targetVec = getFlickVec(targetBox);
                Rotation end = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), targetVec);
                Rotation turn = RotationUtils.getLimitedRotation(start, end, yawSpeed.getRandomDouble(), pitchSpeed.getRandomDouble());
                turn = RotationUtils.addNoise(turn, yawNoise.getRandomDouble(), pitchNoise.getRandomDouble());

                turn = RotationUtils.correctSensitivity(turn);

                rot = turn;
            }
        } else {
            // do nothing.
            timer = System.currentTimeMillis() + reactionTime.getRandomInt();

            doingFlick = false;
            doingTrack = false;
        }

        if (rot != null) {
            if (mode.is(modeEnum.Normal)) {
                RotationUtils.setEntityRotation(mc.player, rot);
            } else {
                Template.rotationManager().setRotation(rot);
            }
        }
    }

    private void getVec(Box box) {
        int tries = 0;
        if (mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(MathUtils.farthestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box)) < playerReach.value) {
            // we are in reach
            Vec3d target = new Vec3d(MathUtils.getRandomDouble(box.minX, box.maxX), MathUtils.getRandomDouble(box.minY + (box.maxY - box.minY) / 2, box.maxY), MathUtils.getRandomDouble(box.minZ, box.maxZ));
            while (mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(target) > playerReach.value && tries < 500) {
                target = new Vec3d(MathUtils.getRandomDouble(box.minX, box.maxX), MathUtils.getRandomDouble(box.minY + (box.maxY - box.minY) / 2, box.maxY), MathUtils.getRandomDouble(box.minZ, box.maxZ));
                tries++;
            }
            targetVecOffset = target.subtract(PlayerUtils.minBox(box));
        } else {
            // we are not in reach
            Vec3d target = new Vec3d(MathUtils.getRandomDouble(box.minX, box.maxX), MathUtils.getRandomDouble(box.minY + (box.maxY - box.minY) / 2, box.maxY), MathUtils.getRandomDouble(box.minZ, box.maxZ));
            while (mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(target) > MathUtils.getRandomDouble(playerReach.value, aimDist.value) && tries < 500) {
                target = new Vec3d(MathUtils.getRandomDouble(box.minX, box.maxX), MathUtils.getRandomDouble(box.minY + (box.maxY - box.minY) / 2, box.maxY), MathUtils.getRandomDouble(box.minZ, box.maxZ));
                tries++;
            }
            targetVecOffset = target.subtract(PlayerUtils.minBox(box));
        }
    }

    public Vec3d getFlickVec(Box box) {
        lastbox = box;

        Vec3d playerhead = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        Vec3d close = MathUtils.closestPointToBox(playerhead, box);
        CombatBotModule cbm = Template.moduleManager.getModule(CombatBotModule.class);
        AutoEatModule aem = Template.moduleManager.getModule(AutoEatModule.class);

        if (cbm != null && cbm.isEnabled() && cbm.runAndEat.isEnabled() && aem != null && aem.eating && !mc.player.isUsingItem() && !cbm.isMinDistance()) {
            return MathUtils.Vec3dWithY(close, playerhead.getY()).add(RotationUtils.forwardVector(RotationUtils.getRotations(MathUtils.Vec3dWithY(close, playerhead.getY()), playerhead)).multiply(MathUtils.Vec3dWithY(close, playerhead.getY()).distanceTo(playerhead) + 3));
        }

        if (!doingFlick) {
            getVec(box);
            doingFlick = true;
        } else {
            double randomAmount = 0.1;
            targetVecOffset = MathUtils.fitVecInBox(PlayerUtils.minBox(box).add(targetVecOffset.add(MathUtils.getRandomDouble(-randomAmount, randomAmount), MathUtils.getRandomDouble(-randomAmount, randomAmount), MathUtils.getRandomDouble(-randomAmount, randomAmount))), box).subtract(PlayerUtils.minBox(box));
        }
        return PlayerUtils.minBox(box).add(targetVecOffset);
    }

    public Vec3d getTrackVec(Box box) {
        lastbox = box;

        Vec3d playerhead = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        Vec3d close = MathUtils.closestPointToBox(playerhead, box);
        CombatBotModule cbm = Template.moduleManager.getModule(CombatBotModule.class);
        AutoEatModule aem = Template.moduleManager.getModule(AutoEatModule.class);

        if (cbm != null && cbm.isEnabled() && cbm.runAndEat.isEnabled() && aem != null && aem.eating && !mc.player.isUsingItem() && !cbm.isMinDistance()) {
            return MathUtils.Vec3dWithY(close, playerhead.getY()).add(RotationUtils.forwardVector(RotationUtils.getRotations(MathUtils.Vec3dWithY(close, playerhead.getY()), playerhead)).multiply(MathUtils.Vec3dWithY(close, playerhead.getY()).distanceTo(playerhead) + 3));
        }

        Vec3d start = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).add(RotationUtils.forwardVector(RotationUtils.entityRotation(mc.player)).multiply(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(box.getCenter())));
        Vec3d end = MathUtils.closestPointToBox(start, new Box(box.minX, box.minY + (box.maxY - box.minY) / 2, box.minZ, box.maxX, box.maxY, box.maxZ));
        double randomAmount = 0.1;
        targetVecOffset = MathUtils.fitVecInBox(end.add(MathUtils.getRandomDouble(-randomAmount, randomAmount), MathUtils.getRandomDouble(-randomAmount, randomAmount), MathUtils.getRandomDouble(-randomAmount, randomAmount)), box).subtract(PlayerUtils.minBox(box));

        return PlayerUtils.minBox(box).add(targetVecOffset);
    }

    private boolean validHit(HitResult hr) {
        return hr instanceof EntityHitResult && hr.getType() == HitResult.Type.ENTITY;
    }
}
