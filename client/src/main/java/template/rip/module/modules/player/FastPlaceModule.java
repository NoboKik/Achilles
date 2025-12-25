package template.rip.module.modules.player;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import template.rip.Template;
import template.rip.api.event.events.ItemUseEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.InvUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.RegistrySetting;

import java.util.Arrays;

public class FastPlaceModule extends Module {

    private final MinMaxNumberSetting delays = new MinMaxNumberSetting(this, 25, 75, 0, 250, 1, "Delays");
    private enum selectMode {Simple, AllItems}
    private final ModeSetting<selectMode> selectOptions = new ModeSetting<>(this, selectMode.Simple, "Select Mode");
    private final BooleanSetting xpOnly = new BooleanSetting(this, false, "Only XP");
    private final BooleanSetting obiOnly = new BooleanSetting(this, false, "Only Obsidian");
    private final BooleanSetting blocks = new BooleanSetting(this, true, "Blocks");
    private final BooleanSetting items = new BooleanSetting(this, true, "Items");
    private final RegistrySetting<Item> advancedItemSelect = new RegistrySetting<>(Arrays.asList(Items.EXPERIENCE_BOTTLE, Items.OBSIDIAN, Items.SNOWBALL, Items.EGG), this, Registries.ITEM, "Whitelisted Items");
    public final BooleanSetting autoPlace = new BooleanSetting(this, Description.of("Doesn't require the minecraft use key to be pressed"), false, "Auto Place");
    private final BooleanSetting onlyIfCanPlace = new BooleanSetting(this, true, "Only if you can place");
    private final BooleanSetting onlyOnSide = new BooleanSetting(this, false, "Only on sides of blocks");

    private long timer = System.currentTimeMillis() + delays.getRandomInt();

    public FastPlaceModule(Category category, Description description, String name) {
        super(category, description, name);
        xpOnly.addConditionMode(selectOptions, selectMode.Simple);
        obiOnly.addConditionMode(selectOptions, selectMode.Simple);
        blocks.addConditionMode(selectOptions, selectMode.Simple);
        items.addConditionMode(selectOptions, selectMode.Simple);
        advancedItemSelect.addConditionMode(selectOptions, selectMode.AllItems);
    }

    @Override
    public void onEnable() {
        timer = System.currentTimeMillis() + delays.getRandomInt();
    }

    @Override
    public String getSuffix() {
        return String.format(" %s %s", delays.getIMinValue(), delays.getIMaxValue());
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()) && !autoPlace.isEnabled())
            return;

        if (System.currentTimeMillis() > timer) {
            if (canDo()) {
                onEnable();
                mc.doItemUse();
                if (Template.isClickSim())
                    MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
            } else {
                timer = System.currentTimeMillis() + delays.getRandomInt();
            }
        }
    }

    private boolean canDo() {
        ItemStack mainHandStack = mc.player.getMainHandStack();
        ItemStack offHandStack = mc.player.getOffHandStack();
        Item mainHandItem = mainHandStack.getItem();
        Item offHandItem = mc.player.getOffHandStack().getItem();

        if (mc.currentScreen != null) {
            return false;
        }

        switch (selectOptions.getMode()) {
            case Simple: {
                if (!(mainHandStack.isOf(Items.EXPERIENCE_BOTTLE) || offHandStack.isOf(Items.EXPERIENCE_BOTTLE)) && xpOnly.isEnabled())
                    return false;

                if (!InvUtils.canUseItem(mc.player, Items.OBSIDIAN) && obiOnly.isEnabled()) {
                    return false;
                }

                boolean mainhandBlock = mainHandItem instanceof BlockItem;
                if (!xpOnly.isEnabled()) {
                    if (mainhandBlock || offHandItem instanceof BlockItem) {
                        if (!blocks.isEnabled()) return false;
                    } else {
                        if (!items.isEnabled()) return false;
                    }
                }

                if ((mainHandStack.isOf(Items.RESPAWN_ANCHOR) || mainHandStack.isOf(Items.GLOWSTONE)) || (offHandStack.isOf(Items.RESPAWN_ANCHOR) || offHandStack.isOf(Items.GLOWSTONE)))
                    return false;

                if (mainHandStack.get(DataComponentTypes.FOOD) != null)
                    return false;

                if (offHandStack.get(DataComponentTypes.FOOD) != null) {
                    if (!mainhandBlock) return false;
                    else if (!(mc.crosshairTarget instanceof BlockHitResult) || BlockUtils.isBlock(Blocks.AIR, ((BlockHitResult) mc.crosshairTarget).getBlockPos()))
                        return false;
                }

                if (mainHandItem instanceof RangedWeaponItem || offHandItem instanceof RangedWeaponItem)
                    return false;
                break;
            }
            case AllItems: {
                ItemStack stack = InvUtils.usableStack(mc.player);
                if (stack.isEmpty()) {
                    return false;
                }
                if (!advancedItemSelect.selected.contains(stack.getItem())) {
                    return false;
                }
                break;
            }
        }

        if (onlyIfCanPlace.isEnabled()) {
            if (mc.crosshairTarget.getType() == HitResult.Type.MISS)
                return false;

            if (mc.crosshairTarget instanceof BlockHitResult bhr) {
                BlockItem bi = mainHandItem instanceof BlockItem ? (BlockItem) mainHandItem : offHandItem instanceof BlockItem ? (BlockItem) offHandItem : null;
                if (bi == null || !bi.canPlace(new ItemPlacementContext(mc.player, Hand.MAIN_HAND, mainHandStack, bhr), mc.world.getBlockState(bhr.getBlockPos())))
                    return false;
            }
        }

        return !onlyOnSide.isEnabled() || !(mc.crosshairTarget instanceof BlockHitResult) || (((BlockHitResult) mc.crosshairTarget).getSide() != Direction.UP && (((BlockHitResult) mc.crosshairTarget).getSide() != Direction.DOWN));
    }

    @EventHandler
    private void onItemUse(ItemUseEvent.Pre event) {
        if (System.currentTimeMillis() > timer && canDo()) {
            if (mc.crosshairTarget instanceof BlockHitResult blockHitResult) {
                if (mc.world.getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.FIRE))
                    event.cancel();
            }
        }
    }
}
