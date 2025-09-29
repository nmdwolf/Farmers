package core;

import java.util.Optional;
import java.util.function.Consumer;

public class UnsafeProperty<T> extends Property<Optional<T>> {

    public UnsafeProperty() {
        super(Optional.empty());
    }

    public UnsafeProperty(T initialValue) {
        super(Optional.ofNullable(initialValue));
    }

    public void setOptional(T value) {
        super.set(Optional.ofNullable(value));
    }

    public void set(Optional<T> value) {
        if(value == null)
            super.set(Optional.empty());
        else
            super.set(value);
    }

    public void ifPresent(Consumer<T> action) {
        property.ifPresent(action);
    }

    public void bindIfPresent(Consumer<T> action) {
        actions.add(prop -> prop.ifPresent(action));
    }
}
