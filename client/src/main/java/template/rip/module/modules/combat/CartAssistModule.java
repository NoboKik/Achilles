package template.rip.module.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;
import template.rip.module.setting.settings.RegistrySetting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class CartAssistModule extends Module {

    public enum modeEnum {Normal, Silent}

    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Silent, "Rotation Mode");
    public final NumberSetting aimTimeout = new NumberSetting(this, 300, 50, 750, 1, "Timeout for bow");
    public final BooleanSetting safetyBlock = new BooleanSetting(this, false, "Safety Block");
    public final RegistrySetting<Item> validSafeBlocks = new RegistrySetting<>(Arrays.asList(Items.OAK_PLANKS, Items.OBSIDIAN), this, Registries.ITEM, "Safe Blocks");

    private BlockHitResult bhr;
    private final HashSet<UUID> usedArrows = new HashSet<>();
    private Pair<List<Vec3d>, HitResult> lastPredict;
    private int ticks;
    private boolean placedTNT, placedRail, rotating, foundSafe, placedSafe;
    private BlockPos railPos;
    private long timer;

    public CartAssistModule(Category category, Description description, String name) {
        super(category, description, name);
        validSafeBlocks.addConditionBoolean(safetyBlock, true);
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    @Override
    public void onEnable() {
        bhr = null;
        lastPredict = null;
        ticks = 0;
        rotating = false;
        placedTNT = false;
        timer = 0;
        placedRail = false;
        foundSafe = false;
        placedSafe = false;
        railPos = null;
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (mc.world == null || mc.player == null)
            return;

        rotating = false;

        if (!placedSafe && placedTNT && safetyBlock.isEnabled()) {
            Direction dir = Direction.fromHorizontalDegrees(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), railPos.toCenterPos()).fyaw());
            BlockPos bPos = mc.player.getBlockPos();
            BlockPos bPosOffset1 = bPos.offset(dir);
            BlockPos bPosOffset2 = bPos.offset(dir, 2);
            BlockPos placePos = null;
            Direction placeDir = null;

            for (BlockPos bbbbb : new BlockPos[]{bPosOffset1, bPosOffset2}) {
                if (mc.world.getBlockState(bbbbb).isReplaceable() && !mc.player.getBoundingBox().intersects(MathUtils.vec3iToVec3d(bbbbb), MathUtils.vec3iToVec3d(bbbbb.add(1, 1, 1)))) {
                    Direction directions = Direction.DOWN;
                    BlockPos testBpos = bbbbb.offset(directions);
                    if (mc.world.getBlockState(testBpos).isOpaque()) {
                        placeDir = directions.getOpposite();
                        placePos = testBpos;
                        break;
                    }
                }
            }

            if (placePos == null || placeDir == null || InvUtils.getItemSlot(lambdaItem -> validSafeBlocks.selected.contains(lambdaItem)) == -1) {
                return;
            }

            Vec3d target = placePos.toCenterPos().offset(placeDir, 0.5);
            Rotation rot = RotationUtils.correctSensitivity(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), target));
            if (mode.is(modeEnum.Silent)) {
                Template.rotationManager().setRotation(rot);
            } else {
                Pair<Double, Double> pairs = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(rot, RotationUtils.entityRotation(mc.player)));
                mc.player.changeLookDirection(pairs.getLeft(), pairs.getRight());
            }

            rotating = true;
            foundSafe = true;
            return;
        }

        if (mc.player.getActiveItem().getItem() instanceof BowItem) {
            lastPredict = ProjectileUtilities.predictBow(mc.world, mc.player, 250, -1,true);
            ticks = mc.player.getItemUseTime();
            timer = System.currentTimeMillis() + aimTimeout.getIValue();
        } else if (lastPredict != null && ticks >= 1 && timer > System.currentTimeMillis()) {
            doBHR(lastPredict);
        }

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof ArrowEntity ae) {
                Entity own = ae.getOwner();
                if (own != null && own.getUuid().equals(mc.player.getUuid()) && !ae.isInGround()) {
                    if (!usedArrows.contains(ae.getUuid())) {
                        lastPredict = null;
                        doBHR(ProjectileUtilities.projectilePredict(ae, 250));
                    }
                } else {
                    usedArrows.add(ae.getUuid());
                }
            }
        }

    }

    private boolean doBHR(Pair<List<Vec3d>, HitResult> pr) {
        HitResult hr = pr.getRight();
        labelBlock:
        if (hr instanceof BlockHitResult result && result.getType() == HitResult.Type.BLOCK) {
            BlockPos bpos = result.getBlockPos();

            Direction side = result.getSide();
            Box box = switch (side) {
                case UP -> new Box(bpos.getX(), bpos.getY() + 1, bpos.getZ(), bpos.getX() + 1, bpos.getY() + 1, bpos.getZ() + 1);
                case DOWN -> null;
                default -> new Box((bpos = bpos.offset(side)).getX(), bpos.getY(), bpos.getZ(), bpos.getX() + 1, bpos.getY(), bpos.getZ() + 1);
            };

            if (box == null) {
                break labelBlock;
            }

            Vec3d closest = MathUtils.closestPointToBox(mc.player.getEyePos(), box.contract(0.05));
            if (mc.player.getEyePos().distanceTo(closest) >= 4.5 || mc.player.getBoundingBox().contains(closest)) {
                break labelBlock;
            }

            bhr = result;
            Rotation rot = RotationUtils.correctSensitivity(RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), bhr.getBlockPos().toCenterPos().offset(bhr.getSide(), 0.5)));
            if (mode.is(modeEnum.Silent)) {
                Template.rotationManager().setRotation(rot);
            } else {
                Pair<Double, Double> pairs = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(rot, RotationUtils.entityRotation(mc.player)));
                mc.player.changeLookDirection(pairs.getLeft(), pairs.getRight());
            }
            rotating = true;
            return true;
        }

        if (!(hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof TntMinecartEntity)) {
            onEnable();
        }
        return false;
    }

    private boolean placeBlock(BlockHitResult bhr) {
        if (mc.player == null || bhr == null || mc.interactionManager == null)
            return false;

        if (Template.isClickSim())
            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        boolean shouldSwing = interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult);
        if (shouldSwing) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        return shouldSwing;
    }

    private boolean isRail(BlockPos bPos) {
        return BlockUtils.isBlock(Blocks.RAIL, bPos) || BlockUtils.isBlock(Blocks.ACTIVATOR_RAIL, bPos) || BlockUtils.isBlock(Blocks.DETECTOR_RAIL, bPos) || BlockUtils.isBlock(Blocks.POWERED_RAIL, bPos);
    }

    private int railSlot() {
        for (Item item : Arrays.asList(Items.RAIL, Items.ACTIVATOR_RAIL, Items.DETECTOR_RAIL, Items.POWERED_RAIL)) {
            int i = InvUtils.getItemSlot(item);
            if (SlotUtils.isHotbar(i))
                return i;
        }
        return -1;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (bhr == null || mc.player == null || mc.interactionManager == null)
            return;

        if (!rotating) {
            onEnable();
            return;
        }

        mc.crosshairTarget = PlayerUtils.getHitResult(mc.player, Template.rotationManager().yaw(), Template.rotationManager().pitch());
        if (!(mc.crosshairTarget instanceof BlockHitResult bhrt) || mc.crosshairTarget.getPos().distanceTo(bhr.getPos()) >= 3)
            return;

        if (!(!placedSafe && placedTNT && foundSafe && safetyBlock.isEnabled())) {
            if (!(SlotUtils.isHotbar(InvUtils.getItemSlot(Items.TNT_MINECART)) || mc.player.getOffHandStack().isOf(Items.TNT_MINECART)) || railSlot() == -1) {
                return;
            }
        }

        if (!placedRail && !placedTNT && !isRail(bhrt.getBlockPos()) && railSlot() != -1 && bhrt.getType() == HitResult.Type.BLOCK) {
            InvUtils.setInvSlot(railSlot());
            placeBlock(bhrt);
            placedRail = true;
            railPos = bhrt.getBlockPos();
        } else if (!placedTNT && isRail(bhrt.getBlockPos())) {
            int usableSlot = InvUtils.getItemSlot(item -> item.getUseAction(item.getDefaultStack()) == UseAction.NONE && !(item instanceof BlockItem) && item != Items.TNT_MINECART);
            if (mc.player.getOffHandStack().isOf(Items.TNT_MINECART)) {
                if (SlotUtils.isHotbar(usableSlot)) {
                    InvUtils.setInvSlot(usableSlot);
                }
            } else if (SlotUtils.isHotbar(InvUtils.getItemSlot(Items.TNT_MINECART))) {
                InvUtils.setInvSlot(InvUtils.getItemSlot(Items.TNT_MINECART));
            }
            mc.doItemUse();
            mc.doItemUse();
            placedTNT = true;
            placedRail = true;
            if (railPos == null) {
                railPos = bhrt.getBlockPos();
            }
        } else {
            if (!placedSafe && placedTNT && foundSafe && safetyBlock.isEnabled() && bhrt.getType() == HitResult.Type.BLOCK) {
                int slot = InvUtils.getItemSlot(lambdaItem -> validSafeBlocks.selected.contains(lambdaItem));
                if (slot != -1) {
                    InvUtils.setInvSlot(slot);
                    boolean result = placeBlock(bhrt);
                }
                placedSafe = true;
            }
        }
    }

    private void print(Object... objects) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            str.append(objects[i]);
            if (i != objects.length - 1) {
                str.append(',');
                str.append(' ');
            }
        }
        mc.inGameHud.getChatHud().addMessage(Text.of(str.toString().concat(" " + mc.player.age)));
    }
}