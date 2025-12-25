package template.rip.module.modules.misc;

import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.StringSetting;

import java.util.HashMap;

public class KillInsultsModule extends Module {

    public final StringSetting insultCfg = new StringSetting("ur fat", this, "Insult");
    public final BooleanSetting message = new BooleanSetting(this, false, "Use /msg");
    public final BooleanSetting range = new BooleanSetting(this, false, "Ignore range");
    public final BooleanSetting notify = new BooleanSetting(this, false, "Notify on insult");
    private OtherClientPlayerEntity target = null;
    private boolean insulted = false;
    private  World world = null;
    private String insult;
    private final HashMap<OtherClientPlayerEntity, Boolean> attacked = new HashMap<>();

    public KillInsultsModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        target = null;
        insulted = false;
        insult = insultCfg.getContent();
        world = mc.world;
        attacked.clear();
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Post event) {
        if (!nullCheck())
            return;

        /*OtherClientPlayerEntity o = target == null ? null : mc.world.getEntityById(target.getId()) instanceof OtherClientPlayerEntity ocpe ? ocpe : null;
        String e = String.valueOf(o == null ? null : o.getEntityName());
        if (!Objects.equals(last, e)) {
            last = e;
            mc.inGameHud.getChatHud().addMessage(Text.of(last));
        }*/
        if (world == mc.world) {
            if (target == null || !isDead(target)) {
                return;
            }
            if (Boolean.FALSE.equals(attacked.get(target))) {
                return;
            }
            if (message.isEnabled()) {
                mc.getNetworkHandler().sendChatCommand("msg " + target.getNameForScoreboard() + " " + insult);
            } else {
                mc.getNetworkHandler().sendChatMessage(target.getNameForScoreboard() + " " + insult);
            }

            if (notify.isEnabled())
                Template.notificationManager().addNotification(new Notification("Player insulted", 1000, String.format("%s was insulted!", target.getNameForScoreboard())));

            insulted = true;
        }
        if (target == null || insulted || mc.world != world || !PlayerUtils.findTargets(range.isEnabled()).contains(mc.world.getEntityById(target.getId()))) {
            Entity possibleTarget = PlayerUtils.findFirstTarget();
            if (possibleTarget instanceof OtherClientPlayerEntity && possibleTarget != target && !isDead((PlayerEntity) possibleTarget)) {
                onEnable();
                target = (OtherClientPlayerEntity) possibleTarget;
            } else target = null;
        }
    }

    private boolean isDead(PlayerEntity player) {
        return player.isDead() || player.getHealth() <= 0.0 || !player.isPartOfGame() || player.isInvulnerable() || !(mc.world.getEntityById(player.getId()) instanceof OtherClientPlayerEntity);
    }

    @EventHandler
    private void attackEvent(AttackEntityEvent.Post event) {
        if (event.target instanceof OtherClientPlayerEntity) {
            attacked.put((OtherClientPlayerEntity) event.target, true);
        }
    }
}
