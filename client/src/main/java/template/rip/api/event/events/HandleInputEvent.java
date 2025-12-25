package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class HandleInputEvent extends Cancellable {

    public static class Pre extends HandleInputEvent {}

    public static class Post extends HandleInputEvent {}
}
