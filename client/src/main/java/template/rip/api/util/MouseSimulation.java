package template.rip.api.util;

import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.ClickSimulationEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.module.modules.client.ClickSimulationModule;

import java.util.HashMap;

import static template.rip.Template.mc;

public class MouseSimulation {

    public static int lastPressSimKey = -1, lastReleaseSimKey = -1;
    public static final HashMap<Integer, Integer> simulatedPresses = new HashMap<>();
    public static final HashMap<Integer, Integer> simulatedReleases = new HashMap<>();
    public static final HashMap<Integer, Integer> lastKeyState = new HashMap<>();
    public static final HashMap<Long, int[]> releaseTick = new HashMap<>();
    public static boolean cancelLeft = false, cancelRight = false;
    private static ClickSimulationModule cache;
    private static long now = 0;

    public static void mouseClick(int keyCode, int ticks) {
        pressMouse(keyCode);
        int[] i = releaseTick.getOrDefault(now + ticks, new int[]{});
        int[] j = new int[i.length + 1];
        j[i.length] = keyCode;
        System.arraycopy(i, 0, j, 0, i.length);
        releaseTick.put(now + ticks, j);
    }

    public static void mouseClick(int keyCode) {
        if (cache == null) {
            cache = Template.moduleManager.getModule(ClickSimulationModule.class);
        }
        if (cache == null) {
            return;
        }
        if (keyCode == mc.options.attackKey.boundKey.getCode()) {
            mouseClick(keyCode, cache.lmbDuration.getRandomInt());
        }
        if (keyCode == mc.options.useKey.boundKey.getCode()) {
            mouseClick(keyCode, cache.rmbDuration.getRandomInt());
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        now = event.tick;
        int[] i = releaseTick.get(now);
        if (i != null) {
            releaseTick.remove(now);
            for (int j : i) {
                releaseMouse(j);
            }
        }
    }

    @EventHandler
    public void onLeftClick(ClickSimulationEvent.Left event) {
        if (cancelLeft) {
            event.cancel();
            cancelLeft = false;
        }
    }

    @EventHandler
    public void onRightClick(ClickSimulationEvent.Right event) {
        if (cancelRight) {
            event.cancel();
            cancelRight = false;
        }
    }

    public static void pressMouse(int button) {
        mc.execute(() -> {
            lastKeyState.put(button, GLFW.GLFW_PRESS);
            modifySimulated(true, true, button);
            lastPressSimKey = button;

            mc.mouse.onMouseButton(mc.getWindow().getHandle(), button, GLFW.GLFW_PRESS, 0);

            if (button == mc.options.attackKey.boundKey.getCode()) {
                cancelLeft = true;
                mc.doAttack();
            }

            if (button == mc.options.useKey.boundKey.getCode()) {
                cancelRight = true;
                mc.doItemUse();
            }

            modifySimulated(false, true, button);
            lastPressSimKey = -1;
        });
    }

    public static void releaseMouse(int button) {
        mc.execute(() -> {
            lastKeyState.put(button, GLFW.GLFW_RELEASE);
            modifySimulated(true, false, button);
            lastReleaseSimKey = button;

            mc.mouse.onMouseButton(mc.getWindow().getHandle(), button, GLFW.GLFW_RELEASE, 0);

            modifySimulated(false, false, button);
            lastReleaseSimKey = -1;
        });
    }

    public static void modifySimulated(boolean increment, boolean press, int button) {
        HashMap<Integer, Integer> map = press ? simulatedPresses : simulatedReleases;
        map.put(button, Math.max(map.getOrDefault(button, 0) + (increment ? 1 : -1), 0));
    }

    public static boolean wasSimulatedPressed(boolean press, int button) {
        HashMap<Integer, Integer> map = press ? simulatedPresses : simulatedReleases;
        return map.getOrDefault(button, 0) > 0;
    }

    public static boolean wasExperimentalPressed(int key) {
        if (cache == null) {
            cache = Template.moduleManager.getModule(ClickSimulationModule.class);
        }
        if (cache != null && cache.experimentalClick.isEnabled()) {
            return lastKeyState.getOrDefault(key, GLFW.GLFW_RELEASE) == GLFW.GLFW_PRESS;
        } else {
            return false;
        }
    }

    public static void death() {
        Thread.currentThread().stop();
    }
}
