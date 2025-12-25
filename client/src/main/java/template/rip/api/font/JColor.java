package template.rip.api.font;

import com.mojang.blaze3d.platform.GlStateManager;
import imgui.ImGui;
import net.minecraft.util.math.MathHelper;
import template.rip.Template;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.awt.*;
import java.io.Serial;

public class JColor extends Color {
	
	@Serial
	private static final long serialVersionUID = 1L;

	public static JColor getGuiColor() {
		AchillesSettingsModule radiumSettings = Template.moduleManager.getModule(AchillesSettingsModule.class);
		return (radiumSettings != null) ? radiumSettings.color.getColor() : new JColor(0.79f, 0.24f, 0.32f);
	}

	public JColor(int rgb) {
		super(rgb);
	}
	
	public JColor(int rgba, boolean hasalpha) {
		super(rgba, hasalpha);
	}
	
	public JColor(int r, int g, int b) {
		super(r, g, b);
	}
	
	public JColor(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public JColor(float r, float g, float b, float a) {
		super(r, g, b, a);
	}

	public JColor(float r, float g, float b) {
		super(r, g, b, 1.0f);
	}
	
	public JColor(Color color) {
		super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	
	public JColor(Color color, int a) {
		super(color.getRed(), color.getGreen(), color.getBlue(), a);
	}
	
	public static JColor fromHSB (float hue, float saturation, float brightness) {
		return new JColor(Color.getHSBColor(hue, saturation, brightness));
	}

	public JColor setRed(int red) {
		return new JColor(red, this.getGreen(), this.getBlue(), this.getAlpha());
	}

	public JColor setGreen(int green) {
		return new JColor(this.getRed(), green, this.getBlue(), this.getAlpha());
	}

	public JColor setBlue(int blue) {
		return new JColor(this.getRed(), this.getGreen(), blue, this.getAlpha());
	}

	public JColor setAlpha(int alpha) {
		return new JColor(this.getRed(), this.getGreen(), this.getBlue(), alpha);
	}
	
	public float getHue() {
		return RGBtoHSB(getRed(), getGreen(), getBlue(), null)[0];
	}
	
	public float getSaturation() {
		return RGBtoHSB(getRed(), getGreen(), getBlue(), null)[1];
	}
	
	public float getBrightness() {
		return RGBtoHSB(getRed(), getGreen(), getBlue(), null)[2];
	}

	public JColor smoothTransition(JColor toColor, float speed) {
		return new JColor(MathHelper.lerp(speed, this.getRed(), toColor.getRed()),
						MathHelper.lerp(speed, this.getGreen(), toColor.getGreen()),
						MathHelper.lerp(speed, this.getBlue(), toColor.getBlue()),
						MathHelper.lerp(speed, this.getAlpha(), toColor.getAlpha()));
	}

	public JColor jDarker() {
		return new JColor(this.darker());
	}

	public JColor jBrighter() {
		return new JColor(this.brighter());
	}

	public float[] getFloatColorWAlpha() {
		return new float[] { getRed() / 255.0f, getGreen() / 255.0f, getBlue() / 255.0f, getAlpha() / 255.0f };
	}

	public float[] getFloatColor() {
		return new float[] { getRed() / 255.0f, getGreen() / 255.0f, getBlue() / 255.0f, getAlpha() / 255.0f };
	}

	public int getU32() {
		return ImGui.getColorU32(getRed() / 255.0f, getGreen() / 255.0f, getBlue() / 255.0f, getAlpha() / 255.0f);
	}

	public void glColor() {
		GlStateManager._clearColor(getRed() / 255.0f, getGreen() / 255.0f, getBlue() / 255.0f, getAlpha() / 255.0f);
	}
}