package core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class Property<T> {
    protected final ArrayList<Consumer<T>> actions;
    protected final ArrayList<Consumer<T>> earlyActions;
    protected final ArrayDeque<Consumer<T>> singleUseActions;
    protected T property;

    public Property() {
        actions = new ArrayList<>();
        earlyActions = new ArrayList<>();
        singleUseActions = new ArrayDeque<>();
    }

    public Property(T initialValue) {
        this();
        property = initialValue;
    }

    @NotNull
    public T get() {
        return property;
    }

    public void set(T property) {
        for (Consumer<T> action : earlyActions)
            action.accept(property);
        this.property = property;
        for (Consumer<T> action : actions)
            action.accept(property);
        while (!singleUseActions.isEmpty())
            singleUseActions.pop().accept(property);
    }

    public void bind(Consumer<T> action) {
        actions.add(action);
    }

    public void bindSingle(Consumer<T> action) {
        singleUseActions.add(action);
    }

    public void bindFirst(Consumer<T> action) {
        earlyActions.add(action);
    }
}
