package template.rip.module.modules.combat;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.blatant.MultiTaskModule;
import template.rip.module.setting.settings.*;

import java.awt.*;
import java.util.HashMap;

public class TriggerBotModule extends Module {

    public final MinMaxNumberSetting range = new MinMaxNumberSetting(this, 2.5d, 3d, 0d, 6d, 0.1d, "Range");
    public final BooleanSetting renderTargettingBox = new BooleanSetting(this, Description.of("Render a box that indicates current reach and state"), true, "Render targetting box");
    //    public final BooleanSetting debug = new BooleanSetting("Debug", this, false);
    public final BooleanSetting throughWalls = new BooleanSetting(this, Description.of("Allows attacking through walls"), false, "Through walls");
    public final BooleanSetting holdLeftClick = new BooleanSetting(this, Description.of("Requires you to hold left click to use."), false, "Hold Left Click").setAdvanced();
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();
    public final BooleanSetting weaponOnly = new BooleanSetting(this, true, "Weapon Only");
    public final BooleanSetting autoShield = new BooleanSetting(this, true, "Auto shield");
    public final BooleanSetting onlyShieldAfterHit = new BooleanSetting(this, true, "Only shield after hit");
    public final BooleanSetting avoidDisable = new BooleanSetting(this, Description.of("Un-shields upon target holding an axe"), true, "Avoid shield cooldown").setAdvanced();
    public final NumberSetting autoUnShield = new NumberSetting(this, 0d, 0d, 1000d, 25d, "Un-shield after time").setAdvanced();
    public final BooleanSetting expectSword = new BooleanSetting(this, false, "Expect sword blocking");
    public final BooleanSetting attachAtShields = new BooleanSetting(this, Description.of("Attacks players that are currently blocking with a shield\nRecommended when playing on 1.8\nIf disabled, the TriggerBot will try to 'back stab' shielded players"), false, "Attack shielded players");
//    public final BooleanSetting attackShielded = new BooleanSetting("Attack shielded players", this, Description.of("Attacks players that are currently blocking with a shield\nRecommended when playing on 1.8"), false);
//    public final BooleanSetting autoSwitchShield = new BooleanSetting("Auto disable shields", this, Description.of("Automatically disables target shields (Blatant)"), true);
//    public final MinMaxNumberSetting autoDisableShieldsDelay = new MinMaxNumberSetting("Auto disable delay", this, Description.of("Attack delay for automatically disable shields"), 50, 100, 0, 250, 10);
    public final BooleanSetting preferCriticals = new BooleanSetting(this, Description.of("Will wait for critical hits"), true, "Prefer critical hits");
    public final NumberSetting criticalPercent = new NumberSetting(this, 85d, 70d, 100d, 1d, "Critical cooldown %").setAdvanced();
    public final NumberSetting onGroundPercentage = new NumberSetting(this, 90d, 70d, 100d, 1d, "Onground cooldown %").setAdvanced();

