package template.rip.module.modules.blatant;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import template.rip.Template;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class MultiTaskModule extends Module {

    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens").setAdvanced();
    public final BooleanSetting attack = new BooleanSetting(this, Description.of("Allows you to attack while using an item"), true, "Attack");
    public final BooleanSetting mine = new BooleanSetting(this, Description.of("Allows you to mine while using an item"), true, "Mine");
    public final BooleanSetting item = new BooleanSetting(this, Description.of("Allows you to interact with items while using an item\nLike placing crystals while eating a golden apple"), false, "ItemUse");
    public final BooleanSetting reblock = new BooleanSetting(this, Description.of("Reblocks when attacking"), false, "Reblock").setAdvanced();
    private boolean pressedL, pressedR;

    public MultiTaskModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        pressedL = false;
        pressedR = false;
    }

    @EventHandler
    public void onPlayerTick(HandleInputEvent.Pre event) {
        if (!nullCheck() || mc.currentScreen != null && disableInScreens.isEnabled())
            return;

        if (item.isEnabled()) {
            if (KeyUtils.isKeyPressed(mc.options.useKey.boundKey.getCode()) && mc.player.isUsingItem()) {
                if (!pressedR) {
                    Hand other = mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                    ItemStack otherStack = mc.player.getStackInHand(other);
//                    mc.inGameHud.getChatHud().addMessage(Text.of(String.valueOf(otherStack.getUseAction())));
                    if (otherStack.getUseAction() == UseAction.NONE || CrossbowItem.isCharged(otherStack)) {
                        PlayerUtils.doItemUseHand(other);
                    }
                }

                pressedR = true;
            } else {
                pressedR = false;
            }
        }

        if (KeyUtils.isKeyPressed(mc.options.attackKey.boundKey.getCode()) && mc.player.isUsingItem()) {
            if (attack.isEnabled() && mc.crosshairTarget instanceof EntityHitResult entityHitResult && !pressedL) {
                mc.interactionManager.attackEntity(mc.player, entityHitResult.getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            if (mine.isEnabled() && mc.crosshairTarget instanceof BlockHitResult blockHitResult && !mc.world.getBlockState(blockHitResult.getBlockPos()).isAir()) {
                mc.interactionManager.updateBlockBreakingProgress(blockHitResult.getBlockPos(), blockHitResult.getSide());
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            pressedL = true;
        } else {
            pressedL = false;
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (!nullCheck()) return;
        if (reblock.isEnabled()) {
            if (event.packet instanceof PlayerInteractEntityC2SPacket) {
                if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.OFF_HAND) {
                    PlayerInteractEntityC2SPacket wrapper = (PlayerInteractEntityC2SPacket) event.packet;

                    if (wrapper.type.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
                        event.setCancelled(true);
                        Template.sendNoEvent(event.packet);
                        Template.sendNoEvent(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, Template.rotationManager().yaw(), Template.rotationManager().pitch()));
                    }
                }
            }
        }
    }
}
