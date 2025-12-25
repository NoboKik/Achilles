package template.rip.api.util;

import imgui.ImGui;
import net.minecraft.util.Identifier;
//import org.ladysnake.satin.api.managed.ManagedShaderEffect;
//import org.ladysnake.satin.api.managed.ShaderEffectManager;
//import org.ladysnake.satin.api.managed.uniform.Uniform1f;
import org.lwjgl.opengl.GL20;

import static template.rip.Template.mc;

public class BlurUtil {

    //public static final ManagedShaderEffect blur = ShaderEffectManager.getInstance().manage(Identifier.of("template", "shaders/post/fade_in_blur.json"));
    //public static final Uniform1f blurProgress = blur.findUniform1f("Progress");
    //public static final Uniform1f blurRadius = blur.findUniform1f("Radius");

    public static void drawFullBlur(float deltaTick, float radius) {
//        blurRadius.set(radius);
//        blurProgress.set(1f);
//        blur.render(deltaTick);
    }

    public static void drawBlur(int x, int y, int width, int height, float deltaTick, float radius) {
        GL20.glEnable(GL20.GL_SCISSOR_TEST);
//        blurRadius.set(radius);
//        blurProgress.set(1f);
//        blur.render(deltaTick);
        //System.out.println(x+" "+y+" "+width+" "+height+" "+deltaTick+" "+radius);
        GL20.glScissor(x,mc.getWindow().getFramebufferHeight()-y-height,width,height);
        GL20.glDisable(GL20.GL_SCISSOR_TEST);
    }

    public static void drawBlur2(int x, int y, int width, int height, float deltaTick, float radius) {
        GL20.glEnable(GL20.GL_SCISSOR_TEST);
//        blurRadius.set(radius);
//        blurProgress.set(1f);
//        blur.render(deltaTick);
        //System.out.println(x+" "+y+" "+width+" "+height+" "+deltaTick+" "+radius);
        GL20.glScissor(x,y,width,height);
        GL20.glDisable(GL20.GL_SCISSOR_TEST);
    }

    public static void drawRoundedBlur(int x, int y, int width, int height, float deltaTick, float radius, int rounding) {
        drawBlur(x+rounding, y, width-rounding, height, deltaTick, radius);
        for (int i=0; i<rounding; i++) {
            int y1 = (int) Math.floor(Math.sqrt(rounding^2 - i^2));
            int y2 = (int) Math.floor(-Math.sqrt(rounding^2 - i^2));
            //drawBlur(x+width-rounding, y+rounding-y1, i, height-y+rounding-y1, deltaTick, radius);
        }
    }

    public static void drawRoundedRect(int x, int y, int width, int height, int rounding) {
        //ImGui.getBackgroundDrawList().addRectFilled(x + rounding, y, x+width - rounding, y+height,ImGui.getColorU32(1f,0f,0f,1f));
        for (int i=0; i<rounding; i++) {
            float y1 = (float) Math.sqrt(rounding^2 - i^2);
            float y2 = (float) -Math.sqrt(rounding^2 - i^2);
            ImGui.getBackgroundDrawList().addRectFilled(x+width-rounding+i, y+rounding-y1, x+width-rounding+i+1, y+height+y2,ImGui.getColorU32(1f,1f,1f,0.8f));
            //drawBlur(x+width-rounding, y+rounding-y1, i, height-y+rounding-y1, deltaTick, radius);
        }
    }

    public static void drawCircle(int x0, int y0, int radius) {
        //ImGui.getBackgroundDrawList().addRectFilled(x + rounding, y, x+width - rounding, y+height,ImGui.getColorU32(1f,0f,0f,1f));
        //for(int i=-rounding; i<rounding; i++) {
        //    float y1 = (float) Math.sqrt(rounding^2 - i^2);
        //    float y2 = (float) -Math.sqrt(rounding^2 - i^2);
        //    ImGui.getBackgroundDrawList().addRectFilled(x+i, y-rounding+y1, x+i+1, y+rounding-y2, ImGui.getColorU32(1f,1f,1f,1f));
        //    //drawBlur(x+width-rounding, y+rounding-y1, i, height-y+rounding-y1, deltaTick, radius);
        //}
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    ImGui.getBackgroundDrawList().addRectFilled(x0 + x, y0 + y, x0 + x + 1, y0 + y + 1, ImGui.getColorU32(1f, 1f, 1f, 1f));
                }
            }
        }
    }
}
