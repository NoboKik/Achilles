package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class ItemUseEvent extends Cancellable {

    public static class Pre extends ItemUseEvent {}

    public static class Post extends ItemUseEvent {}

    public static class Return extends ItemUseEvent {}
}
