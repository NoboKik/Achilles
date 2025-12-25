package template.rip.deprecated;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PickItemFromBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PickItemFromEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class PickTestModule extends Module {
    public final BooleanSetting mainHandAuto = new BooleanSetting(this, true, "MainHand AutoTotem");
    public PickTestModule() {
        super(Category.MISC, Description.of("Pick block experiment (it works)"), "PickTest");
    }
    int i = 0;
    int lastSlot = -1;
    boolean moved = false;

    @Override
    public void onEnable() {
        i = 0;
        moved = false;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof InventoryS2CPacket) {
            moved = false;
        }
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null)
            return;

        if (i > 36)
            i = 0;
        if (mainHandAuto.isEnabled()) {
            if (lastSlot != mc.player.getInventory().selectedSlot) {
                moved = false;
                lastSlot = mc.player.getInventory().selectedSlot;
            }
            if (mc.player.getInventory().getMainHandStack().getItem() == Items.AIR && InvUtils.hasItemInInventory(Items.TOTEM_OF_UNDYING) && !moved) {
                //mc.interactionManager.pickFromInventory(InvUtils.inventorySlotOfItem(Items.TOTEM_OF_UNDYING));
                moved = true;
            }
        } else {
            if (!mc.player.getInventory().getStack(i).isEmpty()) {
                if (PlayerInventory.isValidHotbarIndex(i)) {
                    mc.player.getInventory().selectedSlot = i;
                } else {
                    //mc.interactionManager.pickFromInventory(i);
                }
            }
            i++;
        }
    }
}