    public enum SpaceCrit{None, Full_Legit, Instant_Packet}
    public final ModeSetting<SpaceCrit> autoUnsprint = new ModeSetting<>(this, Description.of("None: Disabled\nFull Legit: Releases your W key for you\nInstant Packet: Sends unsprint packets for an instant critical hit"), SpaceCrit.None, "Un-Sprint on Critical Mode").setAdvanced();
    public final BooleanSetting rayCast = new BooleanSetting(this, Description.of("Check for other entities in front of your target when attacking"), true, "Ray Cast Hits").setAdvanced();
    public enum onlyCritMode {Off, Bind, Always}
    public final ModeSetting<onlyCritMode> onlySpaceCrit = new ModeSetting<>(this, Description.of("Off: All attacks are allowed\nBind: While holding selected bind key, only critical hits are a allowed\nAlways: Only critical hits are allowed"), onlyCritMode.Off, "Only Crits Mode");
    public final KeybindSetting bindCritSetting = new KeybindSetting(this, GLFW.GLFW_KEY_SPACE, "Crit hold bind");
    public final MinMaxNumberSetting random19delay = new MinMaxNumberSetting(this, 0, 0, 0, 200, 1, "1.9+ Random Delay");
    public enum modeEnum{К1$9, К1$9_Reduce, Normal, Reduce, Always}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("1.9: Waits for cooldown specified in advanced settings\n1.9 Reduce: Waits for the cooldown specified and for your target's hurttime to be 0 + reduce hit ticks\nNormal: Clicks the cps set constantly\nReduce: Only clicks the set cps when on hurt tick,\nelse only attacks when target has 0 hurttime (+ pre hit ticks)\nAlways: Perfect 20 CPS"), modeEnum.К1$9, "Click mode");
    public final MinMaxNumberSetting cps = new MinMaxNumberSetting(this, 6d, 12d, 1d, 20d, 1d, "CPS");
    public final NumberSetting reducePreHit = new NumberSetting(this, Description.of("How many ticks to attack before the target has 0 hurttime\nRecommended: for every 50ms of ping, +1 pre hit tick"), 2d, 0d, 6d, 1d, "Reduce pre-hit ticks").setAdvanced();

    public final BooleanSetting extraHits = new BooleanSetting(this, Description.of("Reduces your velocity by 40% by abusing Sprint hit slowdown\nHigh values might flag auto clicker checks!"), false, "Extra Hits (1.9+)").setAdvanced();
    public final MinMaxNumberSetting extraHitsAmount = new MinMaxNumberSetting(this, 0, 0, 0, 20, 1, "Hit Amount").setAdvanced();
    public final BooleanSetting onVelocity = new BooleanSetting(this, Description.of("Only reduces velocity when backtrack is holding it"), false, "Only on velocity").setAdvanced();
    //    public final BooleanSetting grimNoRotations = new BooleanSetting(this, Description.of("Sends rotations in packets, only works on Grim! (Blatant)"), false, "Grim no rotations").setAdvanced();
//    public final BooleanSetting grimFunnyPackets = new BooleanSetting(this, Description.of("Allows you to land critical hits while moving up on Grim ac (Blatant)"), false, "Grim criticals").setAdvanced();
    public final BooleanSetting sixBlockVehicle = new BooleanSetting(this, Description.of("Automatically switches to 6 block reach when in a vehicle (Blatant)"), false, "Grim 6 block vehicle").setAdvanced();
    public final NumberSetting missChance = new NumberSetting(this, Description.of("Chance to miss when calculating attack\nYou will only miss, when you can miss"), 0d, 0d, 100d, 1d, "Miss chance").setAdvanced();
    public final NumberSetting hitboxToMiss = new NumberSetting(this, Description.of("How much to expand the target's box by when calculating a miss"), 0.1, 0, 3, 0.01, "Box increase to miss").setAdvanced();
    //    public final BooleanSetting hitSelect = new BooleanSetting(this, Description.of("Attempts to hit select"), false, "Hit select").setAdvanced();
