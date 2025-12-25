package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class MouseUpdateEvent extends Cancellable {

    public static class Pre extends MouseUpdateEvent {}

    public static class Post extends MouseUpdateEvent {}
}
