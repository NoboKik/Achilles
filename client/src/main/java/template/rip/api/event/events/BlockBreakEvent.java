package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class BlockBreakEvent extends Cancellable {

    public static class Pre extends BlockBreakEvent {}

    public static class Post extends BlockBreakEvent {}
}
