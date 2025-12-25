package template.rip.module.modules.blatant;

import net.minecraft.block.CobwebBlock;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.BlinkEvent;
import template.rip.api.event.events.BlockCollisionEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class NoSlowdownModule extends Module {

    public final NumberSetting slowdownSpeed = new NumberSetting(this, 1, 0.2, 1, 0.1, "Slowdown speed");
    public final BooleanSetting sprint = new BooleanSetting(this, true, "Allow sprint");
    public final BooleanSetting sword = new BooleanSetting(this, true, "NoSlow Sword");
    public final BooleanSetting offHand = new BooleanSetting(this, true, "NoSlow offhand");
    public final BooleanSetting mainHand = new BooleanSetting(this, true, "NoSlow mainhand");

    public enum modeEnum{None, Switch, SwitchUse, Sneak, Grim, Reuse, TickReuse, Hypixel, HypixelNew, Use, Release/*, Blink*/}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.None, "Mode");
    public final BooleanSetting noWeb = new BooleanSetting(this, false, "NoWeb");
    public final NumberSetting velocityH = new NumberSetting(this, 1, 0, 1, 0.01, "NoWeb Horizontal slowdown");
    public final NumberSetting velocityV = new NumberSetting(this, 1, 0, 1, 0.01, "NoWeb Vertical slowdown");
    public final BooleanSetting grim = new BooleanSetting(this, false, "NoWeb Grim (200 pps)");

    public NoSlowdownModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return " " + mode.getDisplayName();
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @EventHandler
    private void onBlock(BlockCollisionEvent event) {
        if (!noWeb.isEnabled() || !slowDown() || event.ent != mc.player || !(event.blockState.getBlock() instanceof CobwebBlock))
            return;

        event.setCancelled(true);
        mc.player.slowMovement(event.blockState, new Vec3d(velocityH.value, velocityV.value, velocityH.value));
        if (grim.isEnabled())
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, event.pos, Direction.DOWN));
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!nullCheck() || !slowDown())
            return;

        if (mc.player.isUsingItem()) {
            switch (mode.getMode()) {
                case Sneak: {
                    if (!mc.player.isSneaking()) {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    }
                    break;
                }
                case Switch: {
                    int slot = SlotUtils.isHotbar(mc.player.getInventory().selectedSlot + 1) ? mc.player.getInventory().selectedSlot + 1 : mc.player.getInventory().selectedSlot - 1;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    break;
                }
                case Grim: {
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    } else {
                        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    }
                    break;
                }
                case Reuse, Release: {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                    break;
                }
                case TickReuse : {
                    if (mc.player.age % 2 == 0) {
                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                    } else {
                        mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    }
                    break;
                }
                case Use: {
                    mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    break;
                }
                case SwitchUse: {
                    int slot = SlotUtils.isHotbar(mc.player.getInventory().selectedSlot + 1) ? mc.player.getInventory().selectedSlot + 1 : mc.player.getInventory().selectedSlot - 1;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    break;
                }
                case Hypixel : {
                    //if (mc.player.age % 3 == 0 && mc.player.getActiveHand() == Hand.OFF_HAND)
                    //    mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                    //            new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, mc.player.getYaw(), mc.player.getPitch())
                    //    );
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        int slot = SlotUtils.isHotbar(mc.player.getInventory().selectedSlot + 1) ? mc.player.getInventory().selectedSlot + 1 : mc.player.getInventory().selectedSlot - 1;
                        Template.sendNoEvent(new UpdateSelectedSlotC2SPacket(slot));
                        Template.sendNoEvent(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                        mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                        //Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
                        //Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
                    } else if (mc.player.age % 3 == 0) {
                        Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    }
                    break;
                }
                case HypixelNew: {
                    //if (mc.player.age % 3 == 0 && mc.player.getActiveHand() == Hand.OFF_HAND)
                    //    mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                    //            new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, mc.player.getYaw(), mc.player.getPitch())
                    //    );
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                        Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    } else if (mc.player.age % 3 == 0) {
                        Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    }
                    break;
                }
                /*case Blink -> {
                    if (mc.player.getItemUseTime() >= ticks) {
                        if (!shouldBlink) {
                            shouldBlink = true;
                            startedUsing = true;
                            ticksToStop = mc.player.getItemUseTimeLeft() + 1 + mc.player.age;
                            if (startedUsing && mc.player.getItemUseTime() == ticks + 2) {
                                mc.interactionManager.stopUsingItem(mc.player);
                            }
                        }
                    }
                }*/
            }
        }
    }

    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (!nullCheck() || !slowDown())
            return;

        if (mc.player.isUsingItem() && mode.is(modeEnum.HypixelNew) && mc.player.getActiveHand() == Hand.OFF_HAND &&
                event.packet instanceof PlayerInteractItemC2SPacket &&
                ((PlayerInteractItemC2SPacket) event.packet).getHand() == Hand.OFF_HAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!nullCheck() || !slowDown())
            return;

        if (mode.is(modeEnum.Sneak) && !mc.player.isSneaking() && mc.player.isUsingItem()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        if (mc.player.isUsingItem()) {
            if (mode.is(modeEnum.Reuse)) {
                mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), sequence, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
            }
        }
    }

    @EventHandler
    private void onBlink(BlinkEvent event) {
        /*if (!mode.is(modeEnum.Blink)) {
            return;
        }
        if (!nullCheck()) {
            return;
        }
        if (mc.player.age > ticksToStop) {
            shouldBlink = false;
        }
        if (shouldBlink) {
            event.blink = true;
        }
        if (event.blink) {
            mc.getMessageHandler().onGameMessage(Text.of("blinking"), true);
        } else {
            mc.getMessageHandler().onGameMessage(Text.of(""), true);
        }*/
    }

    public boolean check() {
        return true;
    }

    public boolean slowDown() {
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) && mainHand.isEnabled() && mc.player.getActiveHand() == Hand.MAIN_HAND
                && (mode.is(modeEnum.HypixelNew) || mode.is(modeEnum.Hypixel))) {
            return mc.player.isOnGround();
        }
        /*if (mode.is(modeEnum.Blink)) {
            return lastWasBlink && mc.player.getItemUseTime() > ticks + 3;
        }*/
        if (offHand.isEnabled() && mc.player.getActiveHand() == Hand.OFF_HAND) {
            return true;
        }
        if (mainHand.isEnabled() && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return true;
        }
        return sword.isEnabled() && (mc.player.getActiveItem().getItem() instanceof SwordItem || mc.player.getActiveItem().isOf(Items.SHIELD));
    }
}
