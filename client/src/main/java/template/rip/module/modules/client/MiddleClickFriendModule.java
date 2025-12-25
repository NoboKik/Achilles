package template.rip.module.modules.client;

import imgui.ImGui;
import me.sootysplash.bite.CharSeq;
import me.sootysplash.bite.TypeObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiddleClickFriendModule extends Module {

    boolean pressed = false;
    boolean done = false;

    public MiddleClickFriendModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        Template.moduleManager.getModule(MiddleClickFocusTargetModule.class).setEnabled(false);
        pressed = false;
        done = false;
    }

    private void toggleFriend(PlayerEntity player) {
        if (PlayerUtils.isFriend(player)) {
            PlayerUtils.removeFriend(player);
            Template.notificationManager().addNotification(new Notification("Friend Removed", 1000, "has been removed!",player.getNameForScoreboard()));
        } else {
            PlayerUtils.addFriend(player);
            Template.notificationManager().addNotification(new Notification("Friend Added", 1000, "has been added!",player.getNameForScoreboard()));
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_MIDDLE))
            pressed = true;
        else
            done = false;
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.currentScreen != null) return;

        if (!pressed)
            return;

        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity && !done) {
            toggleFriend((PlayerEntity) entityHitResult.getEntity());
            done = true;
        }

        pressed = false;
    }

    @Override
    public void renderSettings() {
        ImGui.text("Friends:");
        List<String> remove = new ArrayList<>();
        for (Map.Entry<CharSeq, TypeObject> entry : PlayerUtils.friends.entrySet()) {
            if (ImGui.button(entry.getValue().getCharSequence().toString())) {
                remove.add(entry.getKey().toString());
            }
        }
        remove.forEach(str -> PlayerUtils.friends.remove(str));

        super.renderSettings();
    }
}
