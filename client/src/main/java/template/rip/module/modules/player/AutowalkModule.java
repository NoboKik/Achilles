package template.rip.module.modules.player;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InputUtil;
import template.rip.api.util.ItemHelper;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class AutowalkModule extends Module {

    public final BooleanSetting w = new BooleanSetting(this, true, "W");
    public final BooleanSetting jump = new BooleanSetting(this, false, "Jump");
    public final BooleanSetting ely = new BooleanSetting(this, false, "ElytraGlide");
    public final BooleanSetting disableInScreens = new BooleanSetting(this, true, "Disable in screens");

    public AutowalkModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onDisable() {
        mc.options.forwardKey.setPressed(false);
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (mc.player == null || mc.getNetworkHandler() == null || !event.check) {
            return;
        }

        if (disableInScreens.isEnabled() && mc.currentScreen != null) {
            return;
        }

        if (jump.isEnabled() && mc.player.isOnGround()) {
            event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
        }

        if (w.isEnabled()) {
            event.input.movementForward = 1f;
        }

        ItemStack itemStack;
        if (ely.isEnabled() && !mc.player.getAbilities().flying && !mc.player.hasVehicle() && !mc.player.isClimbing() && (itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST)).isOf(Items.ELYTRA) && ItemHelper.isUsable(itemStack) && mc.player.checkGliding()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }
}
