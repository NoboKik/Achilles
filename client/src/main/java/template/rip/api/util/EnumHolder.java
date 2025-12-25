package template.rip.api.util;

import java.util.function.UnaryOperator;

public class EnumHolder<T extends Enum<T> & EnumIncr<T>> {

    private T value;

    private EnumHolder(T initialValue) {
        this.value = initialValue;
    }

    public static <T extends Enum<T> & EnumIncr<T>> EnumHolder<T> get(T initialValue) {
        return new EnumHolder<>(initialValue);
    }

    public T getValue() {
        return value;
    }

    public T increment() {
        return setValue(getValue().increment());
    }

    public T setValue(T value) {
        return this.value = value;
    }

    public T getAndSet(UnaryOperator<T> op) {
        return setValue(op.apply(getValue()));
    }

    public boolean is(T t) {
        return getValue() == t;
    }
}
