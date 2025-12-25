package template.rip.api.object;

public class ToolTipHolder {

    private static final Object mutex = new Object();
    private static String[] toolTip;

    public static void setToolTip(String... newToolTip) {
        synchronized (mutex) {
            if (newToolTip == null) {
                toolTip = new String[]{};
            } else {
                toolTip = new String[newToolTip.length];
                System.arraycopy(newToolTip, 0, toolTip, 0, newToolTip.length);
            }
        }
    }

    public static void clearToolTip() {
        setToolTip();
    }

    public static String[] getToolTip() {
        synchronized (mutex) {
            return toolTip;
        }
    }
}
