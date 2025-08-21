package core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public class Property<T> {

    private T property;
    private final ArrayList<Action> actions;

    public Property() {
        actions = new ArrayList<>();
    }

    public Property(T initialValue) {
        this();
        property = initialValue;
    }

    @NotNull
    public Optional<T> get() { return Optional.ofNullable(property); }

    public T getFlat() { return property; }

    public void set(T property) {
        this.property = property;
        for(Action action : actions)
            action.actionPerformed();
    }

    public void setAsParent(T property) { this.property = property; }

    public void bind(Action action) { actions.add(action); };
}
