package template.rip.module.modules.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import template.rip.Template;
import template.rip.api.event.events.AttackEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

public class SwordBlockModule extends Module {

    public final BooleanSetting interactBlock = new BooleanSetting(this, false, "Interact Block");
    public final BooleanSetting offhandPacket = new BooleanSetting(this, false, "Packet Offhand");
    public final BooleanSetting legitAb = new BooleanSetting(this, false, "Legit via autoblock");
    public final NumberSetting unblockTick = new NumberSetting(this, 2, 2, 5, 1, "Tick to unblock at");

    private boolean waitTick = false;

    public SwordBlockModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onAttack(AttackEvent.Post event) {
        if (interactBlock.isEnabled() && nullCheck()) {
            mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(offhandPacket.isEnabled() ? Hand.OFF_HAND : Hand.MAIN_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
        }
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (!nullCheck() || !legitAb.isEnabled() || !KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()) || !(mc.player.getMainHandStack().getItem() instanceof SwordItem))
            return;

//      mc.inGameHud.getChatHud().addMessage(Text.of(String.valueOf(mc.player.age % unblockTick.getIValue())));
        mc.options.useKey.setPressed(mc.player.age % unblockTick.getIValue() != 0);
    }
    @EventHandler
    private void onTick(PlayerTickEvent.Post event) {
        waitTick = false;
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (!nullCheck() || waitTick || mc.getNetworkHandler() == null || !(event.packet instanceof PlayerInteractItemC2SPacket) || legitAb.isEnabled() || !offhandPacket.isEnabled()) return;

        Packet<?> packet = event.packet;
        if (packet instanceof PlayerInteractItemC2SPacket wrapper) {
            Hand hand = wrapper.getHand();
            ItemStack itemInHand = mc.player.getStackInHand(hand);

            if (hand == Hand.MAIN_HAND && itemInHand.getItem() instanceof SwordItem) {
                ItemStack offHandItem = mc.player.getStackInHand(Hand.OFF_HAND);
                if (!(offHandItem.getItem() instanceof ShieldItem)) {
                    waitTick = true;

                    mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                } else {
                    event.setCancelled(true);
                    mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, wrapper.getSequence(), Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                }
            }
        }
    }
}
