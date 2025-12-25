package template.rip.api.util;

public class EasingUtil {

    public static float easeOutSine(float x) {return (float) (Math.sin((x * Math.PI) / 2));}
    public static float easeInSine(float x) {return (float) (1 - Math.cos((x * Math.PI) / 2));}
    public static float easeInOutSine(float x) {return (float) (-(Math.cos(Math.PI * x) - 1) / 2);}

    public static float easeOutCubic(float x) {return (float) (1 - Math.pow(1 - x, 3));}
    public static float easeInCubic(float x) {return (float) (x * x * x);}
    public static float easeInOutCubic(float x) {return (float) (x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2);}

    public static float easeOutQuad(float x) {return (float) (1 - (1 - x) * (1 - x));}
    public static float easeInQuad(float x) {return (float) (x * x);}
    public static float easeInOutQuad(float x) {return (float) (x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);}

    public static float easeOutQuart(float x) {return (float) (1 - Math.pow(1 - x, 4));}
    public static float easeInQuart(float x) {return (float) (x * x * x * x);}
    public static float easeInOutQuart(float x) {return (float) (x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2);}

    public static float easeOutExpo(float x) {return (float) (x == 1 ? 1 : 1 - Math.pow(2, -10 * x));}
    public static float easeInExpo(float x) {return (float) (x == 0 ? 0 : Math.pow(2, 10 * x - 10));}
    public static float easeInOutExpo(float x) {return (float) (x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2);}

    public static float easeOutBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;

        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }
}
