package template.rip.module.modules.crystal;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import template.rip.Template;
import template.rip.api.event.events.ItemUseEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

public class AirAnchorModule extends Module {

    public final NumberSetting airPlaceChance = new NumberSetting(this, 100, 0, 100, 1, "Air place Chance");
//    public final BooleanSetting disableAutoDoubleHand = new BooleanSetting(this, false, "Disable Auto Double Hand");

    private BlockPos currentBlockPos;
    private int count;

    public AirAnchorModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        currentBlockPos = null;
        count = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onItemUse(ItemUseEvent.Pre event) {
        if (MathUtils.getRandomInt(0, 100) <= airPlaceChance.getIValue() && mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
            if (mc.crosshairTarget instanceof BlockHitResult result && BlockUtils.isAnchorCharged(result.getBlockPos())) {
                if (result.getBlockPos().equals(currentBlockPos)) {
                    if (count >= 1) return;
                } else {
                    currentBlockPos = result.getBlockPos();
                    count = 0;
                }

                if (Template.isClickSim()) MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, 0));
                mc.player.swingHand(Hand.MAIN_HAND);
                count++;
            }
        }
    }
}
