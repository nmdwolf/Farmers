package core;

import java.util.ArrayList;

public class Property<T> {

    private T property;
    private final ArrayList<Action> actions;

    public Property(T initialValue) {
        actions = new ArrayList<>();
        property = initialValue;
    }

    public Property() {
        actions = new ArrayList<>();
    }

    public T get() { return property; }

    public void set(T property) {
        this.property = property;
        for(Action action : actions)
            action.actionPerformed();
    }

    public void setAsParent(T property) { this.property = property; }

    public void bind(Action action) { actions.add(action); };
}
