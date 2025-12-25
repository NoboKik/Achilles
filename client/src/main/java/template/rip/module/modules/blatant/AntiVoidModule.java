package template.rip.module.modules.blatant;

import com.google.common.collect.Streams;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import template.rip.Template;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static template.rip.module.modules.blatant.ScaffoldModule.lastEnable;

public class AntiVoidModule extends Module {

    public enum modeEnum {Blink, Clutch}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Blink, "Mode");
    public final NumberSetting distance = new NumberSetting(this, 5, 0, 10, 1, "Distance");
    private final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private Vec3d position;
    public static boolean isClutching = false;

    public AntiVoidModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (Template.moduleManager.isModuleEnabled(ScaffoldModule.class) || !nullCheck()) return;

        if (event.packet instanceof PlayerMoveC2SPacket wrapper) {
            if (!mc.player.isOnGround() && mc.player.fallDistance > 0.1) {
                Box box = mc.player.getBoundingBox();
                Box adjustedBox = setMinY(box.offset(0, -0.5, 0), 50);

                Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

                if (blockCollisions.findAny().isEmpty()) {
                    if (mode.is(modeEnum.Blink)) {
                        packets.add(wrapper);
                        event.setCancelled(true);

                        if (position != null && mc.player.fallDistance > distance.getFValue()) {
                            Template.sendNoEvent(new PlayerMoveC2SPacket.PositionAndOnGround(position.x, position.y+0.1, position.z, false, mc.player.horizontalCollision));
                        }
                    } else if (mode.is(modeEnum.Clutch)) {
                        isClutching = true;
                        if (!Template.moduleManager.getModule(ScaffoldModule.class).isEnabled() && lastEnable+500 < System.currentTimeMillis())
                             Template.moduleManager.getModule(ScaffoldModule.class).setEnabled(true);
                    }
                }
            } else {
                isClutching = false;
                if (mc.player.isOnGround()) {
                    position = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                }

                if (!packets.isEmpty()) {
                    packets.forEach(Template::sendNoEvent);
                    packets.clear();
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && packets.size() > 1) {
            packets.clear();
        }
    }

    @EventHandler
    public void worldChange(TickEvent.Pre event) {
        if (mc.currentScreen instanceof DownloadingTerrainScreen && packets.size() > 1) {
            packets.clear();
        }
    }

    private Box setMinY(Box box, double minY) {
        return new Box(box.minX, box.minY - minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    /*
    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            for (int offset = 0; offset < height; offset += 2) {
                final Box bb = mc.player.getBoundingBox().offset(0, -offset, 0);

                if (mc.world.getCollisions(mc.player, bb).iterator().hasNext()) {
                    return true;
                }
            }
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (isFullCube(new BlockPos(mc.player.getBlockPos()).add(0, -offset, 0))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBlockUnder() {
        return isBlockUnder(mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose()));
    }

    public Block blockRelativeToPlayer(final int offsetX, final int offsetY, final int offsetZ) {
        return mc.world.getBlockState(new BlockPos(mc.player.getBlockPos()).add(offsetX, offsetY, offsetZ)).getBlock();
    }
    public boolean isFullCube(BlockPos bPos) {
        BlockState bs = mc.world.getBlockState(bPos);
        VoxelShape shape = bs.getOutlineShape(mc.world, bPos);
        if (shape == null || shape.isEmpty())
            return false;

        for (Box box : shape.getBoundingBoxes()) {
            if (!Objects.equals(box, new Box(Vec3d.ZERO, new Vec3d(1, 1, 1))))
                return false;
        }
        return true;
    }
    */
}
