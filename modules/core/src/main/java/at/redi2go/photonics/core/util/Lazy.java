package at.redi2go.photonics.core.util;

import java.util.function.Supplier;

public class Lazy<T> {
    private static final Object NOT_PRESENT = new Object();

    private Object value = NOT_PRESENT;
    private Supplier<T> supplier;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (value != NOT_PRESENT)
            return (T) NOT_PRESENT;

        var result = supplier.get();
        supplier = null;
        value = result;

        return result;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }
}
