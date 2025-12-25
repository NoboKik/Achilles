package template.rip.api.event.events;

import template.rip.api.event.Cancellable;

public class PlayerTickEvent extends Cancellable {

    public static class Pre extends PlayerTickEvent {}

    public static class PrePacket extends PlayerTickEvent {}

    public static class Post extends PlayerTickEvent {}
}
