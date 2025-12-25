package template.rip.api.object;

public class PixelRegion {

    private final float startX;
    private final float startY;
    private final float endX;
    private final float endY;

    public PixelRegion(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public boolean isInside(float x, float y) {
        return x >= startX && x <= endX && y >= startY && y <= endY;
    }

    public boolean isInsideStrict(float x, float y) {
        return x > startX && x < endX && y > startY && y < endY;
    }
}
