package objects.loadouts;

import objects.GameObject;

public abstract class Loadout<T extends GameObject> {

    private final T owner;

    public Loadout(T owner) {
        this.owner = owner;
    }

    public T getOwner() {
        return owner;
    }

}
