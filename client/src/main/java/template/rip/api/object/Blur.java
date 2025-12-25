package template.rip.api.object;

import template.rip.api.util.BlurUtil;

import java.util.ArrayList;

import static template.rip.api.util.BlurUtil.drawFullBlur;

public class Blur {

    int x;
    int y;
    int width;
    int height;
    float radius;
    public static ArrayList<Blur> blurList = new ArrayList<>();

    public Blur(int x1, int y1, int width1, int height1, float radius1) {
        x=x1;
        y=y1;
        width=width1;
        height=height1;
        radius=radius1;
        blurList.add(this);
    }

    public void draw(float deltaTick) {
        BlurUtil.drawBlur(x, y, width, height, deltaTick, radius);
    }

    public static void drawBlurs(float deltaTick) {
        // requires to draw this to fix an issue
        drawFullBlur(deltaTick, 0f);
        blurList.forEach(b -> b.draw(deltaTick));
        blurList.clear();
    }
}
