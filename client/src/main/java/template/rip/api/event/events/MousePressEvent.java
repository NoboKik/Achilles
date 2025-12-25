package template.rip.api.event.events;

public class MousePressEvent {

    public int button, action;

    public MousePressEvent(int button, int action) {
        this.button = button;
        this.action = action;
    }
}
