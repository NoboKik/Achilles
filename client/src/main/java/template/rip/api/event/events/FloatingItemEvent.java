package template.rip.api.event.events;

import net.minecraft.item.ItemStack;
import template.rip.api.event.Cancellable;

public class FloatingItemEvent extends Cancellable {

    public ItemStack stack;

    public FloatingItemEvent(ItemStack stack) {
        this.stack = stack;
    }
}
