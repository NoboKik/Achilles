package template.rip.api.event.events;

public class MouseDeltaEvent {

    public double deltaX, deltaY;

    public MouseDeltaEvent(double deltaX, double deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}
