package template.rip.module.modules.client;

//import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.event.events.FastTickEvent;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.AnimationUtil;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.*;
import template.rip.gui.windowgui.MainMenu;
import template.rip.gui.windowgui.ModulesMenu;
import template.rip.module.Module;
import template.rip.module.modules.render.ArrayListModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.KeybindSetting;
import template.rip.module.setting.settings.ModeSetting;


import static template.rip.api.object.Blur.drawBlurs;
import static template.rip.api.util.BlurUtil.drawFullBlur;

public class AchillesSettingsModule extends Module {

    public enum modeEnum {ClickGUI, Menu}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.ClickGUI, "Mode");

    public enum styleEnum {Glass, Flat, Moon}

    public final ModeSetting<styleEnum> clickGuiStyle = new ModeSetting<>(this, styleEnum.Glass, "ClickGUI Style");
    // public enum menuModulesModeEnum{Rows, Tabs}
    // public final ModeSetting<menuModulesModeEnum> menuModulesMode = new ModeSetting<>("Menu Style", this, menuModulesModeEnum.Rows, menuModulesModeEnum.values());
    public final ColorSetting color = new ColorSetting(this, new JColor(0.90f, 0.27f, 0.33f), false, "Color");
    public final BooleanSetting blatantMode = new BooleanSetting(this, false, "Blatant Mode");
    public final BooleanSetting advancedMode = new BooleanSetting(this, false, "Advanced Settings");
    public final BooleanSetting notifications = new BooleanSetting(this, true, "Notifications");
    public final ColorSetting searchColor = new ColorSetting(this, new JColor(255, 204, 0), false, true, Description.of(), "Searched setting color");
    public final BooleanSetting blurSetting = new BooleanSetting(this, true, "Blur");
    public final BooleanSetting openAnimation = new BooleanSetting(this, true, "Open Animation");
    public final BooleanSetting pauseMenuTick = new BooleanSetting(this, false, "Pause Menu Events").setAdvanced();
    public final BooleanSetting noVisuals = new BooleanSetting(this, true, "No Visuals in UI");

    public enum moveFixModeEnum {Silent, Direct, Backwards_Sprint, Off}

    public final ModeSetting<moveFixModeEnum> moveFixMode = new ModeSetting<>(this, moveFixModeEnum.Silent, "MoveFix Mode");

    public final KeybindSetting keyBind = new KeybindSetting(this, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "Setting Reset Key");
