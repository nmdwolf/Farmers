package core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;

public class Property<T> {

    private T property;
    private final ArrayList<Action<T>> actions, earlyActions;
    private final ArrayDeque<Action<T>> singleUseActions;

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
    public Optional<T> get() { return Optional.ofNullable(property); }

    public T getUnsafe() { return property; }

    public void set(T property) {
        for(Action<T> action : earlyActions)
            action.accept(property);
        this.property = property;
        for(Action<T> action : actions)
            action.accept(property);
        while(!singleUseActions.isEmpty())
            singleUseActions.pop().accept(property);
    }

    public void setAsParent(T property) { this.property = property; }

    public void bind(Action<T> action) { actions.add(action); }

    public void bindSingle(Action<T> action) { singleUseActions.add(action); }

    public void bindFirst(Action<T> action) { earlyActions.add(action); }

    public void ifPresent(Action<T> action) {
        if(property != null)
            action.accept(property);
    }
}
