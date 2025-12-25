package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class ClickSimulationEvent extends Cancellable {

    public static class Right extends ClickSimulationEvent {}

    public static class Left extends ClickSimulationEvent {}
}
