package template.rip.api.util;

public interface EnumIncr<T extends Enum<T>> {

    default T increment() {
        @SuppressWarnings("unchecked cast")
        Enum<T> emu = (Enum<T>) this;
        T[] enums = emu.getDeclaringClass().getEnumConstants();
        return enums[(emu.ordinal() + 1) % enums.length];
    }

    default T decrement() {
        @SuppressWarnings("unchecked cast")
        Enum<T> emu = (Enum<T>) this;
        T[] enums = emu.getDeclaringClass().getEnumConstants();
        return enums[(emu.ordinal() - 1) % enums.length];
    }

    static <T extends Enum<?>> T increment(T emu) {
        @SuppressWarnings("unchecked cast")
        T[] enums = (T[]) emu.getDeclaringClass().getEnumConstants();
        return enums[(emu.ordinal() + 1) % enums.length];
    }

    static <T extends Enum<?>> T decrement(T emu) {
        @SuppressWarnings("unchecked cast")
        T[] enums = (T[]) emu.getDeclaringClass().getEnumConstants();
        return enums[(emu.ordinal() - 1) % enums.length];
    }
}
