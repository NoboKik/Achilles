package template.rip.module.modules.crystal;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.player.FastPlaceModule;
import template.rip.module.setting.settings.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class AutoHitCrystalModule extends Module {

    public final MinMaxNumberSetting placeDelay = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Obsidian delays");
    public final MinMaxNumberSetting switchDelay = new MinMaxNumberSetting(this, 0, 2, 0, 10, 1, "Switch delays");

    public enum modeEnum {Legit, Fast}

    public final ModeSetting<AutoCrystalRecodeModule.modeEnum> placeMode = new ModeSetting<>(this, Description.of("Legit: Normal minecraft crosshair mechanics with no changes\nFast: Changes your crosshair target to place faster after breaking a crystal"), AutoCrystalRecodeModule.modeEnum.Legit, "Place Crosshair Target Mode");
    public final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch back to slot");
    public final BooleanSetting autoHitCrystalLimit = new BooleanSetting(this, false, "2 AutoHitCrystal Limit");

    public enum ActivateKind {Activate_Key, Look_Away, Both}

    public final ModeSetting<ActivateKind> activateSetting = new ModeSetting<>(this, ActivateKind.Activate_Key, "Activation Mode");
    public final KeybindSetting activateKey = new KeybindSetting(this, GLFW.GLFW_MOUSE_BUTTON_2, "Activate Key");
    public final NumberSetting lookAwayTime = new NumberSetting(this, 10, 0, 40, 1, "Look Away Timeout");
    public final RegistrySetting<Item> workWithItems = new RegistrySetting<>(Arrays.asList(Items.TOTEM_OF_UNDYING, Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.WOODEN_SWORD), this, Registries.ITEM, RegistrySetting.autoHitCrystalPredicate, "Valid Hit Items");
    public final BooleanSetting autoToggle = new BooleanSetting(this, Description.of("Automatically toggles AutoCrystal and FastPlace when necessary\nDisable this if you encounter issues!"), true, "Toggle AutoCrystal").setAdvanced();
    public final DividerSetting fullAutoDiv = new DividerSetting(this, false, "Full Auto Hit (Advanced)");
    public final BooleanSetting fullAuto = new BooleanSetting(this, false, "Full Auto enabled");

    public enum roteEnum {Normal, Silent}

    public final ModeSetting<roteEnum> mode = new ModeSetting<>(this, Description.of("Normal: Change player's rotation\nSilent: Change client's fake rotation"), roteEnum.Normal, "Rotation Mode");
    public final MinMaxNumberSetting yawSpeed = new MinMaxNumberSetting(this, 100, 150, 0, 180, 0.1, "Min Max Yaw");
    public final MinMaxNumberSetting pitchSpeed = new MinMaxNumberSetting(this, 45, 70, 0, 90, 0.1, "Min Max Pitch");
    public final MinMaxNumberSetting yawNoise = new MinMaxNumberSetting(this, 1, 2, 0, 5, 0.1, "Yaw noise");
    public final MinMaxNumberSetting pitchNoise = new MinMaxNumberSetting(this, 0.5, 1, 0, 5, 0.1, "Pitch noise");
    public final NumberSetting minDamage = new NumberSetting(this, 14, 0, 20, 0.1, "Min Damage");
    public final BooleanSetting renderFullyAuto = new BooleanSetting(this, true, "Render Fully Auto");
    public final NumberSetting fullyAutoLookTime = new NumberSetting(this, 1000, 0, 2500, 1, "Auto Obi Look Time");
    public final ColorSetting obsidianTargetBlock = new ColorSetting(this, new JColor(219, 62, 177, 100), true, "Obsidian Block Color");
    public final ColorSetting targetBoxColor = new ColorSetting(this, new JColor(219, 122, 147, 100), true, "Target Box Color");
    public final BooleanSetting debugPlaces = new BooleanSetting(this, false, "Debug places").setAdvanced();

    private boolean fastPlaceLastEnabled, savedBool, wasEnabled, set, activated, selectedCrystal;
    public boolean crystalling, placedObsidian;
    private LivingEntity target = null;
    private int last = -1, placeClock, switchClock, lastOnCrosshairTime, lastAttackTime;
    private long hitTime;
    private BlockPos targetBlock;
    private ArrayList<BlockPos> perhaps;

    public AutoHitCrystalModule(Category category, Description description, String name) {
        super(category, description, name);
        fullAutoDiv.addSetting(fullAuto, mode, yawSpeed, pitchSpeed, yawNoise, pitchNoise, minDamage, renderFullyAuto, fullyAutoLookTime, obsidianTargetBlock, targetBoxColor, debugPlaces);
    }

    public void reset() {
        lastOnCrosshairTime = 0;
        lastAttackTime = 0;
        placeClock = placeDelay.getRandomInt();
        switchClock = switchDelay.getRandomInt();
        activated = false;
        crystalling = false;
        placedObsidian = false;
        selectedCrystal = false;
        last = -1;
        if (autoToggle.isEnabled()) {
            if (set) {
                AutoCrystalRecodeModule autoCrystal = Template.moduleManager.getModule(AutoCrystalRecodeModule.class);
                if (autoCrystal != null) {
                    autoCrystal.setEnabled(wasEnabled);
                }
                set = false;
            }
            FastPlaceModule fpm = Template.moduleManager.getModule(FastPlaceModule.class);
            if (savedBool) {
                if (fpm != null) {
                    fpm.setEnabled(fastPlaceLastEnabled);
                }
                savedBool = false;
            }
        }
        mc.options.useKey.setPressed(KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()));
    }

    public void resetAuto() {
        target = null;
        targetBlock = null;
        hitTime = 0;
        perhaps = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        reset();
        resetAuto();
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!fullAuto.isEnabled() || !renderFullyAuto.isEnabled()) {
            return;
        }
        if (targetBlock != null) {
            RenderUtils.Render3D.renderBox(new Box(MathUtils.vec3iToVec3d(targetBlock), MathUtils.vec3iToVec3d(targetBlock.add(1, 1, 1))), obsidianTargetBlock.getColor(), obsidianTargetBlock.getColor().getAlpha(), event.context);
        }
        if (target != null) {
            RenderUtils.Render3D.renderBox(MathUtils.boxAtPos(target.getBoundingBox(), target.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false))), targetBoxColor.getColor(), targetBoxColor.getColor().getAlpha(), event.context);
        }
        if (debugPlaces.isEnabled()) {
            perhaps.forEach(bp -> RenderUtils.Render3D.renderBox(new Box(MathUtils.vec3iToVec3d(bp), MathUtils.vec3iToVec3d(bp.add(1, 1, 1))), Color.LIGHT_GRAY, 50, event.context));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck() || mc.currentScreen != null) {
            return;
        }

        if (!canHitCrystal() && canReset()) {
            if (last != -1 && switchBack.isEnabled()) {
                if (switchClock > 0) {
                    switchClock--;
                    return;
                }
                switchClock = switchDelay.getRandomInt();
                InvUtils.setInvSlot(last);
                last = -1;
            } else reset();
            return;
        }

        if (!workWithItems.selected.contains(mc.player.getMainHandStack().getItem()) && !activated) {
            return;
        }

        HitResult hr = getHit();

        activated = true;
        if (!crystalling) {
            if (hr instanceof BlockHitResult blockHit) {
                if (blockHit.getType() == HitResult.Type.MISS)
                    return;

                 BlockPos targetBlockPos = fullAuto.isEnabled() && targetBlock != null ? targetBlock : blockHit.getBlockPos();

                if (BlockUtils.isBlockClickable(targetBlockPos))
                    return;

                if (!BlockUtils.isBlock(Blocks.OBSIDIAN, targetBlockPos) && !BlockUtils.isBlock(Blocks.RESPAWN_ANCHOR, targetBlockPos)) {
                    mc.options.useKey.setPressed(false);

                    if (autoToggle.isEnabled()) {
                        if (!savedBool) {
                            FastPlaceModule fpm = Template.moduleManager.getModule(FastPlaceModule.class);
                            if (fpm != null) {
                                fastPlaceLastEnabled = fpm.isEnabled();
                                fpm.setEnabled(false);
                            }
                        }
                        savedBool = true;
                    }

                    if (last == -1) {
                        last = mc.player.getInventory().selectedSlot;
                    }

                    if (!mc.player.getMainHandStack().isOf(Items.OBSIDIAN)) {
                        if (switchClock > 0) {
                            switchClock--;
                            return;
                        }
                        InvUtils.selectItemFromHotbar(Items.OBSIDIAN);
                        switchClock = switchDelay.getRandomInt();
                    }

                    if (mc.player.getMainHandStack().isOf(Items.OBSIDIAN)) {
                        if (placeClock > 0) {
                            placeClock--;
                            return;
                        }

                        if (Template.isClickSim())
                            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

                        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                        if (interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult)) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }

                        placeClock = placeDelay.getRandomInt();
                        crystalling = true;
                        placedObsidian = true;
                    }
                } else crystalling = true;
            }
        }
        if (crystalling
                || (hr instanceof BlockHitResult && BlockUtils.isBlock(Blocks.OBSIDIAN, ((BlockHitResult) hr).getBlockPos()))
                || (hr instanceof EntityHitResult && (((EntityHitResult) hr).getEntity() instanceof EndCrystalEntity || ((EntityHitResult) hr).getEntity() instanceof SlimeEntity))) {
            crystalling = true;

            if (!mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) && !selectedCrystal) {
                if (switchClock > 0) {
                    switchClock--;
                    return;
                }
                selectedCrystal = InvUtils.selectItemFromHotbar(Items.END_CRYSTAL);
                switchClock = switchDelay.getRandomInt();
            }

            if (mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                AutoCrystalRecodeModule autoCrystal = Template.moduleManager.getModule(AutoCrystalRecodeModule.class);
                if (autoCrystal != null) {
                    if (!set) {
                        if (autoToggle.isEnabled()) {
                            boolean wasSet = set;
                            wasEnabled = autoCrystal.isEnabled();

                            autoCrystal.setEnabled(true);
                            if (!wasSet) {
                                autoCrystal.onInput(null);
                            }
                        }
                        autoCrystal.autoHitCrystalLimit = autoHitCrystalLimit.isEnabled();
                        autoCrystal.autoHitCrystalPlacedObsidian = placedObsidian;
                        autoCrystal.placesSinceLastHit = 0;
                        autoCrystal.breaksSinceLastHit = 0;
                        set = true;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onAttackPost(AttackEntityEvent.Post event) {
        if (!nullCheck()) {
            return;
        }

        if (event.target instanceof LivingEntity entity && PlayerUtils.findTargets().contains(event.target)) {
            target = entity;
            hitTime = System.currentTimeMillis();
            lastAttackTime = mc.player.age;
        }
    }

    private boolean canHitCrystal() {
        // Manual mode is handled here!
        if (!fullAuto.isEnabled()) {
            if (mc.player == null) return false;
            
            HitResult result = getHit();
            if (result instanceof EntityHitResult hitResult && PlayerUtils.findTargets().contains(hitResult.getEntity())) {
                lastOnCrosshairTime = mc.player.age;
            }
            
            boolean lookAway = lastOnCrosshairTime != mc.player.age
                && mc.player.age - lastOnCrosshairTime < lookAwayTime.getIValue()
                && mc.player.age - lastAttackTime < lookAwayTime.getIValue()
                && lastAttackTime != mc.player.age;
            boolean key = activateKey.isPressed();
            
            return switch (activateSetting.getMode()) {
                case Activate_Key -> key;
                case Look_Away -> lookAway;
                case Both -> lookAway || key;
            };
        }
        int lookTime = fullyAutoLookTime.getIValue();
        // Check if we should reset stuff, and if so, reset
        if (target == null
            || System.currentTimeMillis() - hitTime >= lookTime
            || mc.world.getEntityById(target.getId()) == null) {
            resetAuto();
        }
        
        // technically redundant, but we save perf by returning here (and in some cases, risking checking same shit twice), because if we didnt return early
        // we would search for new placements every time this check fails...
        if (!crystalling || targetBlock == null || !enoughDamage(targetBlock, fullyAutoLookTime.getIValue())) {
            return false;
        }
        
        if (crystalling && targetBlock != null && enoughDamage(targetBlock, fullyAutoLookTime.getIValue())) {
            Vec3d targetVec = MathUtils.closestPointToBox(
                new Box(
                    MathUtils.vec3iToVec3d(targetBlock).add(0.25, 1.0, 0.25),
                    MathUtils.vec3iToVec3d(targetBlock).add(0.75, 1.0, 0.75)
                )
            );
            Rotation rot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), targetVec);
            rot = RotationUtils.getLimitedRotation(Template.rotationManager().rotation(), rot, yawSpeed.getRandomDouble(), pitchSpeed.getRandomDouble());
            rot = RotationUtils.addNoise(rot, yawNoise.getRandomDouble(), pitchNoise.getRandomDouble());
            rot = RotationUtils.correctSensitivity(rot);
            if (mode.is(roteEnum.Silent)) {
                Template.rotationManager().setRotation(rot);
            } else {
                RotationUtils.setEntityRotation(mc.player, rot);
            }
            mc.crosshairTarget = PlayerUtils.getHitResult(mc.player, e -> true, Template.rotationManager().rotation().fyaw(), Template.rotationManager().rotation().fpitch());
            return true;
        }
        
        // Find a new placement...
        BlockPos origin = new BlockPos(target.getBlockX(), mc.player.getBlockY(), target.getBlockZ());
        ArrayList<Pair<BlockPos, Double>> placePossibilities = new ArrayList<>();
        
        // O(n²) algorithm, this could probably be made better?
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos possibility = origin.add(x, 0, z);
                if ((BlockUtils.crystalBlock(possibility) || mc.world.getBlockState(possibility).isAir()) && validBPos(possibility)) continue;
                
                Vec3d lastPos = target.getPos();
                if (PlayerUtils.lastPosVec(target).subtract(target.getPos()).getY() > 0
                    || System.currentTimeMillis() - hitTime < fullyAutoLookTime.getIValue() / 10 * 3) {
                    target.setPosition(lastPos.add(0.0, 1.0, 0.0));
                }
                placePossibilities.add(new Pair<>(possibility, DamageUtils.crystalDamage(target, possibility.up().toCenterPos(), true, possibility, false)));
                target.setPosition(lastPos);
            }
        }
        
        placePossibilities.removeIf(pr -> pr.getRight() < minDamage.value);
        placePossibilities.sort(Comparator.comparing(pr -> -pr.getRight()));
        
        if (debugPlaces.isEnabled()) {
            perhaps.clear();
            placePossibilities.forEach(pr -> perhaps.add(pr.getLeft()));
        }
        
        if (!placePossibilities.isEmpty()) {
            targetBlock = placePossibilities.get(0).getLeft();
        }
        
        return false;
    }

    private boolean canReset() {
        if (activateSetting.is(ActivateKind.Look_Away) || activateSetting.is(ActivateKind.Both)) {
            HitResult hr = getHit();
            return (hr instanceof BlockHitResult) && (hr.getType() == HitResult.Type.MISS || !mc.world.getBlockState(((BlockHitResult) hr).getBlockPos()).isOf(Blocks.OBSIDIAN));
        }
        return true;
    }

    HitResult getHit() {
        return switch (placeMode.getMode()) {
            case Legit -> mc.crosshairTarget;
            case Fast -> PlayerUtils.getHitResult(mc.player, Template.rotationManager().rotation().fyaw(), Template.rotationManager().rotation().fpitch());
        };
    }

    private boolean enoughDamage(BlockPos bPos, int lookTime) {
        Vec3d lastPos = target.getPos();
        if (PlayerUtils.lastPosVec(target).subtract(target.getPos()).getY() > 0 || System.currentTimeMillis() - hitTime < lookTime / 10 * 3) {
            target.setPosition(lastPos.add(0.0, 1.0, 0.0));
        }
        boolean bl = minDamage.value < DamageUtils.crystalDamage(target, bPos.up().toCenterPos(), true, bPos, false);
        target.setPosition(lastPos);
        return bl;
    }

    private boolean validBPos(BlockPos bPos) {
        if (bPos == null)
            return false;

        Box box = BlockUtils.blockBox(bPos.down());
        Vec3d targetVec = MathUtils.closestPointToBox(new Box(MathUtils.vec3iToVec3d(bPos).add(0.25, 0.0, 0.25), MathUtils.vec3iToVec3d(bPos).add(0.75, 0.0, 0.75)));
        Rotation rot = RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), targetVec);
        Box targetBox = null;
        if (target != null && mc.world.getBlockState(bPos).isAir()) {
            targetBox = target.getBoundingBox();
            target.setBoundingBox(target.getBoundingBox().expand(0.25));
        }
        BlockState last = mc.world.getBlockState(bPos);
        mc.world.setBlockState(bPos, Blocks.AIR.getDefaultState());
        boolean bl = box != null && mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).distanceTo(MathUtils.closestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), box)) < 4.5 && mc.world.getEntitiesByClass(Entity.class, new Box(MathUtils.vec3iToVec3d(bPos), MathUtils.vec3iToVec3d(bPos.add(1, 1, 1))), e -> true).isEmpty() && mc.world.getBlockState(bPos.up()).isAir() && PlayerUtils.rayCast(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), bPos.toCenterPos(), mc.player).getType() == HitResult.Type.MISS && PlayerUtils.getHitResult(mc.player, e -> true, rot.fyaw(), rot.fpitch()).getType() == HitResult.Type.BLOCK;
        mc.world.setBlockState(bPos, last);
        if (targetBox != null && target != null) {
            target.setBoundingBox(targetBox);
        }
        return bl;
    }
}
