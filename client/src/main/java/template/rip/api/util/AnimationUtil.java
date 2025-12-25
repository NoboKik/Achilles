package template.rip.api.util;

import java.util.HashMap;

public class AnimationUtil {
    public static HashMap<String, Long> hoverTime = new HashMap<>();
    public static HashMap<String, Long> unhoverTime = new HashMap<>();

    public static HashMap<String, Long> pressTime = new HashMap<>();
    public static HashMap<String, Long> unpressTime = new HashMap<>();

    public static float getHoverPercentage(String id, float duration) {
        if (!hoverTime.containsKey(id) || !unhoverTime.containsKey(id)) return 0;
        long hover = hoverTime.get(id);
        long unhover = unhoverTime.get(id);
        boolean isHovered = hover > unhover;
        if (isHovered) {
            float percent = (System.currentTimeMillis() - hover) / duration;
            if (percent > 1) percent = 1;
            return percent;
        } else {
            float percent = (System.currentTimeMillis() - unhover) / duration;
            if (percent > 1) percent = 1;
            return 1 - percent;
        }
    }

    public static float getPressPercentage(String id, float duration) {
        if (!pressTime.containsKey(id)) pressTime.put(id, 0L);
        if (!unpressTime.containsKey(id)) unpressTime.put(id, 0L);
        long press = pressTime.get(id);
        long unpress = unpressTime.get(id);
        boolean isPressed = press > unpress;
        if (isPressed) {
            float percent = (System.currentTimeMillis() - press) / duration;
            if (percent > 1) percent = 1;
            return percent;
        } else {
            float percent = (System.currentTimeMillis() - unpress) / duration;
            if (percent > 1) percent = 1;
            return 1-percent;
        }
    }

    public static float getRawPressPercentage(String id, float duration) {
        if (!pressTime.containsKey(id)) pressTime.put(id, 0L);
        if (!unpressTime.containsKey(id)) unpressTime.put(id, 0L);
        long press = pressTime.get(id);
        long unpress = unpressTime.get(id);
        boolean isPressed = press > unpress;
        float percent;
        if (isPressed) {
            percent = (System.currentTimeMillis() - press) / duration;
        } else {
            percent = (System.currentTimeMillis() - unpress) / duration;
        }
        if (percent > 1) percent = 1;
        return percent;
    }

    public static boolean isHovered(String id) {
        return hoverTime.get(id) > unhoverTime.get(id);
    }

    public static void hookHover(String id, boolean b) {
        if (b) hoverTime.put(id, System.currentTimeMillis());
        else unhoverTime.put(id, System.currentTimeMillis());
    }
    public static void hookPress(String id, boolean b) {
        if (b) pressTime.put(id, System.currentTimeMillis());
        else unpressTime.put(id, System.currentTimeMillis());
    }
}
