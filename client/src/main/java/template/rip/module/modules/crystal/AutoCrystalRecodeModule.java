package template.rip.module.modules.crystal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.blatant.MultiTaskModule;
import template.rip.module.setting.settings.*;

public class AutoCrystalRecodeModule extends Module {

    private enum State{Place, Break, Equal}
    public final MinMaxNumberSetting placeDelays = new MinMaxNumberSetting(this, 0, 1, 0, 10, 1, "Place Delays");
    public final MinMaxNumberSetting breakDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Break Delays");
    public enum modeEnum {Legit, Fast}
    public final ModeSetting<modeEnum> placeMode = new ModeSetting<>(this, Description.of("Legit: Normal minecraft crosshair mechanics with no changes\nFast: Changes your crosshair target to place faster after breaking a crystal"), modeEnum.Legit, "Place Crosshair Target Mode");
    public final BooleanSetting killCrystal = new BooleanSetting(this, Description.of("Kills the crystal client side when you attack it"), false, "Crystal Optimizer");
    public final BooleanSetting hurtSync = new BooleanSetting(this, false, "HurtTime Sync");
    public final NumberSetting predictTicks = new NumberSetting(this, 2, 0, 5, 1, "HurtTime Predict Ticks").setAdvanced();
    public final BooleanSetting usPvpMcPVp = new BooleanSetting(this, false, "USPVP/MCPVP Break");
    public final BooleanSetting noCountGlitch = new BooleanSetting(this, Description.of("Correctly decrements crystals"), true, "No Count Glitch");
    public final BooleanSetting noBounce = new BooleanSetting(this, Description.of("Prevents crystals from bouncing in your hotbar"), true, "No Bounce");
    public final BooleanSetting onlyOnRMB = new BooleanSetting(this, true, "Only On RMB");
    public final NumberSetting onDeathCooldown = new NumberSetting(this, 500, 0, 5000, 1, "Death Timeout");
    public final DividerSetting headBob = new DividerSetting(this, false, "Bob Settings");
    public final BooleanSetting headBobEnabled = new BooleanSetting(this, false, "Head Bobbing");
    public enum rotEnum {Normal, Silent}
    public final ModeSetting<rotEnum> rotationMode = new ModeSetting<>(this, rotEnum.Normal, "Rotation Mode");
    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 5d, 7d, 0.1, 10d, 0.1, "Pitch Speeds");
    public final MinMaxNumberSetting yawNoise = new MinMaxNumberSetting(this, 0.5, 1, 0, 5, 0.1, "Yaw noise");
    public final BooleanSetting output = new BooleanSetting(this, false, "Output");
    public final BooleanSetting extraOutput = new BooleanSetting(this, false, "Extra Output");
    public int placesSinceLastHit, breaksSinceLastHit;
    private int placeDelay, breakDelay;
    private long placeTime, breakTime, deathTime;
    private boolean queuedSword, inLowDmgTick, brokeForThisHurtTick;
    public boolean autoHitCrystalPlacedObsidian, autoHitCrystalLimit;
    private BlockPos placePos, lastBobPlace;
    private LivingEntity target;

    public AutoCrystalRecodeModule(Category category, Description description, String name) {
        super(category, description, name);
        headBob.addSetting(headBobEnabled, rotationMode, pitchSpeed, yawNoise);
    }

    @Override
    public void onEnable() {
        placeDelay = placeDelays.getRandomInt();
        breakDelay = breakDelays.getRandomInt();
        placeTime = 0;
        breakTime = 0;
        placePos = null;
        lastBobPlace = null;
        queuedSword = false;
        inLowDmgTick = false;
        brokeForThisHurtTick = false;
        deathTime = 0;
        placesSinceLastHit = 0;
        breaksSinceLastHit = 0;
        autoHitCrystalPlacedObsidian = false;
        autoHitCrystalLimit = false;
    }

    @EventHandler
    void onDamage(DamageEvent event) {
        if (target != null && event.damaged == target) {
            if (queuedSword) {
                inLowDmgTick = true;
                queuedSword = false;
            }
            brokeForThisHurtTick = false;
        }
    }

    @EventHandler
    void onAttack(AttackEntityEvent.Post event) {
        if (target != null && target == event.target) {
            queuedSword = true;
        }
    }

    @EventHandler
    void onStatus(PacketEvent.Receive event) {
        EntityStatusS2CPacket status;
        if (event.packet instanceof EntityStatusS2CPacket && (status = ((EntityStatusS2CPacket) event.packet)).getStatus() == 3) {// status 3 is death
            Entity le = PlayerUtils.findFirstTarget();
            if (le != null && status.getEntity(mc.world) == le) {
                deathTime = System.currentTimeMillis() + onDeathCooldown.getIValue();
            }
        }
    }

    /*@EventHandler
    void onRender(WorldRenderEvent event) {
        State action = nextAction();
        if (lastTarget != null && action != State.Equal) {
            RenderUtils.Render3D.renderBox(new Box(lastTarget.subtract(0.1, 0.1, 0.1), lastTarget.add(0.1, 0.1, 0.1)), action == State.Break ? Color.MAGENTA : Color.cyan, 50, event.context);
        }
        if (lastForward != null) {
            RenderUtils.Render3D.renderBox(new Box(lastForward.subtract(0.1, 0.1, 0.1), lastForward.add(0.1, 0.1, 0.1)), Color.GREEN, 50, event.context);
        }
        if (lastBox != null) {
            RenderUtils.Render3D.renderBox(lastBox, Color.BLUE, 30, event.context);
        }
    }*/

    @EventHandler
    void onMouse(MouseUpdateEvent.Post event) {
        if (!requisites()) {
            return;
        }

        if (!headBobEnabled.isEnabled()) {
            return;
        }
        if (lastBobPlace == null) {
            return;
        }
        if (lastBobPlace.getY() != mc.player.getBlockY()) {
            return;
        }
        Vec3d target;
        Box box = new Box(lastBobPlace.up()).offset(-0.5, 0.0, -0.5).stretch(1, 1, 1);

        if (box.expand(0.5).contains(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)))) {
            return;
        }

        State action = nextAction();

        switch (action) {
            case Break: {
                box = box.contract(0.05);
                target = MathUtils.closestPointToBox(box);
                break;
            }
            case Place: {
                box = box.expand(0.0, MathUtils.getRandomDouble(0.3, 0.5), 0.0);
                Vec3d closest = MathUtils.closestPointToBox(box);
                target = MathUtils.Vec3dWithY(closest, box.minY);
                break;
            }
            default: {
                return;
            }
        }

        float yaw = (float) MathHelper.wrapDegrees(Template.rotationManager().yaw() + MathUtils.getRandomDouble(-(yawNoise.getRandomDouble() / 2.0), yawNoise.getRandomDouble() / 2.0));
        Rotation targetRot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target).withfYaw(yaw);
        HitResult hr = PlayerUtils.getHitResult(mc.player, targetRot.fyaw(), targetRot.fpitch());

        boolean hit = switch (action) {
            case Place -> hr.getType() == HitResult.Type.BLOCK && BlockUtils.crystalBlock(((BlockHitResult) hr).getBlockPos());
            case Break -> {
                Vec3d forwardVec = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).add(RotationUtils.forwardVector(targetRot).multiply(3.0));
                yield box.raycast(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), forwardVec).isPresent();
            }
            default -> false;
        };

        if (!hit) {
            return;
        }

        double pitchStrength = pitchSpeed.getRandomDouble() / 50.0;
        float pitch = MathHelper.lerpAngleDegrees((float) pitchStrength, rotationMode.is(rotEnum.Normal) ? mc.player.getPitch() : Template.rotationManager().getClientRotation().fpitch(), targetRot.fpitch());
        Rotation finalRot = RotationUtils.correctSensitivity(targetRot.withfPitch(pitch));

        switch (rotationMode.getMode()) {
            case Silent -> Template.rotationManager().setRotation(finalRot);
            case Normal -> {
                Pair<Double, Double> pair = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(finalRot, RotationUtils.entityRotation(mc.player)));
                mc.player.changeLookDirection(pair.getLeft(), pair.getRight());
            }
        }
    }

    State nextAction() {
        if (breakTime > placeTime) {
            return State.Place;
        }
        if (breakTime < placeTime && System.currentTimeMillis() - placeTime < 1000) {
            return State.Break;
        }

        return State.Equal;
    }

    boolean requisites() {
        if (!nullCheck()) {
            return false;
        }

        if (onlyOnRMB.isEnabled() && !KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode())) {
            AutoHitCrystalModule ahc = Template.moduleManager.getModule(AutoHitCrystalModule.class);
            if (ahc == null || !ahc.isEnabled() || !ahc.crystalling) {
                return false;
            }
        }

        if (!mc.player.isHolding(Items.END_CRYSTAL) && placeTime < breakTime) {
            return false;
        }

        if (autoHitCrystalLimit && placesSinceLastHit >= 2 && breaksSinceLastHit >= 2 && autoHitCrystalPlacedObsidian) {
            return false;
        }

        MultiTaskModule mtm = Template.moduleManager.getModule(MultiTaskModule.class);
        if (mc.player.isUsingItem() && !(mtm != null && mtm.isEnabled() && mtm.attack.isEnabled())) {
            return false;
        }

        if (System.currentTimeMillis() < deathTime) {
            if (extraOutput.isEnabled()) {
                mc.inGameHud.getChatHud().addMessage(Text.of("Death Time: " + (deathTime - System.currentTimeMillis())));
            }
            return false;
        }
        return true;
    }

    @EventHandler
    public void onInput(HandleInputEvent.Pre event) {
        if (!requisites()) {
            return;
        }

        target = PlayerUtils.findFirstLivingTargetOrNull();
        breakCrystal();
        if (autoHitCrystalLimit && breaksSinceLastHit >= 2 && autoHitCrystalPlacedObsidian) {
            return;
        }
        placeCrystal();
    }

    boolean hurtSync() {
        boolean hurt = target.hurtTime - predictTicks.getIValue() == 0 || target.hurtTime == 0;
        if (brokeForThisHurtTick) {
            if (target.hurtTime == 0) {
                brokeForThisHurtTick = false;
            }
            return false;
        }
        if (inLowDmgTick) {
            inLowDmgTick = false;
            brokeForThisHurtTick = true;
            return true;
        } else if (hurt) {
            brokeForThisHurtTick = true;
            return true;
        }
        return false;
    }

    private void breakCrystal() {
        if (breakTime > placeTime && System.currentTimeMillis() - placeTime < 500) {
            if (extraOutput.isEnabled()) {
                mc.inGameHud.getChatHud().addMessage(Text.of("Break before Place: " + (System.currentTimeMillis() - placeTime)));
            }
            return;
        }
        HitResult hr = getHit();
        if (hr == null) {
            return;
        }
        if (breakDelay > 0) {
            breakDelay--;
            return;
        }
        switch (hr.getType()) {
            case ENTITY -> {
                EntityHitResult ehr = (EntityHitResult) hr;
                if (ehr.getEntity() instanceof EndCrystalEntity || ehr.getEntity() instanceof SlimeEntity || ehr.getEntity() instanceof MagmaCubeEntity) {
                    boolean sync = true;
                    if (hurtSync.isEnabled() && target != null) {
                        sync = hurtSync();
                    }
                    if (sync) {
                        if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
                        mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
                        mc.player.swingHand(Hand.MAIN_HAND);
                        if (killCrystal.isEnabled()) {
                            mc.world.removeEntity(ehr.getEntity().getId(), Entity.RemovalReason.DISCARDED);
                        }
                        breakTime = System.currentTimeMillis();
                        placeDelay = placeDelays.getRandomInt();
                        placePos = null;
                        breaksSinceLastHit++;
                        if (output.isEnabled()) {
                            mc.inGameHud.getChatHud().addMessage(Text.of("Attack " + mc.player.age));
                        }
                    }
                }
            }
            case BLOCK -> {
                BlockHitResult bhr = (BlockHitResult) hr;
                if (usPvpMcPVp.isEnabled()) {
                    BlockPos blockPos = bhr.getBlockPos();
                    if (!mc.player.isHolding(Items.END_CRYSTAL)) {
                        return;
                    }
                    if (!BlockUtils.crystalBlock(blockPos)) {
                        return;
                    }
                    if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
                    mc.interactionManager.attackBlock(blockPos, bhr.getSide());
                    mc.player.swingHand(Hand.MAIN_HAND);
//                  breakTime = System.currentTimeMillis();
//                  placeDelay = placeDelays.getRandomInt();
//                  placePos = null;
                    breakDelay = breakDelays.getRandomInt();
                    if (output.isEnabled()) mc.inGameHud.getChatHud().addMessage(Text.of("Break " + mc.player.age));
                }
            }
        }
    }

    private void placeCrystal() {
        if (placeTime > breakTime && System.currentTimeMillis() - breakTime < 500) {
            if (extraOutput.isEnabled()) {
                mc.inGameHud.getChatHud().addMessage(Text.of("Place before Break: " + (System.currentTimeMillis() - breakTime)));
            }
            return;
        } else {
            placePos = null;
        }
        HitResult hr = getHit();
        if (hr == null) {
            return;
        }
        if (placeDelay > 0) {
            placeDelay--;
            return;
        }
        if (hr.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hr;
            if (!BlockUtils.crystalBlock(bhr.getBlockPos())) {
                return;
            }
            if (bhr.getBlockPos().equals(placePos)) {
                return;
            }
            Hand hand = InvUtils.handWithStack(Items.END_CRYSTAL);
            if (hand == null) {
                return;
            }
            ItemStack itemStack = mc.player.getStackInHand(hand);
            if (!itemStack.isItemEnabled(mc.world.getEnabledFeatures())) {
                return;
            }

            if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

            int i = itemStack.getCount();
            ActionResult actionResult2 = mc.interactionManager.interactBlock(mc.player, hand, bhr);
            if (actionResult2.isAccepted() && PlayerUtils.shouldSwingHand(actionResult2)) {
                mc.player.swingHand(hand);
                if (!itemStack.isEmpty() && (itemStack.getCount() != i || mc.interactionManager.hasCreativeInventory())) {
                    mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                }
            }
            placePos = bhr.getBlockPos();
            lastBobPlace = bhr.getBlockPos();
            placeTime = System.currentTimeMillis();
            breakDelay = breakDelays.getRandomInt();
            placesSinceLastHit++;
            if (output.isEnabled()) {
                mc.inGameHud.getChatHud().addMessage(Text.of("Place " + mc.player.age));
            }
        }
    }

    HitResult getHit() {
        return switch (placeMode.getMode()) {
            case Legit -> mc.crosshairTarget;
            case Fast -> PlayerUtils.getHitResult(mc.player, Template.rotationManager().rotation().fyaw(), Template.rotationManager().rotation().fpitch());
        };
    }
}
