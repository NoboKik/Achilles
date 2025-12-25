package template.rip.module.modules.crystal;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.MousePressEvent;
import template.rip.api.event.events.SwitchSlotEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.util.*;
import template.rip.module.Module;
import template.rip.module.modules.player.FastPlaceModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.RegistrySetting;

import java.util.Arrays;

public class AutoHitCrystalRewriteModule extends Module {

    public final MinMaxNumberSetting slotDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Switch slot delays");
    public final MinMaxNumberSetting placeDelays = new MinMaxNumberSetting(this, 1, 2, 0, 10, 1, "Obsidian place delays");
    public final KeybindSetting activateKey = new KeybindSetting(this, GLFW.GLFW_MOUSE_BUTTON_2, "Activate Key");
    public final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch back to slot");
    public final BooleanSetting autoHitCrystalLimit = new BooleanSetting(this, false, "2 AutoHitCrystal Limit");
    public final BooleanSetting autoToggle = new BooleanSetting(this, Description.of("Automatically toggles AutoCrystal and FastPlace when necessary\nDisable this if you encounter issues!"), true, "Toggle AutoCrystal").setAdvanced();
    public final RegistrySetting<Item> workWithItems = new RegistrySetting<>(Arrays.asList(Items.TOTEM_OF_UNDYING, Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.WOODEN_SWORD), this, Registries.ITEM, RegistrySetting.autoHitCrystalPredicate, "Valid Hit Items");

    public enum hitCrystalStage implements EnumIncr<hitCrystalStage> {Initial, SwitchObsidian, PlaceObsidian, SwitchCrystal, InitCrystal, DoCrystal, SwitchBack, None}
    private final EnumHolder<hitCrystalStage> currentStage = EnumHolder.get(hitCrystalStage.None);

    private int slotCooldown, placeCooldown, lastSlot = -1;
    private boolean placedObsidian, wasFastPlaceEnabled;

    public AutoHitCrystalRewriteModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        internalEnable();
        currentStage.setValue(hitCrystalStage.None);
    }

    private void internalEnable() {
        if (!nullCheck() || InvUtils.getItemSlot(Items.OBSIDIAN) == -1 || InvUtils.getItemSlot(Items.END_CRYSTAL) == -1) {
            currentStage.setValue(hitCrystalStage.None);
            return;
        }
        currentStage.setValue(hitCrystalStage.Initial.increment());
        World world = mc.world;
        Block block;
        if (mc.crosshairTarget instanceof BlockHitResult bhr && world != null && ((block = world.getBlockState(bhr.getBlockPos()).getBlock()) == Blocks.OBSIDIAN || block == Blocks.BEDROCK)) {
            currentStage.setValue(hitCrystalStage.SwitchCrystal);
        }
        placeCooldown = placeDelays.getRandomInt();
        slotCooldown = slotDelays.getRandomInt();
        lastSlot = -1;
        placedObsidian = false;
        KeyBinding use = mc.options.useKey;
        use.setPressed(false);
    }

    @EventHandler
    private void onMousePress(MousePressEvent event) {
        onKeyPress(new KeyPressEvent(event.button, 0, event.action, 0));
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        if (event.key != activateKey.getCode()) {
            return;
        }
        switch (event.action) {
            case GLFW.GLFW_PRESS -> {
                if (mc.player != null && workWithItems.selected.contains(mc.player.getMainHandStack().getItem())) {
                    if (autoToggle.isEnabled()) {
                        FastPlaceModule fpm = Template.moduleManager.getModule(FastPlaceModule.class);
                        if (fpm != null) {
                            wasFastPlaceEnabled = fpm.isEnabled();
                            fpm.setEnabled(false);
                        }
                    }
                    internalEnable();
                }
            }
            case GLFW.GLFW_RELEASE -> {
                currentStage.setValue(hitCrystalStage.SwitchBack);
                if (autoToggle.isEnabled()) {
                    FastPlaceModule fpm = Template.moduleManager.getModule(FastPlaceModule.class);
                    if (fpm != null) {
                        fpm.setEnabled(wasFastPlaceEnabled);
                        wasFastPlaceEnabled = false;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onSwitch(SwitchSlotEvent event) {
        if (!nullCheck()) {
            return;
        }

        if (slotCooldown > 0) {
            slotCooldown--;
            return;
        }

        if (mc.player.getMainHandStack().contains(DataComponentTypes.FOOD)) {
            return;
        }

        slotCooldown = slotDelays.getRandomInt();
        int slotTo;
        switch (currentStage.getValue()) {
            case SwitchObsidian -> {
                if (!(mc.crosshairTarget instanceof BlockHitResult blockHit)
                        || blockHit.getType() == HitResult.Type.MISS
                        || BlockUtils.isBlockClickable(blockHit.getBlockPos())) {
                    currentStage.setValue(hitCrystalStage.None);
                    return;
                }

                slotTo = (slotTo = InvUtils.getItemSlot(Items.OBSIDIAN)) == -1 ? InvUtils.getItemSlot(Items.BEDROCK) : slotTo;
            }
            case SwitchCrystal -> slotTo = InvUtils.getItemSlot(Items.END_CRYSTAL);
            case SwitchBack -> {
                if (switchBack.isEnabled() && lastSlot != -1) {
                    slotTo = lastSlot;
                    lastSlot = -1;
                } else {
                    currentStage.increment();
                    return;
                }
            }
            default -> {
                return;
            }
        }
        if (event.switchSlot(slotTo)) {
            if (lastSlot == -1 && !currentStage.is(hitCrystalStage.SwitchBack)) {
                lastSlot = mc.player.getInventory().selectedSlot;
            }
            currentStage.increment();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onHandleInputs(HandleInputEvent.Pre event) {
        BlockHitResult bhr = mc.crosshairTarget instanceof BlockHitResult hr && hr.getType() == HitResult.Type.BLOCK ? hr : null;
        if (!nullCheck() || bhr == null) {
            return;
        }
        BlockPos bpos = bhr.getBlockPos();
        switch (currentStage.getValue()) {
            case PlaceObsidian -> {

                if (placeCooldown > 0) {
                    placeCooldown--;
                    return;
                }
                placeCooldown = placeDelays.getRandomInt();

                Block b = mc.world.getBlockState(bpos).getBlock();
                if (b != Blocks.OBSIDIAN && b != Blocks.BEDROCK) {
                    if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                    ActionResult ar = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    if (ar.isAccepted() && PlayerUtils.shouldSwingHand(ar)) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        currentStage.increment();
                    }
                } else {
                    currentStage.increment();
                }
                placedObsidian = true;
            }
            case InitCrystal -> {
                AutoCrystalRecodeModule autoCrystal = Template.moduleManager.getModule(AutoCrystalRecodeModule.class);
                if (autoCrystal != null) {
                    if (autoToggle.isEnabled()) {
                        autoCrystal.setEnabled(true);
                    }
                    autoCrystal.autoHitCrystalLimit = autoHitCrystalLimit.isEnabled();
                    autoCrystal.autoHitCrystalPlacedObsidian = placedObsidian;
                    autoCrystal.placesSinceLastHit = 0;
                    autoCrystal.breaksSinceLastHit = 0;
                }
                currentStage.increment();
            }
        }
    }
}
