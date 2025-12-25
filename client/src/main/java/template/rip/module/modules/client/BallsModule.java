package template.rip.module.modules.client;

import template.rip.Template;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.*;
import template.rip.gui.windowgui.ConfigMenu;
import template.rip.gui.windowgui.LegitModulesMenu;
import template.rip.gui.windowgui.MainMenu;
import template.rip.gui.windowgui.ModulesMenu;
import template.rip.module.Module;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BallsModule extends Module {

    public final NumberSetting maxBallSize = new NumberSetting(this, 30, 1, 100, 1,  "Ball Size");
    public final NumberSetting maxBalls = new NumberSetting(this, 50, 50, 500, 1,  "Max Balls");
    public final NumberSetting ballsMultiplier = new NumberSetting(this, 1, 1, 10, 1,  "Balls Multiplier");
    public final ModeSetting<colorModeEnum> colorMode = new ModeSetting<>(this, colorModeEnum.Flat, "Color Mode");
    public final ColorSetting colorSetting = new ColorSetting(this, new JColor(0.79f, 0.24f, 0.32f, 1f), true, "Color");

    private final List<Balls> balls = new ArrayList<>();

    public BallsModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onHudRender(HudRenderEvent e) {
        if (ImguiLoader.isRendered(ModulesMenu.getInstance()) ||
                ImguiLoader.isRendered(AchillesMenu.getInstance()) ||
                (ImguiLoader.isRendered(LegitModules.getInstance()) && LegitMenu.getInstance().isOn) ||
                (ImguiLoader.isRendered(ConfigChild.getInstance()) && ConfigParent.getInstance().isOn) ||
                (ImguiLoader.isRendered(ConfigMenu.getInstance())) ||
                (ImguiLoader.isRendered(LegitModulesMenu.getInstance())) ||
                (ImguiLoader.isRendered(MainMenu.getInstance())) ||
                Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class) ||
                MainMenu.getInstance().selectedSection.contains("Mod Menu") ||
                MainMenu.getInstance().selectedSection.contains("Config") ||
                mc.currentScreen != null) {
            if (balls.size() < maxBalls.getIValue()) {
                for (int i = 0; i < MathUtils.getRandomInt(1, ballsMultiplier.getIValue()); ++i) {
                    balls.add(new Balls(
                            MathUtils.getRandomInt(10, mc.getWindow().getWidth() - 10),
                            MathUtils.getRandomInt(10, mc.getWindow().getHeight() - 10),
                            MathUtils.getRandomInt(1, maxBallSize.getIValue()),
                            1000,
                            MathUtils.getRandomInt(4, 14),
                            true,
                            getColor())); // 1000ms = 1 second
                }
            }

            Iterator<Balls> ballIterator = balls.iterator();
            while (ballIterator.hasNext()) {
                Balls ball = ballIterator.next();
                if (ball.isAlive()) {
                    RenderUtils.Render2D.renderCircle(e.context.getMatrices(), ball.color, ball.x, ball.y, ball.getCurrentRadius(), ball.shape);
                } else {
                    ballIterator.remove(); // Remove the ball if it's expired
                }
            }
        }
    }

    private Color getColor() {
        switch (colorMode.getMode()) {
            case Flat -> {
                return new Color(colorSetting.getColor().getRGB());
            }
            case Fade -> {
                return fade(new Color(colorSetting.getColor().getRGB()), 1000f);
            }
            case RainBow -> {
                return rainBowSimpleColor();
            }
        }
        return Color.WHITE;
    }

    public static Color fade(Color color, float delay) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(System.currentTimeMillis() % 2000L / delay % 2.0f - 1.0f);
        brightness = 0.5f + 0.5f * brightness;
        hsb[2] = brightness % 2.0f;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color rainBowSimpleColor() {
        float hue = System.nanoTime() / 1.0E10f % 1.0f;
        hue = hue + 0.6f;
        int color = Color.HSBtoRGB(hue, 1, 1);
        return new Color(color);
    }

    public static class Balls {

        int x, y, radius, shape, lifetime;
        long startTime;
        Color color;
        boolean shouldShrink;

        public Balls(int x, int y, int radius, int lifetime, int shape, boolean shouldShrink, Color color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.lifetime = lifetime;
            this.shape = shape;
            this.shouldShrink = shouldShrink;
            this.color = color;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isAlive() {
            return (System.currentTimeMillis() - this.startTime) < lifetime;
        }

        // Calculate the current size of the ball based on time
        public int getCurrentRadius() {
            if (!shouldShrink) {
                return radius; // No shrinking, keep original size
            }

            long elapsedTime = System.currentTimeMillis() - this.startTime;
            float timeRatio = (float) elapsedTime / lifetime;
            return Math.max(1, (int) (radius * (1 - timeRatio))); // Shrink down to a minimum size of 1
        }
    }

    public enum colorModeEnum{Flat, Fade, RainBow}
}
