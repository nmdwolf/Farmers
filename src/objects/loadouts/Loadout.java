package objects.loadouts;

import objects.GameObject;

public abstract class Loadout<T extends GameObject> {

    private final String name;

    public Loadout(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
