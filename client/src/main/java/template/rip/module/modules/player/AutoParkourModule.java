package template.rip.module.modules.player;

import com.google.common.collect.Streams;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.InputUtil;
import template.rip.module.Module;
import template.rip.module.setting.settings.NumberSetting;

import java.util.stream.Stream;

public class AutoParkourModule extends Module {

    public final NumberSetting checkHeight = new NumberSetting(this, 5, 0, 10, 1, "Min Height");
    private final NumberSetting edgeDistance = new NumberSetting(this, 0.2, 0.1, 0.3, 0.001, "Edge Distance");

    public AutoParkourModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    private Box setMinY(Box box, double minY) {
        return new Box(box.minX, box.minY - minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (mc.player == null || mc.world == null || !event.check || mc.currentScreen != null || !mc.player.isOnGround())
            return;

        Box box = mc.player.getBoundingBox();
        Box adjustedBox = setMinY(box.offset(0, -0.5, 0).expand(-edgeDistance.getValue(), 0, -edgeDistance.getValue()), checkHeight.getValue());

        Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));
        if (blockCollisions.findAny().isEmpty()) {
            if (!event.input.playerInput.sneak()) {
                event.input.playerInput = InputUtil.setJumping(event.input.playerInput, true);
            }
        }
    }
}
