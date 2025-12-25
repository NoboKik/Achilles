package template.rip.module.modules.player;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.Comparator;
import java.util.List;

public class AutoFishModule extends Module {

    public final MinMaxNumberSetting reelDelay = new MinMaxNumberSetting(this, 20, 50, 0, 500, 5, "Reel delays");
    private final NumberSetting reelChance = new NumberSetting(this, 100d, 0d, 100d, 1d, "Reel chance").setAdvanced();
    private final BooleanSetting autoSort = new BooleanSetting(this, true, "Auto sort inventory");
    private final NumberSetting sortDelay = new NumberSetting(this, 2, 1, 10, 1, "Sort delay");
    private final AnyNumberSetting sortEveryX = new AnyNumberSetting(this, 25, false, "Sort every X reels");

    public enum modeEnum{Temporary, Permanent, Dont_Disable}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Temporary, "Disable mode");
    private final NumberSetting blockDisableRange = new NumberSetting(this, 64d, 0d, 100d, 1d, "Disable range for players");
    private final BooleanSetting onlyIfSee = new BooleanSetting(this, false, "Only if they can see you").setAdvanced();
    private long disableTimer = System.currentTimeMillis();
    private int reels = 0;
    private int ticks = 0;
    private int i = 0;
    private boolean sorting = false;

    public AutoFishModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        disableTimer = System.currentTimeMillis();
        reels = 0;
        ticks = 0;
        i = 0;
        sorting = false;
    }

    @EventHandler
    private void onPlayCustomSound(PacketEvent.Receive event) {
        if (!nullCheck()) return;

        if (event.packet instanceof PlaySoundS2CPacket wrapper) {
            RegistryKey<SoundEvent> splash = wrapper.getSound().getKey().get();
            FishingBobberEntity bobber = mc.player.fishHook;

            if (splash.toString().contains("fishing_bobber.splash") && bobber != null) {
                if (MathUtils.getRandomInt(1, 100) > reelChance.getIValue())
                    disableTimer = System.currentTimeMillis() + 500;

                if (scren()) {
                    boolean closeEnough = new Vec3d(bobber.getX(), bobber.getY(), bobber.getZ()).distanceTo(new Vec3d(wrapper.getX(), wrapper.getY(), wrapper.getZ())) <= 0.75;

                    if (closeEnough) {
                        reels++;
                        use(2);

                        if (MathUtils.getRandomInt(1, 100) > reelChance.getIValue())
                            disableTimer = System.currentTimeMillis() + 500;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null)
            return;

        if (mc.player.fishHook == null && System.currentTimeMillis() >= disableTimer && mc.player.getMainHandStack().getItem() instanceof FishingRodItem && scren()) {
            disableTimer = System.currentTimeMillis() + 500;
            use(1);
        }

        if (!mode.is(modeEnum.Dont_Disable)) {
            for (PlayerEntity pe : mc.world.getPlayers()) {
                if (pe == mc.player)
                    continue;

                if (mc.player.distanceTo(pe) < blockDisableRange.getValue() && (!onlyIfSee.isEnabled() || pe.canSee(mc.player))) {
                    if (mode.is(modeEnum.Temporary))
                        disableTimer = System.currentTimeMillis() + 500;

                    if (mode.is(modeEnum.Permanent))
                        toggle();
                }
            }
        }

        List<ItemStack> items = InvUtils.hotbarSlotItemSlots(Items.FISHING_ROD);

        if (!items.isEmpty()) {
            items.sort(Comparator.comparing(stack -> stack.getDamage() + (stack.hasEnchantments() ? 0 : 100)));
            InvUtils.setInvSlot(mc.player.getInventory().getSlotWithStack(items.get(0)));
        }

        if (reels >= sortEveryX.getIValue() && sortEveryX.getIValue() != 0 && autoSort.isEnabled() && !sorting) {
            sorting = true;
            mc.setScreen(new InventoryScreen(mc.player));
            reels = 0;
            i = 0;
        }

        if (sorting) {
            if (mc.currentScreen instanceof InventoryScreen containerScreen) {
                int id = SlotUtils.indexToId(i);
                Slot slot = containerScreen.getScreenHandler().getSlot(id);

                if ((i < 9 && !slot.getStack().isOf(Items.FISHING_ROD)) || (i > 8 && slot.getStack().isOf(Items.FISHING_ROD))) {
                    if (ticks < sortDelay.getIValue() * 2) {
                        ticks++;
                        return;
                    }
                    mc.interactionManager.clickSlot(containerScreen.getScreenHandler().syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
                i++;
                ticks = 0;
            }
            if (i > 36) {
                sorting = false;
                if (mc.currentScreen instanceof HandledScreen<?>)
                    mc.currentScreen.close();
                i = 0;
            }
        }
    }

    private boolean scren() {
        return mc.currentScreen == null || mc.currentScreen instanceof ChatScreen;
    }

    private void use(int uses) {
        new Thread(() -> {
            for (int i = 0; i < uses; i++) {
                try {
                    for (int j = 0; j < uses; j++) {
                        Thread.sleep(reelDelay.getRandomInt());
                    }
                } catch (InterruptedException ignored) {
                }
                if (scren()) {
                    mc.options.useKey.timesPressed++;
                    if (Template.isClickSim())
                        MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                }
            }
        }).start();
    }
}