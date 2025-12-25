package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class AttackEvent extends Cancellable {

    public static class Pre extends AttackEvent {}

    public static class Post extends AttackEvent {}
}
