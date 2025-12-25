package template.rip.api.util;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.module.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static template.rip.Template.mc;

public class KeyUtils {

    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_MOUSE_BUTTON_2: {
                return "RMB";
            }
            case GLFW.GLFW_MOUSE_BUTTON_3: {
                return "MMB";
            }
            case GLFW.GLFW_MOUSE_BUTTON_4: {
                return "4MB";
            }
            case GLFW.GLFW_MOUSE_BUTTON_5: {
                return "5MB";
            }
            case GLFW.GLFW_KEY_UNKNOWN: {
                return "None";
            }
            case GLFW.GLFW_KEY_ESCAPE: {
                return "Esc";
            }
            case GLFW.GLFW_KEY_GRAVE_ACCENT: {
                return "~";
            }
            case GLFW.GLFW_KEY_WORLD_1: {
                return "Wrld1";
            }
            case GLFW.GLFW_KEY_WORLD_2: {
                return "Wrld2";
            }
            case GLFW.GLFW_KEY_PRINT_SCREEN: {
                return "PrntScrn";
            }
            case GLFW.GLFW_KEY_PAUSE: {
                return "Pause";
            }
            case GLFW.GLFW_KEY_INSERT: {
                return "Insert";
            }
            case GLFW.GLFW_KEY_DELETE: {
                return "Delete";
            }
            case GLFW.GLFW_KEY_HOME: {
                return "Home";
            }
            case GLFW.GLFW_KEY_PAGE_UP: {
                return "PgUp";
            }
            case GLFW.GLFW_KEY_PAGE_DOWN: {
                return "PgDwn";
            }
            case GLFW.GLFW_KEY_END: {
                return "End";
            }
            case GLFW.GLFW_KEY_TAB: {
                return "Tab";
            }
            case GLFW.GLFW_KEY_LEFT_CONTROL: {
                return "LCtrl";
            }
            case GLFW.GLFW_KEY_RIGHT_CONTROL: {
                return "RCtrl";
            }
            case GLFW.GLFW_KEY_LEFT_ALT: {
                return "LAlt";
            }
            case GLFW.GLFW_KEY_RIGHT_ALT: {
                return "RAlt";
            }
            case GLFW.GLFW_KEY_LEFT_SHIFT: {
                return "LShift";
            }
            case GLFW.GLFW_KEY_RIGHT_SHIFT: {
                return "RShift";
            }
            case GLFW.GLFW_KEY_UP: {
                return "ArrowUp";
            }
            case GLFW.GLFW_KEY_DOWN: {
                return "ArrowDown";
            }
            case GLFW.GLFW_KEY_LEFT: {
                return "ArrowLeft";
            }
            case GLFW.GLFW_KEY_RIGHT: {
                return "ArrowRight";
            }
            case GLFW.GLFW_KEY_APOSTROPHE: {
                return "Apostrophe";
            }
            case GLFW.GLFW_KEY_BACKSPACE: {
                return "Backspace";
            }
            case GLFW.GLFW_KEY_CAPS_LOCK: {
                return "CapsLock";
            }
            case GLFW.GLFW_KEY_MENU: {
                return "Menu";
            }
            case GLFW.GLFW_KEY_LEFT_SUPER: {
                return "Left Super";
            }
            case GLFW.GLFW_KEY_RIGHT_SUPER: {
                return "Right Super";
            }
            case GLFW.GLFW_KEY_ENTER: {
                return "Enter";
            }
            case GLFW.GLFW_KEY_KP_ENTER: {
                return "Numpad Enter";
            }
            case GLFW.GLFW_KEY_NUM_LOCK: {
                return "Num Lock";
            }
            case GLFW.GLFW_KEY_SPACE: {
                return "Space";
            }
            case GLFW.GLFW_KEY_F1: {
                return "F1";
            }
            case GLFW.GLFW_KEY_F2: {
                return "F2";
            }
            case GLFW.GLFW_KEY_F3: {
                return "F3";
            }
            case GLFW.GLFW_KEY_F4: {
                return "F4";
            }
            case GLFW.GLFW_KEY_F5: {
                return "F5";
            }
            case GLFW.GLFW_KEY_F6: {
                return "F6";
            }
            case GLFW.GLFW_KEY_F7: {
                return "F7";
            }
            case GLFW.GLFW_KEY_F8: {
                return "F8";
            }
            case GLFW.GLFW_KEY_F9: {
                return "F9";
            }
            case GLFW.GLFW_KEY_F10: {
                return "F10";
            }
            case GLFW.GLFW_KEY_F11: {
                return "F11";
            }
            case GLFW.GLFW_KEY_F12: {
                return "F12";
            }
            case GLFW.GLFW_KEY_F13: {
                return "F13";
            }
            case GLFW.GLFW_KEY_F14: {
                return "F14";
            }
            case GLFW.GLFW_KEY_F15: {
                return "F15";
            }
            case GLFW.GLFW_KEY_F16: {
                return "F16";
            }
            case GLFW.GLFW_KEY_F17: {
                return "F17";
            }
            case GLFW.GLFW_KEY_F18: {
                return "F18";
            }
            case GLFW.GLFW_KEY_F19: {
                return "F19";
            }
            case GLFW.GLFW_KEY_F20: {
                return "F20";
            }
            case GLFW.GLFW_KEY_F21: {
                return "F21";
            }
            case GLFW.GLFW_KEY_F22: {
                return "F22";
            }
            case GLFW.GLFW_KEY_F23: {
                return "F23";
            }
            case GLFW.GLFW_KEY_F24: {
                return "F24";
            }
            case GLFW.GLFW_KEY_F25: {
                return "F25";
            }
            default: {
                String keyName = GLFW.glfwGetKeyName(keyCode, 0);
                if (keyName == null) return "None";
                return StringUtils.capitalize(keyName);
            }
        }
    }

    public static HashMap<Integer, ArrayList<Module>> binds() {
        ArrayList<Module> modules = new ArrayList<>(Template.moduleManager.getModules());
        HashMap<Integer, ArrayList<Module>> map = new HashMap<>();
        for (Module module : modules) {
            if (map.containsKey(module.keybind.getCode())) {
                ArrayList<Module> list = map.get(module.keybind.getCode());
                list.add(module);
                map.put(module.keybind.getCode(), list);
            } else {
                map.put(module.keybind.getCode(), new ArrayList<>(Collections.singleton(module)));
            }
        }
        return map;
    }

    public static boolean isKeyPressed(int keyCode) {
        if (keyCode < 0) return false;

        if (keyCode <= GLFW.GLFW_MOUSE_BUTTON_LAST)
            return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;

        return GLFW.glfwGetKey(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
    }
}
