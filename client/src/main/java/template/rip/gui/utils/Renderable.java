package template.rip.gui.utils;

public interface Renderable extends Nameable {
    default void render() {
        // do nothing
    }

    Theme templateTheme = new Ttheme();
    default Theme getTheme() {
        return templateTheme;
    }

    class Ttheme implements Theme {}
}