//    public final BooleanSetting dontStopParty = new BooleanSetting("Don't stop the partehhh!!!!!", this, false);

    public static final KeybindSetting loginBind = new KeybindSetting(null, GLFW.GLFW_KEY_F8, "Login Screen Key") {
        @Override
        public void setKeyCode(int code) {
            super.setKeyCode(GLFW.GLFW_KEY_F8);
        }
    };

    public AchillesSettingsModule(Category category, Description description, String name) {
        super(category, description, name);
        //ShaderEffectRenderCallback.EVENT.register((deltaTick) -> {
        //    if (this.isEnabled() && blurSetting.isEnabled()) {
        //        //drawFullBlur(deltaTick, 5f);
        //    }
        //    //drawBlurs(deltaTick);
        //});

        loginBind.setParent(this);
    }

    @Override
    protected void doFunny() {}

    @Override
    public void setInitKey() {
        setKey(GLFW.GLFW_KEY_F8);
    }

    @Override
    public void onEnable() {
        mc.execute(() -> {
            if (mc.currentScreen == null) mc.mouse.unlockCursor();
        });

        if (mode.getMode() == modeEnum.ClickGUI) {
            if (!ImguiLoader.isRendered(AchillesMenu.getInstance())) {
                AchillesMenu.toggleVisibility();
            }
            if (ImguiLoader.isRendered(ModulesMenu.getInstance())) {
                ModulesMenu.toggleVisibility();
            }
        } else {
            if (ImguiLoader.isRendered(AchillesMenu.getInstance())) {
                AchillesMenu.toggleVisibility();
            }
            if (!ImguiLoader.isRendered(ModulesMenu.getInstance())) {
                ModulesMenu.toggleVisibility();
            }
        }
        if (!ImguiLoader.isRendered(LegitMenu.getInstance())) {
            LegitMenu.toggleVisibility();
        }
        if (!ImguiLoader.isRendered(LegitModules.getInstance())) {
            LegitModules.toggleVisibility();
        }
        if (!ImguiLoader.isRendered(ConfigParent.getInstance())) {
            ConfigParent.toggleVisibility();
        }
        if (!ImguiLoader.isRendered(ConfigChild.getInstance())) {
            ConfigChild.toggleVisibility();
        }
        if (!ImguiLoader.isRendered(SearchBar.getInstance())) {
            SearchBar.toggleVisibility();
        }
        if (openAnimation.isEnabled()) MainMenu.getInstance().firstFrame = true;
        AnimationUtil.hookPress("CategorySwitch", true);
    }

    @Override
    public void onDisable() {
        if (ImguiLoader.isRendered(AchillesMenu.getInstance())) {
            AchillesMenu.toggleVisibility();
        }
        if (ImguiLoader.isRendered(ModulesMenu.getInstance())) {
            ModulesMenu.toggleVisibility();
        }
        if (ImguiLoader.isRendered(LegitMenu.getInstance())) {
            LegitMenu.toggleVisibility();
        }
        if (ImguiLoader.isRendered(LegitModules.getInstance())) {
            LegitModules.toggleVisibility();
        }
        if (ImguiLoader.isRendered(ConfigChild.getInstance())) {
            ConfigChild.toggleVisibility();
        }
        if (ImguiLoader.isRendered(ConfigParent.getInstance())) {
            ConfigParent.toggleVisibility();
        }
        Template.moduleManager.getModule(ModMenuModule.class).disable();
        Template.moduleManager.getModule(ConfigModule.class).disable();
        Template.moduleManager.getModule(ArrayListModule.class).clearTextCache();
        mc.execute(() -> {
            if (mc.currentScreen == null) mc.mouse.lockCursor();
        });
    }

    public void updateMode() {
        if (this.isEnabled()) {
            if (mode.getMode() == modeEnum.ClickGUI) {
                if (!ImguiLoader.isRendered(AchillesMenu.getInstance())) {
                    AchillesMenu.toggleVisibility();
                }
                if (ImguiLoader.isRendered(ModulesMenu.getInstance())) {
                    ModulesMenu.toggleVisibility();
                }
            } else {
                if (ImguiLoader.isRendered(AchillesMenu.getInstance())) {
                    AchillesMenu.toggleVisibility();
                }
                if (!ImguiLoader.isRendered(ModulesMenu.getInstance())) {
                    ModulesMenu.toggleVisibility();
                }
            }
        }
    }

    @EventHandler
    private void onFastTick(FastTickEvent event) {
        if (!this.isEnabled()) return;

        if (notifications.isEnabled()) {
            if (!ImguiLoader.isRendered(Template.notificationManager()))
                ImguiLoader.addRenderable(Template.notificationManager());
        } else if (ImguiLoader.isRendered(Template.notificationManager()))
            ImguiLoader.queueRemove(Template.notificationManager());

        /*if (!dontStopParty.isEnabled()) {
            if (clip != null) {
                clip.stop();
                clip = null;
            }
        } else if (clip == null || !clip.isRunning()) {
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(SoundUtils.class.getClassLoader().getResourceAsStream("assets/dontstop.wav"));
                clip = AudioSystem.getClip();
                clip.open(stream);
                clip.start();
                clip.setFramePosition(frame);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ignored) {}
        }*/
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_PRESS && event.key == GLFW.GLFW_KEY_ESCAPE) setEnabled(false);
    }
}
