package template.rip.deprecated;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.NumberSetting;

public class PathModule extends Module {
    public final ColorSetting boxColor = new ColorSetting(this, new JColor(JColor.RED), true, "Line Color");
    public final ColorSetting cornerColor = new ColorSetting(this, new JColor(JColor.PINK), true, "Corner Color");
    public final ColorSetting startEndColor = new ColorSetting(this, new JColor(JColor.WHITE), true, "Start and End Color");
    public final NumberSetting maxTries = new NumberSetting(this, 50, 0, 500, 1, "Max Tries");

    public PathModule() {
        super(Category.MISC, Description.of("Renders a path to the target"), "Path");
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        Packet<?> p = event.packet;
        if (p instanceof PlayerInteractBlockC2SPacket) {
            mc.inGameHud.getChatHud().addMessage(Text.of("Block " + ((PlayerInteractBlockC2SPacket) p).getHand() + " " + mc.player.age));
        }
        if (p instanceof PlayerInteractItemC2SPacket) {
            mc.inGameHud.getChatHud().addMessage(Text.of("Item " + ((PlayerInteractItemC2SPacket) p).getHand() + " " + mc.player.age));
        }
        if (p instanceof HandSwingC2SPacket) {
            mc.inGameHud.getChatHud().addMessage(Text.of("Swing " + ((HandSwingC2SPacket) p).getHand() + " " + mc.player.age));
        }
    }
    @EventHandler
    private void onRender(WorldRenderEvent event) {
        Entity target = PlayerUtils.findFirstTarget();
        if (target == null)
            return;

        /*RenderUtils.Render3D.renderBox(new Box(mc.player.getBlockPos(), mc.player.getBlockPos().add(1, 1, 1)), startEndColor.getColor(), 25, event.context);
        RenderUtils.Render3D.renderBox(new Box(target.getBlockPos(), target.getBlockPos().add(1, 1, 1)), startEndColor.getColor(), 25, event.context);

        Path path = PlayerUtils.path(mc.player.getBlockPos(), target.getBlockPos(), maxTries.getIValue());

        for (Pair<Vec3d, Vec3d> pr : path.lines) {
            RenderUtils.Render3D.renderLineTo(pr.getLeft(), pr.getRight(), boxColor.getColor(), 1f, event.context);
        }
        for (Vec3d vec3d : path.corners) {
            RenderUtils.Render3D.renderBox(new Box(vec3d.subtract(0.1, 0.1, 0.1), vec3d.add(0.1, 0.1, 0.1)), cornerColor.getColor(), cornerColor.getColor().getAlpha(), event.context);
        }*/


    }

   /* @EventHandler
    private void onTick(PlayerTickEvent.Post event) {
        if (mc.player.age % 200 == 0) {
            Entity le = PlayerUtils.findFirstTarget();
            if (le != null) {
                for (ItemStack eq : le.getItemsEquipped()) {
                    if (!eq.isEmpty()) {
                        StringBuilder item = new StringBuilder();
                        item.append(eq.getItem().toString()).append(" ").append(eq.getCount()).append(" dmg: ").append(eq.getDamage()).append(" ");
                        for (Map.Entry<Enchantment, Integer> str : EnchantmentHelper.get(eq).entrySet()) {
                            item.append(str.getKey().getName(str.getValue())).append(" ");
                        }
                        mc.inGameHud.getChatHud().addMessage(Text.of(item.toString()));
                    }
                }
            }
        }
    }*/

}
