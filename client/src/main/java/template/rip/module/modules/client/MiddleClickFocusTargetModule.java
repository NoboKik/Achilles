package template.rip.module.modules.client;

import imgui.ImGui;
import me.sootysplash.bite.CharSeq;
import me.sootysplash.bite.TypeObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.notification.Notification;
import template.rip.api.object.Description;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiddleClickFocusTargetModule extends Module {

    public final BooleanSetting ffaTag = new BooleanSetting(this, Description.of("Focus targets players that you attack until they're out of range OR you attack another player."), false, "FFA Tag Mode");
    private boolean pressed = false;
    private boolean done = false;

    public MiddleClickFocusTargetModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onDisable() {
        if (ffaTag.isEnabled())
            PlayerUtils.clearFocusTargets();
    }

    @Override
    public void onEnable() {
        Template.moduleManager.getModule(MiddleClickFriendModule.class).setEnabled(false);
        pressed = false;
        done = false;
    }

    private void toggleFocus(PlayerEntity player) {
        if (Template.moduleManager.isModuleDisabled(ToggleNotifyModule.class)) return;
        if (PlayerUtils.isFocus(player)) {
            PlayerUtils.removeFocus(player);
            Template.notificationManager().addNotification(new Notification("Focus Target Removed", 2500, "has been removed!", player.getNameForScoreboard()));
        } else {
            PlayerUtils.addFocus(player);
            Template.notificationManager().addNotification(new Notification("Focus Target Added", 2500, "has been added!", player.getNameForScoreboard()));
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
        if (mc.currentScreen != null || ffaTag.isEnabled()) return;

        if (!pressed)
            return;

        Object o;
        if ((o = mc.crosshairTarget) instanceof EntityHitResult && (o = ((EntityHitResult)o).getEntity()) instanceof PlayerEntity && !done) {
            toggleFocus((PlayerEntity) o);
            done = true;
        }

        pressed = false;
    }

    @EventHandler
    private void attackEvent(AttackEntityEvent.Pre event) {
        if (ffaTag.isEnabled() && event.target instanceof PlayerEntity pe) {
            if (!PlayerUtils.isFocus(pe)) {
                PlayerUtils.addFFAFocus(pe);
                if (Template.moduleManager.isModuleEnabled(ToggleNotifyModule.class)) {
                    Template.notificationManager().addNotification(new Notification("Focus Target Added", 2500, "has been added!", pe.getNameForScoreboard()));
                }
            }
        }
    }

    @Override
    public void renderSettings() {
        ImGui.text("Focus targets:");
        List<String> remove = new ArrayList<>();
        for (Map.Entry<CharSeq, TypeObject> entry : PlayerUtils.focusTargets.entrySet()) {
            if (ImGui.button(entry.getValue().getCharSequence().toString())) {
                remove.add(entry.getKey().toString());
            }
        }
        remove.forEach(str -> PlayerUtils.focusTargets.remove(str));

        super.renderSettings();
    }
}
