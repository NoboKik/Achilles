package template.rip.gui.utils;

public interface Theme {
    default void preRender() {
        // do nothing
    }

    default void postRender() {
        // do nothing
    }
}
