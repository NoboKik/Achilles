package template.rip.module.modules.crystal;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.ExcludeMode;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.NumberSetting;

public class MainHandTotemModule extends Module {

    private final BooleanSetting anySlot = new BooleanSetting(this, false, "Any empty slot");
    private final NumberSetting totemSlot = new NumberSetting(this, 1, 1, 9, 1, "Totem Slot");
    public final MinMaxNumberSetting delay = new MinMaxNumberSetting(this, 0, 1, 0, 5, 1, "Delay");

    private int clock = 0;
    private int lastSlot = -1;
    private boolean moved = false;

    public MainHandTotemModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        moved = false;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
            moved = false;
        }
//        Packet<?> p = event.getPacket();
//        if (!(p instanceof ChunkDataS2CPacket || p instanceof HealthUpdateS2CPacket || p instanceof EntityPositionS2CPacket || p instanceof CommonPingS2CPacket || p instanceof WorldTimeUpdateS2CPacket || p instanceof BundleS2CPacket || p instanceof EntityAttributesS2CPacket || p instanceof EntityVelocityUpdateS2CPacket || p instanceof BlockUpdateS2CPacket || p instanceof EntityS2CPacket))
//            mc.inGameHud.getChatHud().addMessage(Text.of(p.getClass().getName()));
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null)
            return;

        if (mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            clock = delay.getRandomInt();
            return;
        }

        if (clock > 0) {
            clock--;
            return;
        }

        if (lastSlot != mc.player.getInventory().selectedSlot) {
            moved = false;
            lastSlot = mc.player.getInventory().selectedSlot;
        }
        if (mc.player.getInventory().getMainHandStack().getItem() == Items.AIR && InvUtils.hasItemInInventory(Items.TOTEM_OF_UNDYING) && !moved && (mc.player.getInventory().selectedSlot == totemSlot.getIValue() - 1 || anySlot.isEnabled())) {
            //mc.interactionManager.pickFromInventory(InvUtils.screenSlotOfItem(Items.TOTEM_OF_UNDYING, mc.player.playerScreenHandler, ExcludeMode.HotbarAndOffhand));
            moved = true;
        }
    }
}
