package template.rip.module.modules.combat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class ObiTrapMacroModule extends Module {

    public final NumberSetting placeDelayMin = new NumberSetting(this, 50, 0, 500, 1, "Obi Place Delay min");
    public final NumberSetting placeDelayMax = new NumberSetting(this, 100, 0, 500, 1, "Obi Place Delay max");
    private final BooleanSetting goToPrevSlot = new BooleanSetting(this, true, "Go To Prev Slot");
    private final BooleanSetting boxPlayer = new BooleanSetting(this, false, "Tries to box the target");
    private final KeybindSetting activateKey = new KeybindSetting(this, -1, "Activate Key");
    private boolean placedWater = false;
    private boolean placedFootLava = false;
    private boolean placedHeadLava = false;
    private int prevSlot = -1;
    private boolean pressed = false;
    private long timer = System.currentTimeMillis();

    public ObiTrapMacroModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
        pressed = false;
        placedWater = false;
        placedFootLava = false;
        placedHeadLava = false;
        timer = System.currentTimeMillis();
    }

    public void updateSlot(int slot) {
        if (mc.player == null)
            return;

        mc.player.getInventory().selectedSlot = slot;
    }

    public boolean passedTime(double time, long timer) {
        return timer + time <= System.currentTimeMillis();
    }

    private boolean canPlace() {
        if (placeDelayMin.getIValue() == 0 && placeDelayMax.getIValue() == 0)
            return true;
        if (passedTime(MathUtils.getRandomDouble(placeDelayMin.value, placeDelayMax.value), timer)) {
            timer = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    private boolean canSwitch() {
        if (placeDelayMin.getIValue() == 0 && placeDelayMax.getIValue() == 0)
            return true;
        if (passedTime(MathUtils.getRandomDouble(placeDelayMin.value, placeDelayMax.value), timer)) {
            timer = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    private void placeBlock() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null)
            return;

        if (!(mc.crosshairTarget instanceof BlockHitResult) || mc.crosshairTarget.getType() == HitResult.Type.MISS)
            return;

        if (Template.isClickSim())
            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget);
        if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void place() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null)
            return;

        if (!(mc.crosshairTarget instanceof BlockHitResult) || mc.crosshairTarget.getType() == HitResult.Type.MISS)
            return;


        if (Template.isClickSim())
            MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());

        ActionResult interactionResult = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        if ((interactionResult.isAccepted() && PlayerUtils.shouldSwingHand(interactionResult))) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public List<BlockPos> toBox() {
        List<BlockPos> empty = new ArrayList<>();

        Entity target = PlayerUtils.findFirstTarget();
        if (target == null || mc.world == null) {
            return empty;
        }

        for (int x = -1; x <= 1; x += 2) {
            BlockPos bpos = target.getBlockPos().add(x, 0, 0);
            if (mc.world.getBlockState(bpos).isLiquid() || mc.world.getBlockState(bpos).isAir())
                empty.add(bpos);
        }

        for (int z = -1; z <= 1; z += 2) {
            BlockPos bpos = target.getBlockPos().add(0, 0, z);
            if (mc.world.getBlockState(bpos).isLiquid() || mc.world.getBlockState(bpos).isAir())
                empty.add(bpos);
        }

        BlockPos bpos = target.getBlockPos().add(0, 2, 0);
        if (mc.world.getBlockState(bpos).isLiquid() || mc.world.getBlockState(bpos).isAir())
            empty.add(bpos);

        return empty;
    }

    public boolean validWater(BlockPos targetBlock, Entity target) {
        return ((targetBlock.getY() == target.getBlockY() + 1) && ((Math.abs(targetBlock.getZ() - target.getBlockZ()) == 1 && targetBlock.getX() - target.getBlockX() == 0) || (Math.abs(targetBlock.getX() - target.getBlockX()) == 1 && targetBlock.getZ() - target.getBlockZ() == 0))) || targetBlock.compareTo(target.getBlockPos().up(2)) == 0;
    }

    boolean key = false;

    @EventHandler
    private void onInput(InputEvent event) {
        if (activateKey.isPressed()) {
            if (key) {
                if (prevSlot == -1 && mc.player != null) {
                    prevSlot = mc.player.getInventory().selectedSlot;
                }
                pressed = !pressed;
                key = false;
            }
        } else if (!key) {
            key = true;
        }
    }

    public BlockPos targetBlock() {
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || mc.crosshairTarget.getType() == HitResult.Type.MISS || mc.world == null || mc.player == null) {
            // this should not be a problem, since we always check if we're close to the client target
            return new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        Item item = mc.player.getMainHandStack().getItem();
        if (item instanceof BucketItem bi) {
            BlockState state = mc.world.getBlockState(bhr.getBlockPos());
            Block block = state.getBlock();
            // vanilla checks
            boolean bl3 = state.isAir() || state.canBucketPlace(bi.fluid) || block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(mc.player, mc.world, bhr.getBlockPos(), state, bi.fluid);
            if (bl3)
                return bhr.getBlockPos().offset(bhr.getSide(), 0);
        }
        return bhr.getBlockPos().offset(bhr.getSide(), 1);
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (mc.currentScreen != null || mc.player == null || mc.world == null) return;

        if (!pressed) {
            if (prevSlot != -1 && goToPrevSlot.isEnabled()) {
                if (canSwitch()) {
                    updateSlot(prevSlot);
                    prevSlot = -1;
                }
            } else {
                onEnable();
            }
            return;
        }

        LivingEntity target = PlayerUtils.findFirstLivingTargetOrNull();
        if (target == null) {
            return;
        }

        if (boxPlayer.isEnabled()) {
            List<BlockPos> box = toBox();
            if (InvUtils.getBlockSlot() != -1 && !box.isEmpty()) {
                if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem) && canSwitch()) {
                    updateSlot(InvUtils.getBlockSlot());
                }
                if (mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem) {
                    for (BlockPos bpos : box) {
                        if (targetBlock().compareTo(bpos) == 0 && canPlace()) {
                            placeBlock();
                        }
                    }
                }
                return;
            }
        }
        if (!placedWater) {
            if (InvUtils.hasItemInHotbar(Items.WATER_BUCKET)) {
                if (!(mc.player.getInventory().getMainHandStack().getItem() == Items.WATER_BUCKET) && canSwitch()) {
                    updateSlot(InvUtils.getItemSlot(Items.WATER_BUCKET));
                }
                if (mc.player.getInventory().getMainHandStack().getItem() == Items.WATER_BUCKET && canPlace() && validWater(targetBlock(), target)) {
                    place();
                    placedWater = true;
                }
                if (!placedWater)
                    return;
            }
        }

        if (!placedFootLava) {
            if (InvUtils.hasItemInHotbar(Items.LAVA_BUCKET)) {
                if (!(mc.player.getInventory().getMainHandStack().getItem() == Items.LAVA_BUCKET) && canSwitch()) {
                    updateSlot(InvUtils.getItemSlot(Items.LAVA_BUCKET));
                }
                if (mc.player.getInventory().getMainHandStack().getItem() == Items.LAVA_BUCKET && canPlace() && targetBlock().compareTo(target.getBlockPos()) == 0) {
                    place();
                    placedFootLava = true;
                }
                if (!placedFootLava)
                    return;
            }
        }

        if (!placedHeadLava) {
            if (InvUtils.hasItemInHotbar(Items.LAVA_BUCKET)) {
                if (!(mc.player.getInventory().getMainHandStack().getItem() == Items.LAVA_BUCKET) && canSwitch()) {
                    updateSlot(InvUtils.getItemSlot(Items.LAVA_BUCKET));
                }
                if (mc.player.getInventory().getMainHandStack().getItem() == Items.LAVA_BUCKET && canPlace() && targetBlock().compareTo(target.getBlockPos().up()) == 0) {
                    place();
                    placedHeadLava = true;
                }
                if (!placedHeadLava)
                    return;
            }
        }
        pressed = false;
    }
}
