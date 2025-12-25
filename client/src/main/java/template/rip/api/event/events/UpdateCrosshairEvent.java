package template.rip.api.event.events;

public class UpdateCrosshairEvent {

    public float tickDelta;

    public UpdateCrosshairEvent(float tickDelta) {
        this.tickDelta = tickDelta;
    }
}