//    public final NumberSetting ticksToHitAFter = new NumberSetting(this, 1d, 0d, 5d, 1d, "Ticks to hit damage").setAdvanced();
//    public final NumberSetting ticksToHitAnyway = new NumberSetting(this, 3d, 0d, 10d, 1d, "Ticks to hit no damage").setAdvanced();
    public boolean firstKbHit = false;
    public int combo = 0; //useless rn, can be used for logic in the future
    private long timer = System.currentTimeMillis();
    private long timer19 = 0;
    private long unShieldTimer = System.currentTimeMillis();
    private int tick = 0;
    private Vec3d pos = null;
    private Vec3d prevPos = null;
    private boolean attacked = false;
    private Color color = Color.WHITE;
    private boolean unSprinted = false;
    private LivingEntity target = null;
    private boolean missHit = false;
    private boolean lastReach = false;
    private boolean currentReach = false;
    private HitResult lastHr = null;
    private HitResult tickHr = null;
    private HitResult hr;
    private HitResult realhr;
    private HitResult missHr;
    private double currentRange;

    private final HashMap<Entity, Boolean> critEnts = new HashMap<>();

    public TriggerBotModule(Category category, Description description, String name) {
        super(category, description, name);
        bindCritSetting.addConditionMode(onlySpaceCrit, onlyCritMode.Bind);
    }

    @Override
    public String getSuffix() {
        return " " + range.getMinValue() + " " + range.getMaxValue();
    }

    private boolean isHoldingWeapon() {
        if (mc.player == null)
            return false;

        ItemStack heldItem = mc.player.getMainHandStack();

        return heldItem.getItem() instanceof SwordItem || heldItem.getItem() instanceof AxeItem;
    }

    @Override
    public void onEnable() {
        firstKbHit = false;
        combo = 0; //useless rn, can be used for logic in the future
        timer = System.currentTimeMillis();
        unShieldTimer = System.currentTimeMillis();
        tick = 0;
        pos = null;
        prevPos = null;
        attacked = false;
        color = Color.WHITE;
        unSprinted = false;
        target = null;
        missHit = false;
        lastReach = false;
        currentReach = false;
        currentRange = range.getRandomDouble();
        tickHr = null;
        lastHr = null;
        critEnts.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onAttackPre(AttackEntityEvent.Pre event) {
        if (!nullCheck()) return;

        if ((PlayerUtils.canCrit(mc.player) || (PlayerUtils.canCriticalsModule() && PlayerUtils.canCritStatic(mc.player))) && autoUnsprint.is(SpaceCrit.Instant_Packet) && mode.is(modeEnum.К1$9) && !unSprinted)
            if (PlayerUtils.stopSprint(true))
                unSprinted = true;

        /*if (grimNoRotations.isEnabled() && grim != null && mc.getNetworkHandler() != null) {
            // hacky solution
            CriticalsModule crit = Template.moduleManager.getModule(CriticalsModule.class);
            // we don't want our no rotation packets to be forced to match the current silent rotation
            if (*//*mc.player.isOnGround()*//*true) {
                if (grimFunnyPackets.isEnabled()) {
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                    mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                            new PlayerInteractItemC2SPacket(mc.player.getActiveHand() == Hand.OFF_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch())
                    );
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                    Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), grim.fyaw(), grim.fpitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                    mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                            new PlayerInteractItemC2SPacket(mc.player.getActiveHand() == Hand.OFF_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch())
                    );
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                } else {
                    Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), grim.fyaw(), grim.fpitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
                }
            } else if (false && crit.isEnabled() && crit.mode.getMode() == CriticalsModule.modeEnum.OldGrim_OffGround) {
                if (grimFunnyPackets.isEnabled()) {
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                    mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                            new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch())
                    );
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                    Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.0001, mc.player.getZ(), grim.fyaw(), grim.fpitch(), false));
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                    mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                            new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch())
                    );
                    Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                } else {
                    Template.sendNoEvent(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.0001, mc.player.getZ(), grim.fyaw(), grim.fpitch(), false));

                }
            }
        }*/
    }

    // I think we need to send the rotation back first
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onAttackPost(AttackEntityEvent.Post event) {
        if (!nullCheck()) return;

        if (autoUnsprint.is(SpaceCrit.Instant_Packet) && mode.is(modeEnum.К1$9) && unSprinted)
            if (PlayerUtils.startSprint(true))
                unSprinted = false;

        combo++;
        if (mc.player.isSprinting())
            firstKbHit = true;

        /*if (grimNoRotations.isEnabled() && grim != null) {
            // our packet rotation being overriden here is completely fine
            if (grimFunnyPackets.isEnabled()) {
                Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                        new PlayerInteractItemC2SPacket(mc.player.getActiveHand() == Hand.OFF_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch())
                );
                Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
                Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
                mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                        new PlayerInteractItemC2SPacket(mc.player.getActiveHand() == Hand.OFF_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch())
                );
                Template.sendNoEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos(), Direction.DOWN));
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
            }
            grim = null;

        }*/

    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (renderTargettingBox.isEnabled() && preChecks()) {
            boolean miss = color == Color.YELLOW;
            boolean misshr = color == Color.WHITE;

            if (misshr) {
                assert mc.player != null;
                pos = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)).add(RotationUtils.forwardVector(getRot(true)).multiply(currentRange));
            } else if (miss) {
                pos = missHr.getPos();
            } else {
                pos = hr.getPos();
            }

            if (prevPos != null)
                pos = MathUtils.smoothVec3d(prevPos, pos, mc.getRenderTickCounter().getTickDelta(false));

            prevPos = pos;
            RenderUtils.Render3D.renderBox(new Box(pos.subtract(0.1, 0.1, 0.1), pos.add(0.1, 0.1, 0.1)), color, 50, event.context);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        tick++;
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        if (mc.world == null)
            return;

        if (event.damaged == mc.player)
            combo = 0;
    }

    @EventHandler
    private void onInput(HandleInputEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null)
            return;

        if (!mc.player.isSprinting())
            firstKbHit = false;

        target = PlayerUtils.findFirstLivingTargetOrNull();

        if (!preChecks() || !canAttack())
            return;

        if (canReach() && click(target)) {
            if (!missHit || !onlyShieldAfterHit.isEnabled())
                attacked = true;
            hitEntity(target);
            currentRange = 0;
            unShieldTimer = System.currentTimeMillis();
        }
    }

    @EventHandler
    private void onKeyBind(KeyBindingEvent event) {
        if (event.key == mc.options.useKey && target != null && mc.player != null && autoShield.isEnabled()) {
            if (!(expectSword.isEnabled() && mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                if (!((mc.player.getMainHandStack().getItem() instanceof ShieldItem) || (mc.player.getMainHandStack().getUseAction() == UseAction.NONE && mc.player.getOffHandStack().getItem() instanceof ShieldItem)))
                    return;

                if (mc.player.getItemCooldownManager().isCoolingDown(Items.SHIELD.getDefaultStack()))
                    return;
            }

            event.setPressed(event.isPressed() || attacked && (!target.disablesShield() || !avoidDisable.isEnabled()) && (autoUnShield.getIValue() == 0 || System.currentTimeMillis() <= autoUnShield.getIValue() + unShieldTimer));
        }
    }

    @EventHandler
    private void updateCrosshair(UpdateCrosshairEvent event) {
        if (mc.player == null) return;
        Rotation rot = getRot(false);
        hr = PlayerUtils.getHitResult(mc.player, e -> e == target, rot.fyaw(), rot.fpitch(), currentRange, throughWalls.isEnabled(), 0);
        realhr = PlayerUtils.getHitResult(mc.player, e -> true, rot.fyaw(), rot.fpitch(), currentRange, throughWalls.isEnabled(), 0);
        missHr = PlayerUtils.getHitResult(mc.player, e -> e == target, rot.fyaw(), rot.fpitch(), currentRange, throughWalls.isEnabled(), hitboxToMiss.value);

        lastHr = tickHr;
        tickHr = PlayerUtils.getHitResult(mc.player, e -> true, rot.fyaw(), rot.fpitch(), currentRange, throughWalls.isEnabled(), 0);
        lastReach = currentReach;
        currentReach = canReach(tickHr);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onInput(InputEvent event) {
        if (!nullCheck())
            return;

        if (autoUnsprint.is(SpaceCrit.Full_Legit) && target != null && preferCriticals.isEnabled() && PlayerUtils.canCritStatic(mc.player) && !mc.player.isOnGround() && !PlayerUtils.canCriticalsModule()) {
            Rotation rot = getRot(false);
            if (PlayerUtils.getHitResult(mc.player, e -> e == target, rot.fyaw(), rot.fpitch(), currentRange, throughWalls.isEnabled(), 0).getType() == HitResult.Type.ENTITY) {
                event.input.movementForward = Math.min(0f, event.input.movementForward);
            }
        }
    }

    private boolean canAttack() {
        if (mode.is(modeEnum.К1$9)) {
            if (PlayerUtils.isBlockedByShield(mc.player, target, false) && !attachAtShields.isEnabled()) {
                return false;
            }

            if ((mc.player.isOnGround() && mc.player.getAttackCooldownProgress(0.5f) < onGroundPercentage.value / 100) || (!mc.player.isOnGround() && mc.player.getAttackCooldownProgress(0.5f) < criticalPercent.getValue() / 100))
                return false;

            if (preferCriticals.isEnabled() && PlayerUtils.canCritStatic(mc.player) && !PlayerUtils.canCrit(mc.player, true) && !PlayerUtils.canCriticalsModule()) {
                switch (onlySpaceCrit.getMode()) {
                    case Bind -> {
                        if (KeyUtils.isKeyPressed(bindCritSetting.getCode())) {
                            return false;
                        }
                    }
                    case Always -> {
                        return false;
                    }
                }

                if (((!mc.player.isSprinting() || firstKbHit) && !autoUnsprint.is(SpaceCrit.Instant_Packet)) && !mc.player.isOnGround())
                    return false;
            }

            /*if (hitSelect.isEnabled()) {
                if (canReach()) {
                    if (!reached) {
                        reached = true;
                        hitTick = tick;
                    }
                } else reached = false;


                if ((reached && tick <= hitTick + ticksToHitAnyway.getIValue()) && mc.player.hurtTime != mc.player.maxHurtTime - ticksToHitAFter.getIValue() && combo == 0)
                    return false;
            }*/
        }

        if (mc.player.isBlocking() && autoShield.isEnabled() && !Template.moduleManager.getModule(MultiTaskModule.class).isEnabled()) {
            attacked = false;
            return false;
        }
        return true;
    }

    public boolean preChecks() {
        if (target == null || mc.interactionManager == null || target.getHealth() <= 0.0f || (mc.currentScreen != null && disableInScreens.isEnabled())) {
            return false;
        }

        if (holdLeftClick.isEnabled() && !KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            return false;
        }

        if (weaponOnly.isEnabled() && !isHoldingWeapon()) {
            return false;
        }

        return (!mc.player.isUsingItem() || Template.moduleManager.getModule(MultiTaskModule.class).isEnabled() || (mc.player.getActiveItem().getItem() instanceof ShieldItem && autoShield.isEnabled()));
    }

    public Rotation opt() {
        if (target != null) return RotationUtils.correctSensitivity(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), MathUtils.closestPointToBox(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target.getBoundingBox().expand(target.getTargetingMargin()))));
        else return RotationUtils.entityRotation(mc.player, true);
    }

    public Rotation getRot(boolean tickDelta) {
        if (mc.player == null) return null;
        return /*grimNoRotations.isEnabled() || */(sixBlockVehicle.isEnabled() && mc.player.hasVehicle()) ? opt() : (Template.rotationManager().isEnabled() ? Template.rotationManager().getClientRotation() : mc.player != null ? RotationUtils.entityRotation(mc.player, tickDelta) : new Rotation(0, 0));
    }

    public boolean canReach(HitResult hr) {
        if (hr == null)
            return false;

        if (currentRange == 0) {
            currentRange = range.getRandomDouble();
        }

        if (sixBlockVehicle.isEnabled()) {
            assert mc.player != null;
            if (mc.player.hasVehicle()) {
                currentRange = 6.0;
            } else if (currentRange > range.getMaxValue()) {
                currentRange = range.getRandomDouble();
            }
        }

//        if (grimNoRotations.isEnabled())
//            grim = opt();

        boolean contains = false;
        boolean doMissHit = false;
        color = Color.WHITE;

        if (!missHit) {
            if (!throughWalls.isEnabled() && hr.getType() == HitResult.Type.BLOCK) {
                color = Color.RED;
            }
        }

        if (hr.getType() == HitResult.Type.ENTITY) {
            contains = true;
            missHit = false;
            color = Color.GREEN;
        }

        return contains;
    }

    public boolean canReach() {
        if (hr == null || missHr == null)
            return false;

        if (currentRange == 0) {
            currentRange = range.getRandomDouble();
        }

        if (sixBlockVehicle.isEnabled()) {
            assert mc.player != null;
            if (mc.player.hasVehicle()) {
                currentRange = 6.0;
            } else if (currentRange > range.getMaxValue()) {
                currentRange = range.getRandomDouble();
            }
        }

//        if (grimNoRotations.isEnabled())
//            grim = opt();

        boolean contains = false;
        boolean doMissHit = MathUtils.getRandomInt(1, 100) <= missChance.getIValue();
        color = Color.WHITE;

        if (!missHit) {
            if (!throughWalls.isEnabled() && hr.getType() == HitResult.Type.BLOCK) {
                color = Color.RED;
            }
        }

        if (doMissHit) {
            if (hr.getType() == HitResult.Type.MISS && missHr.getType() == HitResult.Type.ENTITY) {
                color = Color.YELLOW;
                missHit = true;
            }
        }

        if (hr.getType() == HitResult.Type.ENTITY) {
            contains = true;
            missHit = false;
            color = Color.GREEN;
        }

        if (missHit && doMissHit && color != Color.RED)
            contains = true;

        return contains;
    }

    private void hitEntity(LivingEntity e) {
//        BlockHitResult bhr = PlayerUtils.rayCast(mc.player.getPos(), mc.player.getPos().subtract(0.0, 3.0, 0.0), mc.player);
//        double y = mc.player.getY() - bhr.getPos().getY();
//        mc.inGameHud.getChatHud().addMessage(Text.of(String.format("§%s %s, %s, %s", PlayerUtils.canCrit(mc.player, true) ? 'a' : '4', mc.player.fallDistance, !mc.player.isOnGround(), y)));
        if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
        if (rayCast.isEnabled()) {
            if (realhr instanceof EntityHitResult) {
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) realhr).getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }

        /*if (mc.crosshairTarget != null && debug.isEnabled()) {
            mc.inGameHud.getChatHud().addMessage(Text.of(String.valueOf(mc.crosshairTarget.getPos().length() - hr.getPos().length())));
            mc.inGameHud.getChatHud().addMessage(Text.of(String.valueOf(mc.crosshairTarget.getPos().subtract(hr.getPos()))));
        }*/

        if (missHit) {
            if (mc.interactionManager.hasLimitedAttackSpeed()) {
                mc.attackCooldown = 10;
            }
            mc.player.resetLastAttackedTicks();
            mc.player.swingHand(Hand.MAIN_HAND);
            missHit = false;
        } else {
            mc.interactionManager.attackEntity(mc.player, e);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (/*mc.crosshairTarget instanceof EntityHitResult ehr && */mc.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR && extraHits.isEnabled() && (!onVelocity.isEnabled() || BacktrackModule.hadVelo)) {
            // we can only reduce if we are sprinting/have knock-back (it's how it's done in vanilla)
            if (mc.player.isSprinting() || mc.player.getMainHandStack().getEnchantments().getEnchantments().stream().anyMatch(r -> r.matchesKey(Enchantments.KNOCKBACK))) {
                if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
                if (Template.EVENTBUS.post(new AttackEntityEvent.Pre(e)).isCancelled())
                    return;
                int count = extraHitsAmount.getRandomInt();
                for (int i = 0; i < count; i++) {
                    // mc.interactionManager.attackEntity(player, entity)
                    mc.interactionManager.syncSelectedSlot();
                    mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(e, mc.player.isSneaking()));

                    // mc.player.attack(entity)
                    mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
                    // we go full legit if they're only doing one reduce hit
                    if (count == 1) {
                        mc.player.setSprinting(false);
                    }

                    // self-explanatory
                    mc.player.resetLastAttackedTicks();

                    // we need to do this
                    mc.player.swingHand(Hand.MAIN_HAND);
                    Template.EVENTBUS.post(new AttackEntityEvent.Post(e));
                }
            }
        }
    }

    private boolean click(LivingEntity target) {
        double CPS = cps.getRandomDouble();
        double delay = 950 / CPS;
        int delay19 = random19delay.getRandomInt();
        switch (mode.getMode()) {
            case Normal: {
                if (System.currentTimeMillis() >= timer + delay) {
                    timer = System.currentTimeMillis();
                    return true;
                }
                break;
            }
            case Reduce: {
                if ((((target.hurtTime <= 1 + reducePreHit.getIValue() && target.hurtTime >= reducePreHit.getIValue() - 1) || target.hurtTime == 0) && 2 < tick) || (critEnts.get(target) == Boolean.FALSE && PlayerUtils.canCrit(mc.player, true))) {
                    tick = 0;
                    critEnts.put(target, PlayerUtils.canCrit(mc.player, true));
                    return true;
                } else if ((mc.player != null ? mc.player.hurtTime : 0) > 0 && System.currentTimeMillis() >= timer + delay) {
                    timer = System.currentTimeMillis();
                    return true;
                }
                break;
            }
            case К1$9: {
                if (timer19 == 0) {
                    timer19 = System.currentTimeMillis() + delay19;
                }
                if (timer19 <= System.currentTimeMillis() || delay19 == 0) {
                    timer19 = 0;
                    return true;
                }
                break;
            }
            case К1$9_Reduce: {
                if ((((target.hurtTime <= 1 + reducePreHit.getIValue() && target.hurtTime >= reducePreHit.getIValue() - 1) || target.hurtTime == 0) && 2 < tick)) {
                    tick = 0;
                    if (timer19 == 0) {
                        timer19 = System.currentTimeMillis() + delay19;
                    }
                    if (timer19 <= System.currentTimeMillis() || delay19 == 0) {
                        timer19 = 0;
                        return true;
                    }
                }
                break;
            }
            case Always: {
                return true;
            }
        }
        return false;
    }
}