package at.redi2go.photonics.core.config;

import at.redi2go.photonics.api.Disposable;

import java.util.function.Consumer;
import java.util.function.Function;

public class PhConfigWatcher<T> implements Disposable {
    private static final Object NO_VALUE = new Object();
    private static final Object CLOSED = new Object();

    private final Function<PhStorage, T> supplier;
    private final Consumer<T> consumer;

    private Object previousValue = NO_VALUE;

    PhConfigWatcher(Function<PhStorage, T> supplier, Consumer<T> consumer) {
        this.supplier = supplier;
        this.consumer = consumer;
    }

    void reload(PhStorage config) {
        if (previousValue == CLOSED) return;

        final T newValue = supplier.apply(config);
        if (previousValue == newValue) return;

        previousValue = newValue;
        consumer.accept(newValue);
    }

    @Override
    public void close() {
        if (previousValue == CLOSED) return;

        PhConfig.removeWatcher(this);
        previousValue = CLOSED;
    }
}