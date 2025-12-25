package template.rip.module.modules.blatant;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;

public class CriticalsModule extends Module {

    public enum modeEnum{Packet, OldGrim_OffGround, BlocksMC}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Packet, "Mode");

    public CriticalsModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    // please add an "OffGround" to the name if the criticals only works while offground, it is required for logic in PlayerUtils

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
    private void onAttack(AttackEntityEvent.Pre event) {
        if (mc.player == null || mc.getNetworkHandler() == null || mc.player.isInLava() || mc.player.isInSwimmingPose())
            return;

        float yaw = Template.rotationManager().isEnabled() ? Template.rotationManager().getClientRotation().fyaw() : mc.player.getYaw();
        switch (mode.getMode()) {
            case Packet: {
                if (!mc.player.isOnGround()) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.0001, mc.player.getZ(), yaw, PlayerUtils.randomPitch(), false, mc.player.horizontalCollision));
                } else {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 0.0001, mc.player.getZ(), yaw, PlayerUtils.randomPitch(), false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.0001, mc.player.getZ(), yaw, PlayerUtils.randomPitch(), false, mc.player.horizontalCollision));
                }
                break;
            }
            case BlocksMC: {
                double x = mc.player.getVelocity().x;
                double y = mc.player.getVelocity().y;
                double z = mc.player.getVelocity().z;

                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.001091981, z, true, mc.player.horizontalCollision));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.000114514, z, false, mc.player.horizontalCollision));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
                break;
            }
            case OldGrim_OffGround: {
                if (!mc.player.isOnGround())
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.000001, mc.player.getZ(), yaw, PlayerUtils.randomPitch(), false, mc.player.horizontalCollision));
                break;
            }
        }
    }
}
