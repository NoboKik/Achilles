package template.rip.module.modules.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.blatant.LegitAimBotModule;
import template.rip.module.modules.combat.AimAssistModule;
import template.rip.module.modules.combat.AutoEatModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CombatBotModule extends Module {

    public final BooleanSetting onlyWithWeapon = new BooleanSetting(this, true, "Only with a weapon");
    public final BooleanSetting moveFor = new BooleanSetting(this, true, "Forward");
    public final BooleanSetting moveSide = new BooleanSetting(this, true, "Strafe");
    public final NumberSetting strafeTimerAfterHit = new NumberSetting(this, 50, 0, 2500, 1, "Strafe timer after hit");
    public final MinMaxNumberSetting directionTimer = new MinMaxNumberSetting(this, 300, 600, 0, 1250, 1, "Direction Timer");
    public final BooleanSetting moveJump = new BooleanSetting(this, true, "Jumping");
    public final MinMaxNumberSetting jumpDist = new MinMaxNumberSetting(this, 6.5, 8, 0, 9, 0.1, "Jump Distance");
    public final BooleanSetting dodgeArrows = new BooleanSetting(this, true, "Dodge Arrows");
    public final NumberSetting dodgeThresh = new NumberSetting(this, 3, 0, 6, 0.1, "Dodge Threshold");
    public final BooleanSetting runAndEat = new BooleanSetting(this, true, "Run and Eat");
    public final NumberSetting minEatDistance = new NumberSetting(this, 4.0, 0, 6, 0.1, "Min Eat Distance");
    public final NumberSetting fallHealthper = new NumberSetting(this, 0.33, 0, 1, 0.1, "FallBack Health %");
    public final BooleanSetting chase = new BooleanSetting(this, true, "Chase runners");
    public final NumberSetting advantageTicksToChase = new NumberSetting(this, 10, 0, 50, 1, "Run Ticks for chase");
    public final BooleanSetting debugOut = new BooleanSetting(this, true, "Debug");

    private PlayerEntity lastTarget;
    private boolean firstKBHit;
    private long hitTime, oneDirStopWatch;
    private int strafeDir, advantageTicks;

    public CombatBotModule() {
        super(Category.PLAYER, Description.of("Automatically performs combat maneuvers for effective 1.9+ sword pvp gameplay."), "CombatBot");
    }

    @Override
    public void onEnable() {
        lastTarget = null;
        firstKBHit = false;
        hitTime = 0;
        strafeDir = 0;
        oneDirStopWatch = 0;
        advantageTicks = 0;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onAttack(AttackEntityEvent.Pre event) {
        if (!nullCheck() ||!shouldBot() ) {
            return;
        }

        if (mc.player.isSprinting()) {
            firstKBHit = true;
        }
        hitTime = System.currentTimeMillis() + strafeTimerAfterHit.getIValue();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onInput(InputEvent event) {
        if (!nullCheck() || !event.check || !shouldBot()) {
            return;
        }

        Entity current = PlayerUtils.findFirstTarget();
        if (lastTarget != current && current instanceof PlayerEntity) {
            lastTarget = (PlayerEntity) current;
        }

        if (lastTarget == null) {
            onEnable();
            return;
        }

        StringBuilder debug = new StringBuilder();
        if (moveSide.isEnabled()) {
            if (hitTime > System.currentTimeMillis()) {
                if (strafeDir != 0) {
                    event.input.movementSideways = strafeDir;
                }
                if (oneDirStopWatch < System.currentTimeMillis()) {
                    oneDirStopWatch = System.currentTimeMillis() + directionTimer.getRandomInt();
                    strafeDir = MathUtils.getRandomInt(-1, 1);

                }
                debug.append(strafeDir).append(" ");
            }
        }

        if (moveFor.isEnabled()) {
            if (MathUtils.closestPosBoxDistance(lastTarget.getBoundingBox()) > 0.25 && (Template.moduleManager.isModuleEnabled(AimAssistModule.class) || Template.moduleManager.isModuleEnabled(LegitAimBotModule.class))) {
                event.input.movementForward = 1f;
            }
        }

//      debug.append(firstKBHit).append(" ");

        if (moveJump.isEnabled()) {
            if ((jumpDist.containsNumber(MathUtils.closestPosBoxDistance(lastTarget.getBoundingBox())) || (firstKBHit && mc.player.hurtTime != 0)) || mc.player.horizontalCollision || lastTarget.isUsingItem()) {
                event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
            }
        }
        if (!mc.player.isSprinting()) {
            firstKBHit = false;
        }

        if (dodgeArrows.isEnabled()) {
            List<Vec3d> vectors = new ArrayList<>();

            ItemStack is = InvUtils.usableStack(lastTarget, Items.BOW);
            if (is != null) {
                vectors.addAll(ProjectileUtilities.predictBow(mc.world, lastTarget, 250, true).getLeft());
            }

            is = InvUtils.usableStack(lastTarget, Items.CROSSBOW);
            if (is != null) {
                List<Pair<List<Vec3d>, HitResult>> arrows = ProjectileUtilities.predictCrossbowArrows(mc.world, lastTarget, is, 250);
                if (!arrows.isEmpty()) {
                    vectors.addAll(arrows.get(0).getLeft());
                }
            }

            for (Entity e : mc.world.getEntities()) {
                if (e instanceof ArrowEntity arrow && !arrow.isOnGround()) {
                    vectors.addAll(ProjectileUtilities.projectilePredict(e, 250).getLeft());
                }
            }

            vectors.sort(Comparator.comparing(vec -> MathUtils.closestPosBoxDistance(vec, mc.player.getBoundingBox())));
            vectors.removeIf(vec -> MathUtils.closestPosBoxDistance(vec, mc.player.getBoundingBox()) > dodgeThresh.value);
//          debug.append(" s: ").append(vectors.size());
            if (!vectors.isEmpty()) {
                Vec3d closest = vectors.get(0);
                event.input.movementSideways = (float) -PlayerUtils.correctedInputForPos(closest).getRight();
            }
        }

        if (runAndEat.isEnabled() && shouldRun()) {
            if (mc.player.isSprinting()) {
                event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
            }
            if (mc.player.isUsingItem()) {
                event.input.movementForward = -1;
            }
        }

        if (chase.isEnabled()) {
            debug.append(" Advantage: ").append(advantageTicks);
            if (advantageTicksToChase.getValue() <= advantageTicks) {
                if (mc.player.isSprinting()) {
                    event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
                }
            }
        }

        if (debugOut.isEnabled() && !debug.isEmpty()) {
            mc.inGameHud.getChatHud().addMessage(Text.of(debug.toString()));
        }
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (!nullCheck() || !shouldBot() || lastTarget == null) {
            return;
        }
        if (mc.player.getEyePos().distanceTo(lastTarget.getPos()) > PlayerUtils.lastPosVecEye(mc.player).distanceTo(PlayerUtils.lastPosVec(lastTarget))) {
            if (lastTarget.hurtTime == 0) {
                advantageTicks++;
            }
        } else {
            advantageTicks = 0;
        }
    }

    public boolean shouldBot() {
        return !onlyWithWeapon.isEnabled() || (mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem);
    }

    public boolean shouldRun() {
        AutoEatModule aem = Template.moduleManager.getModule(AutoEatModule.class);
        return (aem != null && aem.eating && canEat()) || mc.player.isUsingItem();
    }

    public boolean canEat() {
        AimAssistModule asm = Template.moduleManager.getModule(AimAssistModule.class);
        if (asm != null && asm.lastBox != null) {
            double diff = RotationUtils.getTotalDiff(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), asm.getTargetPos(asm.lastBox)), Template.rotationManager().rotation());
//            mc.inGameHud.getChatHud().addMessage(Text.of("Diff: " + diff));
            return diff < 15;
        }
        LegitAimBotModule asmn = Template.moduleManager.getModule(LegitAimBotModule.class);
        if (asmn != null && asmn.lastbox != null) {
            double diff = RotationUtils.getTotalDiff(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), asmn.getFlickVec(asmn.lastbox)), Template.rotationManager().rotation());
//            mc.inGameHud.getChatHud().addMessage(Text.of("Diff: " + diff));
            return diff < 15;
        }
        return false;
    }

    public boolean isMinDistance() {
        if (lastTarget != null) {
            return MathUtils.closestPosBoxDistance(lastTarget.getBoundingBox()) > minEatDistance.value || Math.min((mc.player.getHealth() + mc.player.getAbsorptionAmount()) / mc.player.getMaxHealth(), 1) < fallHealthper.value;
        }
        return false;
    }
}
